package com.combine.auditorium.websocket;

import com.combine.auditorium.entity.LiveDanmu;
import com.combine.auditorium.entity.LiveGift;
import com.combine.auditorium.service.LiveInteractionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class LiveWebSocketHandler extends TextWebSocketHandler {

    private final LiveInteractionService interactionService;
    private final ObjectMapper objectMapper;

    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomCode = getRoomCode(session);
        if (!StringUtils.hasText(roomCode)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        roomSessions.computeIfAbsent(roomCode, key -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("WebSocket connected, room={}, session={}", roomCode, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String roomCode = getRoomCode(session);
        Long userId = (Long) session.getAttributes().get("userId");
        if (!StringUtils.hasText(roomCode) || userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        Map<String, Object> payload;
        try {
            payload = objectMapper.readValue(message.getPayload(), new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse incoming message: {}", e.getMessage());
            return;
        }

        String type = payload.getOrDefault("type", "").toString().toLowerCase();
        try {
            if ("danmu".equals(type)) {
                String content = payload.getOrDefault("content", "").toString();
                LiveDanmu danmu = interactionService.sendDanmu(roomCode, userId, content);
                broadcast(roomCode, Map.of("type", "danmu", "data", danmu));
            } else if ("gift".equals(type)) {
                String giftType = payload.getOrDefault("giftType", "").toString();
                Integer giftCount = parseInt(payload.get("giftCount"), 1);
                String note = payload.get("message") == null ? null : payload.get("message").toString();
                LiveGift gift = interactionService.sendGift(roomCode, userId, giftType, giftCount, note);
                broadcast(roomCode, Map.of("type", "gift", "data", gift));
            } else {
                log.warn("Unknown message type: {}", type);
            }
        } catch (Exception e) {
            log.warn("Failed to handle message type={}, err={}", type, e.getMessage());
            sendError(session, e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.warn("WebSocket transport error: {}", exception.getMessage());
        removeSession(session);
    }

    private void broadcast(String roomCode, Object payload) {
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.warn("Serialize broadcast payload failed: {}", e.getMessage());
            return;
        }
        for (WebSocketSession session : roomSessions.getOrDefault(roomCode, Collections.emptySet())) {
            if (session.isOpen()) {
                try {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(json));
                    }
                } catch (IOException e) {
                    log.warn("Failed to send message to session {}: {}", session.getId(), e.getMessage());
                }
            }
        }
    }

    private void removeSession(WebSocketSession session) {
        String roomCode = getRoomCode(session);
        if (roomCode == null) {
            return;
        }
        Set<WebSocketSession> sessions = roomSessions.get(roomCode);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomCode);
            }
        }
        log.info("WebSocket closed, room={}, session={}", roomCode, session.getId());
    }

    private String getRoomCode(WebSocketSession session) {
        Object room = session.getAttributes().get("roomCode");
        return room == null ? null : room.toString();
    }

    private Integer parseInt(Object obj, int defaultVal) {
        if (obj == null) return defaultVal;
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private void sendError(WebSocketSession session, String message) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            String payload = objectMapper.writeValueAsString(Map.of("type", "error", "message", message));
            synchronized (session) {
                session.sendMessage(new TextMessage(payload));
            }
        } catch (Exception e) {
            log.warn("Failed to send error message: {}", e.getMessage());
        }
    }
}
