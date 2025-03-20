package com.rsupport.rs_sujeong.domain.notice;

import com.rsupport.rs_sujeong.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path fileStoragePath;

    public FileStorageService(@Value("${file.upload-dir:./uploads}") String uploadDir) {
        this.fileStoragePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStoragePath);
        } catch (IOException e) {
            log.error("파일 업로드 디렉토리를 생성할 수 없습니다.", e);
            throw new RuntimeException("파일 업로드 디렉토리를 생성할 수 없습니다.", e);
        }
    }

    public String storeFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new ApiException("001", "파일명이 유효하지 않습니다.");
            }

            // UUID로 고유한 파일명 생성
            String storedFilename = UUID.randomUUID() + "_" + originalFilename;
            Path targetLocation = this.fileStoragePath.resolve(storedFilename);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return storedFilename;
        } catch (IOException e) {
            log.error("파일 저장 중 오류가 발생했습니다.", e);
            throw new ApiException("002", "파일 저장 중 오류가 발생했습니다.");
        }
    }

    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = this.fileStoragePath.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new ApiException("003", "파일을 찾을 수 없습니다: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new ApiException("003", "파일을 찾을 수 없습니다: " + filename);
        }
    }

    public void deleteFile(String filename) {
        try {
            Path filePath = this.fileStoragePath.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("파일 삭제 중 오류가 발생했습니다: {}", filename, e);
        }
    }
}