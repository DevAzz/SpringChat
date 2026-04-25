package com.test.chat.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceImplTest {

    @TempDir
    Path tempDir;

    private FileStorageServiceImpl fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageServiceImpl();
        setUploadPath(tempDir.toString());
    }

    @Nested
    @DisplayName("Тестирование saveFile")
    class SaveFileTests {

        @Test
        @DisplayName("Должна сохранить файл с валидным содержимым")
        void saveFile_WithValidContent_ShouldSaveSuccessfully() throws Exception {
            // Arrange
            String content = "Hello, World!";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
            String fileName = "test.txt";

            // Act
            String resultPath = fileStorageService.saveFile(inputStream, fileName);

            // Assert
            assertNotNull(resultPath);
            assertTrue(resultPath.endsWith(fileName));
            Path expectedFilePath = tempDir.resolve(resultPath);
            assertTrue(Files.exists(expectedFilePath));
            assertEquals(content, Files.readString(expectedFilePath));
        }

        @Test
        @DisplayName("Должна создать вложенные директории при сохранении файла")
        void saveFile_ShouldCreateDirectories() throws Exception {
            // Arrange
            String content = "Test content";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
            String fileName = "nested_test.txt";

            // Act
            String resultPath = fileStorageService.saveFile(inputStream, fileName);

            // Assert
            assertNotNull(resultPath);
            Path expectedFilePath = tempDir.resolve(resultPath);
            assertTrue(Files.exists(expectedFilePath));
        }

        @Test
        @DisplayName("Должна переопределить существующий файл")
        void saveFile_WithExistingFile_ShouldReplace() throws Exception {
            // Arrange
            String fileName = "overwrite_test.txt";

            // Сохраняем первый файл
            String firstContent = "First content";
            ByteArrayInputStream firstStream = new ByteArrayInputStream(firstContent.getBytes());
            fileStorageService.saveFile(firstStream, fileName);

            // Перезаписываем
            String secondContent = "Second content";
            ByteArrayInputStream secondStream = new ByteArrayInputStream(secondContent.getBytes());
            String resultPath = fileStorageService.saveFile(secondStream, fileName);

            // Assert
            Path expectedFilePath = tempDir.resolve(resultPath);
            assertEquals(secondContent, Files.readString(expectedFilePath));
        }

        @Test
        @DisplayName("Должна выбросить исключение при ошибке записи")
        void saveFile_WithIOException_ShouldThrowException() throws Exception {
            // Arrange - создаем ситуацию где родительский каталог это файл (не директория)
            String fileName = "test.txt";
            
            // Получаем текущую дату для формирования правильного пути
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            Path fakeDir = tempDir.resolve(String.valueOf(now.getYear()));
            Files.createFile(fakeDir);  // Это будет файл, а не директория
            
            // Act & Assert - при попытке создать директории для файла возникнет IOException
            UncheckedIOException exception = assertThrows(UncheckedIOException.class, () ->
                    fileStorageService.saveFile(new ByteArrayInputStream("test".getBytes()), fileName));
            assertNotNull(exception.getCause());
        }

        @Test
        @DisplayName("Должна использовать текущую дату для формирования пути")
        void saveFile_ShouldUseCurrentDate() throws Exception {
            // Arrange
            String content = "Date test";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
            String fileName = "date_test.txt";

            // Act
            String resultPath = fileStorageService.saveFile(inputStream, fileName);

            // Assert
            assertNotNull(resultPath);
            String currentYear = String.valueOf(java.time.LocalDateTime.now().getYear());
            assertTrue(resultPath.startsWith(currentYear), "Путь должен начинаться с года");
        }

        @Test
        @DisplayName("Должна сохранить файл с бинарным содержимым (Base64)")
        void saveFile_WithBinaryContent_ShouldSaveCorrectly() throws Exception {
            // Arrange
            byte[] binaryData = Base64.getDecoder().decode("SGVsbG8sIFdvcmxkIQ==");
            ByteArrayInputStream inputStream = new ByteArrayInputStream(binaryData);
            String fileName = "binary_test.txt";

            // Act
            String resultPath = fileStorageService.saveFile(inputStream, fileName);

            // Assert
            Path expectedFilePath = tempDir.resolve(resultPath);
            assertTrue(Files.exists(expectedFilePath));
            assertArrayEquals(binaryData, Files.readAllBytes(expectedFilePath));
        }
    }

    @Nested
    @DisplayName("Тестирование getFileContent")
    class GetFileContentTests {

        @Test
        @DisplayName("Должна получить содержимое существующего файла")
        void getFileContent_WithExistingFile_ShouldReturnInputStream() throws Exception {
            // Arrange
            String content = "Read this file";
            String fileName = "read_test.txt";

            // Сначала сохраняем файл
            String savedPath = fileStorageService.saveFile(
                    new ByteArrayInputStream(content.getBytes()), fileName);

            // Act
            try (InputStream resultStream = fileStorageService.getFileContent(savedPath)) {
                String resultContent = new String(resultStream.readAllBytes());

                // Assert
                assertEquals(content, resultContent);
            }
        }

        @Test
        @DisplayName("Должна выбросить исключение при чтении несуществующего файла")
        void getFileContent_WithNonexistentFile_ShouldThrowException() {
            // Arrange
            String nonExistentPath = "2024/1/1/nonexistent.txt";

            // Act & Assert
            UncheckedIOException exception = assertThrows(UncheckedIOException.class, () ->
                    fileStorageService.getFileContent(nonExistentPath));
            assertNotNull(exception.getCause());
        }

        @Test
        @DisplayName("Должна вернуть разные потоки для разных вызовов")
        void getFileContent_MultipleCalls_ShouldReturnDifferentStreams() throws Exception {
            // Arrange
            String content = "Multiple reads";
            String fileName = "multi_read.txt";

            // Сохраняем файл
            String savedPath = fileStorageService.saveFile(
                    new ByteArrayInputStream(content.getBytes()), fileName);

            // Act
            try (InputStream stream1 = fileStorageService.getFileContent(savedPath);
                 InputStream stream2 = fileStorageService.getFileContent(savedPath)) {
                String content1 = new String(stream1.readAllBytes());
                String content2 = new String(stream2.readAllBytes());

                // Assert
                assertEquals(content, content1);
                assertEquals(content, content2);
            }
        }
    }

    @Nested
    @DisplayName("Тестирование интеграции")
    class IntegrationTests {

        @Test
        @DisplayName("Должна сохранить и считать файл целиком")
        void saveAndReadFile_Lifecycle_ShouldWorkCorrectly() throws Exception {
            // Arrange
            String originalContent = "Full lifecycle test content with special chars: !@#$%^&*()";
            String fileName = "lifecycle_test.txt";

            // Act
            String savedPath = fileStorageService.saveFile(
                    new ByteArrayInputStream(originalContent.getBytes()), fileName);
            try (InputStream resultStream = fileStorageService.getFileContent(savedPath)) {
                String readContent = new String(resultStream.readAllBytes());

                // Assert
                assertEquals(originalContent, readContent);
                assertTrue(Files.exists(tempDir.resolve(savedPath)));
            }
        }

        @Test
        @DisplayName("Должна работать с большими файлами")
        void saveAndReadFile_LargeFile_ShouldWorkCorrectly() throws Exception {
            // Arrange
            int fileSize = 1024 * 1024; // 1MB
            byte[] largeContent = new byte[fileSize];
            for (int i = 0; i < fileSize; i++) {
                largeContent[i] = (byte) (i % 256);
            }
            String fileName = "large_file.bin";

            // Act
            String savedPath = fileStorageService.saveFile(
                    new ByteArrayInputStream(largeContent), fileName);
            try (InputStream resultStream = fileStorageService.getFileContent(savedPath)) {
                byte[] readContent = resultStream.readAllBytes();

                // Assert
                assertArrayEquals(largeContent, readContent);
                assertEquals(fileSize, readContent.length);
            }
        }
    }

    // Метод для установки uploadPath через рефлексию
    private void setUploadPath(String path) {
        try {
            java.lang.reflect.Field field = FileStorageServiceImpl.class.getDeclaredField("uploadPath");
            field.setAccessible(true);
            field.set(fileStorageService, path);
        } catch (Exception e) {
            fail("Не удалось установить uploadPath: " + e.getMessage());
        }
    }

    // Метод для вызова приватного метода resolveFile через рефлексию
    private Path invokeResolveFile(String path) {
        try {
            java.lang.reflect.Method method = FileStorageServiceImpl.class.getDeclaredMethod("resolveFile", String.class);
            method.setAccessible(true);
            return (Path) method.invoke(fileStorageService, path);
        } catch (Exception e) {
            fail("Не удалось вызвать resolveFile: " + e.getMessage());
            return null;
        }
    }
}