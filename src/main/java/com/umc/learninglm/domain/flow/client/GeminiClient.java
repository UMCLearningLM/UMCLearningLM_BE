package com.umc.learninglm.domain.flow.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
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
			@Value("${gemini.model:gemini-2.0-flash}") String model) {
		this.restClient = RestClient.create(baseUrl);
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
				.uri("/v1beta/models/{model}:generateContent?key={key}", model, apiKey)
				.contentType(MediaType.APPLICATION_JSON)
				.body(body)
				.retrieve()
				.body(GeminiResponse.class);

		if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
			throw new IllegalStateException("Gemini 응답에 candidates가 없습니다.");
		}
		return response.candidates().get(0).content().parts().get(0).text();
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
