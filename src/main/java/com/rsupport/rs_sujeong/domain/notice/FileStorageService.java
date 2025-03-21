package com.rsupport.rs_sujeong.domain.notice;

import com.rsupport.rs_sujeong.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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

    private static final String FILE_INVALID_ERROR = "001";
    private static final String FILE_SAVE_ERROR = "002";
    private static final String FILE_NOT_FOUND_ERROR = "003";

    private final Path fileStoragePath;

    public FileStorageService(@Value("${file.upload-dir:./uploads}") String uploadDir) {
        this.fileStoragePath = initializeStoragePath(uploadDir);
    }

    private Path initializeStoragePath(String uploadDir) {
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(path);
            return path;
        } catch (IOException e) {
            log.error("파일 업로드 디렉토리를 생성할 수 없습니다.", e);
            throw new RuntimeException("파일 업로드 디렉토리를 생성할 수 없습니다.", e);
        }
    }

    public String storeFile(MultipartFile file) {
        validateFile(file);
        String storedFilename = generateUniqueFilename(file.getOriginalFilename());

        try {
            Path targetLocation = this.fileStoragePath.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return storedFilename;
        } catch (IOException e) {
            log.error("파일 저장 중 오류가 발생했습니다.", e);
            throw new ApiException(FILE_SAVE_ERROR, "파일 저장 중 오류가 발생했습니다.");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty() ||
                !StringUtils.hasText(file.getOriginalFilename())) {
            throw new ApiException(FILE_INVALID_ERROR, "파일이 유효하지 않습니다.");
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        return UUID.randomUUID() + "_" + originalFilename;
    }

    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = this.fileStoragePath.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new ApiException(FILE_NOT_FOUND_ERROR, "파일을 찾을 수 없습니다: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new ApiException(FILE_NOT_FOUND_ERROR, "파일을 찾을 수 없습니다: " + filename);
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