package com.combine.auditorium.interceptor;

import cn.hutool.core.util.StrUtil;
import com.combine.auditorium.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器
 * 拦截所有请求，验证 Header 中的 Token 是否有效
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求 (跨域预检请求不需要 Token)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从 Header 获取 Token
        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token)) {
            response.setStatus(401);
            return false;
        }
        
        // 去除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证 Token
        if (!JwtUtils.validate(token)) {
            response.setStatus(401);
            return false;
        }
        
        // Token 有效，解析出 userId 存入 request attribute，方便后续 Controller 使用
        request.setAttribute("userId", JwtUtils.getUserId(token));

        return true;
    }
}
