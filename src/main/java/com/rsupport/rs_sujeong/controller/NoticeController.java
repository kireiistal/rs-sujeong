package com.rsupport.rs_sujeong.controller;


import com.rsupport.rs_sujeong.domain.notice.FileStorageService;
import com.rsupport.rs_sujeong.domain.notice.NoticeService;
import com.rsupport.rs_sujeong.domain.notice.dto.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public Page<NoticeResponse> search(@ParameterObject NoticeSearchCondition searchCondition,
                                       @ParameterObject @PageableDefault(sort = "createdAt,desc") Pageable pageable) {
        return noticeService.searchNotices(searchCondition, pageable);
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)))
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestPart @Valid NoticeCreateRequest request,
                       @RequestPart(required = false) List<MultipartFile> files) {
        noticeService.createNotice(request, files);
    }


    @GetMapping("/{id}")
    public NoticeDetailResponse searchOne(@PathVariable Long id) {
        return noticeService.searchOne(id);
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)))
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void updateNotice(@PathVariable Long id,
                             @RequestPart @Valid NoticeUpdateRequest request,
                             @RequestPart(required = false) List<MultipartFile> files) {
        noticeService.updateNotice(id, request, files);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        try {
            NoticeFileResponse fileInfo = noticeService.getFileInfo(fileId);
            Resource resource = fileStorageService.loadFileAsResource(fileInfo.getStoredFilename());

            String encodedFilename = new String(
                    fileInfo.getOriginalFilename().getBytes(StandardCharsets.UTF_8),
                    StandardCharsets.ISO_8859_1
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileInfo.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFilename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}