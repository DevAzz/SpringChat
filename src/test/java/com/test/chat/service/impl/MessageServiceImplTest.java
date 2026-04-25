package com.test.chat.service.impl;

import com.test.chat.domain.MessageEntity;
import com.test.chat.domain.User;
import com.test.chat.dto.MessageDto;
import com.test.chat.repository.MessageRepo;
import com.test.chat.service.api.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageRepo messageRepo;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private MessageServiceImpl messageService;

    private User testUser;
    private MessageDto textOnlyDto;
    private MessageDto fileDto;
    private MessageEntity savedMessageEntity;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setActive(true);

        textOnlyDto = new MessageDto();
        textOnlyDto.setText("Hello, World!");

        String testFileName = "test.png";
        byte[] fileBytes = "fake-image-content".getBytes();
        String base64Content = Base64.getEncoder().encodeToString(fileBytes);
        fileDto = new MessageDto();
        fileDto.setText("Check this image");
        fileDto.setFileName(testFileName);
        fileDto.setFileContent("data:image/png;base64," + base64Content);

        savedMessageEntity = new MessageEntity("saved text", testUser, LocalDateTime.now());
        savedMessageEntity.setId(1);
        savedMessageEntity.setFilename(null);
    }

    @Nested
    @DisplayName("Тестирование addMessage без файла")
    @ExtendWith(MockitoExtension.class)
    class AddMessageWithoutFile {

        @Test
        @DisplayName("Должна сохранить сообщение с текстом и вернуть OutputMessage")
        void addMessage_WithTextOnly_ShouldSaveAndReturnMessage() {
            // Arrange
            when(messageRepo.save(any(MessageEntity.class))).thenReturn(savedMessageEntity);

            // Act
            var result = messageService.addMessage(testUser, textOnlyDto);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("saved text", result.getText());
            assertEquals(testUser, result.getAuthor());
            assertNull(result.getFilename());

            verify(messageRepo, times(1)).save(any(MessageEntity.class));
            verify(fileStorageService, never()).saveFile(any(), any());
        }

        @Test
        @DisplayName("Должна сохранить сообщение с null текстом")
        void addMessage_WithNullText_ShouldSaveAndReturnMessage() {
            // Arrange
            textOnlyDto.setText(null);
            when(messageRepo.save(any(MessageEntity.class))).thenReturn(savedMessageEntity);

            // Act
            var result = messageService.addMessage(testUser, textOnlyDto);

            // Assert
            assertNotNull(result);
            verify(messageRepo, times(1)).save(any(MessageEntity.class));
        }

        @Test
        @DisplayName("Должна сохранить сообщение с пустым текстом")
        void addMessage_WithEmptyText_ShouldSaveAndReturnMessage() {
            // Arrange
            textOnlyDto.setText("");
            when(messageRepo.save(any(MessageEntity.class))).thenReturn(savedMessageEntity);

            // Act
            var result = messageService.addMessage(testUser, textOnlyDto);

            // Assert
            assertNotNull(result);
            verify(messageRepo, times(1)).save(any(MessageEntity.class));
        }
    }

    @Nested
    @DisplayName("Тестирование addMessage с файлом")
    @ExtendWith(MockitoExtension.class)
    class AddMessageWithFile {

        @Test
        @DisplayName("Должна сохранить сообщение с файлом и вызвать fileStorageService")
        void addMessage_WithValidBase64File_ShouldSaveMessageAndCallStorage() {
            // Arrange
            String fullPath = "2024/1/15/10/test.png";
            MessageEntity savedWithFile = new MessageEntity("saved text", testUser, LocalDateTime.now());
            savedWithFile.setId(1);
            savedWithFile.setFilename(fullPath);

            when(messageRepo.save(any(MessageEntity.class))).thenReturn(savedWithFile);
            when(fileStorageService.saveFile(any(InputStream.class), eq("test.png"))).thenReturn(fullPath);

            // Act
            var result = messageService.addMessage(testUser, fileDto);

            // Assert
            assertNotNull(result);
            assertEquals(fullPath, result.getFilename());
            verify(messageRepo, times(2)).save(any(MessageEntity.class));
            verify(fileStorageService).saveFile(any(InputStream.class), eq("test.png"));
        }

        @Test
        @DisplayName("Должна корректно декодировать base64 контент файла")
        void addMessage_WithBase64File_ShouldDecodeBase64Correctly() {
            // Arrange
            String testFileName = "document.pdf";
            byte[] fileBytes = "pdf-content-here".getBytes();
            String base64Content = Base64.getEncoder().encodeToString(fileBytes);
            MessageDto pdfDto = new MessageDto();
            pdfDto.setText("Here is the document");
            pdfDto.setFileName(testFileName);
            pdfDto.setFileContent("data:application/pdf;base64," + base64Content);

            String fullPath = "2024/1/15/10/document.pdf";
            MessageEntity savedWithFile = new MessageEntity("saved text", testUser, LocalDateTime.now());
            savedWithFile.setId(1);
            savedWithFile.setFilename(fullPath);

            when(messageRepo.save(any(MessageEntity.class))).thenReturn(savedWithFile);
            when(fileStorageService.saveFile(any(InputStream.class), eq(testFileName))).thenReturn(fullPath);

            // Act
            var result = messageService.addMessage(testUser, pdfDto);

            // Assert
            assertNotNull(result);
            assertEquals(fullPath, result.getFilename());
            verify(fileStorageService).saveFile(any(InputStream.class), eq(testFileName));
            verify(messageRepo, times(2)).save(any(MessageEntity.class));
        }

        @Test
        @DisplayName("Не должна вызывать fileStorageService при неверном формате base64")
        void addMessage_WithInvalidBase64Format_ShouldNotCallStorage() {
            // Arrange
            fileDto.setFileContent("not-a-base64-string");
            when(messageRepo.save(any(MessageEntity.class))).thenReturn(savedMessageEntity);

            // Act
            var result = messageService.addMessage(testUser, fileDto);

            // Assert
            assertNotNull(result);
            verify(fileStorageService, never()).saveFile(any(), any());
            verify(messageRepo, times(1)).save(any(MessageEntity.class));
        }

        @Test
        @DisplayName("Не должна вызывать fileStorageService при пустом fileContent")
        void addMessage_WithEmptyFileContent_ShouldNotCallStorage() {
            // Arrange
            fileDto.setFileContent("");
            when(messageRepo.save(any(MessageEntity.class))).thenReturn(savedMessageEntity);

            // Act
            var result = messageService.addMessage(testUser, fileDto);

            // Assert
            assertNotNull(result);
            verify(fileStorageService, never()).saveFile(any(), any());
            verify(messageRepo, times(1)).save(any(MessageEntity.class));
        }
    }

    @Nested
    @DisplayName("Тестирование edge cases")
    @ExtendWith(MockitoExtension.class)
    class EdgeCases {

        @Test
        @DisplayName("Должна обработить null author")
        void addMessage_WithNullAuthor_ShouldHandleGracefully() {
            // Arrange
            when(messageRepo.save(any(MessageEntity.class))).thenReturn(savedMessageEntity);

            // Act
            var result = messageService.addMessage(null, textOnlyDto);

            // Assert
            assertNotNull(result);
            verify(messageRepo, times(1)).save(any(MessageEntity.class));
        }

        @Test
        @DisplayName("Должна обработить null dto")
        void addMessage_WithNullDto_ShouldThrowException() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    messageService.addMessage(testUser, null));
        }

        @Test
        @DisplayName("Должна обработить dto с null fileContent")
        void addMessage_WithNullFileContent_ShouldNotCallStorage() {
            // Arrange
            fileDto.setFileContent(null);
            when(messageRepo.save(any(MessageEntity.class))).thenReturn(savedMessageEntity);

            // Act
            var result = messageService.addMessage(testUser, fileDto);

            // Assert
            assertNotNull(result);
            verify(fileStorageService, never()).saveFile(any(), any());
            verify(messageRepo).save(any(MessageEntity.class));
        }

        @Test
        @DisplayName("Должна выбросить исключение при ошибке fileStorageService")
        void addMessage_WhenFileStorageFails_ShouldPropagateException() {
            // Arrange
            when(messageRepo.save(any(MessageEntity.class))).thenReturn(savedMessageEntity);
            when(fileStorageService.saveFile(any(InputStream.class), anyString()))
                    .thenThrow(new RuntimeException("Storage error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () ->
                    messageService.addMessage(testUser, fileDto));
        }

        @Test
        @DisplayName("Должна выбросить исключение при ошибке messageRepo")
        void addMessage_WhenRepoFails_ShouldPropagateException() {
            // Arrange
            when(messageRepo.save(any(MessageEntity.class)))
                    .thenThrow(new RuntimeException("DB error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () ->
                    messageService.addMessage(testUser, textOnlyDto));
        }
    }

    @Nested
    @DisplayName("Тестирование паттерна FILE_PATTERN")
    class FilePatternTests {

        @Test
        @DisplayName("Паттерн должен распознавать валидный data URL с image/png")
        void filePattern_ShouldMatchImagePng() {
            String validDataUrl = "data:image/png;base64," + Base64.getEncoder().encodeToString("test".getBytes());
            Matcher matcher = MessageServiceImpl.FILE_PATTERN.matcher(validDataUrl);
            assertTrue(matcher.find(), "Паттерн должен совпадать с data:image/png;base64,");
            assertEquals("data:image/png;base64,", matcher.group(1));
            assertEquals("dGVzdA==", matcher.group(2));
        }

        @Test
        @DisplayName("Паттерн не должен совпадать с application/pdf без data: префикса")
        void filePattern_ShouldNotMatchApplicationPdfWithoutDataPrefix() {
            String invalidDataUrl = "application/pdf;base64," + Base64.getEncoder().encodeToString("test".getBytes());
            Matcher matcher = MessageServiceImpl.FILE_PATTERN.matcher(invalidDataUrl);
            assertFalse(matcher.find(), "Паттерн не должен совпадать без data: префикса");
        }

        @Test
        @DisplayName("Паттерн должен совпадать с application/pdf при наличии data: префикса")
        void filePattern_ShouldMatchApplicationPdfWithDataPrefix() {
            String validDataUrl = "data:application/pdf;base64," + Base64.getEncoder().encodeToString("test".getBytes());
            Matcher matcher = MessageServiceImpl.FILE_PATTERN.matcher(validDataUrl);
            assertTrue(matcher.find(), "Паттерн должен совпадать с data:application/pdf;base64,");
            assertEquals("data:application/pdf;base64,", matcher.group(1));
            assertEquals("dGVzdA==", matcher.group(2));
        }

        @Test
        @DisplayName("Паттерн не должен совпадать с обычным текстом")
        void filePattern_ShouldNotMatchPlainString() {
            Matcher matcher = MessageServiceImpl.FILE_PATTERN.matcher("just plain text");
            assertFalse(matcher.find(), "Паттерн не должен совпадать с обычным текстом");
        }

        @Test
        @DisplayName("Паттерн не должен совпадать с пустой строкой")
        void filePattern_ShouldNotMatchEmptyString() {
            Matcher matcher = MessageServiceImpl.FILE_PATTERN.matcher("");
            assertFalse(matcher.find(), "Паттерн не должен совпадать с пустой строкой");
        }

        @Test
        @DisplayName("Паттерн не должен совпадать с null")
        void filePattern_ShouldNotMatchNull() {
            assertThrows(NullPointerException.class, () -> 
                MessageServiceImpl.FILE_PATTERN.matcher((String) null).find());
        }
    }
}