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
        User user = new User(id, name, password, role);
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
}
