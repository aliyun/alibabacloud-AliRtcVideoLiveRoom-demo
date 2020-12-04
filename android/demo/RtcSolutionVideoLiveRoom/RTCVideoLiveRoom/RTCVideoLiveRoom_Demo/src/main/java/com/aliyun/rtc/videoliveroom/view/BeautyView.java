package com.aliyun.rtc.videoliveroom.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aliyun.rtc.videoliveroom.R;
import com.aliyun.rtc.videoliveroom.rtc.BaseRTCVideoLiveRoom;

public class BeautyView extends FrameLayout {

    private SeekBar mPgWhiteing;
    private SeekBar mPgSmoothness;
    private TextView mTvWhiteningValue;
    private TextView mTvSmoothnessValue;
    private float mWhiteLevel;
    private float mSmoothLevel;

    public BeautyView(@NonNull Context context) {
        this(context, null);
    }

    public BeautyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BeautyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mWhiteLevel = BaseRTCVideoLiveRoom.sharedInstance().getWhiteLevel();
        mSmoothLevel = BaseRTCVideoLiveRoom.sharedInstance().getSmoothLevel();
        LayoutInflater.from(getContext()).inflate(R.layout.rtc_videoliveroom_layout_beauty_effect,this,true);
        mPgWhiteing = findViewById(R.id.rtc_videoliveroom_progress_whitening);
        mPgSmoothness = findViewById(R.id.rtc_videoliveroom_progress_smoothness);
        mTvWhiteningValue = findViewById(R.id.rtc_videoliveroom_tv_whitening_value);
        mTvSmoothnessValue = findViewById(R.id.rtc_videoliveroom_tv_smoothness_value);
        reflushWhite(mWhiteLevel);
        reflushSmooth(mSmoothLevel);

        mPgWhiteing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mWhiteLevel = progress * 1.0f / seekBar.getMax();
                reflushWhite(mWhiteLevel);
                BaseRTCVideoLiveRoom.sharedInstance().setBeautyEffect(mWhiteLevel, mSmoothLevel);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mPgSmoothness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSmoothLevel = progress * 1.0f / seekBar.getMax();
                reflushSmooth(mSmoothLevel);
                BaseRTCVideoLiveRoom.sharedInstance().setBeautyEffect(mWhiteLevel, mSmoothLevel);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void reflushSmooth(float smoothLevel) {
        int smoothValue = (int) (mPgSmoothness.getMax() * smoothLevel);
        mPgSmoothness.setProgress(smoothValue);
        mTvSmoothnessValue.setText(String.valueOf(smoothValue));
    }

    private void reflushWhite(float whiteLevel) {
        int whiteValue = (int) (mPgWhiteing.getMax() * whiteLevel);
        mPgWhiteing.setProgress(whiteValue);
        mTvWhiteningValue.setText(String.valueOf(whiteValue));
    }
}
