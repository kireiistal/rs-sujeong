package com.rsupport.rs_sujeong.domain.notice.dto;

import com.rsupport.rs_sujeong.domain.notice.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeResponse {

    private Long id;
    private String title;
    private boolean hasAttachments;
    private LocalDateTime createdAt;
    private Long viewCount;
    private String createdBy;

    public static NoticeResponse from(Notice notice) {
        return NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .hasAttachments(!notice.getFiles().isEmpty())
                .createdAt(notice.getCreatedAt())
                .viewCount(notice.getViewCount())
                .createdBy(notice.getCreatedBy())
                .build();
    }
}