package com.test.chat.service.api;

import com.test.chat.domain.User;
import com.test.chat.dto.MessageDto;

public interface MessageService {
    void addMessage(User user, MessageDto dto);
}
