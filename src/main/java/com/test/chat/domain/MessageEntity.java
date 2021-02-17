package com.test.chat.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@Entity(name = "message")
public class MessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String text;

    private String tag;

    private String filename;

    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User author;

    public MessageEntity(String text, String tag, User author, LocalDateTime date) {
        this.text = text;
        this.tag = tag;
        this.author = author;
        this.date = date;
    }

    public String getAuthorName() {
        return null != author ? author.getUsername() : "<none>";
    }
}
