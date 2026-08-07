package com.umc.learninglm.domain.flow.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Duration;
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

	private final RestClient restClient;
	private final String apiKey;
	private final String model;

	public GeminiClient(
			@Value("${gemini.base-url:https://generativelanguage.googleapis.com}") String baseUrl,
			@Value("${gemini.api-key:}") String apiKey,
			@Value("${gemini.model:gemini-2.5-flash}") String model,
			@Value("${gemini.connect-timeout-ms:3000}") long connectTimeoutMs,
			@Value("${gemini.read-timeout-ms:20000}") long readTimeoutMs) {
		ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
				.withConnectTimeout(Duration.ofMillis(connectTimeoutMs))
				.withReadTimeout(Duration.ofMillis(readTimeoutMs));
		ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect().build(settings);
		this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
		this.apiKey = apiKey;
		this.model = model;
	}

	public String getModel() {
		return model;
	}

	// Gemini v1beta generateContent 호출. 실패 시 예외를 그대로 던지며, 호출부(FlowPreviewService)에서 TEMPLATE Fallback으로 전환한다.
	public String generateContent(String prompt) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException("GEMINI_API_KEY가 설정되지 않았습니다.");
		}

		Map<String, Object> body = Map.of(
				"contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));

		GeminiResponse response = restClient.post()
				.uri("/v1beta/models/{model}:generateContent", model)
				.header("x-goog-api-key", apiKey)
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
