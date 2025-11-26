package com.combine.auditorium.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成和验证用户登录的 Token
 */
public class JwtUtils {

    // 密钥和过期时长优先读取环境变量，便于生产环境安全配置
    private static final String KEY = resolveSecret();
    private static final long EXPIRE_MILLIS = resolveExpireMillis();

    /**
     * 创建 Token
     * @param userId 用户ID
     * @param username 用户名
     * @return 加密后的 Token 字符串
     */
    public static String createToken(Long userId, String username) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("username", username);
        // 签发时间
        map.put(JWTPayload.ISSUED_AT, DateUtil.date());
        map.put(JWTPayload.EXPIRES_AT, DateUtil.date(System.currentTimeMillis() + EXPIRE_MILLIS));
        
        return JWTUtil.createToken(map, KEY.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 验证 Token 是否有效
     * @param token Token 字符串
     * @return true-有效, false-无效
     */
    public static boolean validate(String token) {
        try {
            if (!JWTUtil.verify(token, KEY.getBytes(StandardCharsets.UTF_8))) {
                return false;
            }
            final JWT jwt = JWTUtil.parseToken(token);
            final Object expObj = jwt.getPayload().getClaim(JWTPayload.EXPIRES_AT);
            if (expObj instanceof Date exp && exp.before(new Date())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 从 Token 中获取用户 ID
     * @param token Token 字符串
     * @return userId
     */
    public static Long getUserId(String token) {
        try {
            final JWT jwt = JWTUtil.parseToken(token);
            return Long.valueOf(jwt.getPayload("userId").toString());
        } catch (Exception e) {
            return null;
        }
    }

    private static String resolveSecret() {
        String env = System.getenv("JWT_SECRET");
        if (env != null && !env.isEmpty()) {
            return env;
        }
        String prop = System.getProperty("jwt.secret");
        if (prop != null && !prop.isEmpty()) {
            return prop;
        }
        // 默认值仅用于开发环境，生产必须通过环境变量或系统属性覆盖
        return "change-me-in-env";
    }

    private static long resolveExpireMillis() {
        String env = System.getenv("JWT_EXPIRE_HOURS");
        if (env != null && !env.isEmpty()) {
            try {
                long hours = Long.parseLong(env);
                if (hours > 0) {
                    return Duration.ofHours(hours).toMillis();
                }
            } catch (NumberFormatException ignored) {
                // fall through to default
            }
        }
        return Duration.ofHours(24).toMillis();
    }
}
