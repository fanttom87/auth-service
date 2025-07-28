package ru.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/greet")
    public ResponseEntity<String> greetAdmin() {
        return ResponseEntity.ok("Тестовый ответ приветствие ADMIN.");
    }

    @GetMapping("/users")
    public ResponseEntity<String> listAllUsers() {
        return ResponseEntity.ok("Показ всех пользователей для админа.");
    }
}
