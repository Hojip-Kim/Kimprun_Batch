package org.kimprun.batch.slack.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Slack Incoming Webhook 메시지 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlackMessage {

    /**
     * 메시지 본문 텍스트
     */
    private String text;

    /**
     * 첨부 파일 (추가 정보)
     */
    private List<SlackAttachment> attachments;

    /**
     * 간단한 텍스트 메시지 생성
     *
     * @param text 메시지 텍스트
     * @return SlackMessage 인스턴스
     */
    public static SlackMessage of(String text) {
        return SlackMessage.builder()
                .text(text)
                .build();
    }
}
