package com.aliyun.rtc.videoliveroom.play;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public class LifeFragment extends Fragment {

    private LifeListener mLifeListener;

    public void setLifeListener(LifeListener lifeListener) {
        mLifeListener = lifeListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mLifeListener != null) {
            mLifeListener.onCreate();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLifeListener != null) {
            mLifeListener.onResume();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mLifeListener != null) {
            mLifeListener.onStart();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLifeListener != null) {
            mLifeListener.onPause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mLifeListener != null) {
            mLifeListener.onStop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLifeListener != null) {
            mLifeListener.onDestroy();
        }
    }
}
