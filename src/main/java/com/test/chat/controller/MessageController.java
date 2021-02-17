package com.test.chat.controller;

import com.test.chat.domain.User;
import com.test.chat.dto.MessageDto;
import com.test.chat.service.api.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Optional;

@AllArgsConstructor
@Controller
public class MessageController {

    private final MessageService messageService;

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public void send(Principal principal, MessageDto messageDto) {
        messageService.addMessage(getUserByPrincipal(principal), messageDto);
    }

    private User getUserByPrincipal(Principal principal) {
        User result = null;
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
            result = Optional.of(token).map(UsernamePasswordAuthenticationToken::getPrincipal)
                    .map(value -> {
                        User usr = null;
                        if (value instanceof User) {
                            usr = (User) value;
                        }
                        return usr;
                    }).orElse(null);
        }
        return result;
    }

}
