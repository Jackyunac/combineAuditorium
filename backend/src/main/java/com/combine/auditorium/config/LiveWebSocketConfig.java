package com.combine.auditorium.config;

import com.combine.auditorium.websocket.LiveWebSocketAuthInterceptor;
import com.combine.auditorium.websocket.LiveWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class LiveWebSocketConfig implements WebSocketConfigurer {

    private final LiveWebSocketHandler liveWebSocketHandler;
    private final LiveWebSocketAuthInterceptor liveWebSocketAuthInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(liveWebSocketHandler, "/ws/live")
                .addInterceptors(liveWebSocketAuthInterceptor)
                .setAllowedOrigins("*");
    }
}
