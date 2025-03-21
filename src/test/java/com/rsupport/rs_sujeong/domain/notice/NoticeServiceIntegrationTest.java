package com.rsupport.rs_sujeong.domain.notice;

import com.rsupport.rs_sujeong.domain.notice.dto.NoticeCreateRequest;
import com.rsupport.rs_sujeong.domain.notice.dto.NoticeDetailResponse;
import com.rsupport.rs_sujeong.domain.notice.dto.NoticeSearchCondition;
import com.rsupport.rs_sujeong.domain.notice.entity.Notice;
import com.rsupport.rs_sujeong.domain.notice.repository.NoticeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NoticeServiceIntegrationTest {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private FileStorageService fileStorageService;

    private Path testUploadDir;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트용 업로드 디렉토리 생성
        testUploadDir = Paths.get("./test-uploads");
        Files.createDirectories(testUploadDir);
    }

    @AfterEach
    void tearDown() throws Exception {
        // 테스트 후 생성된 파일 정리
        if (Files.exists(testUploadDir)) {
            Files.walk(testUploadDir)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            Files.delete(file);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    @Test
    @DisplayName("공지사항 CRUD 통합 테스트")
    void noticeCrudTest() {
        // 1. 공지사항 생성
        LocalDateTime now = LocalDateTime.now();
        NoticeCreateRequest createRequest = NoticeCreateRequest.builder()
                .title("통합 테스트 공지사항")
                .content("통합 테스트 내용입니다.")
                .startDate(now)
                .endDate(now.plusDays(7))
                .build();

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "integration-test.txt",
                "text/plain",
                "통합 테스트 파일 내용".getBytes());

        noticeService.createNotice(createRequest, Collections.singletonList(mockFile));

        // 2. 공지사항 목록 조회
        Page<Notice> notices = noticeRepository.findAll(PageRequest.of(0, 10));
        assertThat(notices.getTotalElements()).isEqualTo(1);

        Long noticeId = notices.getContent().get(0).getId();

        // 3. 공지사항 상세 조회
        NoticeDetailResponse detailResponse = noticeService.searchOne(noticeId);
        assertThat(detailResponse.getTitle()).isEqualTo("통합 테스트 공지사항");
        assertThat(detailResponse.getContent()).isEqualTo("통합 테스트 내용입니다.");
        assertThat(detailResponse.getFiles()).hasSize(1);

        // 4. 검색 테스트
        NoticeSearchCondition searchCondition = NoticeSearchCondition.builder()
                .searchType(NoticeSearchCondition.SearchType.TITLE)
                .filter("통합 테스트")
                .build();

        Page<?> searchResult = noticeService.searchNotices(searchCondition, PageRequest.of(0, 10));
        assertThat(searchResult.getTotalElements()).isEqualTo(1);

        // 5. 공지사항 삭제
        noticeService.deleteNotice(noticeId);

        // 6. 삭제 확인
        assertThat(noticeRepository.findById(noticeId)).isEmpty();
    }
}