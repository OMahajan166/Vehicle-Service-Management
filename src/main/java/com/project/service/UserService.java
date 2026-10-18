package com.project.service;

import com.project.model.User;
import com.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User registerUser(User user) {

        user.setPassword(
                passwordEncoder.encode(
                        user.getPassword()));

        user.setRole("CUSTOMER");

        return userRepository.save(user);
    }

    /** Create a login account with an explicit role (e.g. MECHANIC). Password is BCrypt-encoded. */
    public User createAccount(User user, String role) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(role);
        return userRepository.save(user);
    }

    /** Reset a user's password (stored BCrypt-encoded). */
    public void changePassword(Long userId, String newPassword) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        });
    }

    public Optional<User> loginUser(
            String email,
            String password) {

        Optional<User> user =
                userRepository.findByEmail(email);

        if (user.isPresent()
                && passwordEncoder.matches(
                        password,
                        user.get().getPassword())) {

            return user;
        }

        return Optional.empty();
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }
    
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();
}
