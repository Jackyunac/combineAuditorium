package com.combine.auditorium.service;

import com.combine.auditorium.entity.LiveRoom;

import java.util.List;

public interface LiveRoomService {
    /**
     * 获取我的直播间 (如果不存在则创建)
     */
    LiveRoom getMyRoom(Long userId);

    /**
     * 获取直播间列表 (仅展示直播中的)
     */
    List<LiveRoom> listLiveRooms();

    /**
     * 根据房间号获取直播间
     */
    LiveRoom getRoomByCode(String roomCode);

    /**
     * SRS 回调: 推流开始
     * @param param SRS 回调参数
     * @return 0 允许, 其他 禁止
     */
    int onPublish(String app, String stream, String param, String remoteIp);

    /**
     * SRS 回调: 推流结束
     */
    void onUnpublish(String app, String stream, String remoteIp);

    /**
     * 更新直播间信息
     */
    void updateRoomInfo(Long userId, String title, String coverUrl);
}

