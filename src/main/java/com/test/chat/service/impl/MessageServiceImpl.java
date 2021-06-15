package com.test.chat.service.impl;

import com.test.chat.domain.MessageEntity;
import com.test.chat.domain.User;
import com.test.chat.domain.converter.MessageConverter;
import com.test.chat.dto.MessageDto;
import com.test.chat.dto.OutputMessage;
import com.test.chat.repository.MessageRepo;
import com.test.chat.service.api.FileStorageService;
import com.test.chat.service.api.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class MessageServiceImpl  implements MessageService {

    private static final Pattern FILE_PATTERN = Pattern.compile("^(data:.*;base64,)(.*)$");

    private final MessageRepo messageRepo;

    private final FileStorageService fileStorageService;

    @Override
    public OutputMessage addMessage(User author, MessageDto dto) {
        MessageEntity message = messageRepo.save(new MessageEntity(dto.getText(), author, LocalDateTime.now()));
        if (dto.getFileContent() != null && !dto.getFileContent().isEmpty()) {
            Matcher matcher = FILE_PATTERN.matcher(dto.getFileContent());
            if (matcher.find()) {
                String value = matcher.group(2);
                InputStream content = new ByteArrayInputStream(Base64.getDecoder().decode(value));
                String fullPath = fileStorageService.saveFile(content, dto.getFileName());
                message.setFilename(fullPath);
                messageRepo.save(message);
            }
        }
        return MessageConverter.entityToDto(message);
    }
}
