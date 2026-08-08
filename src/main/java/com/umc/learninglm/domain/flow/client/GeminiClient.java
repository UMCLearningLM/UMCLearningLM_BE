package com.umc.learninglm.domain.flow.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GeminiClient {

	private static final String CLOUD_PLATFORM_SCOPE = "https://www.googleapis.com/auth/cloud-platform";

	private final RestClient restClient;
	private final String project;
	private final String location;
	private final String model;
	private final String credentialsBase64;
	private GoogleCredentials credentials;

	public GeminiClient(
			@Value("${gemini.project-id:}") String project,
			@Value("${gemini.location:global}") String location,
			@Value("${gemini.model:gemini-2.5-flash}") String model,
			@Value("${gemini.credentials-base64:}") String credentialsBase64,
			@Value("${gemini.connect-timeout-ms:3000}") long connectTimeoutMs,
			@Value("${gemini.read-timeout-ms:20000}") long readTimeoutMs) {
		ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
				.withConnectTimeout(Duration.ofMillis(connectTimeoutMs))
				.withReadTimeout(Duration.ofMillis(readTimeoutMs));
		ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect().build(settings);
		this.restClient = RestClient.builder().baseUrl(baseUrl(location)).requestFactory(requestFactory).build();
		this.project = project;
		this.location = location;
		this.model = model;
		this.credentialsBase64 = credentialsBase64;
	}

	public String getModel() {
		return model;
	}

	// Vertex AI generateContent 호출(서비스 계정/ADC 인증). 실패 시 예외를 그대로 던지며, 호출부(FlowPreviewService)에서 TEMPLATE Fallback으로 전환한다.
	public String generateContent(String prompt) {
		if (project == null || project.isBlank()) {
			throw new IllegalStateException("gemini.project-id(GEMINI_PROJECT_ID)가 설정되지 않았습니다.");
		}

		Map<String, Object> body = Map.of(
				"contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))));

		GeminiResponse response = restClient.post()
				.uri("/v1/projects/{project}/locations/{location}/publishers/google/models/{model}:generateContent",
						project, location, model)
				.header("Authorization", "Bearer " + accessToken())
				.contentType(MediaType.APPLICATION_JSON)
				.body(body)
				.retrieve()
				.body(GeminiResponse.class);

		String text = extractText(response);
		if (text == null || text.isBlank()) {
			throw new IllegalStateException("Gemini 응답에서 유효한 텍스트를 찾을 수 없습니다. (안전 필터 차단 가능성 포함)");
		}
		return text;
	}

	// 서비스 계정 Access Token 발급. credentials는 첫 호출 시 지연 생성해 앱 기동 시점에는 GCP 인증 정보가 없어도 뜨도록 한다.
	private String accessToken() {
		try {
			if (credentials == null) {
				credentials = loadCredentials(credentialsBase64);
			}
			credentials.refreshIfExpired();
			if (credentials.getAccessToken() == null) {
				credentials.refresh();
			}
			return credentials.getAccessToken().getTokenValue();
		} catch (IOException | IllegalArgumentException e) {
			throw new IllegalStateException("GCP 서비스 계정 인증 토큰을 발급할 수 없습니다.", e);
		}
	}

	private GoogleCredentials loadCredentials(String credentialsBase64) throws IOException {
		GoogleCredentials source = credentialsBase64 == null || credentialsBase64.isBlank()
				? GoogleCredentials.getApplicationDefault()
				: GoogleCredentials.fromStream(
						new ByteArrayInputStream(Base64.getDecoder().decode(credentialsBase64)));
		return source.createScoped(CLOUD_PLATFORM_SCOPE);
	}

	private String baseUrl(String location) {
		return "global".equalsIgnoreCase(location)
				? "https://aiplatform.googleapis.com"
				: "https://" + location + "-aiplatform.googleapis.com";
	}

	private String extractText(GeminiResponse response) {
		if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
			return null;
		}
		GeminiContent content = response.candidates().get(0).content();
		if (content == null || content.parts() == null || content.parts().isEmpty()) {
			return null;
		}
		return content.parts().get(0).text();
	}
}

@JsonIgnoreProperties(ignoreUnknown = true)
record GeminiResponse(List<GeminiCandidate> candidates) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record GeminiCandidate(GeminiContent content) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record GeminiContent(List<GeminiPart> parts) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record GeminiPart(String text) {
}
