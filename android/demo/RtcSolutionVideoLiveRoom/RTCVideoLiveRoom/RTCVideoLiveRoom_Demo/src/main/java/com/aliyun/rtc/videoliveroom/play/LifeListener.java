package com.aliyun.rtc.videoliveroom.play;

public interface LifeListener {

    void onCreate();

    void onResume();

    void onStart();

    void onStop();

    void onPause();

    void onDestroy();
}
