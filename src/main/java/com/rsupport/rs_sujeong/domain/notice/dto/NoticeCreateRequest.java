package com.rsupport.rs_sujeong.domain.notice.dto;

import com.rsupport.rs_sujeong.domain.notice.entity.Notice;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeCreateRequest {

    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String title;

    @NotBlank(message = "내용은 필수 입력값입니다.")
    private String content;

    @NotNull(message = "공지 시작일시는 필수 입력값입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    @NotNull(message = "공지 종료일시는 필수 입력값입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    public Notice toEntity() {
        return Notice.builder()
                .title(title)
                .content(content)
                .startDate(startDate)
                .endDate(endDate)
                .createdBy("admin") //TODO 로그인 기능을 만들면 로그인한 사용자정보 입력
                .build();
    }
}