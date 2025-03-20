package com.rsupport.rs_sujeong.domain.notice.dto;


import com.rsupport.rs_sujeong.domain.notice.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDetailResponse {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private Long viewCount;
    private String createdBy;

    @Builder.Default
    private List<NoticeFileResponse> files = new ArrayList<>();

    public static NoticeDetailResponse from(Notice notice) {
        List<NoticeFileResponse> fileResponses = notice.getFiles().stream()
                .map(NoticeFileResponse::from)
                .collect(Collectors.toList());

        return NoticeDetailResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdAt(notice.getCreatedAt())
                .viewCount(notice.getViewCount())
                .createdBy(notice.getCreatedBy())
                .files(fileResponses)
                .build();
    }
}