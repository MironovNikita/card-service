package org.application.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.application.common.entity.EmailStructure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

//https://www.youtube.com/watch?v=GBKY8QyfNDk
class WebClientServiceTest {
    private MockWebServer mockWebServer;
    private WebClientService webClientService;

    @BeforeEach
    void initialize() {
        mockWebServer = new MockWebServer();
        WebClient.Builder webClientBuilder = WebClient.builder().baseUrl(mockWebServer.url("/").toString());
        webClientService = new WebClientService(webClientBuilder, mockWebServer.url("/").toString());
    }

    @AfterEach
    void shutDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("Проверка webClient на успешную отправку запроса на отправку email")
    void shouldSendNotificationByEmail() throws InterruptedException {
        String email = "testemail@test.com";
        EmailStructure emailStructure = new EmailStructure("subject", "message");

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody("OK")
        );

        webClientService.sendNotification(email, emailStructure);

        RecordedRequest request = mockWebServer.takeRequest();

        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/email/send/" + encodeEmail(email));
        assertThat(request.getBody().readUtf8()).isEqualTo("{\"subject\":\"subject\",\"message\":\"message\"}");
    }

    private String encodeEmail(String email) {
        return URLEncoder.encode(email, StandardCharsets.UTF_8);
    }
}
