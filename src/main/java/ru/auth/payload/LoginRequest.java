package ru.auth.payload;

import lombok.Data;

@Data
public class LoginRequest {
    private String login;
    private String password;
}