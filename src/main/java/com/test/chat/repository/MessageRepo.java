package com.test.chat.repository;

import com.test.chat.domain.MessageEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface MessageRepo extends CrudRepository<MessageEntity, Integer> {
    List<MessageEntity> findByText(String text);

    @NonNull
    List<MessageEntity> findAll();
}
