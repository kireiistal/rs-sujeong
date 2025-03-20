package com.rsupport.rs_sujeong.domain.notice.dto;

import com.rsupport.rs_sujeong.domain.notice.entity.NoticeFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeFileResponse {

    private Long id;
    private String originalFilename;
    private String storedFilename;
    private String fileType;
    private Long fileSize;

    public static NoticeFileResponse from(NoticeFile noticeFile) {
        return NoticeFileResponse.builder()
                .id(noticeFile.getId())
                .originalFilename(noticeFile.getOriginalFilename())
                .storedFilename(noticeFile.getStoredFilename())
                .fileType(noticeFile.getFileType())
                .fileSize(noticeFile.getFileSize())
                .build();
    }
}
