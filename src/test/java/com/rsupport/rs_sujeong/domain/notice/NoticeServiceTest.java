package com.rsupport.rs_sujeong.domain.notice;

import com.rsupport.rs_sujeong.domain.notice.dto.*;
import com.rsupport.rs_sujeong.domain.notice.entity.Notice;
import com.rsupport.rs_sujeong.domain.notice.repository.NoticeFileRepository;
import com.rsupport.rs_sujeong.domain.notice.repository.NoticeRepository;
import com.rsupport.rs_sujeong.exception.NoticeNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private NoticeFileRepository noticeFileRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private NoticeService noticeService;

    private Notice mockNotice;
    private NoticeCreateRequest createRequest;
    private NoticeUpdateRequest updateRequest;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        // 공통 테스트 데이터 설정
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusDays(7);

        mockNotice = Notice.builder()
                .title("테스트 공지사항")
                .content("테스트 내용입니다.")
                .startDate(now)
                .endDate(later)
                .createdBy("admin")
                .build();

        // ID 설정을 위한 리플렉션 사용
        try {
            java.lang.reflect.Field idField = Notice.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockNotice, 1L);
        } catch (Exception e) {
            e.printStackTrace();
        }

        createRequest = NoticeCreateRequest.builder()
                .title("새 공지사항")
                .content("새 내용입니다.")
                .startDate(now)
                .endDate(later)
                .build();

        updateRequest = NoticeUpdateRequest.builder()
                .title("수정된 공지사항")
                .content("수정된 내용입니다.")
                .startDate(now)
                .endDate(later)
                .deleteFileIds(new ArrayList<>())
                .build();

        mockFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "테스트 파일 내용".getBytes()
        );
    }

    @Test
    @DisplayName("공지사항 생성 성공 테스트")
    void createNoticeSuccess() {
        // Given
        when(noticeRepository.save(any(Notice.class))).thenReturn(mockNotice);
        when(fileStorageService.storeFile(any(MultipartFile.class))).thenReturn("stored-filename.txt");

        // When
        noticeService.createNotice(createRequest, Collections.singletonList(mockFile));

        // Then
        verify(noticeRepository, times(1)).save(any(Notice.class));
        verify(fileStorageService, times(1)).storeFile(any(MultipartFile.class));
    }

    @Test
    @DisplayName("공지사항 목록 검색 테스트")
    void searchNoticesTest() {
        // Given
        NoticeSearchCondition condition = new NoticeSearchCondition();
        Pageable pageable = PageRequest.of(0, 10);
        List<Notice> notices = Collections.singletonList(mockNotice);
        Page<Notice> noticePage = new PageImpl<>(notices, pageable, notices.size());

        when(noticeRepository.search(condition, pageable)).thenReturn(noticePage);

        // When
        Page<NoticeResponse> result = noticeService.searchNotices(condition, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo(mockNotice.getTitle());
        verify(noticeRepository, times(1)).search(condition, pageable);
    }

    @Test
    @DisplayName("공지사항 상세 조회 테스트")
    void searchOneNoticeTest() {
        // Given
        Long noticeId = 1L;
        when(noticeRepository.findById(noticeId)).thenReturn(Optional.of(mockNotice));

        // When
        NoticeDetailResponse result = noticeService.searchOne(noticeId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(mockNotice.getTitle());
        assertThat(result.getContent()).isEqualTo(mockNotice.getContent());
        verify(noticeRepository, times(1)).findById(noticeId);
    }

    @Test
    @DisplayName("존재하지 않는 공지사항 조회시 예외 발생 테스트")
    void searchOneNoticeNotFoundTest() {
        // Given
        Long noticeId = 999L;
        when(noticeRepository.findById(noticeId)).thenReturn(Optional.empty());

        // When, Then
        assertThatThrownBy(() -> noticeService.searchOne(noticeId))
                .isInstanceOf(NoticeNotFoundException.class);
    }

    @Test
    @DisplayName("공지사항 수정 테스트")
    void updateNoticeTest() {
        // Given
        Long noticeId = 1L;
        when(noticeRepository.findById(noticeId)).thenReturn(Optional.of(mockNotice));
        when(fileStorageService.storeFile(any(MultipartFile.class))).thenReturn("updated-file.txt");

        // When
        noticeService.updateNotice(noticeId, updateRequest, Collections.singletonList(mockFile));

        // Then
        assertThat(mockNotice.getTitle()).isEqualTo(updateRequest.getTitle());
        assertThat(mockNotice.getContent()).isEqualTo(updateRequest.getContent());
        verify(noticeRepository, times(1)).findById(noticeId);
        verify(noticeRepository, times(1)).save(mockNotice);
        verify(fileStorageService, times(1)).storeFile(any(MultipartFile.class));
    }

    @Test
    @DisplayName("공지사항 삭제 테스트")
    void deleteNoticeTest() {
        // Given
        Long noticeId = 1L;
        when(noticeRepository.findById(noticeId)).thenReturn(Optional.of(mockNotice));

        // When
        noticeService.deleteNotice(noticeId);

        // Then
        verify(noticeRepository, times(1)).findById(noticeId);
        verify(noticeRepository, times(1)).delete(mockNotice);
    }
}