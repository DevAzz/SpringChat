package com.test.chat.dto.auth;

import lombok.Data;
import java.util.Set;

@Data
public class LoginResponse {
    private String token;
    private Long id;
    private String username;
    private Set<String> roles;

    public LoginResponse(String token, Long id, String username, Set<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.roles = roles;
    }
}