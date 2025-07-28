package ru.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.auth.config.CustomUserDetailsService;
import ru.auth.model.User;
import ru.auth.payload.LoginRequest;
import ru.auth.payload.LoginResponse;
import ru.auth.payload.RegistrationRequest;
import ru.auth.security.JwtUtil;
import ru.auth.service.AuthService;
import ru.auth.service.RevokedTokenService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final RevokedTokenService revokedTokenService;

    @Autowired
    public AuthController(AuthService authService,
                          AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService,
                          JwtUtil jwtUtil,
                          RevokedTokenService revokedTokenService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.revokedTokenService = revokedTokenService;
    }

    /**
     * Эндпоинт для регистрации новых пользователей.
     * @param request Тело запроса с данными для регистрации (login, password, email).
     * @return Ответ с информацией о созданном пользователе или сообщение об ошибке.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest request) {
        try {
            User newUser = authService.registerUser(request.getLogin(), request.getPassword(), request.getEmail());
            // Можно вернуть только часть информации о пользователе, без пароля
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("id", newUser.getId());
            responseBody.put("login", newUser.getLogin());
            responseBody.put("email", newUser.getEmail());
            responseBody.put("roles", newUser.getRoles().stream().map(role -> role.getName()).collect(java.util.stream.Collectors.toList()));
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        } catch (IllegalArgumentException e) {
            // Если логин или email заняты
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Обработка других возможных ошибок
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during registration.");
        }
    }

    /**
     * Эндпоинт для авторизации существующих пользователей.
     * @param request Тело запроса с данными для логина (login, password).
     * @return Ответ с JWT токеном или сообщение об ошибке.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
            );

            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getLogin());

            final String jwt = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(new LoginResponse(jwt));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid login or password.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during login.");
        }
    }

    /**
     * Эндпоинт для отзыва текущего авторизованного токена.
     * Токен должен быть передан в заголовке Authorization.
     * @return Ответ об успехе или ошибке.
     */
    @PostMapping("/revoke")
    public ResponseEntity<?> revokeToken(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Authorization header missing or in wrong format.");
        }

        String jwt = authorizationHeader.substring(7);

        try {
            String username = jwtUtil.extractUsername(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            Date expirationDate = jwtUtil.extractExpiration(jwt);

            if (!jwtUtil.validateToken(jwt, userDetails)) {
                return ResponseEntity.badRequest().body("Токе не валидный или истек.");
            }
            if (revokedTokenService.isTokenRevoked(jwt)) {
                return ResponseEntity.badRequest().body("Токен уже отозван.");
            }

            revokedTokenService.revokeToken(jwt, expirationDate);

            return ResponseEntity.ok("Токер успешно отозван.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при отзывании токена.");
        }
    }
}