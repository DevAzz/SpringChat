package com.test.chat.domain.converter;

import com.test.chat.domain.MessageEntity;
import com.test.chat.dto.OutputMessage;

public class MessageConverter {

    public MessageConverter() {
        throw new IllegalCallerException("Utility class!");
    }

    public static OutputMessage entityToDto(MessageEntity entity) {
        OutputMessage result = null;
        if (entity != null) {
            result = new OutputMessage();
            result.setAuthor(entity.getAuthor());
            result.setDate(entity.getDate());
            result.setFilename(entity.getFilename());
            result.setId(entity.getId());
            result.setText(entity.getText());
        }
        return result;
    }

}
