package ru.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.auth.model.Role;
import ru.auth.model.User;
import ru.auth.repository.RoleRepository;
import ru.auth.repository.UserRepository;

import java.util.Optional;

@Configuration
public class DataLoader {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Выполняем код сразу после запуска прилжения
    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            // Добавляем роли, если их нет
            if (roleRepository.findByName("ADMIN").isEmpty()) {
                roleRepository.save(new Role("ADMIN"));
            }
            if (roleRepository.findByName("PREMIUM_USER").isEmpty()) {
                roleRepository.save(new Role("PREMIUM_USER"));
            }
            if (roleRepository.findByName("GUEST").isEmpty()) {
                roleRepository.save(new Role("GUEST"));
            }

            // Добавляем первого админа, если он не существует
            String adminLogin = "admin";
            if (userRepository.findByLogin(adminLogin).isEmpty()) {
                User admin = new User();
                admin.setLogin(adminLogin);
                admin.setPassword(passwordEncoder.encode("secure_pass"));
                admin.setEmail("admin@test.ru");

                // Находим роль ADMIN и добавляем ее пользователю
                Optional<Role> adminRoleOpt = roleRepository.findByName("ADMIN");
                adminRoleOpt.ifPresent(admin::addRole);

                userRepository.save(admin);

                System.out.println("Пользователь admin успешно создан");
            }
        };
    }
}
