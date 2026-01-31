package com.castlelecs.booking.service;

import com.castlelecs.booking.dto.RegisterRequest;
import com.castlelecs.booking.entity.Role;
import com.castlelecs.booking.entity.User;
import com.castlelecs.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = User.builder()
        .username(req.username())
        .password(passwordEncoder.encode(req.password()))
        .role(Role.USER)
        .build();

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Username already exists");
        }
    }

    @Transactional(readOnly = true)
    public User authenticate(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return user;
    }
}
