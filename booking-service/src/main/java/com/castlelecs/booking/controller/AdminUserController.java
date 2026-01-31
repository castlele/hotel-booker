package com.castlelecs.booking.service;

import com.castlelecs.booking.dto.CreateUserRequest;
import com.castlelecs.booking.dto.UpdateUserRequest;
import com.castlelecs.booking.dto.UserResponse;
import com.castlelecs.booking.entity.User;
import com.castlelecs.booking.service.UserAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class AdminUserController {

    private final UserAdminService userAdminService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userAdminService.getAll();

        return ResponseEntity.ok(users.stream().map(user -> toResponse(user)).toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest req) {
        User user = userAdminService.create(req);
        return ResponseEntity.ok(toResponse(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> patchUser(@PathVariable Long id, @RequestBody @Valid UpdateUserRequest req) {
        User user = userAdminService.patch(id, req);
        return ResponseEntity.ok(toResponse(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userAdminService.delete(id);
        return ResponseEntity.ok().build();
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getRole(), u.getCreatedAt());
    }
}

