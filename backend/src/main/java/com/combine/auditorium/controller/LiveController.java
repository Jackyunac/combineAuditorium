package com.combine.auditorium.controller;

import com.combine.auditorium.common.Result;
import com.combine.auditorium.dto.LiveRoomUpdateDTO;
import com.combine.auditorium.entity.LiveRoom;
import com.combine.auditorium.service.LiveRoomService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 直播控制器
 * 包含 SRS 回调接口和前端业务接口
 */
@RestController
@RequestMapping("/api/live")
@RequiredArgsConstructor
@Slf4j
public class LiveController {

    private final LiveRoomService liveRoomService;

    // ================= 前端业务接口 =================

    /**
     * 获取我的直播间信息
     */
    @GetMapping("/my-room")
    public Result<LiveRoom> getMyRoom(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        
        return Result.success(liveRoomService.getMyRoom(userId));
    }

    /**
     * 获取直播间列表
     */
    @GetMapping("/list")
    public Result<List<LiveRoom>> list() {
        return Result.success(liveRoomService.listLiveRooms());
    }

    /**
     * 获取直播间详情 (根据房间号)
     */
    @GetMapping("/room/{code}")
    public Result<LiveRoom> getRoom(@PathVariable String code) {
        LiveRoom room = liveRoomService.getRoomByCode(code);
        if (room == null) return Result.error("Room not found");
        return Result.success(room);
    }

    /**
     * 更新直播间信息
     */
    @PostMapping("/update")
    public Result<String> updateRoom(@RequestBody LiveRoomUpdateDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        
        liveRoomService.updateRoomInfo(userId, dto.getTitle(), dto.getCoverUrl());
        return Result.success("Updated");
    }

    // ================= SRS 回调接口 =================
    // 只有 SRS 服务器会调用这些接口
    // 注意: 实际生产中应限制调用来源 IP 仅为 SRS 服务器 IP

    /**
     * SRS 回调: 推流时调用
     */
    @PostMapping("/callback/on_publish")
    public int onPublish(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        // SRS POST body JSON: { "app": "live", "stream": "roomCode", "param": "?secret=xxx", ... }
        String app = (String) params.get("app");
        String stream = (String) params.get("stream");
        String param = (String) params.get("param");
        
        return liveRoomService.onPublish(app, stream, param, request.getRemoteAddr());
    }

    /**
     * SRS 回调: 停止推流时调用
     */
    @PostMapping("/callback/on_unpublish")
    public int onUnpublish(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String app = (String) params.get("app");
        String stream = (String) params.get("stream");
        
        liveRoomService.onUnpublish(app, stream, request.getRemoteAddr());
        return 0;
    }
    
    // 其他回调 (on_play, on_stop 等) 可按需实现，默认返回 0 允许
    @PostMapping("/callback/on_play")
    public int onPlay(@RequestBody Map<String, Object> params) { return 0; }
    
    @PostMapping("/callback/on_stop")
    public int onStop(@RequestBody Map<String, Object> params) { return 0; }
}

