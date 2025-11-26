package com.combine.auditorium.controller;

import com.combine.auditorium.common.Result;
import com.combine.auditorium.dto.LoginDTO;
import com.combine.auditorium.dto.RegisterDTO;
import com.combine.auditorium.entity.User;
import com.combine.auditorium.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 * 处理用户注册、登录相关的 HTTP 请求
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 用户登录接口
     * @param loginDTO 包含用户名和密码
     * @return 包含 token 的 JSON 对象
     */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginDTO loginDTO) {
        try {
            String token = userService.login(loginDTO);
            // 返回 Token 给前端，前端需将其存储 (如 localStorage)
            return Result.success(Map.of("token", token));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户注册接口
     * @param registerDTO 包含用户名、密码、确认密码
     * @return 注册成功的用户信息
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterDTO registerDTO) {
        try {
            User user = userService.register(registerDTO);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
