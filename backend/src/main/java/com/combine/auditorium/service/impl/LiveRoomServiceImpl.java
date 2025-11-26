package com.combine.auditorium.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.combine.auditorium.entity.LiveRoom;
import com.combine.auditorium.mapper.LiveRoomMapper;
import com.combine.auditorium.service.LiveRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class LiveRoomServiceImpl implements LiveRoomService {

    @Value("${live.srs.allowed-ips:}")
    private String allowedIpList;

    private final LiveRoomMapper liveRoomMapper;

    @Override
    public LiveRoom getMyRoom(Long userId) {
        LiveRoom room = liveRoomMapper.selectOne(new LambdaQueryWrapper<LiveRoom>()
                .eq(LiveRoom::getUserId, userId));
        
        if (room == null) {
            room = new LiveRoom();
            room.setUserId(userId);
            room.setTitle("未命名直播间");
            room.setRoomCode(IdUtil.simpleUUID()); // 随机生成房间号
            room.setStreamKey(IdUtil.fastSimpleUUID()); // 随机生成推流密钥
            room.setStatus(0);
            liveRoomMapper.insert(room);
        }
        return room;
    }

    @Override
    public List<LiveRoom> listLiveRooms() {
        // 只查询直播中的房间 (status = 1)
        return liveRoomMapper.selectList(new LambdaQueryWrapper<LiveRoom>()
                .eq(LiveRoom::getStatus, 1)
                .orderByDesc(LiveRoom::getUpdateTime));
    }

    @Override
    public LiveRoom getRoomByCode(String roomCode) {
        return liveRoomMapper.selectOne(new LambdaQueryWrapper<LiveRoom>()
                .eq(LiveRoom::getRoomCode, roomCode));
    }

    /**
     * SRS 回调: 推流开始
     * 
     * @param app    应用名 (默认 live)
     * @param stream 流名称 (即 roomCode)
     * @param param  参数字符串 (如 ?secret=xxx)
     */
    @Override
    public int onPublish(String app, String stream, String param, String remoteIp) {
        log.info("SRS on_publish: app={}, stream={}, param={}, ip={}", app, stream, param, remoteIp);

        if (!isAllowedIp(remoteIp)) {
            log.warn("Publish rejected: ip {} not allowed", remoteIp);
            return 1;
        }
        
        // 1. 解析密钥
        // param 格式通常为: ?secret=xxx 或 secret=xxx
        String secret = null;
        if (StrUtil.isNotBlank(param)) {
            String[] params = param.replace("?", "").split("&");
            for (String p : params) {
                if (p.startsWith("secret=")) {
                    secret = p.split("=")[1];
                    break;
                }
            }
        }
        
        if (StrUtil.isBlank(secret)) {
            log.warn("Publish rejected: No secret provided");
            return 1; // 拒绝
        }

        // 2. 校验房间号与密钥
        LiveRoom room = getRoomByCode(stream);
        if (room == null) {
            log.warn("Publish rejected: Room not found {}", stream);
            return 1;
        }
        
        if (!secret.equals(room.getStreamKey())) {
            log.warn("Publish rejected: Invalid secret");
            return 1;
        }

        // 3. 更新直播状态
        room.setStatus(1); // 直播中
        liveRoomMapper.updateById(room);
        
        log.info("Live started: room={}", stream);
        return 0; // 允许
    }

    /**
     * SRS 回调: 推流结束
     */
    @Override
    public void onUnpublish(String app, String stream, String remoteIp) {
        log.info("SRS on_unpublish: app={}, stream={}, ip={}", app, stream, remoteIp);

        if (!isAllowedIp(remoteIp)) {
            log.warn("Unpublish ignored: ip {} not allowed", remoteIp);
            return;
        }
        
        LiveRoom room = getRoomByCode(stream);
        if (room != null) {
            room.setStatus(0); // 未开播
            liveRoomMapper.updateById(room);
            log.info("Live stopped: room={}", stream);
        }
    }

    @Override
    public void updateRoomInfo(Long userId, String title, String coverUrl) {
        LiveRoom room = getMyRoom(userId);
        if (room != null) {
            if (StrUtil.isNotBlank(title)) room.setTitle(title);
            if (StrUtil.isNotBlank(coverUrl)) room.setCoverUrl(coverUrl);
            liveRoomMapper.updateById(room);
        }
    }

    private boolean isAllowedIp(String remoteIp) {
        if (StrUtil.isBlank(allowedIpList)) {
            return true; // allow all if not configured
        }
        Set<String> allowed = Stream.of(allowedIpList.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        if (allowed.isEmpty()) {
            return true;
        }
        return allowed.contains(remoteIp);
    }
}

