package ru.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.auth.model.Role;
import ru.auth.model.User;
import ru.auth.repository.RoleRepository;
import ru.auth.repository.UserRepository;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Регистрирует пользователя в системе.
     * @param login Логин пользователя.
     * @param password Пароль пользователя.
     * @param email Email пользователя.
     * @return Созданный User, если регистрация прошла успешно.
     * @throws IllegalArgumentException если логин или email уже заняты.
     */
    @Transactional
    public User registerUser(String login, String password, String email) {
        if (userRepository.findByLogin(login).isPresent()) {
            throw new IllegalArgumentException("Логин уже занят!");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Почта уже занята!");
        }

        // Создаем новый объект пользователя
        User newUser = new User();
        newUser.setLogin(login);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(email);

        // По умолчанию, получает роль гостя
        Optional<Role> guestRoleOpt = roleRepository.findByName("GUEST");
        if (guestRoleOpt.isPresent()) {
            newUser.addRole(guestRoleOpt.get());
        } else {
            System.err.println("Ошибка: нет гостевой роли!");
        }

        // Сохраняем пользователя в базе данных
        return userRepository.save(newUser);
    }

    // TODO login
}
