package com.combine.auditorium.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.combine.auditorium.entity.LiveDanmu;
import com.combine.auditorium.entity.LiveGift;
import com.combine.auditorium.entity.LiveRoom;
import com.combine.auditorium.entity.User;
import com.combine.auditorium.mapper.LiveDanmuMapper;
import com.combine.auditorium.mapper.LiveGiftMapper;
import com.combine.auditorium.mapper.LiveRoomMapper;
import com.combine.auditorium.service.LiveInteractionService;
import com.combine.auditorium.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LiveInteractionServiceImpl implements LiveInteractionService {

    private static final int MAX_LIMIT = 100;
    private static final int MAX_CONTENT_LENGTH = 200;

    private final LiveDanmuMapper liveDanmuMapper;
    private final LiveGiftMapper liveGiftMapper;
    private final LiveRoomMapper liveRoomMapper;
    private final UserService userService;

    @Override
    public LiveDanmu sendDanmu(String roomCode, Long userId, String content) {
        LiveRoom room = requireRoom(roomCode);
        String trimmedContent = StrUtil.nullToEmpty(content).trim();
        if (trimmedContent.isEmpty()) {
            throw new RuntimeException("弹幕内容不能为空");
        }
        if (trimmedContent.length() > MAX_CONTENT_LENGTH) {
            trimmedContent = trimmedContent.substring(0, MAX_CONTENT_LENGTH);
        }

        LiveDanmu danmu = new LiveDanmu();
        danmu.setRoomCode(room.getRoomCode());
        danmu.setUserId(userId);
        danmu.setContent(trimmedContent);
        danmu.setCreatedAt(LocalDateTime.now());
        liveDanmuMapper.insert(danmu);
        attachUserInfo(Collections.singletonList(danmu));
        return danmu;
    }

    @Override
    public LiveGift sendGift(String roomCode, Long userId, String giftType, Integer giftCount, String message) {
        LiveRoom room = requireRoom(roomCode);
        String resolvedGift = StrUtil.nullToEmpty(giftType).trim();
        if (resolvedGift.isEmpty()) {
            throw new RuntimeException("礼物类型不能为空");
        }
        int count = giftCount == null || giftCount <= 0 ? 1 : giftCount;
        String note = StrUtil.emptyToNull(StrUtil.nullToEmpty(message).trim());
        if (note != null && note.length() > MAX_CONTENT_LENGTH) {
            note = note.substring(0, MAX_CONTENT_LENGTH);
        }

        LiveGift gift = new LiveGift();
        gift.setRoomCode(room.getRoomCode());
        gift.setUserId(userId);
        gift.setGiftType(resolvedGift);
        gift.setGiftCount(count);
        gift.setMessage(note);
        gift.setCreatedAt(LocalDateTime.now());
        liveGiftMapper.insert(gift);
        attachUserInfo(Collections.singletonList(gift));
        return gift;
    }

    @Override
    public List<LiveDanmu> listDanmu(String roomCode, Long sinceId, int limit) {
        requireRoom(roomCode);
        int rows = normalizeLimit(limit);
        LambdaQueryWrapper<LiveDanmu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LiveDanmu::getRoomCode, roomCode);
        if (sinceId != null) {
            wrapper.gt(LiveDanmu::getId, sinceId);
        }
        wrapper.orderByDesc(LiveDanmu::getId).last("limit " + rows);
        List<LiveDanmu> list = liveDanmuMapper.selectList(wrapper);
        Collections.reverse(list);
        attachUserInfo(list);
        return list;
    }

    @Override
    public List<LiveGift> listGifts(String roomCode, Long sinceId, int limit) {
        requireRoom(roomCode);
        int rows = normalizeLimit(limit);
        LambdaQueryWrapper<LiveGift> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LiveGift::getRoomCode, roomCode);
        if (sinceId != null) {
            wrapper.gt(LiveGift::getId, sinceId);
        }
        wrapper.orderByDesc(LiveGift::getId).last("limit " + rows);
        List<LiveGift> list = liveGiftMapper.selectList(wrapper);
        Collections.reverse(list);
        attachUserInfo(list);
        return list;
    }

    private LiveRoom requireRoom(String roomCode) {
        LiveRoom room = liveRoomMapper.selectOne(new LambdaQueryWrapper<LiveRoom>()
                .eq(LiveRoom::getRoomCode, roomCode));
        if (room == null) {
            throw new RuntimeException("直播间不存在");
        }
        return room;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 20;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private void attachUserInfo(List<?> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> userIds = items.stream()
                .map(obj -> {
                    if (obj instanceof LiveDanmu danmu) {
                        return danmu.getUserId();
                    }
                    if (obj instanceof LiveGift gift) {
                        return gift.getUserId();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Long userId : userIds) {
            User user = userService.getById(userId);
            items.forEach(item -> {
                if (user == null) {
                    return;
                }
                if (item instanceof LiveDanmu danmu && userId.equals(danmu.getUserId())) {
                    danmu.setNickname(user.getNickname());
                    danmu.setAvatar(user.getAvatar());
                }
                if (item instanceof LiveGift gift && userId.equals(gift.getUserId())) {
                    gift.setNickname(user.getNickname());
                    gift.setAvatar(user.getAvatar());
                }
            });
        }
    }
}
