package com.se2026.ims.service;

import com.se2026.ims.model.Role;
import com.se2026.ims.model.User;
import com.se2026.ims.repository.Repository;

import java.util.List;
import java.util.Optional;

public class UserService {
    private final Repository<User> userRepository;

    public UserService(Repository<User> userRepository) {
        this.userRepository = userRepository;
    }

    public void addUser(String id, String name, String password, Role role) {
        requireText(id, "User ID cannot be empty");
        requireText(name, "User name cannot be empty");
        requireText(password, "Password cannot be empty");
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be empty");
        }
        User user = new User(id.trim(), name.trim(), password, role);
        userRepository.save(user);
    }

    public Optional<User> login(String id, String password) {
        return userRepository.findById(id)
                .filter(user -> user.getPassword().equals(password));
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void updateUser(String id, String name, String password, Role role) {
        userRepository.findById(id).ifPresent(user -> {
            requireText(name, "User name cannot be empty");
            if (role == null) {
                throw new IllegalArgumentException("Role cannot be empty");
            }
            user.setName(name.trim());
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(password);
            }
            user.setRole(role);
            userRepository.update(user);
        });
    }

    public void deleteUser(String id) {
        userRepository.delete(id);
    }

    public void initializeDemoUsers() {
        if (userRepository.findAll().isEmpty()) {
            addUser("admin", "Admin", "admin123", Role.ADMIN);
            addUser("pl1", "Project Leader 1", "pl123", Role.PL);
            addUser("pl2", "Project Leader 2", "pl123", Role.PL);
            for (int i = 1; i <= 10; i++) {
                addUser("dev" + i, "Developer " + i, "dev123", Role.DEV);
            }
            for (int i = 1; i <= 5; i++) {
                addUser("tester" + i, "Tester " + i, "test123", Role.TESTER);
            }
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
