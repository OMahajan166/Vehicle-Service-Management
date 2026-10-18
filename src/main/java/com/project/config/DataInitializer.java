package com.project.config;

import com.project.model.User;
import com.project.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer
        implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args)
            throws Exception {

        if (userRepository
                .findByEmail("admin@example.com")
                .isEmpty()) {

            User admin = new User();

            admin.setName("Administrator");
            admin.setEmail("admin@example.com");
            admin.setPhone("9999999999");
            admin.setRole("ADMIN");

            admin.setPassword(
                    passwordEncoder.encode(
                            "admin123"));

            userRepository.save(admin);

            System.out.println(
                    "Default Admin Created!");
        }
    }
}