package com.combine.auditorium.websocket;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.combine.auditorium.entity.LiveRoom;
import com.combine.auditorium.entity.User;
import com.combine.auditorium.mapper.LiveRoomMapper;
import com.combine.auditorium.service.UserService;
import com.combine.auditorium.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class LiveWebSocketAuthInterceptor implements HandshakeInterceptor {

    private final UserService userService;
    private final LiveRoomMapper liveRoomMapper;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }
        HttpServletRequest httpRequest = servletRequest.getServletRequest();
        String roomCode = httpRequest.getParameter("roomCode");
        if (!StringUtils.hasText(roomCode)) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        String token = httpRequest.getParameter("token");
        if (!StringUtils.hasText(token)) {
            token = httpRequest.getHeader("Authorization");
        }
        if (!StringUtils.hasText(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!JwtUtils.validate(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        Long userId = JwtUtils.getUserId(token);
        if (userId == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        User user = userService.getById(userId);
        if (user == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        LiveRoom room = liveRoomMapper.selectOne(new LambdaQueryWrapper<LiveRoom>().eq(LiveRoom::getRoomCode, roomCode));
        if (room == null) {
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return false;
        }

        attributes.put("userId", userId);
        attributes.put("nickname", user.getNickname());
        attributes.put("avatar", user.getAvatar());
        attributes.put("roomCode", roomCode);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
