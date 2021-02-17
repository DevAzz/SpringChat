package com.test.chat.controller;

import com.test.chat.domain.Role;
import com.test.chat.domain.User;
import com.test.chat.repository.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;
import java.util.Map;

@AllArgsConstructor
@Controller
public class RegistrationController {

    private final UserRepo userRepo;

    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(User user, Map<String, Object> model){
        User userfromDb = userRepo.findByUsername(user.getUsername());
        if (null != userfromDb) {
            model.put("message", "User exist");
            return "registration";
        }
        user.setActive(true);
        user.setRoles(Collections.singleton(Role.USER));
        userRepo.save(user);
        return "redirect:/login";
    }

}
