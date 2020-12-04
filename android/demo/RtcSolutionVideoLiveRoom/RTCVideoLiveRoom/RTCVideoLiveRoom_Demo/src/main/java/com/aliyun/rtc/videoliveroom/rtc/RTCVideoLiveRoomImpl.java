package com.aliyun.rtc.videoliveroom.rtc;

import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Toast;

import com.alivc.rtc.AliRtcAuthInfo;
import com.alivc.rtc.AliRtcEngine;
import com.alivc.rtc.AliRtcEngineEventListener;
import com.alivc.rtc.AliRtcEngineNotify;
import com.alivc.rtc.device.utils.StringUtils;
import com.aliyun.player.AliPlayer;
import com.aliyun.player.IPlayer;
import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.rtc.videoliveroom.api.impl.RTCVideoLiveRoomApiImpl;
import com.aliyun.rtc.videoliveroom.api.impl.VideoLiveRoomModelFactory;
import com.aliyun.rtc.videoliveroom.api.net.OkhttpClient;
import com.aliyun.rtc.videoliveroom.bean.IResponse;
import com.aliyun.rtc.videoliveroom.bean.PlayUrlInfo;
import com.aliyun.rtc.videoliveroom.bean.RtcAuthInfo;
import com.aliyun.rtc.videoliveroom.play.AliLivePlayerStatus;
import com.aliyun.rtc.videoliveroom.play.AliLivePlayerView;
import com.aliyun.rtc.videoliveroom.utils.ApplicationContextUtil;

import org.webrtc.ali.Logging;
import org.webrtc.sdk.SophonSurfaceView;

import java.util.ArrayList;
import java.util.List;

public class RTCVideoLiveRoomImpl extends BaseRTCVideoLiveRoom {

    private static final String TAG = RTCVideoLiveRoomImpl.class.getSimpleName();
    private AliRtcEngine mEngine;
    private ArrayList<String> mSeatInfos;
    private static RTCVideoLiveRoomImpl mInstance;
    private RTCVideoLiveRoomDelegate mRTCVideoLiveRoomDelegate;
    private String mChannelId;
    private String mUserName;
    private Handler mUiHandler;
    private final Object mRtcUserInfoLock = new Object();
    private String mLocalUserId, mAnchorUserId;
    public static final int JOIN_CHANNEL_FAILD_CODE_BY_BAD_NETWORK = -1;
    public static final int JOIN_CHANNEL_FAILD_CODE_BY_DATA_EMPTY = -2;
    private AliRtcEngine.AliRtcBeautyConfig mAliRtcBeautyConfig;
    private RTCVideoLiveRoomApiImpl mRtcVideoLiveRoomApi;
    private AliRtcEngine.AliVideoCanvas mAliVideoCanvas;
    private boolean exitRoom;
    private AliLivePlayerView mPlayView;

    private RTCVideoLiveRoomImpl() {
        mRtcVideoLiveRoomApi = VideoLiveRoomModelFactory.createRTCVideoLiveApi();
        mSeatInfos = new ArrayList<>();
        mUiHandler = new Handler(Looper.getMainLooper());
        //初始化美颜等级
        mAliRtcBeautyConfig = new AliRtcEngine.AliRtcBeautyConfig();
        mAliRtcBeautyConfig.smoothnessLevel = 0.5f;
        mAliRtcBeautyConfig.whiteningLevel = 0.5f;
    }

    /***
     * 需要在主线程中调用
     */
    private void initEngine() {
        if (mEngine == null) {
            mEngine = AliRtcEngine.getInstance(ApplicationContextUtil.getAppContext());
            //默认开启扬声器
            mEngine.enableSpeakerphone(true);
            //设置频道模式为互动模式
            mEngine.setChannelProfile(AliRtcEngine.AliRTCSDK_Channel_Profile.AliRTCSDK_Communication);
            //设置自动订阅，不自动发布
            mEngine.setAutoPublishSubscribe(true, true);
            //设置高音质和媒体场景
            mEngine.setAudioProfile(AliRtcEngine.AliRtcAudioProfile.AliRtcEngineHighQualityMode,AliRtcEngine.AliRtcAudioScenario.AliRtcSceneMediaMode);
            //设置监听
            mEngine.setRtcEngineEventListener(mRtcEngineEventListener);
            mEngine.setRtcEngineNotify(mRtcEngineNotify);
            //初始化设置默认美颜等级
            mEngine.setBeautyEffect(true, mAliRtcBeautyConfig);
            //给相机流设置推流属性
            mEngine.setVideoProfile(AliRtcEngine.AliRtcVideoProfile.AliRTCSDK_Video_Profile_540_960P_15_1200Kb, AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera);
        }
    }

    public static RTCVideoLiveRoomImpl sharedInstance() {
        if (mInstance == null) {
            synchronized (RTCVideoLiveRoomImpl.class) {
                if (mInstance == null) {
                    mInstance = new RTCVideoLiveRoomImpl();
                }
            }
        }
        return mInstance;
    }

    /**
     * 学生加入房间，使用播放器播放
     *
     * @param channelId 房间号
     */
    @Override
    public void joinRoom(String channelId, String userId, String userName, AliLivePlayerView view) {
        mChannelId = channelId;
        mLocalUserId = userId;
        mUserName = userName;
        mAnchorUserId = "";
        mPlayView = view;
        exitRoom = false;
        initPlayer();
        //播放链接有时效性
        mRtcVideoLiveRoomApi.getPlayUrl(channelId, new OkhttpClient.BaseHttpCallBack<IResponse<PlayUrlInfo>>() {
            @Override
            public void onSuccess(IResponse<PlayUrlInfo> data) {
                if (data != null && data.getData() != null && data.getData().getPlayUrl() != null) {
                    String playUrl = data.getData().getPlayUrl().getRtmp();
                    if(mPlayView != null){
                        mPlayView.setDataSource(playUrl);
                        mPlayView.start();
                    }
                }
            }

            public void onError(String errorMsg) {
                Log.i(TAG, "onError: " + errorMsg);
            }
        });
    }

    /**
     * 学生离开房间
     */
    @Override
    public void leaveRoom() {
        exitRoom = true;
        if (mEngine != null && mEngine.isInCall()) {
            mEngine.stopPreview();
            mEngine.leaveChannel();
        } else {
            if(mPlayView != null){
                mPlayView.stop();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mRTCVideoLiveRoomDelegate != null) {
                        mRTCVideoLiveRoomDelegate.onLeaveChannelResult(0);
                    }
                }
            });
        }
    }

    /**
     * 主播创建并加入房间
     * 1.获取鉴权信息
     * 2.joinchannel
     *
     * @param channelId 房间号
     * @param userName  用户名
     */
    @Override
    public void createRoom(String channelId, String userId, String userName) {
        mChannelId = channelId;
        mAnchorUserId = mLocalUserId = userId;
        mUserName = userName;
        exitRoom = false;
        if (TextUtils.isEmpty(channelId) || TextUtils.isEmpty(userId)) {
            joinChannelResult(JOIN_CHANNEL_FAILD_CODE_BY_DATA_EMPTY);
        } else {
            joinChannel(channelId, userId, userName);
        }
    }

    /**
     * 主播销毁房间
     * 销毁频道
     */
    @Override
    public void destroyRoom() {
        exitRoom = true;
        //离开房间
        leaveChannel();
    }

    @Override
    public void setAudioEffectVolume(int soundId, int volume) {
        initEngine();
        mEngine.setAudioEffectPlayoutVolume(soundId, volume);
        mEngine.setAudioEffectPublishVolume(soundId, volume);
    }

    @Override
    public void setAudioAccompanyVolume(int volume) {
        initEngine();
        mEngine.setAudioAccompanyPublishVolume(volume);
        mEngine.setAudioAccompanyPlayoutVolume(volume);
    }

    /**
     * 上麦
     */
    @Override
    public void enterSeat() {
        //停止旁路播放
        if (mPlayView != null) {
            mPlayView.stop();
        }
        //入会
        joinChannel(mChannelId, mLocalUserId, mUserName);
    }

    /**
     * 下麦
     */
    @Override
    public void leaveSeat() {
        stopCameraPreview();
        leaveChannel();
    }

    @Override
    public int enableEarBack(boolean enableEarBack) {
        initEngine();
        return mEngine.enableEarBack(enableEarBack);
    }

    @Override
    public void startAudioAccompany(String fileName, boolean onlyLocalPlay, boolean replaceMic, int loopCycles) {
        initEngine();
        mEngine.startAudioAccompany(fileName, onlyLocalPlay, replaceMic, loopCycles);
    }

    @Override
    public void stopAudioAccompany() {
        initEngine();
        mEngine.stopAudioAccompany();
    }

    @Override
    public void destroySharedInstance() {
        destroy();
        if (mPlayView != null) {
            mPlayView.release();
        }
        mInstance = null;
        mUiHandler.removeCallbacksAndMessages(null);
        mUiHandler = null;
    }

    @Override
    public void playAudioEffect(int soundId, String filePath, int cycles, boolean publish) {
        initEngine();
        mEngine.playAudioEffect(soundId, filePath, cycles, publish);
    }

    @Override
    public void stopAudioEffect(int soundId) {
        initEngine();
        mEngine.stopAudioEffect(soundId);
    }

    @Override
    public void setDelegate(RTCVideoLiveRoomDelegate audioLiveRoomDelegate) {
        this.mRTCVideoLiveRoomDelegate = audioLiveRoomDelegate;
        if (mRTCVideoLiveRoomDelegate != null) {
            renotifySeatsInfo();
        }
    }

    @Override
    public void setAudioEffectReverbMode(AliRtcEngine.AliRtcAudioEffectReverbMode aliRtcAudioEffectReverbMode) {
        initEngine();
        mEngine.setAudioEffectReverbMode(aliRtcAudioEffectReverbMode);
    }


    @Override
    public void kickOut() {
        mRtcVideoLiveRoomApi.removeTerminals(mChannelId, mAnchorUserId, new OkhttpClient.BaseHttpCallBack<IResponse<String>>() {
            @Override
            public void onSuccess(IResponse<String> data) {
                Log.i(TAG, "onSuccess: " + data);
            }

            @Override
            public void onError(String errorMsg) {
                Log.i(TAG, "onError: " + errorMsg);
            }
        });
    }

    @Override
    public void switchCamera() {
        initEngine();
        mEngine.switchCamera();
    }

    @Override
    public void setBeautyEffect(float whiteLevel, float smoothLevel) {
        initEngine();
        mAliRtcBeautyConfig.smoothnessLevel = smoothLevel;
        mAliRtcBeautyConfig.whiteningLevel = whiteLevel;
        mEngine.setBeautyEffect(true, mAliRtcBeautyConfig);
    }

    @Override
    public void startCameraPreView(ViewGroup viewGroup) {
        if (mAliVideoCanvas == null) {
            mAliVideoCanvas = new AliRtcEngine.AliVideoCanvas();
        }
        SophonSurfaceView sophonSurfaceView = mAliVideoCanvas.view;
        if (sophonSurfaceView == null) {
            sophonSurfaceView = new SophonSurfaceView(viewGroup.getContext());
            sophonSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            // true 在最顶层，会遮挡一切view
            sophonSurfaceView.setZOrderOnTop(false);
            //true 如已绘制SurfaceView则在surfaceView上一层绘制。
            sophonSurfaceView.setZOrderMediaOverlay(false);
            mAliVideoCanvas.view = sophonSurfaceView;
            //设置渲染模式,一共有四种
            mAliVideoCanvas.renderMode = AliRtcEngine.AliRtcRenderMode.AliRtcRenderModeAuto;
        } else {
            ViewParent parent = sophonSurfaceView.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(sophonSurfaceView);
            }
        }
        viewGroup.removeAllViews();
        viewGroup.addView(sophonSurfaceView);
        initEngine();
        mEngine.setLocalViewConfig(mAliVideoCanvas, AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera);
        mEngine.startPreview();
    }

    @Override
    public void stopCameraPreview() {
        int i = mEngine.stopPreview();
        Log.i(TAG, "stopCameraPreview: " + i);
    }

    @Override
    public void startPlay(String uid, ViewGroup viewGroup) {
        if (StringUtils.equals(uid, mLocalUserId)) {
            startCameraPreView(viewGroup);
        } else {
            AliRtcEngine.AliVideoCanvas mAliVideoCanvas = new AliRtcEngine.AliVideoCanvas();
            SophonSurfaceView sophonSurfaceView = new SophonSurfaceView(viewGroup.getContext());
            sophonSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            // true 在最顶层，会遮挡一切view
            sophonSurfaceView.setZOrderOnTop(false);
            //true 如已绘制SurfaceView则在surfaceView上一层绘制。
            sophonSurfaceView.setZOrderMediaOverlay(true);
            mAliVideoCanvas.view = sophonSurfaceView;
            //设置渲染模式,一共有四种
            mAliVideoCanvas.renderMode = AliRtcEngine.AliRtcRenderMode.AliRtcRenderModeAuto;
            //添加LocalView
            viewGroup.removeAllViews();
            viewGroup.addView(mAliVideoCanvas.view);
            initEngine();
            mEngine.setRemoteViewConfig(mAliVideoCanvas, uid, AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera);
        }
    }

    @Override
    public float getWhiteLevel() {
        return mAliRtcBeautyConfig == null ? 0f : mAliRtcBeautyConfig.whiteningLevel;
    }

    @Override
    public float getSmoothLevel() {
        return mAliRtcBeautyConfig == null ? 0f : mAliRtcBeautyConfig.smoothnessLevel;
    }


    public void renotifySeatsInfo() {
        synchronized (mRtcUserInfoLock) {
            if (mSeatInfos != null && mRTCVideoLiveRoomDelegate != null) {
                for (String userId : mSeatInfos) {
                    mRTCVideoLiveRoomDelegate.onEnterSeat(userId);
                }
            }
        }
    }

    /**
     * SDK事件通知(回调接口都在子线程)
     */
    private AliRtcEngineNotify mRtcEngineNotify = new AliRtcEngineNotify() {

        /**
         * 远端用户上线通知
         *
         * @param s userid
         */
        @Override
        public void onRemoteUserOnLineNotify(String s) {
            Logging.d(TAG, "onRemoteUserOnLineNotify: s --> " + s);
            getSeatList();
            //主播需要调，观众不需要
            if (!TextUtils.isEmpty(mAnchorUserId)) {
                updateLayout();
            }
        }

        /**
         * 远端用户下线通知
         *
         * @param s userid
         */
        @Override
        public void onRemoteUserOffLineNotify(String s) {
            Logging.d(TAG, "onRemoteUserOffLineNotify: s --> " + s);
            getSeatList();
            //主播需要调，观众不需要
            if (!TextUtils.isEmpty(mAnchorUserId)) {
                updateLayout();
            }
        }

        /**
         * 远端用户发布音视频流变化通知
         *
         * @param s                userid
         * @param aliRtcAudioTrack 音频流
         * @param aliRtcVideoTrack 相机流
         */
        @Override
        public void onRemoteTrackAvailableNotify(String s, AliRtcEngine.AliRtcAudioTrack aliRtcAudioTrack,
                                                 AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack) {
            Logging.d(TAG, "onRemoteTrackAvailableNotify: s --> " + s);
        }

        /**
         * 首帧的接收回调
         *
         * @param s  callId
         * @param s1 stream_label
         * @param s2 track_label 分为video和audio
         * @param i  时间
         */
        @Override
        public void onFirstFramereceived(String s, String s1, String s2, int i) {
            Logging.d(TAG, "onFirstFramereceived: ");
        }

        /**
         * 首包的发送回调
         *
         * @param s  callId
         * @param s1 stream_label
         * @param s2 track_label 分为video和audio
         * @param i  时间
         */
        @Override
        public void onFirstPacketSent(String s, String s1, String s2, int i) {
            Logging.d(TAG, "onFirstPacketSent: ");
        }

        /**
         * 首包数据接收成功
         *
         * @param callId      远端用户callId
         * @param streamLabel 远端用户的流标识
         * @param trackLabel  远端用户的媒体标识
         * @param timeCost    耗时
         */
        @Override
        public void onFirstPacketReceived(String callId, String streamLabel, String trackLabel, int timeCost) {
            Logging.d(TAG, "onFirstPacketReceived: ");
        }

        /**
         * 被服务器踢出或者频道关闭时回调
         */
        @Override
        public void onBye(int i) {
            Logging.d(TAG, "onBye: " + i);
            if (i == 2) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //用户被踢出房间（体验时长结束）
                        if (mRTCVideoLiveRoomDelegate != null) {
                            mRTCVideoLiveRoomDelegate.onRoomDestroy();
                        }
                    }
                });
            } else if (i == 1) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //用户被踢出房间（被主播手动挂断）
                        if (mRTCVideoLiveRoomDelegate != null) {
                            mRTCVideoLiveRoomDelegate.onKickOuted();
                        }
                    }
                });
            }
        }

        @Override
        public void onAudioPlayingStateChanged(final AliRtcEngine.AliRtcAudioPlayingStateCode playState, AliRtcEngine.AliRtcAudioPlayingErrorCode errorCode) {
            super.onAudioPlayingStateChanged(playState, errorCode);
            Logging.d(TAG, "onAudioPlayingStateChanged: audioPlayingStatus : " + playState);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mRTCVideoLiveRoomDelegate != null) {
                        mRTCVideoLiveRoomDelegate.onAudioPlayingStateChanged(playState);
                    }
                }
            });
        }

    };

    /**
     * 主播在观众上下麦的时候调用此方法更新旁路直播布局
     */
    private void updateLayout() {
        mRtcVideoLiveRoomApi.updateMPULayout(mChannelId, new OkhttpClient.BaseHttpCallBack<IResponse<String>>() {
            @Override
            public void onSuccess(IResponse<String> data) {

            }

            @Override
            public void onError(String errorMsg) {

            }
        });
    }

    /**
     * 获取所有麦序
     */
    private void getSeatList() {
        if (TextUtils.isEmpty(mChannelId)) {
            return;
        }
        mRtcVideoLiveRoomApi.getUserList(mChannelId, new OkhttpClient.BaseHttpCallBack<IResponse<List<String>>>() {
            @Override
            public void onSuccess(IResponse<List<String>> data) {
                if (data != null && data.getData() != null) {
                    Log.i(TAG, "onSuccess: " + data.getData());
                    parseSeatListInfo(data.getData());
                }
            }

            @Override
            public void onError(String errorMsg) {
                Log.i(TAG, "onError: " + errorMsg);
            }
        });
    }

    private AliRtcEngineEventListener mRtcEngineEventListener = new AliRtcEngineEventListener() {

        @Override
        public void onJoinChannelResult(int result) {
            Logging.d(TAG, "onJoinChannelResult: result --> " + result);
            joinChannelResult(result);
            getSeatList();
        }

        public void onLiveStreamingSignalingResult(int i) {
            Logging.d(TAG, "onLiveStreamingSignalingResult: " + i);
        }

        /**
         * 离开房间的回调
         *
         * @param i 结果码
         */
        @Override
        public void onLeaveChannelResult(final int i) {
            Logging.d(TAG, "onLeaveChannelResult: i --> " + i + exitRoom);
            //离会清除麦序
            mSeatInfos.clear();
            //不是退出房间，是下麦操作
            if (!exitRoom) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //离会后再调用播放接口
                        joinRoom(mChannelId, mLocalUserId, mUserName, mPlayView);
                        if (mRTCVideoLiveRoomDelegate != null) {
                            mRTCVideoLiveRoomDelegate.onLeaveSeatReult(mLocalUserId, i);
                        }
                    }
                });
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mRTCVideoLiveRoomDelegate != null) {
                        mRTCVideoLiveRoomDelegate.onLeaveChannelResult(i);
                    }
                }
            });

        }

        /**
         * 网络状态变化的回调
         *
         * @param aliRtcNetworkQuality1 下行网络质量
         * @param aliRtcNetworkQuality  上行网络质量
         * @param s                     String  用户ID
         */
        @Override
        public void onNetworkQualityChanged(String s, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality1) {
            if (mRTCVideoLiveRoomDelegate != null) {
                mRTCVideoLiveRoomDelegate.onNetworkQualityChanged(s, aliRtcNetworkQuality, aliRtcNetworkQuality1);
            }
        }

        /**
         * 出现警告的回调
         *
         * @param i 错误码
         */
        @Override
        public void onOccurWarning(int i) {
            Logging.d(TAG, "onOccurWarning: i --> " + i);
        }

        /**
         * 出现错误的回调
         *
         * @param error 错误码
         */
        @Override
        public void onOccurError(final int error) {
            Logging.d(TAG, "onOccurError: error --> " + error);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mRTCVideoLiveRoomDelegate != null) {
                        mRTCVideoLiveRoomDelegate.onOccurError(error);
                    }
                }
            });
        }

        /**
         * 当前设备性能不足
         */
        @Override
        public void onPerformanceLow() {
            Logging.d(TAG, "onPerformanceLow: ");
        }

        /**
         * 当前设备性能恢复
         */
        @Override
        public void onPermormanceRecovery() {
            Logging.d(TAG, "onPermormanceRecovery: ");
        }

        /**
         * 连接丢失
         */
        @Override
        public void onConnectionLost() {
            Logging.d(TAG, "onConnectionLost: ");
        }

        /**
         * 尝试恢复连接
         */
        @Override
        public void onTryToReconnect() {
            Logging.d(TAG, "onTryToReconnect: ");
        }

        /**
         * 连接已恢复
         */
        @Override
        public void onConnectionRecovery() {
            Logging.d(TAG, "onConnectionRecovery: ");
        }

        @Override
        public void onSubscribeChangedNotify(String uid, AliRtcEngine.AliRtcAudioTrack audioTrack, AliRtcEngine.AliRtcVideoTrack videoTrack) {
            super.onSubscribeChangedNotify(uid, audioTrack, videoTrack);
            Logging.d(TAG, "onSubscribeChangedNotify: " + uid);
        }

    };


    private void runOnUiThread(Runnable runnable) {
        if (mUiHandler == null) {
            runnable.run();
            return;
        }
        if (mUiHandler.getLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            mUiHandler.post(runnable);
        }
    }

    private void destroy() {
        if (mEngine != null) {
            mEngine.destroy();
            mEngine = null;
        }
    }

    private void leaveChannel() {
        Logging.d(TAG, "leaveChannel: ");
        initEngine();
        mEngine.leaveChannel();
    }


    private void parseSeatListInfo(List<String> seatListInfos) {
        if (seatListInfos == null || seatListInfos.size() == 0) {
            return;
        }
        synchronized (RTCVideoLiveRoomImpl.class) {
            //清除下麦用户
            removeLeaveSeatUser(seatListInfos);
            //添加新上麦用户
            addEnterSeatUser(seatListInfos);
        }

    }

    private void addEnterSeatUser(List<String> seatListInfos) {

        for (final String userId : seatListInfos) {
            Log.i(TAG, "addEnterSeatUser: " + mSeatInfos);
            //数组角标
            int index = indexOfSeatList(userId);
            //不存在
            if (index == -1) {
                if (mRTCVideoLiveRoomDelegate != null) {
                    updateSeatInfo(userId);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRTCVideoLiveRoomDelegate.onEnterSeat(userId);
                        }
                    });
                }
            }
        }
    }

    private void removeLeaveSeatUser(List<String> seatListInfos) {
        for (int i = mSeatInfos.size() - 1; i >= 0; i--) {
            final String userId = mSeatInfos.get(i);
            int index = seatListInfos.indexOf(userId);
            if (index == -1) {
                if (mRTCVideoLiveRoomDelegate != null) {
                    removeSeatInfo(userId);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRTCVideoLiveRoomDelegate.onLeaveSeat(userId);
                        }
                    });
                }
            }
        }
    }

    private void joinChannel(String channelId, String userId, final String userName) {
        initEngine();
        //获取鉴权信息
        mRtcVideoLiveRoomApi.getRtcAuth(channelId, userId, new OkhttpClient.BaseHttpCallBack<IResponse<RtcAuthInfo>>() {
            @Override
            public void onSuccess(IResponse<RtcAuthInfo> data) {
                if (data != null && data.getData() != null) {
                    RtcAuthInfo rtcAuthInfo = data.getData();
                    AliRtcAuthInfo aliRtcAuthInfo = createAliRtcAuthInfo(rtcAuthInfo);
                    mLocalUserId = aliRtcAuthInfo.getUserId();
                    mEngine.joinChannel(aliRtcAuthInfo, userName);
                } else if (mRTCVideoLiveRoomDelegate != null) {
                    joinChannelResult(JOIN_CHANNEL_FAILD_CODE_BY_BAD_NETWORK);
                }
            }

            @Override
            public void onError(String errorMsg) {
                Logging.d(TAG, errorMsg);
                joinChannelResult(JOIN_CHANNEL_FAILD_CODE_BY_BAD_NETWORK);
            }
        });
    }

    private void joinChannelResult(final int result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRTCVideoLiveRoomDelegate != null) {
                    if (!TextUtils.isEmpty(mAnchorUserId)) {
                        mRTCVideoLiveRoomDelegate.onJoinChannelResult(result);
                    } else {
                        mRTCVideoLiveRoomDelegate.onEnterSeatReult(mLocalUserId, result);
                    }
                }
            }
        });
    }

    private void initPlayer() {
        //超时时间设置
        if(mPlayView != null){
            mPlayView.setLoadingTimeout(10000,3);
            mPlayView.setStartTimeout(10000,3);
            //软硬解设置
            mPlayView.enableHardwareDecoder(true);
            //设置缩放模式
            mPlayView.setScaleModel(IPlayer.ScaleMode.SCALE_ASPECT_FILL);
            //设置播放的载体为textureview
            mPlayView.setSurfaceType(AliLivePlayerView.SurfaceType.TEXTURE_VIEW);
            //错误监听
            mPlayView.setOnErrorListener(new IPlayer.OnErrorListener() {
                @Override
                public void onError(ErrorInfo errorInfo) {
                    Log.i(TAG, "onError: " + errorInfo);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mRTCVideoLiveRoomDelegate != null) {
                                mRTCVideoLiveRoomDelegate.onLivePlayRetryError();
                            }
                        }
                    });
                }
            });

            //首帧监听
            mPlayView.setOnRenderingStartListener(new IPlayer.OnRenderingStartListener() {
                @Override
                public void onRenderingStart() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mRTCVideoLiveRoomDelegate != null) {
                                mRTCVideoLiveRoomDelegate.onRenderingStart();
                            }
                        }
                    });
                }
            });
        }
    }

    private AliRtcAuthInfo createAliRtcAuthInfo(RtcAuthInfo rtcAuthInfo) {
        if (rtcAuthInfo == null) {
            return null;
        }
        List<String> gslb = rtcAuthInfo.getGslb();
        AliRtcAuthInfo userInfo = new AliRtcAuthInfo();
        //频道ID
        userInfo.setConferenceId(rtcAuthInfo.getChannelId());
        String appid = rtcAuthInfo.getAppid();
        /* 应用ID */
        userInfo.setAppid(appid);
        /* 随机码 */
        userInfo.setNonce(rtcAuthInfo.getNonce());
        /* 时间戳*/
        userInfo.setTimestamp(rtcAuthInfo.getTimestamp());
        String userid = rtcAuthInfo.getUserid();
        /* 用户ID
         * */
        userInfo.setUserId(userid);
        /* GSLB地址*/
        userInfo.setGslb(gslb.toArray(new String[0]));
        /*鉴权令牌Token*/
        userInfo.setToken(rtcAuthInfo.getToken());
        return userInfo;
    }

    private void updateSeatInfo(String userId) {
        synchronized (mRtcUserInfoLock) {
            if (mSeatInfos == null) {
                return;
            }
            if (mSeatInfos.contains(userId)) {
                mSeatInfos.set(mSeatInfos.indexOf(userId), userId);
            } else {
                mSeatInfos.add(userId);
            }
        }
    }


    private void removeSeatInfo(String userId) {
        synchronized (mRtcUserInfoLock) {
            if (mSeatInfos != null) {
                mSeatInfos.remove(userId);
            }
        }
    }

    private int indexOfSeatList(String userId) {
        synchronized (mRtcUserInfoLock) {
            if (mSeatInfos != null) {
                return mSeatInfos.indexOf(userId);
            }
            return -1;
        }
    }

}
