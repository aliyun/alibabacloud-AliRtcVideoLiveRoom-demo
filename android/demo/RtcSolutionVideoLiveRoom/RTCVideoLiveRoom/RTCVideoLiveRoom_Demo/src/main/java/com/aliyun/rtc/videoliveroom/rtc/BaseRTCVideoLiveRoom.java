package com.aliyun.rtc.videoliveroom.rtc;

import android.view.SurfaceView;
import android.view.ViewGroup;

import com.alivc.rtc.AliRtcEngine;
import com.aliyun.rtc.videoliveroom.play.AliLivePlayerView;

public abstract class BaseRTCVideoLiveRoom {

   /***********************************************


                      获取和销毁实例


   ***********************************************/
    /**
     * 获取单例
     */
    public static BaseRTCVideoLiveRoom sharedInstance() {
        return RTCVideoLiveRoomImpl.sharedInstance();
    }

    /**
     * 销毁实例
     */
    public abstract void destroySharedInstance();



    /***********************************************


                观众加入或离开直播间；上下麦


     ***********************************************/
    /**
     * 加入直播
     *
     * @param channelId 房间号
     * @param view  画面载体
     * @param userId uid
     * @param userName 昵称
     */
    public abstract void joinRoom(String channelId, String userId, String userName, AliLivePlayerView view);

    /**
     * 退出房间
     */
    public abstract void leaveRoom();

    /**
     * 上麦
     */
    public abstract void enterSeat();

    /**
     * 下麦
     */
    public abstract void leaveSeat();



    /***********************************************


                    主播创建、销毁直播间、踢人


     ***********************************************/

    /**
     * 创建房间
     * @param channelId 房间号
     * @param userId uid
     * @param userName  用户名
     */
    public abstract void createRoom(String channelId, String userId, String userName);

    /**
     * 销毁房间
     */
    public abstract void destroyRoom();

    /**
     * 踢人（主播才可以踢人）
     *
     */
    public abstract void kickOut();



    /***********************************************


                    调音台相关方法


     ***********************************************/

    /**
     * 设置音效音量
     * @param soundId  音效id
     * @param volume 音量
     */
    public abstract void setAudioEffectVolume(int soundId, int volume);

    /**
     * 设置伴奏音量
     * @param volume 音量
     */
    public abstract void setAudioAccompanyVolume(int volume);

    /**
     * 是否耳返
     *
     * @param enableEarBack 是否开启耳返 true为开启 false关闭
     * @return
     */
    public abstract int enableEarBack(boolean enableEarBack);

    /**
     * 开始伴奏
     *
     * @param fileName      伴奏文件路径，支持本地文件和网络url
     * @param onlyLocalPlay 是否仅本地播放，true表示仅仅本地播放，false表示本地播放且推流到远端
     * @param replaceMic    是否替换mic的音频流，true表示伴奏音频流替换本地mic音频流，false表示伴奏音频流和mic音频流同时推
     * @param loopCycles    循环播放次数，-1表示一直循环
     */
    public abstract void startAudioAccompany(String fileName, boolean onlyLocalPlay, boolean replaceMic, int loopCycles);

    /**
     * 停止伴奏
     */
    public abstract void stopAudioAccompany();

    /**
     * 播放音效
     *
     * @param soundId  音效ID
     * @param filePath 音效文件路径，支持本地文件和网络url
     * @param cycles   循环播放次数。-1表示一直循环
     * @param publish  是否将音效音频流推到远端
     */
    public abstract void playAudioEffect(int soundId, String filePath, int cycles, boolean publish);

    /**
     * 停止音效
     *
     * @param soundId 音效ID
     */
    public abstract void stopAudioEffect(int soundId);

    /**
     * 设置音效场景
     */
    public abstract void setAudioEffectReverbMode(AliRtcEngine.AliRtcAudioEffectReverbMode aliRtcAudioEffectReverbMode);



    /***********************************************


                    视频相关方法


     ***********************************************/
    /**
     * 翻转摄像头
     */
    public abstract void switchCamera();

    /**
     * 预览本地摄像头画面
     * @param viewGroup 播放画面的载体
     */
    public abstract void startCameraPreView(ViewGroup viewGroup);

    /**
     * 停止预览本地摄像头画面
     */
    public abstract void stopCameraPreview();

    /**
     * 播放远端流
     * @param uid 用户id
     * @param viewGroup 播放画面的载体
     */
    public abstract void startPlay(String uid, ViewGroup viewGroup);



    /***********************************************


                        美颜相关方法


     ***********************************************/

    /**
     * 设置美颜等级
     */
    public abstract void setBeautyEffect(float whiteLevel, float smoothLevel);

    /**
     * 获取当前美白等级
     */
    public abstract float getWhiteLevel();

    /**
     * 获取当前磨皮等级
     */
    public abstract float getSmoothLevel();



    /***********************************************


                       rtc和播放器的监听


     ***********************************************/
    /**
     * 设置rtc监听
     *
     * @param audioLiveRoomDelegate 监听
     */
    public abstract void setDelegate(RTCVideoLiveRoomDelegate audioLiveRoomDelegate);

}

