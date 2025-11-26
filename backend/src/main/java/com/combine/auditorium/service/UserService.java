package com.combine.auditorium.service;

import com.combine.auditorium.dto.LoginDTO;
import com.combine.auditorium.dto.RegisterDTO;
import com.combine.auditorium.entity.User;

public interface UserService {
    String login(LoginDTO loginDTO);
    User register(RegisterDTO registerDTO);
    User getById(Long id);
    void updateAvatar(Long userId, String avatarUrl);
}
