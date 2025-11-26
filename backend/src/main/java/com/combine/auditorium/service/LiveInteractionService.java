package com.combine.auditorium.service;

import com.combine.auditorium.entity.LiveDanmu;
import com.combine.auditorium.entity.LiveGift;

import java.util.List;

public interface LiveInteractionService {

    LiveDanmu sendDanmu(String roomCode, Long userId, String content);

    LiveGift sendGift(String roomCode, Long userId, String giftType, Integer giftCount, String message);

    List<LiveDanmu> listDanmu(String roomCode, Long sinceId, int limit);

    List<LiveGift> listGifts(String roomCode, Long sinceId, int limit);
}
