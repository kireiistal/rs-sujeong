package com.rsupport.rs_sujeong.domain.notice;

import com.rsupport.rs_sujeong.domain.notice.dto.*;
import com.rsupport.rs_sujeong.domain.notice.entity.Notice;
import com.rsupport.rs_sujeong.domain.notice.entity.NoticeFile;
import com.rsupport.rs_sujeong.domain.notice.repository.NoticeFileRepository;
import com.rsupport.rs_sujeong.domain.notice.repository.NoticeRepository;
import com.rsupport.rs_sujeong.exception.NoticeNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
        processFiles(notice, files);
        noticeRepository.save(notice);
    }

    @Cacheable(value = "noticeList", key = "#condition.toString() + #pageable.toString()")
    @Transactional(readOnly = true)
    public Page<NoticeResponse> searchNotices(NoticeSearchCondition condition, Pageable pageable) {
        return noticeRepository.search(condition, pageable)
                .map(NoticeResponse::from);
    }

    @CacheEvict(value = {"noticeList"}, allEntries = true)
    @Transactional
    public NoticeDetailResponse searchOne(Long noticeId) {
        Notice notice = findNoticeById(noticeId);
        notice.increaseViewCount();
        return NoticeDetailResponse.from(notice);
    }

    @CacheEvict(value = {"noticeList"}, allEntries = true)
    @Transactional
    public void updateNotice(Long noticeId, NoticeUpdateRequest request, List<MultipartFile> files) {
        Notice notice = findNoticeById(noticeId);

        // 기본 정보 업데이트
        notice.updateBasicInfo(
                request.getTitle(),
                request.getContent(),
                request.getStartDate(),
                request.getEndDate()
        );

        // 파일 처리
        deleteFiles(notice, request.getDeleteFileIds());
        processFiles(notice, files);

        noticeRepository.save(notice);
    }

    @CacheEvict(value = {"noticeList"}, allEntries = true)
    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = findNoticeById(noticeId);
        deleteAllNoticeFiles(notice);
        noticeRepository.delete(notice);
    }

    @Transactional(readOnly = true)
    public NoticeFileResponse getFileInfo(Long fileId) {
        return noticeFileRepository.findById(fileId)
                .map(NoticeFileResponse::from)
                .orElseThrow(() -> new NoticeNotFoundException("파일을 찾을 수 없습니다: " + fileId));
    }

    // 헬퍼 메소드
    private Notice findNoticeById(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException("공지사항을 찾을 수 없습니다: " + noticeId));
    }

    private void processFiles(Notice notice, List<MultipartFile> files) {
        if (CollectionUtils.isEmpty(files)) {
            return;
        }

        files.stream()
                .filter(file -> !file.isEmpty())
                .forEach(file -> addFileToNotice(notice, file));
    }

    private void deleteFiles(Notice notice, List<Long> fileIdsToDelete) {
        if (CollectionUtils.isEmpty(fileIdsToDelete)) {
            return;
        }

        List<NoticeFile> filesToDelete = notice.getFiles().stream()
                .filter(file -> fileIdsToDelete.contains(file.getId()))
                .toList();

        for (NoticeFile file : filesToDelete) {
            notice.getFiles().remove(file);
            fileStorageService.deleteFile(file.getStoredFilename());
        }
    }

    private void deleteAllNoticeFiles(Notice notice) {
        notice.getFiles().forEach(file ->
                fileStorageService.deleteFile(file.getStoredFilename())
        );
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
}