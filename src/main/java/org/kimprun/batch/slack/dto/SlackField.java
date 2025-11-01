package org.kimprun.batch.slack.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Slack 메시지 필드 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlackField {

    /**
     * 필드 제목
     */
    private String title;

    /**
     * 필드 값
     */
    private String value;

    /**
     * 짧은 필드 여부 (한 줄에 두 개의 필드를 표시)
     */
    @JsonProperty("short")
    private Boolean isShort;

    /**
     * 필드 생성 팩토리 메서드
     *
     * @param title 필드 제목
     * @param value 필드 값
     * @return SlackField 인스턴스
     */
    public static SlackField of(String title, String value) {
        return SlackField.builder()
                .title(title)
                .value(value)
                .isShort(true)
                .build();
    }

    /**
     * 긴 필드 생성 팩토리 메서드
     *
     * @param title 필드 제목
     * @param value 필드 값
     * @return SlackField 인스턴스
     */
    public static SlackField longField(String title, String value) {
        return SlackField.builder()
                .title(title)
                .value(value)
                .isShort(false)
                .build();
    }
}
