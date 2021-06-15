package com.test.chat.dto;

import com.test.chat.domain.User;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OutputMessage {
    private Integer id;

    private String text;

    private String filename;

    private LocalDateTime date;

    private User author;
}
