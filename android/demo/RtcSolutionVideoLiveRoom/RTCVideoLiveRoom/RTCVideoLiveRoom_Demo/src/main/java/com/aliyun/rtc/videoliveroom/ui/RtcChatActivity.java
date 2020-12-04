package com.aliyun.rtc.videoliveroom.ui;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alivc.rtc.AliRtcEngine;
import com.alivc.rtc.device.utils.StringUtils;
import com.aliyun.rtc.alivcrtcviewcommon.listener.OnTipsDialogListener;
import com.aliyun.rtc.alivcrtcviewcommon.widget.RTCDialogHelper;
import com.aliyun.rtc.videoliveroom.R;
import com.aliyun.rtc.videoliveroom.adapter.RtcBgmAdapter;
import com.aliyun.rtc.videoliveroom.api.impl.RTCVideoLiveRoomApiImpl;
import com.aliyun.rtc.videoliveroom.api.impl.VideoLiveRoomModelFactory;
import com.aliyun.rtc.videoliveroom.api.net.OkhttpClient;
import com.aliyun.rtc.videoliveroom.bean.ChannelUsersInfo;
import com.aliyun.rtc.videoliveroom.bean.IResponse;
import com.aliyun.rtc.videoliveroom.bean.RtcAudioFileInfo;
import com.aliyun.rtc.videoliveroom.bean.RtcFunctionBean;
import com.aliyun.rtc.videoliveroom.bean.UserInfo;
import com.aliyun.rtc.videoliveroom.constant.Constant;
import com.aliyun.rtc.videoliveroom.play.AliLivePlayerView;
import com.aliyun.rtc.videoliveroom.rtc.BaseRTCVideoLiveRoom;
import com.aliyun.rtc.videoliveroom.rtc.RTCVideoLiveRoomImpl;
import com.aliyun.rtc.videoliveroom.rtc.RTCVideoLiveRoomDelegate;
import com.aliyun.rtc.videoliveroom.rtc.SimpleRTCVideoLiveRoomDelegate;
import com.aliyun.rtc.videoliveroom.runnable.LoadAssetsFileRunnable;
import com.aliyun.rtc.videoliveroom.runnable.RunnableCallBack;
import com.aliyun.rtc.videoliveroom.ui.base.BaseActivity;
import com.aliyun.rtc.videoliveroom.utils.FileUtil;
import com.aliyun.rtc.videoliveroom.utils.NetWatchdogUtils;
import com.aliyun.rtc.videoliveroom.utils.ScreenUtil;
import com.aliyun.rtc.videoliveroom.utils.ThreadUtils;
import com.aliyun.rtc.videoliveroom.utils.UIHandlerUtil;
import com.aliyun.rtc.videoliveroom.utils.UserHelper;
import com.aliyun.rtc.videoliveroom.view.BeautyView;
import com.aliyun.rtc.videoliveroom.view.RTCAudioEffectView;
import com.aliyun.rtc.videoliveroom.view.RTCBottomDialog;
import com.aliyun.rtc.videoliveroom.view.RTCUserChatView;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;
import com.aliyun.svideo.common.utils.image.ImageLoaderOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.webrtc.alirtcInterface.ErrorCodeEnum.ERR_ICE_CONNECTION_HEARTBEAT_TIMEOUT;
import static org.webrtc.alirtcInterface.ErrorCodeEnum.ERR_SDK_INVALID_STATE;
import static org.webrtc.alirtcInterface.ErrorCodeEnum.ERR_SESSION_REMOVED;

public class RtcChatActivity extends BaseActivity implements View.OnClickListener, NetWatchdogUtils.NetChangeListener, RtcBgmAdapter.AudioPlayingListener {
    private static final String TAG = RtcChatActivity.class.getSimpleName();
    //连麦进来的为false，创建房间的为true；
    private boolean isAnchor;
    //连麦进来的为false，创建房间的为true；   连麦后置为！isInteractiveUser
    private boolean isInteractiveUser;
    
    private String mChannelId, mTitle;
    private RTCDialogHelper mRtcDialogHelper;
    private RTCAudioEffectView mRTCAudioEffectView;
    private boolean hasShowBadNetwork;
    private RTCUserChatView mRTCUserChatView;
    private AliLivePlayerView mPlaySurfaceView;
    private RTCVideoLiveRoomApiImpl mVideoLiveRoomApi;
    private TextView mTvExperienceTime;
    private RtcFunctionBean mRtcFunctionBean;
    private List<RtcAudioFileInfo> mBgmFiles, mAudioEffectFiles;
    private TimeCountRunnable mTimeCountRunnable;
    private String mCoverUrl;
    private TextView mTvEnterSeat;
    private LinearLayout mLlLeaveSeat;
    private LinearLayout mLlSwitchCameraLive;
    private RelativeLayout mRlPlayLayout;
    private TextView mTvLiveNotBegin;
    private AudioManager mAudioManager;

    public static void start(Context context, boolean isAnchor, String channelId, String title, String coverUrl) {
        Intent intent = new Intent(context, RtcChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isAnchor", isAnchor);
        bundle.putString("channelId", channelId);
        bundle.putString("title", title);
        bundle.putString("coverUrl", coverUrl);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntentData();
        initView();
        RTCVideoLiveRoomImpl.sharedInstance().setDelegate(mCallBack);
        mVideoLiveRoomApi = VideoLiveRoomModelFactory.createRTCVideoLiveApi();
        mRtcFunctionBean = new RtcFunctionBean();
        mBgmFiles = new ArrayList<>();
        mAudioEffectFiles = new ArrayList<>();
        initBgmAndAudioEffect();
        //第一次进入页面的时候根据身份控制显示旁路还是rtc本地预览
        if (!isAnchor) {
            UserInfo userInfo = UserHelper.getInstance().obtainUserInfo();
            RTCVideoLiveRoomImpl.sharedInstance().joinRoom(mChannelId, userInfo.getUserId(), userInfo.getUserName(), mPlaySurfaceView);
        }

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isAnchor) {
            if (mTimeCountRunnable == null) {
                mTimeCountRunnable = new TimeCountRunnable(System.currentTimeMillis());
                ThreadUtils.runOnSubThread(mTimeCountRunnable);
            } else {
                if (!mTimeCountRunnable.loop) {
                    mTimeCountRunnable.setLoop(true);
                }
            }
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.rtc_videoliveroom_activity_chat;
    }

    private void getIntentData() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            isInteractiveUser = isAnchor = extras.getBoolean("isAnchor", false);
            mChannelId = extras.getString("channelId", "");
            mTitle = extras.getString("title", "");
            mCoverUrl = extras.getString("coverUrl", "");
        }
    }

    private void initView() {
        TextView tvTitle = findViewById(R.id.rtc_videoliveroom_tv_title);
        ImageView ivClose = findViewById(R.id.rtc_videoliveroom_iv_close_chat);
        ImageView ivDisplayCover = findViewById(R.id.rtc_videoliveroom_iv_display_user);
        LinearLayout llExperienceTime = findViewById(R.id.rtc_videoliveroom_ll_experience_time);
        mTvEnterSeat = findViewById(R.id.rtc_videoliveroom_tv_conn_mic_live);
        mRTCUserChatView = findViewById(R.id.rtc_videoliveroom_view_interactive_user);
        mTvExperienceTime = findViewById(R.id.rtc_videoliveroom_tv_experience_time);
        RelativeLayout rlInteractiveFun = findViewById(R.id.rtc_videoliveroom_rl_funtion_interactive_user);
        LinearLayout llLiveFun = findViewById(R.id.rtc_videoliveroom_ll_funtion_live_user);
        mLlLeaveSeat = findViewById(R.id.rtc_videoliveroom_ll_kickout_interactive);
        mPlaySurfaceView = findViewById(R.id.rtc_videoliveroom_surface_play);
        mRlPlayLayout = findViewById(R.id.rtc_videoliveroom_rl_play_layout);
        mTvLiveNotBegin = findViewById(R.id.rtc_videoliveroom_tv_live_not_begin);
        LinearLayout llBeauty = findViewById(R.id.rtc_videoliveroom_ll_beauty_interactive);
        LinearLayout llSwitchCameraInteractive = findViewById(R.id.rtc_videoliveroom_ll_switchcamera_interactive);
        LinearLayout llAudioEffect = findViewById(R.id.rtc_videoliveroom_ll_audioeffect_interactive);
        LinearLayout llConnMic = findViewById(R.id.rtc_videoliveroom_ll_conn_mic_live);
        mLlSwitchCameraLive = findViewById(R.id.rtc_videoliveroom_ll_switchcamera_live);
        RelativeLayout rlContent = findViewById(R.id.rtc_videoliveroom_rl_content_chat);
        rlContent.setPadding(0, ScreenUtil.getStatusBarHeight(this), 0, 0);
        tvTitle.setText(mTitle);

        rlInteractiveFun.setVisibility(isAnchor ? View.VISIBLE : View.GONE);
        llLiveFun.setVisibility(isAnchor ? View.GONE : View.VISIBLE);
        llExperienceTime.setVisibility(isAnchor ? View.VISIBLE : View.GONE);
        mLlLeaveSeat.setVisibility(View.GONE);
        mLlSwitchCameraLive.setVisibility(View.GONE);
        showPlayView(isAnchor);

        ImageLoaderOptions imageLoaderOptions = new ImageLoaderOptions.Builder()
                .circle()
                .build();
        new ImageLoaderImpl().loadImage(RtcChatActivity.this, mCoverUrl, imageLoaderOptions).into(ivDisplayCover);

        llBeauty.setOnClickListener(this);
        llSwitchCameraInteractive.setOnClickListener(this);
        llAudioEffect.setOnClickListener(this);
        llConnMic.setOnClickListener(this);
        mLlSwitchCameraLive.setOnClickListener(this);
        ivClose.setOnClickListener(this);
        mLlLeaveSeat.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.rtc_videoliveroom_ll_beauty_interactive) {
            showBeautyDialog();
        } else if (id == R.id.rtc_videoliveroom_iv_close_chat) {
            showExitDialog();
        } else if (id == R.id.rtc_videoliveroom_ll_switchcamera_interactive) {
            RTCVideoLiveRoomImpl.sharedInstance().switchCamera();
        } else if (id == R.id.rtc_videoliveroom_ll_audioeffect_interactive) {
            showAudioEffectDialog();
        } else if (id == R.id.rtc_videoliveroom_ll_switchcamera_live) {
            RTCVideoLiveRoomImpl.sharedInstance().switchCamera();
        } else if (id == R.id.rtc_videoliveroom_ll_conn_mic_live) {
            if (!isInteractiveUser) {
                //连麦
                mVideoLiveRoomApi.describeChannelUsers(mChannelId, new OkhttpClient.BaseHttpCallBack<IResponse<ChannelUsersInfo>>() {
                    @Override
                    public void onSuccess(IResponse<ChannelUsersInfo> data) {
                        if (data.getData().getUserList().size() < Constant.MAX_SEAT_COUNT) {
                            enterSeat();
                        } else {
                            showToastInCenter(getString(R.string.rtc_videoliveroom_string_channel_user_num_empty));
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        showToastInCenter(errorMsg);
                    }
                });
            } else {
                //断开连麦
                leaveSeat();
            }
        } else if (id == R.id.rtc_videoliveroom_ll_kickout_interactive) {
            showKickOutUserDialog();
        }
    }

    private void enterSeat() {
        UIHandlerUtil.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                showLoading();
                isInteractiveUser = !isInteractiveUser;
                mTvEnterSeat.setText(getString(R.string.rtc_videoliveroom_string_unconn_mic));
                mLlSwitchCameraLive.setVisibility(View.VISIBLE);
                RTCVideoLiveRoomImpl.sharedInstance().enterSeat();
            }
        });
    }

    private void leaveSeat() {
        UIHandlerUtil.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                showLeaveSeatDialog();
            }
        });
    }

    /**
     * 加载assets中的音频文件
     */
    private void initBgmAndAudioEffect() {
        String bgmOutPath = FileUtil.getExternalCacheDirPath(this, Constant.PATH_DIR_BGM_OUT);
        String audioEffectOutPath = FileUtil.getExternalCacheDirPath(this, Constant.PATH_DIR_AUDIOEFFECT_OUT);
        //bgm
        ThreadUtils.runOnSubThread(new LoadAssetsFileRunnable(this, Constant.PATH_ASSETS_BGM, bgmOutPath, new RunnableCallBack<List<File>>() {
            @Override
            public void callBack(List<File> data) {
                mBgmFiles.clear();
                for (File datum : data) {
                    RtcAudioFileInfo info = new RtcAudioFileInfo();
                    info.file = datum;
                    info.volume = Constant.VALUE_AUDIO_EFFECT_VOLUME;
                    mBgmFiles.add(info);
                }
                mRtcFunctionBean.setBgmFiles(mBgmFiles);
            }
        }));
        //audioeffect
        ThreadUtils.runOnSubThread(new LoadAssetsFileRunnable(this, Constant.PATH_ASSETS_AUDIOEFFECT, audioEffectOutPath, new RunnableCallBack<List<File>>() {
            @Override
            public void callBack(List<File> data) {
                mAudioEffectFiles.clear();
                Collections.sort(data, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        boolean b = StringUtils.equals(o1.getName(), "笑声");
                        return !b ? -1 : 0;
                    }
                });
                for (File datum : data) {
                    RtcAudioFileInfo info = new RtcAudioFileInfo();
                    info.file = datum;
                    info.volume = Constant.VALUE_AUDIO_EFFECT_VOLUME;
                    mAudioEffectFiles.add(info);
                }
                mRtcFunctionBean.setAudioEffectFiles(mAudioEffectFiles);
            }
        }));
    }

    /**
     * 背景乐、音效播放状态监听
     */
    @Override
    public void onPlayStateChange(RtcAudioFileInfo rtcAudioFileInfo) {
        if (mBgmFiles.contains(rtcAudioFileInfo)) {
            flushBgmState(rtcAudioFileInfo);
        } else {
            flushAudioEffectState(rtcAudioFileInfo);
        }
    }

    @Override
    public void onVolumeChange(RtcAudioFileInfo rtcAudioFileInfo) {
        if (mBgmFiles.contains(rtcAudioFileInfo)) {
            RTCVideoLiveRoomImpl.sharedInstance().setAudioAccompanyVolume(rtcAudioFileInfo.volume);
        } else {
            RTCVideoLiveRoomImpl.sharedInstance().setAudioEffectVolume(mAudioEffectFiles.indexOf(rtcAudioFileInfo), rtcAudioFileInfo.volume);
        }
    }

    /**
     * 刷新音效的播放状态
     */
    private void flushAudioEffectState(RtcAudioFileInfo rtcAudioFileInfo) {
        RtcAudioFileInfo currAudioFileInfo = mRtcFunctionBean.getCurrAudioFileInfo();
        if (currAudioFileInfo != rtcAudioFileInfo && currAudioFileInfo != null) {
            currAudioFileInfo.playState = RtcAudioFileInfo.PERPARE;
            int index = mAudioEffectFiles.indexOf(currAudioFileInfo);
            if (index != -1) {
                RTCVideoLiveRoomImpl.sharedInstance().stopAudioEffect(index);
            }
        }
        mRtcFunctionBean.setCurrAudioFileInfo(rtcAudioFileInfo);
        int sourceId = mAudioEffectFiles.indexOf(rtcAudioFileInfo);
        int playState = rtcAudioFileInfo.playState;
        int prePlayState = rtcAudioFileInfo.prePlayState;
        if (prePlayState == RtcAudioFileInfo.PLAYING || prePlayState == RtcAudioFileInfo.STOP) {
            rtcAudioFileInfo.prePlayState = RtcAudioFileInfo.PERPARE;
            RTCVideoLiveRoomImpl.sharedInstance().playAudioEffect(sourceId, rtcAudioFileInfo.file.getAbsolutePath(), 1, false);
        } else if (playState == RtcAudioFileInfo.PLAYING || playState == RtcAudioFileInfo.STOP) {
            rtcAudioFileInfo.playState = RtcAudioFileInfo.PERPARE;
            RTCVideoLiveRoomImpl.sharedInstance().playAudioEffect(sourceId, rtcAudioFileInfo.file.getAbsolutePath(), 1, true);
        }
        RTCVideoLiveRoomImpl.sharedInstance().setAudioEffectVolume(sourceId, rtcAudioFileInfo.volume);
    }

    /**
     * 刷新背景乐播放状态
     */
    private void flushBgmState(RtcAudioFileInfo rtcAudioFileInfo) {
        RtcAudioFileInfo currAudioFileInfo = mRtcFunctionBean.getCurrAudioFileInfo();
        if (currAudioFileInfo != rtcAudioFileInfo && currAudioFileInfo != null) {
            currAudioFileInfo.playState = RtcAudioFileInfo.PERPARE;
            int index = mBgmFiles.indexOf(currAudioFileInfo);
            if (index != -1) {
                RTCVideoLiveRoomImpl.sharedInstance().stopAudioAccompany();
            }
        }
        mRtcFunctionBean.setCurrAudioFileInfo(rtcAudioFileInfo);
        int playState = rtcAudioFileInfo.playState;
        int prePlaySate = rtcAudioFileInfo.prePlayState;
        if (prePlaySate == RtcAudioFileInfo.PLAYING) {
            RTCVideoLiveRoomImpl.sharedInstance().startAudioAccompany(rtcAudioFileInfo.file.getAbsolutePath(), true, false, 1);
        } else if (playState == RtcAudioFileInfo.STOP || prePlaySate == RtcAudioFileInfo.STOP) {
            RTCVideoLiveRoomImpl.sharedInstance().stopAudioAccompany();
        } else if (playState == RtcAudioFileInfo.PLAYING) {
            RTCVideoLiveRoomImpl.sharedInstance().startAudioAccompany(rtcAudioFileInfo.file.getAbsolutePath(), false, false, 1);
        }
        RTCVideoLiveRoomImpl.sharedInstance().setAudioAccompanyVolume(rtcAudioFileInfo.volume);
    }

    private void showAudioEffectDialog() {
        RTCBottomDialog mBottomDialog = new RTCBottomDialog(this);
        mBottomDialog.setTitle(getString(R.string.rtc_videoliveroom_string_audio_effect));
        mRTCAudioEffectView = new RTCAudioEffectView(mRtcFunctionBean, this);
        mRTCAudioEffectView.setListener(this);
        mBottomDialog.setContentView(mRTCAudioEffectView);
        mBottomDialog.show();
    }

    @Override
    protected void onDestroy() {
        if (mRtcDialogHelper != null) {
            mRtcDialogHelper.hideAll();
            mRtcDialogHelper.release();
        }
        if (mTimeCountRunnable != null) {
            mTimeCountRunnable.setLoop(false);
        }
        ToastUtils.cancel();
        RTCVideoLiveRoomImpl.sharedInstance().destroyRoom();
        super.onDestroy();
    }

    /**
     * 根据当前的角色状态和上下麦状态控制布局显示隐藏
     *
     */
    private void showPlayView(boolean isInteractiveUser) {
        mRlPlayLayout.setVisibility(isInteractiveUser ? View.INVISIBLE : View.VISIBLE);
        mRTCUserChatView.setVisibility(isInteractiveUser ? View.VISIBLE : View.INVISIBLE);
    }

    private RTCVideoLiveRoomDelegate mCallBack = new SimpleRTCVideoLiveRoomDelegate() {

        /**
         * 远端用户上麦通知
         * @param userId 用户id
         */
        @Override
        public void onEnterSeat(String userId) {
            super.onEnterSeat(userId);
            showLoading();
            mRTCUserChatView.enterSeat(userId);
            if (mRTCUserChatView.getSeat().size() > 1 && mLlLeaveSeat.getVisibility() != View.VISIBLE) {
                mLlLeaveSeat.setVisibility(View.VISIBLE);
            }
            showPlayView(isInteractiveUser);
            hideLoading();
        }

        /**
         * 自己上线通知
         * @param userId 自己的id
         * @param result 0为成功反之失败
         */
        @Override
        public void onEnterSeatReult(String userId, int result) {
            super.onEnterSeatReult(userId, result);
            if (result != 0) {
                showToastInCenter(getString(R.string.rtc_videoliveroom_string_enter_seat_faild));
            }
        }

        /**
         * 远端用户下麦通知
         * @param userId 用户id
         */
        @Override
        public void onLeaveSeat(String userId) {
            super.onLeaveSeat(userId);
            showLoading();
            mRTCUserChatView.leaveSeat(userId);
            if (mRTCUserChatView.getSeat().size() <= 1) {
                mLlLeaveSeat.setVisibility(View.GONE);
            }
            showPlayView(isInteractiveUser);
            hideLoading();
        }

        /**
         * 自己下线通知
         * @param userId 自己的id
         * @param result 0为成功反之失败
         */
        @Override
        public void onLeaveSeatReult(String userId, int result) {
            Log.d(TAG,"自己下线通知onLeaveSeatReult==========="+userId+"  result====="+result);
            super.onLeaveSeatReult(userId, result);
            isInteractiveUser = false;
            mTvEnterSeat.setText(getString(R.string.rtc_videoliveroom_string_conn_mic));
            mLlSwitchCameraLive.setVisibility(View.GONE);
            if (result == 0) {
            } else {
                hideLoading();
                showToastInCenter(getString(R.string.rtc_videoliveroom_string_leave_seat_faild));
            }
        }

        /**
         * 退出房间回调
         */
        @Override
        public void onLeaveChannelResult(int result) {
            Log.d(TAG,"退出房间回调onLeaveChannelResult===========" +" result====="+result);
            hideLoading();
            if (result != 0) {
                showToastInCenter(String.valueOf(result));
            }
            if (isAnchor) {
                showLivingTimeDialog();
            } else {
                finish();
            }
        }

        /**
         * 播放状态更新回调
         * @param audioPlayingStatus 当前播放状态
         */
        @Override
        public void onAudioPlayingStateChanged(AliRtcEngine.AliRtcAudioPlayingStateCode audioPlayingStatus) {
            super.onAudioPlayingStateChanged(audioPlayingStatus);
            RtcAudioFileInfo currAudioFileInfo = mRtcFunctionBean.getCurrAudioFileInfo();
            if (audioPlayingStatus == AliRtcEngine.AliRtcAudioPlayingStateCode.AliRtcAudioPlayingEnded) {
                //播放完毕
                currAudioFileInfo.playState = RtcAudioFileInfo.PERPARE;
                currAudioFileInfo.prePlayState = RtcAudioFileInfo.PERPARE;
                mRtcFunctionBean.setCurrAudioFileInfo(currAudioFileInfo);
                if (mRTCAudioEffectView != null) {
                    mRTCAudioEffectView.notifyItemChanged(currAudioFileInfo);
                }
            }
        }

        /**
         * 体验时长结束
         */
        @Override
        public void onRoomDestroy() {

            if (isAnchor) {
                //体验时长结束时停止倒计时
                if (mTimeCountRunnable != null) {
                    mTimeCountRunnable.setLoop(false);
                }
                showTimeoutDialog();
            } else {
                showLiveEndDialog();
            }
        }

        /**
         * sdk报错,需要销毁实例
         */
        @Override
        public void onOccurError(int error) {
            //出现这几个错误码需要销毁sdk，否则无法再次观看
            switch (error) {
                case ERR_SDK_INVALID_STATE:
                case ERR_ICE_CONNECTION_HEARTBEAT_TIMEOUT:
                case ERR_SESSION_REMOVED:
                    //销毁
                    RTCVideoLiveRoomImpl.sharedInstance().destroySharedInstance();
                    showRtcErrorDialog();
                    break;
                default:
                    break;
            }

        }

        @Override
        public void onNetworkQualityChanged(String s, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality, AliRtcEngine.AliRtcNetworkQuality aliRtcNetworkQuality1) {
            super.onNetworkQualityChanged(s, aliRtcNetworkQuality, aliRtcNetworkQuality1);
            synchronized (RTCVideoLiveRoomImpl.class) {
                if (aliRtcNetworkQuality1.getValue() >= AliRtcEngine.AliRtcNetworkQuality.Network_Bad.getValue() && aliRtcNetworkQuality1.getValue() <= AliRtcEngine.AliRtcNetworkQuality.Network_VeryBad.getValue() && !hasShowBadNetwork) {//网络质量差
                    hasShowBadNetwork = true;
                    if (TextUtils.isEmpty(s)) {
                        showToastInCenter(getString(R.string.rtc_videoliveroom_string_network_bad));
                    }
                } else if (TextUtils.isEmpty(s) && aliRtcNetworkQuality1.getValue() <= AliRtcEngine.AliRtcNetworkQuality.Network_Good.getValue()) {
                    hasShowBadNetwork = false;
                }
            }
        }

        /**
         * 被主播挂断了
         */
        @Override
        public void onKickOuted() {
            super.onKickOuted();
            showToastInCenter(getString(R.string.rtc_videoliveroom_string_kickouted));
            mTvEnterSeat.setText(getString(R.string.rtc_videoliveroom_string_conn_mic));
            mLlSwitchCameraLive.setVisibility(View.GONE);
            RTCVideoLiveRoomImpl.sharedInstance().leaveSeat();
            //leaveSeat();
        }

        /**
         * 播放器首帧回调
         */
        @Override
        public void onRenderingStart() {
            super.onRenderingStart();
            mTvLiveNotBegin.setVisibility(View.GONE);
            //收到回调后再显示播放画面
            showPlayView(isInteractiveUser);
        }

        /**
         * 拉不到数据后重试3次还拉不到数据，提示直播已结束
         */
        @Override
        public void onLivePlayRetryError() {
            super.onLivePlayRetryError();
            showLiveEndDialog();
        }
    };

    private void showLivingTimeDialog() {
        //提示展示
        mRtcDialogHelper = RTCDialogHelper.getInstance();

        mRtcDialogHelper.setTitle(getString(R.string.rtc_videoliveroom_string_live_complete));
        mRtcDialogHelper.setTipsTitle(String.format(getString(R.string.rtc_videoliveroom_string_living_time), getLivingTime()));
        mRtcDialogHelper.setConfirmText(getString(R.string.rtc_videoliveroom_string_back_to_home));

        mRtcDialogHelper.setOnTipsDialogListener(new OnTipsDialogListener() {
            @Override
            public void onCancel() {
                mRtcDialogHelper.hideAll();
            }

            @Override
            public void onComfirm() {
                mRtcDialogHelper.hideAll();
                finish();
            }
        });

        if(!RtcChatActivity.this.isFinishing()){
            mRtcDialogHelper.showCustomTipsView(RtcChatActivity.this);
            mRtcDialogHelper.hideCancelText();
        }

    }

    private String getLivingTime() {
        return mTvExperienceTime == null ? "" : mTvExperienceTime.getText().toString().trim();
    }


    /**
     * 体验时间结束
     */
    private void showTimeoutDialog() {
        //提示展示
        mRtcDialogHelper = RTCDialogHelper.getInstance();

        mRtcDialogHelper.setTitle(getString(R.string.rtc_videoliveroom_string_experience_time_out));
        mRtcDialogHelper.setTipsTitle(getString(R.string.rtc_videoliveroom_string_experience_time_out_please_try_angin));
        mRtcDialogHelper.setConfirmText(getString(R.string.rtc_videoliveroom_string_know));

        mRtcDialogHelper.setOnTipsDialogListener(new OnTipsDialogListener() {
            @Override
            public void onCancel() {
                mRtcDialogHelper.hideAll();
            }

            @Override
            public void onComfirm() {
                mRtcDialogHelper.hideAll();
                finish();
            }
        });
        if(!RtcChatActivity.this.isFinishing()){
            mRtcDialogHelper.showCustomTipsView(RtcChatActivity.this);
            mRtcDialogHelper.hideCancelText();
        }


    }

    /**
     * 当rtc sdk报错时弹出
     */
    private void showRtcErrorDialog() {
        //提示展示
        mRtcDialogHelper = RTCDialogHelper.getInstance();

        mRtcDialogHelper.setTitle(getString(R.string.rtc_videoliveroom_string_title_dialog_tip));
        mRtcDialogHelper.setTipsTitle(getString(R.string.rtc_videoliveroom_string_error_rtc_normal));
        mRtcDialogHelper.setConfirmText(getString(R.string.rtc_videoliveroom_string_confrim_btn));

        mRtcDialogHelper.setOnTipsDialogListener(new OnTipsDialogListener() {
            @Override
            public void onCancel() {
                mRtcDialogHelper.hideAll();
            }

            @Override
            public void onComfirm() {
                mRtcDialogHelper.hideAll();
                finish();
            }
        });
        if(!RtcChatActivity.this.isFinishing()){
            mRtcDialogHelper.showCustomTipsView(RtcChatActivity.this);
            mRtcDialogHelper.hideCancelText();
        }

    }


    /**
     * 连麦观众下麦的二次确认提示
     *
     */
    private void showLeaveSeatDialog() {
        //提示展示
        mRtcDialogHelper = RTCDialogHelper.getInstance();

        mRtcDialogHelper.setTitle(getString(R.string.rtc_videoliveroom_string_title_dialog_tip));
        mRtcDialogHelper.setTipsTitle(getString(R.string.rtc_videoliveroom_string_leave_seat_tips));
        mRtcDialogHelper.setConfirmText(getString(R.string.rtc_videoliveroom_string_confrim_btn));
        mRtcDialogHelper.setCancelText(getString(R.string.rtc_videoliveroom_string_cancel));

        mRtcDialogHelper.setOnTipsDialogListener(new OnTipsDialogListener() {
            @Override
            public void onCancel() {
                mRtcDialogHelper.hideAll();
            }

            @Override
            public void onComfirm() {
                mRtcDialogHelper.hideAll();
                //showLoading();
                mTvEnterSeat.setText(getString(R.string.rtc_videoliveroom_string_conn_mic));
                mLlSwitchCameraLive.setVisibility(View.GONE);
                RTCVideoLiveRoomImpl.sharedInstance().leaveSeat();
            }
        });
        if(!RtcChatActivity.this.isFinishing()){
            mRtcDialogHelper.showCustomTipsView(RtcChatActivity.this);
            mRtcDialogHelper.showCancelText();
        }

    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    /**
     * 展示退出dialog
     */
    private void showExitDialog() {
        if (!isAnchor && !isInteractiveUser) {
            RTCVideoLiveRoomImpl.sharedInstance().leaveRoom();
            return;
        }
        //提示展示
        mRtcDialogHelper = RTCDialogHelper.getInstance();

        mRtcDialogHelper.setTitle(getString(R.string.rtc_videoliveroom_string_leave_channel));
        mRtcDialogHelper.setTipsTitle(getString(R.string.rtc_videoliveroom_string_hint_leave_channel_intime));
        mRtcDialogHelper.setConfirmText(getString(R.string.rtc_videoliveroom_string_confirm_leave_channel));
        mRtcDialogHelper.setCancelText(getString(R.string.rtc_videoliveroom_string_continue_to_experience));

        mRtcDialogHelper.setOnTipsDialogListener(new OnTipsDialogListener() {
            @Override
            public void onCancel() {
                mRtcDialogHelper.hideAll();
            }

            @Override
            public void onComfirm() {
                mRtcDialogHelper.hideAll();
                if (isAnchor) {
                    RTCVideoLiveRoomImpl.sharedInstance().destroyRoom();
                    //停止旁路直播
                    mVideoLiveRoomApi.stopMPUTask(mChannelId, new OkhttpClient.BaseHttpCallBack<IResponse<String>>() {
                        @Override
                        public void onSuccess(IResponse<String> data) {
                            Log.i(TAG, "onSuccess: " + data.getData());
                        }

                        @Override
                        public void onError(String errorMsg) {
                            Log.i(TAG, "onError: " + errorMsg);
                        }
                    });
                } else {
                    RTCVideoLiveRoomImpl.sharedInstance().leaveRoom();
                }
            }
        });
        if(!RtcChatActivity.this.isFinishing()){
            mRtcDialogHelper.showCustomTipsView(RtcChatActivity.this);
            mRtcDialogHelper.showCancelText();
        }

    }

    /**
     * 展示退出dialog
     */
    private void showKickOutUserDialog() {
        //提示展示
        mRtcDialogHelper = RTCDialogHelper.getInstance();

        mRtcDialogHelper.setTitle(getString(R.string.rtc_videoliveroom_string_title_kickout_user));
        mRtcDialogHelper.setTipsTitle(getString(R.string.rtc_videoliveroom_string_des_kickout_user));
        mRtcDialogHelper.setConfirmText(getString(R.string.rtc_videoliveroom_string_confrim_kickout_user));
        mRtcDialogHelper.setCancelText(getString(R.string.rtc_videoliveroom_string_continue_conn_mic));

        mRtcDialogHelper.setOnTipsDialogListener(new OnTipsDialogListener() {
            @Override
            public void onCancel() {
                mRtcDialogHelper.hideAll();
            }

            @Override
            public void onComfirm() {
                mRtcDialogHelper.hideAll();
                RTCVideoLiveRoomImpl.sharedInstance().kickOut();
            }
        });
        if(!RtcChatActivity.this.isFinishing()){
            mRtcDialogHelper.showCustomTipsView(RtcChatActivity.this);
            mRtcDialogHelper.showCancelText();
        }

    }

    /**
     * 美颜弹窗
     */
    private void showBeautyDialog() {
        RTCBottomDialog bottomDialog = new RTCBottomDialog(this);
        bottomDialog.setTitle(getString(R.string.rtc_videoliveroom_string_beauty));
        BeautyView beautyView = new BeautyView(this);
        bottomDialog.setContentView(beautyView);
        bottomDialog.show();
    }

    private class TimeCountRunnable implements Runnable {
        private boolean loop = true;
        private long mStartTime;

        public TimeCountRunnable(long channelStartTimeTs) {
            mStartTime = channelStartTimeTs;
        }

        public void setLoop(boolean loop) {
            this.loop = loop;
        }

        @Override
        public void run() {
            while (loop) {
                SystemClock.sleep(1000);
                long tempTime = System.currentTimeMillis() - mStartTime;
                Date date = new Date(tempTime);
                int second = date.getSeconds();
                int minute = date.getMinutes();
                reflushExperienceTimeView(minute, second);
            }
        }
    }

    private void reflushExperienceTimeView(final int minute, final int second) {
        UIHandlerUtil.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                mTvExperienceTime.setText(String.format(getString(R.string.rtc_videoliveroom_string_experience_time), "00", minute < 10 ? "0" + minute : String.valueOf(minute), second < 10 ? "0" + second : String.valueOf(second)));
            }
        });
    }


    /**
     * 播放器超时回调后展示的dialog
     */
    private void showLiveEndDialog() {
        //提示展示
        mRtcDialogHelper = RTCDialogHelper.getInstance();
        mRtcDialogHelper.setTitle(getString(R.string.rtc_videoliveroom_string_title_dialog_tip));
        mRtcDialogHelper.setTipsTitle(getString(R.string.rtc_videoliveroom_string_live_completed));
        mRtcDialogHelper.setConfirmText(getString(R.string.rtc_videoliveroom_string_confrim_btn));

        mRtcDialogHelper.setOnTipsDialogListener(new OnTipsDialogListener() {
            @Override
            public void onCancel() {
                mRtcDialogHelper.hideAll();
            }

            @Override
            public void onComfirm() {
                mRtcDialogHelper.hideAll();
                finish();
            }
        });

        if(!RtcChatActivity.this.isFinishing()){
            mRtcDialogHelper.showCustomTipsView(RtcChatActivity.this);
            mRtcDialogHelper.hideCancelText();
        }
    }

    /**
     * 适配部分机型点击音量按键控制的时通话音量
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (mAudioManager != null) {
                    mAudioManager.adjustStreamVolume(!isInteractiveUser ? AudioManager.STREAM_MUSIC : AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (mAudioManager != null) {
                    mAudioManager.adjustStreamVolume(!isInteractiveUser ? AudioManager.STREAM_MUSIC : AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                }
                break;
            default:
        }
        return super.onKeyDown(keyCode, event);
    }
}
