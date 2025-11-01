package org.kimprun.batch.slack.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Slack 메시지 첨부 정보 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlackAttachment {

    /**
     * 색상 (good: 녹색, warning: 주황색, danger: 빨간색)
     */
    private String color;

    /**
     * 첨부 파일 제목
     */
    private String title;

    /**
     * 첨부 파일 본문
     */
    private String text;

    /**
     * 필드 목록 (key-value 쌍)
     */
    private List<SlackField> fields;

    /**
     * 푸터 텍스트
     */
    private String footer;

    /**
     * 타임스탬프 (Unix timestamp)
     */
    @JsonProperty("ts")
    private Long timestamp;
}
