package com.javaprep.backend.service.impl;

import com.javaprep.backend.entity.User;
import com.javaprep.backend.repository.UserRepository;
import com.javaprep.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService { // Implements interface[cite: 1]
    private final UserRepository userRepository;

    @Override
    public User findById(String userId) {
        return userRepository.findByEmail(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}