package com.combine.auditorium.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.combine.auditorium.common.RoleConstants;
import com.combine.auditorium.dto.LoginDTO;
import com.combine.auditorium.dto.RegisterDTO;
import com.combine.auditorium.entity.User;
import com.combine.auditorium.mapper.UserMapper;
import com.combine.auditorium.service.UserService;
import com.combine.auditorium.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    /**
     * 用户登录
     * @param loginDTO 登录参数
     * @return JWT Token
     */
    @Override
    public String login(LoginDTO loginDTO) {
        // 根据用户名查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginDTO.getUsername()));

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // 验证密码 (BCrypt)
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // 验证通过，生成并返回 Token
        return JwtUtils.createToken(user.getId(), user.getUsername());
    }

    /**
     * 用户注册
     * @param registerDTO 注册参数
     * @return 注册成功的用户对象
     */
    @Override
    public User register(RegisterDTO registerDTO) {
        // 校验两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        // 检查用户名是否已存在
        User exist = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, registerDTO.getUsername()));
        if (exist != null) {
            throw new RuntimeException("Username already exists");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        // 密码加密存储
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));
        user.setRole(RoleConstants.USER);
        // 设置默认头像 (Element UI 默认头像)
        user.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        
        userMapper.insert(user);
        return user;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = new User();
        user.setId(userId);
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
    }
}
