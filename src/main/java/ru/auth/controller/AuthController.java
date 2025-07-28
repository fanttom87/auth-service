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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(AuthService authService,
                          AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService,
                          JwtUtil jwtUtil) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
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
            // 1. Аутентификация пользователя с использованием AuthenticationManager
            // Это вызовет наш CustomUserDetailsService для проверки логина и пароля.
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
            );

            // 2. Если аутентификация прошла успешно, получаем UserDetails
            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getLogin());

            // 3. Генерируем JWT токен
            final String jwt = jwtUtil.generateToken(userDetails);

            // 4. Возвращаем токен в ответе
            return ResponseEntity.ok(new LoginResponse(jwt));

        } catch (BadCredentialsException e) {
            // Если логин или пароль неверные
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid login or password.");
        } catch (Exception e) {
            // Обработка других возможных ошибок
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during login.");
        }
    }

    // TODO: Добавим эндпоинт для отзыва токена позже
}