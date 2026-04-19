package com.indralokbright.config;

import com.indralokbright.model.User;
import com.indralokbright.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createDefaultAdmin();
    }

    private void createDefaultAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Administrator")
                    .email("admin@indralokbright.com")
                    .role("ROLE_ADMIN")
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("Default admin user created: admin / admin123");
        }

        if (!userRepository.existsByUsername("user")) {
            User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user123"))
                    .fullName("Staff User")
                    .email("user@indralokbright.com")
                    .role("ROLE_USER")
                    .enabled(true)
                    .build();
            userRepository.save(user);
            log.info("Default staff user created: user / user123");
        }
    }
}
