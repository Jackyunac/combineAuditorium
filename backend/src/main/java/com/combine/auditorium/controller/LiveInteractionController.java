package com.combine.auditorium.controller;

import com.combine.auditorium.common.Result;
import com.combine.auditorium.entity.LiveDanmu;
import com.combine.auditorium.entity.LiveGift;
import com.combine.auditorium.service.LiveInteractionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/live/{roomCode}")
@RequiredArgsConstructor
public class LiveInteractionController {

    private final LiveInteractionService liveInteractionService;

    @GetMapping("/danmu")
    public Result<List<LiveDanmu>> listDanmu(@PathVariable String roomCode,
                                             @RequestParam(value = "sinceId", required = false) Long sinceId,
                                             @RequestParam(value = "limit", defaultValue = "50") Integer limit) {
        return Result.success(liveInteractionService.listDanmu(roomCode, sinceId, limit));
    }

    @PostMapping("/danmu")
    public Result<LiveDanmu> sendDanmu(@PathVariable String roomCode,
                                       @RequestBody Map<String, String> payload,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        String content = payload.get("content");
        return Result.success(liveInteractionService.sendDanmu(roomCode, userId, content));
    }

    @GetMapping("/gifts")
    public Result<List<LiveGift>> listGifts(@PathVariable String roomCode,
                                            @RequestParam(value = "sinceId", required = false) Long sinceId,
                                            @RequestParam(value = "limit", defaultValue = "30") Integer limit) {
        return Result.success(liveInteractionService.listGifts(roomCode, sinceId, limit));
    }

    @PostMapping("/gifts")
    public Result<LiveGift> sendGift(@PathVariable String roomCode,
                                     @RequestBody Map<String, Object> payload,
                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        String giftType = payload.get("giftType") == null ? null : payload.get("giftType").toString();
        Integer giftCount = 1;
        if (payload.get("giftCount") != null) {
            try {
                giftCount = Integer.parseInt(payload.get("giftCount").toString());
            } catch (NumberFormatException ignored) {
                giftCount = 1;
            }
        }
        String note = payload.get("message") == null ? null : payload.get("message").toString();
        return Result.success(liveInteractionService.sendGift(roomCode, userId, giftType, giftCount, note));
    }
}
