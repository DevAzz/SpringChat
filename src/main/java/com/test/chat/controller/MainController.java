package com.test.chat.controller;

import com.test.chat.domain.MessageEntity;
import com.test.chat.repository.MessageRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Controller
public class MainController {

    private final MessageRepo messageRepo;

    @GetMapping("/")
    public String greeting(Map<String, Object> model) {
        return "greeting";
    }

    @GetMapping("/main")
    public String main(@RequestParam(required = false, defaultValue = "") String filter, Model model) {
        List<MessageEntity> messages;

        //TODO Загрузка по скроллу
        if(filter == null || !filter.isBlank()) {
            messages = messageRepo.findByText(filter);
        } else {
            messages = messageRepo.findAll();
        }

        messages.sort(Comparator.comparing(MessageEntity::getDate));

        model.addAttribute("messages", messages);
        model.addAttribute("filter", filter);
        return "main";
    }

}
