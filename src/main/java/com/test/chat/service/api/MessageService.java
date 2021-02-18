package com.test.chat.service.api;

import com.test.chat.domain.User;
import com.test.chat.dto.MessageDto;
import com.test.chat.dto.OutputMessage;

public interface MessageService {
    OutputMessage addMessage(User user, MessageDto dto);
}
