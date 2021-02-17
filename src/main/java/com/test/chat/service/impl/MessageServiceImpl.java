package com.test.chat.service.impl;

import com.test.chat.domain.MessageEntity;
import com.test.chat.domain.User;
import com.test.chat.dto.MessageDto;
import com.test.chat.repository.MessageRepo;
import com.test.chat.service.api.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class MessageServiceImpl  implements MessageService {

    private final MessageRepo messageRepo;

    @Override
    public void addMessage(User author, MessageDto dto) {
        MessageEntity message = new MessageEntity(dto.getText(), dto.getTag(), author, LocalDateTime.now());
        messageRepo.save(message);
    }
}
