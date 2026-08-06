package com.umc.learninglm.domain.flow.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.umc.learninglm.domain.flow.dto.ai.AiGenerationResult;
import com.umc.learninglm.domain.flow.dto.ai.AiModelConfiguration;
import com.umc.learninglm.domain.flow.dto.harness.CompiledAiHarness;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class VertexAiClient implements AiModelClient {

	private static final String CLOUD_PLATFORM_SCOPE =
			"https://www.googleapis.com/auth/cloud-platform";

	private final String project;
	private final String location;
	private final String model;
	private final GoogleCredentials credentials;
	private final RestClient restClient;
	private final ObjectMapper objectMapper;

	public VertexAiClient(
			@Value("${GOOGLE_CLOUD_PROJECT}") String project,
			@Value("${GOOGLE_CLOUD_LOCATION:global}") String location,
			@Value("${GOOGLE_AI_MODEL:gemini-2.5-flash}") String model,
			@Value("${GCP_SERVICE_ACCOUNT_JSON_BASE64:}") String credentialBase64,
			@Value("${ai.vertex.connect-timeout-ms:5000}") long connectTimeoutMs,
			@Value("${ai.vertex.read-timeout-ms:90000}") long readTimeoutMs,
			ObjectMapper objectMapper
	) {
		this.project = project;
		this.location = location;
		this.model = model;
		this.credentials = createCredentials(credentialBase64);
		this.objectMapper = objectMapper;
		this.restClient = RestClient.builder()
				.baseUrl(createBaseUrl(location))
				.requestFactory(createRequestFactory(
						connectTimeoutMs,
						readTimeoutMs
				))
				.build();
	}

	@Override
	public AiGenerationResult generate(
			CompiledAiHarness harness,
			AiModelConfiguration configuration
	) {
		long startedAt = System.nanoTime();
		JsonNode response = restClient.post()
				.uri(createGenerateContentPath())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
				.contentType(MediaType.APPLICATION_JSON)
				.body(createRequestBody(harness, configuration))
				.retrieve()
				.body(JsonNode.class);

		if (response == null) {
			throw new IllegalStateException("Vertex AI 응답이 없습니다.");
		}

		String text = extractText(response);
		JsonNode usageMetadata = response.path("usageMetadata");
		long durationMs = (System.nanoTime() - startedAt) / 1_000_000;

		return new AiGenerationResult(
				model,
				text,
				parseStructuredOutput(text),
				usageMetadata.path("promptTokenCount").asInt(),
				usageMetadata.path("candidatesTokenCount").asInt(),
				usageMetadata.path("thoughtsTokenCount").asInt(),
				usageMetadata.path("totalTokenCount").asInt(),
				durationMs,
				response.path("candidates").path(0).path("finishReason").asText(),
				configuration.thinkingProfile()
		);
	}

	private Map<String, Object> createRequestBody(
			CompiledAiHarness harness,
			AiModelConfiguration configuration
	) {
		Map<String, Object> generationConfig = new LinkedHashMap<>();
		generationConfig.put("temperature", configuration.temperature());
		generationConfig.put("maxOutputTokens", configuration.maxOutputTokens());
		generationConfig.put(
				"thinkingConfig",
				Map.of("thinkingBudget", configuration.thinkingBudget())
		);
		generationConfig.put("responseMimeType", "application/json");
		generationConfig.put("responseSchema", harness.responseSchema());

		Map<String, Object> requestBody = new LinkedHashMap<>();
		requestBody.put("systemInstruction", Map.of(
				"parts", List.of(Map.of("text", harness.systemInstruction()))
		));
		requestBody.put("contents", List.of(Map.of(
				"role", "user",
				"parts", List.of(Map.of("text", harness.prompt()))
		)));
		requestBody.put("generationConfig", generationConfig);
		return requestBody;
	}

	private ClientHttpRequestFactory createRequestFactory(
			long connectTimeoutMs,
			long readTimeoutMs
	) {
		ClientHttpRequestFactorySettings settings =
				ClientHttpRequestFactorySettings.defaults()
						.withConnectTimeout(Duration.ofMillis(connectTimeoutMs))
						.withReadTimeout(Duration.ofMillis(readTimeoutMs));
		return ClientHttpRequestFactoryBuilder.detect().build(settings);
	}

	private GoogleCredentials createCredentials(String credentialBase64) {
		try {
			GoogleCredentials sourceCredentials;
			if (credentialBase64 == null || credentialBase64.isBlank()) {
				sourceCredentials = GoogleCredentials.getApplicationDefault();
			} else {
				byte[] credentialJson = Base64.getDecoder().decode(credentialBase64);
				sourceCredentials = GoogleCredentials.fromStream(
						new ByteArrayInputStream(credentialJson)
				);
			}
			return sourceCredentials.createScoped(CLOUD_PLATFORM_SCOPE);
		} catch (IOException | IllegalArgumentException exception) {
			throw new IllegalStateException(
					"GCP 서비스 계정 인증 정보를 읽을 수 없습니다.",
					exception
			);
		}
	}

	private String createBaseUrl(String location) {
		if ("global".equalsIgnoreCase(location)) {
			return "https://aiplatform.googleapis.com";
		}
		return "https://" + location + "-aiplatform.googleapis.com";
	}

	private String createGenerateContentPath() {
		return "/v1/projects/%s/locations/%s/publishers/google/models/%s:generateContent"
				.formatted(project, location, model);
	}

	private String getAccessToken() {
		try {
			credentials.refreshIfExpired();
			if (credentials.getAccessToken() == null) {
				credentials.refresh();
			}
			return credentials.getAccessToken().getTokenValue();
		} catch (IOException exception) {
			throw new IllegalStateException(
					"GCP Access Token을 발급할 수 없습니다.",
					exception
			);
		}
	}

	private String extractText(JsonNode response) {
		JsonNode parts = response.path("candidates")
				.path(0)
				.path("content")
				.path("parts");
		String text = StreamSupport.stream(parts.spliterator(), false)
				.map(part -> part.path("text").asText(""))
				.filter(part -> !part.isBlank())
				.collect(Collectors.joining("\n"));

		if (text.isBlank()) {
			throw new IllegalStateException(
					"Vertex AI 응답에 생성된 텍스트가 없습니다."
			);
		}
		return text;
	}

	private JsonNode parseStructuredOutput(String text) {
		try {
			return objectMapper.readTree(text);
		} catch (JsonProcessingException exception) {
			return null;
		}
	}
}
