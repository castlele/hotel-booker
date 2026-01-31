package com.castlelecs.booking.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.castlelecs.booking.configuration.AdminProperties;
import com.castlelecs.booking.entity.Role;
import com.castlelecs.booking.entity.User;
import com.castlelecs.booking.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties adminProps;

    @Override
    public void run(String... args) {
        if (!adminProps.isEnabled()) {
            return;
        }

        if (userRepository.existsByUsername(adminProps.getUsername())) {
            return;
        }

        User admin = User.builder()
                .username(adminProps.getUsername())
                .password(passwordEncoder.encode(adminProps.getPassword()))
                .role(Role.valueOf(adminProps.getRole()))
                .build();

        userRepository.save(admin);

        System.out.println("✅ Admin user created: " + admin.getUsername());
    }
}

