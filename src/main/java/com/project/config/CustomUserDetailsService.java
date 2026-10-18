package com.project.config;

import com.project.model.User;
import com.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(
            String email)
            throws UsernameNotFoundException {

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "User not found"));

        return new CustomUserDetails(user);
    }
}