package com.aliyun.rtc.videoliveroom.rtc;

import android.util.Log;

import com.alivc.rtc.AliRtcEngine;
import com.aliyun.rtc.videoliveroom.bean.SeatInfo;

public class SimpleRTCVideoLiveRoomDelegate implements RTCVideoLiveRoomDelegate {

    private static final String TAG = RTCVideoLiveRoomDelegate.class.getSimpleName();

    @Override
    public void onEnterSeat(String userId) {
        Log.i(TAG, "onEnterSeat: " + userId);
    }

    @Override
    public void onLeaveSeat(String userId) {
        Log.i(TAG, "onLeaveSeat: " + userId);
    }

    @Override
    public void onEnterSeatReult(String userId, int result) {
        Log.i(TAG, "onEnterSeatReult: " + userId + "; result : " + result);
    }

    @Override
    public void onLeaveSeatReult(String userId, int result) {
        Log.i(TAG, "onLeaveSeatReult: " + userId + "; result : " + result);
    }

    @Override
    public void onLeaveChannelResult(int result) {
        Log.i(TAG, "onLeaveChannelResult: ");
    }

    @Override
    public void onAudioPlayingStateChanged(AliRtcEngine.AliRtcAudioPlayingStateCode audioPlayingStatus) {
        Log.i(TAG, "onAudioPlayingStateChanged: ");
    }

    @Override
    public void onRoomDestroy() {
        Log.i(TAG, "onRoomDestroy: ");
    }

    @Override
    public void onOccurError(int error) {
        Log.i(TAG, "onSDKError: ");
    }

    @Override
    public void onNetworkQualityChanged(String s, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality1) {

    }

    @Override
    public void onJoinChannelResult(int result) {
        Log.i(TAG, "onCreateRoomResult: result : " + result);
    }

    @Override
    public void onKickOuted() {

    }

    @Override
    public void onRenderingStart() {

    }

    @Override
    public void onLivePlayRetryError() {

    }
}
