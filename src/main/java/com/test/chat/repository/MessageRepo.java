package com.test.chat.repository;

import com.test.chat.domain.MessageEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MessageRepo extends CrudRepository<MessageEntity, Integer> {
    List<MessageEntity> findByTag(String tag);
    List<MessageEntity> findAll();
}
