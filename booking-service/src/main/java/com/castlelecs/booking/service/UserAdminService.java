package com.castlelecs.booking.service;

import com.castlelecs.booking.dto.CreateUserRequest;
import com.castlelecs.booking.dto.UpdateUserRequest;
import com.castlelecs.booking.entity.User;
import com.castlelecs.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User create(CreateUserRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = User.builder()
        .username(req.username())
        .password(passwordEncoder.encode(req.password()))
        .role(req.role())
        .build();

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Username already exists");
        }
    }

    @Transactional
    public User patch(Long id, UpdateUserRequest req) {
        User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (req.username() != null && !req.username().isBlank()) {
            if (!req.username().equals(user.getUsername()) && userRepository.existsByUsername(req.username())) {
                throw new IllegalArgumentException("Username already exists");
            }
            user.setUsername(req.username());
        }

        if (req.password() != null && !req.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.password()));
        }

        if (req.role() != null) {
            user.setRole(req.role());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public List<User> getAll() {
        return userRepository.findAll();
    }
}

