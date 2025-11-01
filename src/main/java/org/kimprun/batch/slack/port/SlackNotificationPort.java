package org.kimprun.batch.slack.port;

import org.kimprun.batch.slack.dto.SlackMessage;

/**
 * Slack 알림 전송을 위한 Port 인터페이스
 * Port/Adapter 패턴의 Port 역할
 */
public interface SlackNotificationPort {

    /**
     * Slack 메시지를 전송합니다.
     *
     * @param message 전송할 Slack 메시지
     * @return 전송 성공 여부
     */
    boolean sendMessage(SlackMessage message);

    /**
     * 간단한 텍스트 메시지를 전송합니다.
     *
     * @param text 전송할 텍스트
     * @return 전송 성공 여부
     */
    boolean sendSimpleMessage(String text);
}
