package com.aliyun.rtc.videoliveroom.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.alivc.rtc.AliRtcEngine;
import com.aliyun.rtc.videoliveroom.R;
import com.aliyun.rtc.videoliveroom.adapter.EffectReverbAdapter;
import com.aliyun.rtc.videoliveroom.rtc.BaseRTCVideoLiveRoom;
import com.aliyun.rtc.videoliveroom.rtc.RtcEffectReverbMode;

import java.util.ArrayList;
import java.util.List;

public class EffectReverbModeView extends FrameLayout implements CompoundButton.OnCheckedChangeListener, EffectReverbAdapter.onItemClickListener {

    private Switch mSwitchEarBack;
    private TextView mTvSwitchState;
    private boolean mEnableEarBack;
    private EffectReverbModeListener mEffectReverbModeListener;
    private List<Pair<String, Integer>> mEffectReverbDatas;
    private RtcEffectReverbMode[] mRtcEffectReverbModes;
    private int mSelectedPosition;

    public EffectReverbModeView(@NonNull Context context) {
        this(context, null);
    }

    public EffectReverbModeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EffectReverbModeView(@NonNull Context context, boolean enableEarBack, int selectedPosition) {
        this(context);
        mEnableEarBack = enableEarBack;
        mEffectReverbDatas = new ArrayList<>();
        mSelectedPosition = selectedPosition;
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.rtc_videoliveroom_layout_audio_effect_reverb_mode, this, true);
        mSwitchEarBack = findViewById(R.id.rtc_videoliveroom_switch_earback);
        mTvSwitchState = findViewById(R.id.rtc_videoliveroom_tv_switch_state);
        RecyclerView rcyEffectReverb = findViewById(R.id.rtc_videoliveroom_rcy_audio_effect_reverb);
        mSwitchEarBack.setOnCheckedChangeListener(this);

        initData();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rcyEffectReverb.setLayoutManager(linearLayoutManager);
        EffectReverbAdapter adapter = new EffectReverbAdapter(getContext(), mEffectReverbDatas);
        adapter.setClickListener(this);
        adapter.setSelectedItem(mSelectedPosition);
        rcyEffectReverb.setAdapter(adapter);
    }

    private void initData() {
        mRtcEffectReverbModes = RtcEffectReverbMode.values();
        for (int i = 0; i < mRtcEffectReverbModes.length; i++) {
            RtcEffectReverbMode rtcEffectReverbMode = mRtcEffectReverbModes[i];
            String reverbModeDes = rtcEffectReverbMode.getDes();
            int drawable = getContext().getResources().getIdentifier("rtc_videoliveroom_effect_reverb_cover_" + i, "drawable", getContext().getPackageName());
            Pair<String, Integer> effectReverbData = new Pair<>(reverbModeDes, drawable);
            mEffectReverbDatas.add(effectReverbData);
        }
        mSwitchEarBack.setChecked(mEnableEarBack);
    }


    /**
     * switch checjedchanged监听
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mTvSwitchState.setText(isChecked ? R.string.rtc_videoliveroom_string_switch_open : R.string.rtc_videoliveroom_string_switch_close);
        mEnableEarBack = isChecked;
        if (mEffectReverbModeListener != null) {
            mEffectReverbModeListener.onCheckedChanged(mEnableEarBack);
        }
        BaseRTCVideoLiveRoom.sharedInstance().enableEarBack(isChecked);
    }

    public void setEffectReverbModeListener(EffectReverbModeListener effectReverbModeListener) {
        mEffectReverbModeListener = effectReverbModeListener;
    }

    @Override
    public void onClick(int position) {
        if (mRtcEffectReverbModes != null && position >= 0 && position < mRtcEffectReverbModes.length) {
            RtcEffectReverbMode rtcEffectReverbMode = mRtcEffectReverbModes[position];
            if (rtcEffectReverbMode != null) {
                AliRtcEngine.AliRtcAudioEffectReverbMode mode = rtcEffectReverbMode.getMode();
                BaseRTCVideoLiveRoom.sharedInstance().setAudioEffectReverbMode(mode);
            }
        }
        if (mEffectReverbModeListener != null) {
            mEffectReverbModeListener.onReverbModeSelectedChanged(position);
        }
    }

    public interface EffectReverbModeListener {
        void onReverbModeSelectedChanged(int selectedPosition);

        void onCheckedChanged(boolean enableEarBack);
    }
}
