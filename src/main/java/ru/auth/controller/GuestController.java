package ru.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/guest")
public class GuestController {

    @GetMapping("/hello")
    public ResponseEntity<String> helloGuest() {
        return ResponseEntity.ok("Привет, гость!");
    }
}