package com.aliyun.rtc.videoliveroom.bean;

import java.util.List;

/**
 * rtc 支持的背景乐、音效、混响等功能字段实体类
 * 合在一起管理比较方便
 */
public class RtcFunctionBean {
    /**
     * 耳返状态
     */
    private boolean earBack;

    /**
     * 当前混响模式
     */
    private int mEffectReverbMode;

    /**
     * 背景音乐
     */
    private List<RtcAudioFileInfo> mBgmFiles;

    /**
     * 音效文件
     */
    private List<RtcAudioFileInfo> mAudioEffectFiles;

    /**
     * 当前选中的背景乐或者音效文件
     */
    private RtcAudioFileInfo mCurrAudioFileInfo;

    public boolean isEarBack() {
        return earBack;
    }

    public void setEarBack(boolean earBack) {
        this.earBack = earBack;
    }

    public int getEffectReverbMode() {
        return mEffectReverbMode;
    }

    public void setEffectReverbMode(int effectReverbMode) {
        mEffectReverbMode = effectReverbMode;
    }

    public List<RtcAudioFileInfo> getBgmFiles() {
        return mBgmFiles;
    }

    public void setBgmFiles(List<RtcAudioFileInfo> bgmFiles) {
        mBgmFiles = bgmFiles;
    }

    public List<RtcAudioFileInfo> getAudioEffectFiles() {
        return mAudioEffectFiles;
    }

    public void setAudioEffectFiles(List<RtcAudioFileInfo> audioEffectFiles) {
        mAudioEffectFiles = audioEffectFiles;
    }

    public RtcAudioFileInfo getCurrAudioFileInfo() {
        return mCurrAudioFileInfo;
    }

    public void setCurrAudioFileInfo(RtcAudioFileInfo currAudioFileInfo) {
        mCurrAudioFileInfo = currAudioFileInfo;
    }
}
