package org.kimprun.batch.slack.adapter;

import lombok.extern.slf4j.Slf4j;
import org.kimprun.batch.slack.dto.SlackMessage;
import org.kimprun.batch.slack.port.SlackNotificationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Slack Webhook을 통한 알림 전송 Adapter
 * Port/Adapter 패턴의 Adapter 역할
 */
@Slf4j
@Component
public class SlackWebhookAdapter implements SlackNotificationPort {

    private final WebClient webClient;
    private final String webhookUrl;

    public SlackWebhookAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${slack.webhook.url:}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.webClient = webClientBuilder.build();
    }

    @Override
    public boolean sendMessage(SlackMessage message) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            return false;
        }

        try {
            String response = webClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(message)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .onErrorResume(error -> {
                        log.error("Slack 메시지 전송 중 오류 발생", error);
                        return Mono.just("error");
                    })
                    .block();

            if ("ok".equals(response)) {
                return true;
            } else {
                log.warn("Slack 메시지 전송 실패: {}", response);
                return false;
            }
        } catch (Exception e) {
            log.error("Slack 메시지 전송 중 예외 발생", e);
            return false;
        }
    }

    @Override
    public boolean sendSimpleMessage(String text) {
        return sendMessage(SlackMessage.of(text));
    }
}
