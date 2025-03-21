package com.rsupport.rs_sujeong.domain.notice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;
    private Path testUploadDir;

    @BeforeEach
    void setUp() throws IOException {
        testUploadDir = Paths.get("./test-file-uploads");
        Files.createDirectories(testUploadDir);
        fileStorageService = new FileStorageService(testUploadDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
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
            Files.deleteIfExists(testUploadDir);
        }
    }

    @Test
    @DisplayName("파일 저장 테스트")
    void storeFileTest() {
        // given
        String content = "테스트 파일 내용";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-file.txt",
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8));

        // when
        String storedFilename = fileStorageService.storeFile(file);

        // then
        assertThat(storedFilename).isNotNull();
        assertThat(storedFilename).contains("test-file.txt");

        Path storedFilePath = testUploadDir.resolve(storedFilename);
        assertThat(Files.exists(storedFilePath)).isTrue();
    }

    @Test
    @DisplayName("파일 로드 테스트")
    void loadFileAsResourceTest() throws IOException {
        // given
        String content = "파일 로드 테스트";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "load-test.txt",
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8));

        String storedFilename = fileStorageService.storeFile(file);

        // when
        Resource resource = fileStorageService.loadFileAsResource(storedFilename);

        // then
        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
        assertThat(resource.isReadable()).isTrue();

        String loadedContent = new String(Files.readAllBytes(
                Paths.get(resource.getURI())), StandardCharsets.UTF_8);
        assertThat(loadedContent).isEqualTo(content);
    }

    @Test
    @DisplayName("파일 삭제 테스트")
    void deleteFileTest() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "delete-test.txt",
                "text/plain",
                "삭제 테스트".getBytes(StandardCharsets.UTF_8));

        String storedFilename = fileStorageService.storeFile(file);
        Path storedFilePath = testUploadDir.resolve(storedFilename);

        assertThat(Files.exists(storedFilePath)).isTrue();

        // when
        fileStorageService.deleteFile(storedFilename);

        // then
        assertThat(Files.exists(storedFilePath)).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 파일 로드시 예외 발생")
    void loadNonExistentFileTest() {
        // when & then
        assertThrows(Exception.class, () ->
                fileStorageService.loadFileAsResource("non-existent-file.txt")
        );
    }
}