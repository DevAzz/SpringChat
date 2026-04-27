package com.test.chat.repository;

import com.test.chat.domain.MessageEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.UUID;

public interface MessageRepo extends CrudRepository<MessageEntity, UUID> {
    List<MessageEntity> findByText(String text);

    @NonNull
    List<MessageEntity> findAll();
}
