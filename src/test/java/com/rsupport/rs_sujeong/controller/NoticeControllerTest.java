package com.rsupport.rs_sujeong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsupport.rs_sujeong.domain.notice.FileStorageService;
import com.rsupport.rs_sujeong.domain.notice.NoticeService;
import com.rsupport.rs_sujeong.domain.notice.dto.*;
import com.rsupport.rs_sujeong.exception.NoticeNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoticeController.class)
class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NoticeService noticeService;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("공지사항 목록 조회 API 테스트")
    void searchNoticesTest() throws Exception {
        // Given
        List<NoticeResponse> responses = Collections.singletonList(
                NoticeResponse.builder()
                        .id(1L)
                        .title("테스트 공지사항")
                        .createdAt(LocalDateTime.now())
                        .viewCount(10L)
                        .createdBy("admin")
                        .hasAttachments(false)
                        .build()
        );

        Page<NoticeResponse> page = new PageImpl<>(responses);

        when(noticeService.searchNotices(any(NoticeSearchCondition.class), any(Pageable.class)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/notices")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].title", is("테스트 공지사항")));

        verify(noticeService, times(1)).searchNotices(any(NoticeSearchCondition.class), any(Pageable.class));
    }

    @Test
    @DisplayName("공지사항 상세 조회 API 테스트")
    void searchOneTest() throws Exception {
        // Given
        Long noticeId = 1L;
        NoticeDetailResponse response = NoticeDetailResponse.builder()
                .id(noticeId)
                .title("상세 공지사항")
                .content("상세 내용입니다")
                .createdAt(LocalDateTime.now())
                .viewCount(5L)
                .createdBy("admin")
                .files(new ArrayList<>())
                .build();

        when(noticeService.searchOne(noticeId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/notices/{id}", noticeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("상세 공지사항")))
                .andExpect(jsonPath("$.content", is("상세 내용입니다")));

        verify(noticeService, times(1)).searchOne(noticeId);
    }

    @Test
    @DisplayName("존재하지 않는 공지사항 조회 시 404 응답 테스트")
    void searchNonExistentNoticeTest() throws Exception {
        // Given
        Long noticeId = 999L;
        when(noticeService.searchOne(noticeId))
                .thenThrow(new NoticeNotFoundException("공지사항을 찾을 수 없습니다: " + noticeId));

        // When & Then
        mockMvc.perform(get("/api/v1/notices/{id}", noticeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(noticeService, times(1)).searchOne(noticeId);
    }

    @Test
    @DisplayName("공지사항 생성 API 테스트")
    void createNoticeTest() throws Exception {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusDays(7);

        NoticeCreateRequest createRequest = NoticeCreateRequest.builder()
                .title("새 공지사항")
                .content("새 공지사항 내용")
                .startDate(now)
                .endDate(later)
                .build();

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(createRequest)
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "files",
                "test.txt",
                "text/plain",
                "테스트 파일 내용".getBytes()
        );

        doNothing().when(noticeService).createNotice(any(NoticeCreateRequest.class), anyList());

        // When & Then
        mockMvc.perform(multipart("/api/v1/notices")
                        .file(requestPart)
                        .file(filePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isCreated());

        verify(noticeService, times(1)).createNotice(any(NoticeCreateRequest.class), anyList());
    }

    @Test
    @DisplayName("공지사항 수정 API 테스트")
    void updateNoticeTest() throws Exception {
        // Given
        Long noticeId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusDays(7);

        NoticeUpdateRequest updateRequest = NoticeUpdateRequest.builder()
                .title("수정된 공지사항")
                .content("수정된 내용")
                .startDate(now)
                .endDate(later)
                .deleteFileIds(new ArrayList<>())
                .build();

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(updateRequest)
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "files",
                "updated.txt",
                "text/plain",
                "업데이트된 파일 내용".getBytes()
        );

        doNothing().when(noticeService).updateNotice(eq(noticeId), any(NoticeUpdateRequest.class), anyList());

        // When & Then
        mockMvc.perform(multipart("/api/v1/notices/{id}", noticeId)
                        .file(requestPart)
                        .file(filePart)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isOk());

        verify(noticeService, times(1)).updateNotice(eq(noticeId), any(NoticeUpdateRequest.class), anyList());
    }

    @Test
    @DisplayName("공지사항 삭제 API 테스트")
    void deleteNoticeTest() throws Exception {
        // Given
        Long noticeId = 1L;
        doNothing().when(noticeService).deleteNotice(noticeId);

        // When & Then
        mockMvc.perform(delete("/api/v1/notices/{id}", noticeId))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(noticeService, times(1)).deleteNotice(noticeId);
    }

    @Test
    @DisplayName("파일 다운로드 API 테스트")
    void downloadFileTest() throws Exception {
        // Given
        Long fileId = 1L;
        NoticeFileResponse fileResponse = NoticeFileResponse.builder()
                .id(fileId)
                .originalFilename("original.txt")
                .storedFilename("stored.txt")
                .fileType("text/plain")
                .fileSize(100L)
                .build();

        Resource mockResource = mock(Resource.class);
        when(mockResource.exists()).thenReturn(true);
        when(mockResource.isReadable()).thenReturn(true);

        when(noticeService.getFileInfo(fileId)).thenReturn(fileResponse);
        when(fileStorageService.loadFileAsResource(fileResponse.getStoredFilename())).thenReturn(mockResource);

        // When & Then
        mockMvc.perform(get("/api/v1/notices/files/{fileId}", fileId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"original.txt\""))
                .andExpect(content().contentType("text/plain"));

        verify(noticeService, times(1)).getFileInfo(fileId);
        verify(fileStorageService, times(1)).loadFileAsResource(fileResponse.getStoredFilename());
    }

    @Test
    @DisplayName("존재하지 않는 파일 다운로드 시 404 응답 테스트")
    void downloadNonExistentFileTest() throws Exception {
        // Given
        Long fileId = 999L;
        when(noticeService.getFileInfo(fileId))
                .thenThrow(new NoticeNotFoundException("파일을 찾을 수 없습니다: " + fileId));

        // When & Then
        mockMvc.perform(get("/api/v1/notices/files/{fileId}", fileId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(noticeService, times(1)).getFileInfo(fileId);
        verify(fileStorageService, never()).loadFileAsResource(anyString());
    }
}