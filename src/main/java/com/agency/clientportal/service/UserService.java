package com.agency.clientportal.service;

import com.agency.clientportal.dto.UserRegistrationForm;
import com.agency.clientportal.entity.Role;
import com.agency.clientportal.entity.User;
import com.agency.clientportal.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(UserRegistrationForm form) {
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new IllegalArgumentException("Username already in use: " + form.getUsername());
        }
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + form.getEmail());
        }

        User user = new User();
        user.setUsername(form.getUsername());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setFullName(form.getFullName());
        user.setEmail(form.getEmail());
        user.setCompanyName(form.getCompanyName());
        user.setPhone(form.getPhone());
        user.setRole(form.getRole() != null ? form.getRole() : Role.ROLE_CLIENT);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    public User createSeedUser(String username, String rawPassword, String fullName, String email, String companyName, Role role) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setFullName(fullName);
            user.setEmail(email);
            user.setCompanyName(companyName);
            user.setRole(role);
            user.setEnabled(true);
            return userRepository.save(user);
        });
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findAllClients() {
        return userRepository.findByRole(Role.ROLE_CLIENT);
    }

    public List<User> findAllAdmins() {
        return userRepository.findByRole(Role.ROLE_ADMIN);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElse(null);
    }
}
