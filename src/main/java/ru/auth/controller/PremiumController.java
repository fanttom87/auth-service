package ru.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/premium")
public class PremiumController {

    @GetMapping("/feature")
    public ResponseEntity<String> premiumFeature() {
        return ResponseEntity.ok("Привет premium юзер!");
    }
}
