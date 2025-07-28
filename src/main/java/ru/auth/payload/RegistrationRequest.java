package ru.auth.payload;

import lombok.Data;

@Data
public class RegistrationRequest {
    private String login;
    private String password;
    private String email;
}
