package com.retailhr.ems.service;

import com.retailhr.ems.model.entity.User;
import com.retailhr.ems.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    public Optional<User> login(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        if (!user.getIsActive()) {
            return Optional.empty();
        }
        if (!BCrypt.checkpw(rawPassword, user.getPasswordHash())) {
            auditService.log(null, "LOGIN_FAILED", "User", user.getUserId(), "Invalid password for " + username);
            return Optional.empty();
        }
        user.setLastLogin(LocalDateTime.now());
        userRepository.update(user);
        auditService.log(user, "LOGIN_SUCCESS", "User", user.getUserId(), "User logged in");
        return Optional.of(user);
    }

    public User register(String username, String rawPassword, com.retailhr.ems.model.entity.Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(hashPassword(rawPassword));
        user.setRole(role);
        user.setIsActive(true);
        User saved = userRepository.save(user);
        auditService.log(saved, "USER_CREATED", "User", saved.getUserId(), "Account created for " + username);
        return saved;
    }

    public void changePassword(User user, String newRawPassword) {
        user.setPasswordHash(hashPassword(newRawPassword));
        userRepository.update(user);
        auditService.log(user, "PASSWORD_CHANGED", "User", user.getUserId(), "Password updated");
    }

    public String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
    }
}