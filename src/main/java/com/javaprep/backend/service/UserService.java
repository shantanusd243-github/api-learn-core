package com.javaprep.backend.service;

import com.javaprep.backend.entity.User;

public interface UserService {
    User findById(String userId);
}