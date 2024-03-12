package org.application.client;

import lombok.extern.slf4j.Slf4j;
import org.application.common.entity.EmailStructure;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class WebClientService {
    private final WebClient webClient;

    public WebClientService(WebClient.Builder webClientBuilder, @Value("${webclient.baseurl}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public void sendNotification(String email, EmailStructure emailStructure) {
        webClient.post()
                .uri("/email/send/{email}", email)
                .bodyValue(emailStructure)
                .retrieve()
                .toBodilessEntity()
                .subscribe(
                        success -> log.info("Уведомление успешно отправлено на адрес {}", email),
                        error -> log.error("Ошибка отправки уведомления на адрес {}: {}", email, error.getMessage())
                );
    }
}
