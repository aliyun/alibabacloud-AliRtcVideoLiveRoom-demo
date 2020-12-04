package com.aliyun.rtc.videoliveroom.rtc;

import com.alivc.rtc.AliRtcEngine;
import com.aliyun.rtc.videoliveroom.bean.SeatInfo;

public interface RTCVideoLiveRoomDelegate {

       /**
     * 麦序更新通知
     *
     * @param userId 麦位
     */
    void onEnterSeat(String userId);

    /**
     * 麦序更新通知
     *
     * @param userId 麦位
     */
    void onLeaveSeat(String userId);

    void onEnterSeatReult(String userId, int result);

    void onLeaveSeatReult(String userId, int result);

    /**
     * 退出房间回调
     */
    void onLeaveChannelResult(int result);

    /**
     * 播放状态更新回调
     *
     * @param audioPlayingStatus 当前播放状态
     */
    void onAudioPlayingStateChanged(AliRtcEngine.AliRtcAudioPlayingStateCode audioPlayingStatus);

    /**
     * 体验时长结束
     */
    void onRoomDestroy();

    /**
     * sdk报错,需要销毁实例
     */
    void onOccurError(int error);

    /**
     * 网络状态回调
     *
     * @param aliRtcNetworkQuality1 下行网络质量
     * @param aliRtcNetworkQuality  上行网络质量
     * @param s                     String  用户ID
     */
    void onNetworkQualityChanged(String s, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality1);


    /**
     * 创建房间的回调
     * @param result 0为成功 反之失败
     */
    void onJoinChannelResult(int result);

    /**
     * 被踢出房间
     */
    void onKickOuted();

    /**
     * 播放器首帧回调
     */
    void onRenderingStart();

    /**
     * 播放器拉不到流后重试3次还是拉不到数据时回调
     */
    void onLivePlayRetryError();
}
