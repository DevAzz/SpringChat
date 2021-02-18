package com.test.chat.dto;

import lombok.Data;

@Data
public class MessageDto {
    private String text;
    private String tag;
    private String fileName;
    private String fileContent;
}
