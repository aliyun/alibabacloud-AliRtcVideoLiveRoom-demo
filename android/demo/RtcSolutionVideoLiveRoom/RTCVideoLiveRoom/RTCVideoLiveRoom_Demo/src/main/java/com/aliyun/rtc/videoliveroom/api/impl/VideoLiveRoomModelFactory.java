package com.aliyun.rtc.videoliveroom.api.impl;

public class VideoLiveRoomModelFactory {

    public static <T> T createLoader(Class<T> tClass) {
        T t = null;
        try {
            t = tClass.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return t;
    }

    public static RTCVideoLiveRoomApiImpl createRTCVideoLiveApi() {
        return createLoader(RTCVideoLiveRoomApiImpl.class);
    }

}
