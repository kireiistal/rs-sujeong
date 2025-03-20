package com.rsupport.rs_sujeong.domain.notice;


import com.rsupport.rs_sujeong.domain.notice.dto.*;
import com.rsupport.rs_sujeong.domain.notice.entity.Notice;
import com.rsupport.rs_sujeong.domain.notice.entity.NoticeFile;
import com.rsupport.rs_sujeong.domain.notice.repository.NoticeFileRepository;
import com.rsupport.rs_sujeong.domain.notice.repository.NoticeRepository;
import com.rsupport.rs_sujeong.exception.NoticeNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeFileRepository noticeFileRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public void createNotice(NoticeCreateRequest request, List<MultipartFile> files) {
        Notice notice = request.toEntity();

        // 첨부파일 처리
        if (!CollectionUtils.isEmpty(files)) {
            files.stream().filter(file -> !file.isEmpty()).forEach(file -> addFileToNotice(notice, file));
        }

        noticeRepository.save(notice);
    }

    @Transactional(readOnly = true)
    public Page<NoticeResponse> searchNotices(NoticeSearchCondition condition, Pageable pageable) {
        Page<Notice> notices = noticeRepository.search(condition, pageable);
        return notices.map(NoticeResponse::from);
    }

    @Transactional
    public NoticeDetailResponse searchOne(Long noticeId) {
        Notice notice = findNoticeById(noticeId);
        noticeRepository.increaseViewCount(noticeId);
        return NoticeDetailResponse.from(notice);
    }

    @Transactional
    public void updateNotice(Long noticeId, NoticeUpdateRequest request, List<MultipartFile> files) {
        Notice notice = findNoticeById(noticeId);

        // 기본 정보 업데이트 - setter 방식 사용
        notice.updateBasicInfo(request.getTitle(), request.getContent(),
                request.getStartDate(), request.getEndDate());

        // 삭제할 파일 처리
        if (!CollectionUtils.isEmpty(request.getDeleteFileIds())) {
            // orphanRemoval=true가 설정되어 있으므로 컬렉션에서 제거하면 자동 삭제됨
            List<NoticeFile> filesToDelete = notice.getFiles().stream()
                    .filter(file -> request.getDeleteFileIds().contains(file.getId()))
                    .toList();

            for (NoticeFile file : filesToDelete) {
                notice.getFiles().remove(file);
                fileStorageService.deleteFile(file.getStoredFilename());
            }
        }

        // 새 파일 추가
        if (!CollectionUtils.isEmpty(files)) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    addFileToNotice(notice, file);
                }
            }
        }

        // 변경된 엔티티 저장
        noticeRepository.save(notice);
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = findNoticeById(noticeId);
        notice.getFiles().forEach(file -> fileStorageService.deleteFile(file.getStoredFilename()));
        noticeRepository.delete(notice);
    }

    private Notice findNoticeById(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException("공지사항을 찾을 수 없습니다: " + noticeId));
    }

    private void addFileToNotice(Notice notice, MultipartFile file) {
        String storedFilename = fileStorageService.storeFile(file);

        NoticeFile noticeFile = NoticeFile.builder()
                .originalFilename(file.getOriginalFilename())
                .storedFilename(storedFilename)
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .build();

        notice.addFile(noticeFile);
    }

    @Transactional(readOnly = true)
    public NoticeFileResponse getFileInfo(Long fileId) {
        NoticeFile noticeFile = noticeFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다: " + fileId));
        return NoticeFileResponse.from(noticeFile);
    }
}