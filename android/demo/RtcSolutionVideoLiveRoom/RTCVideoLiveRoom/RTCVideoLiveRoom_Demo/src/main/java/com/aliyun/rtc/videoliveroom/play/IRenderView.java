package com.aliyun.rtc.videoliveroom.play;

import android.view.Surface;
import android.view.View;

public interface IRenderView {

    void addRenderCallback(IRenderCallback renderCallback);

    View getView();

    public interface IRenderCallback{
        void onSurfaceCreate(Surface surface);

        void onSurfaceChanged(int width, int height);

        void onSurfaceDestroyed();
    }
}
