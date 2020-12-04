package com.aliyun.rtc.videoliveroom.play;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.Surface;
import android.widget.FrameLayout;

import com.aliyun.player.AliPlayer;
import com.aliyun.player.AliPlayerFactory;
import com.aliyun.player.IPlayer;
import com.aliyun.player.bean.ErrorCode;
import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.player.bean.InfoBean;
import com.aliyun.player.nativeclass.PlayerConfig;
import com.aliyun.player.nativeclass.TrackInfo;
import com.aliyun.player.source.UrlSource;
import com.aliyun.utils.NetWatchdog;

import java.lang.ref.WeakReference;

public class AliLivePlayerView extends FrameLayout implements LifeListener {

    private Context mContext;
    /**
     * Surface
     */
    private IRenderView mIRenderView;

    /**
     * loading重试
     */
    private static final int LOADING_RETRY_WHAT = 0x0001;
    /**
     * 起播重试
     */
    private static final int START_RETRY_WHAT = 0x0002;

    /**
     * 真正的播放器实例对象
     */
    private AliPlayer mAliPlayer;

    /**
     * 后台播放开关
     */
    private boolean mKeepPlay = false;
    /**
     * 播放状态
     */
    private AliLivePlayerStatus mCurrentPlayerStatus = AliLivePlayerStatus.STOP;
    /**
     * 起播重试次数和timeout时间
     */
    private int mStartRetryCount, mStartRetryTimeout;
    /**
     * loading重试次数和timeout时间
     */
    private int mLoadingRetryTimeout, mLoadingRetryCount;
    /**
     * SDK OnError回调是否拦截,默认不拦截
     * 当loading和起播重试的时候,进行拦截,重试失败后,如果SDK的onError回调有错误信息,则回调
     * 给UI层,如果SDK的onError回调没有错误信息,则等待SDK的onError回调
     */
    private boolean mEnableErrorCallback = true;

    /**
     * 首帧回调拦截,是否拦截
     * 当通过 changeDataSource 以及 setDataSource 起播时,不拦截SDK首帧回调,
     * 其他情况下,内部重新prepare时,进行拦截
     */
    private boolean mEnableRenderStartCallback = true;

    /**
     * SDK错误回调信息,用于重试完成后,回调给UI层
     */
    private ErrorInfo mErrorInfo = null;


    /**
     * 判断当前解码状态,true:硬解,false:软解
     * 默认是硬解
     */
    private boolean mCurrentEnableHardwareDecoder = true;

    private Surface mSurface;

    private AVPLivePlayerHandler mAVPLivePlayerHandler;

    private final String TAG = AliLivePlayerView.class.getSimpleName();
    private NetWatchdog mNetWatchDog;
    private NetWorkingMode mNetWorkingMode;
    private boolean mDataSourceIsFlv;

    private static class AVPLivePlayerHandler extends Handler {

        private WeakReference<AliLivePlayerView> weakReference;

        private AVPLivePlayerHandler(AliLivePlayerView avpLivePlayer) {
            weakReference = new WeakReference<>(avpLivePlayer);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AliLivePlayerView avpLivePlayer = weakReference.get();
            if (avpLivePlayer != null) {
                if (msg.what == LOADING_RETRY_WHAT) {
                    avpLivePlayer.playerLoadingRetry();
                } else if (msg.what == START_RETRY_WHAT) {
                    avpLivePlayer.playerStartRetry();
                }
            }
        }
    }

    /**
     * loading重试
     */
    private void playerLoadingRetry() {
        mLoadingRetryCount--;
        if (mLoadingRetryCount <= 0) {
            mAVPLivePlayerHandler.removeMessages(LOADING_RETRY_WHAT);
            retryFailed();
        } else {
            start();
            mAVPLivePlayerHandler.sendEmptyMessageDelayed(LOADING_RETRY_WHAT, mLoadingRetryTimeout);
        }
    }

    /**
     * 起播重试
     */
    private void playerStartRetry() {
        mStartRetryCount--;
        if (mStartRetryCount <= 0) {
            mAVPLivePlayerHandler.removeMessages(START_RETRY_WHAT);
            retryFailed();
        } else {
            start();
            mAVPLivePlayerHandler.sendEmptyMessageDelayed(START_RETRY_WHAT, mLoadingRetryTimeout);
        }
    }

    /**
     * 重试失败
     */
    private void retryFailed() {
        //无重试次数,将当前播放器状态置位stop状态
        mCurrentPlayerStatus = AliLivePlayerStatus.STOP;
        mEnableErrorCallback = true;
        //重试失败,发送onError回调
        if (mErrorInfo != null) {
            onError(mErrorInfo);
        }
    }


    public AliLivePlayerView(Context context) {
        super(context);
        init(context);
    }

    public AliLivePlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AliLivePlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        initPlayer();
        initNetWatchDog();
    }


    /**
     * 被加载到window上时绑定生命周期
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Context context = getContext();
        if (context instanceof Activity) {
            addFragment(((Activity) context));
        }
    }

    private void addFragment(Activity context) {
        if (context instanceof AppCompatActivity) {
            FragmentManager supportFragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            Fragment fragment = getFragment(supportFragmentManager);
            if (fragment instanceof LifeFragment) {
                ((LifeFragment) fragment).setLifeListener(this);
            }
            supportFragmentManager.beginTransaction().add(fragment, TAG).commitAllowingStateLoss();
        }
    }

    private Fragment getFragment(FragmentManager fragmentManager) {
        Fragment fragment = null;
        fragment = fragmentManager.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new LifeFragment();
        }
        return fragment;
    }

    /**
     * 初始化播放器
     */
    private void initPlayer() {
        mAVPLivePlayerHandler = new AVPLivePlayerHandler(this);
        mAliPlayer = AliPlayerFactory.createAliPlayer(mContext.getApplicationContext());
        initConfig();
        initPlayerListener();
    }

    private void initNetWatchDog() {
        mNetWatchDog = new NetWatchdog(getContext());
        mNetWatchDog.setNetChangeListener(new PlayerNetChangeListener(this));
        mNetWatchDog.startWatch();
    }

    /**
     * 初始化播放器配置
     */
    private void initConfig() {
        PlayerConfig config = mAliPlayer.getConfig();
        //开启停止保留最后一帧画面
        config.mClearFrameWhenStop = false;
        //直播追赶设置为0  直播最大延迟
        config.mMaxDelayTime = 2000;
        mAliPlayer.setConfig(config);
        //开启自动播放
        mAliPlayer.setAutoPlay(true);
    }

    private void initPlayerListener() {
        //TODO 播放器默认时间隔15s重试两次，先修改为间隔10s重试3次
        PlayerConfig config = mAliPlayer.getConfig();
        config.mNetworkTimeout = 10 * 1000;
        config.mNetworkRetryCount = 3;
        //error  prepare -- start
        mAliPlayer.setConfig(config);
        mAliPlayer.setOnInfoListener(new OnAVPInfoListener(this));
        mAliPlayer.setOnErrorListener(new OnAVPErrorListener(this));
        mAliPlayer.setOnSnapShotListener(new OnAVPSnapShotListener(this));
        mAliPlayer.setOnPreparedListener(new OnAVPPreparedListener(this));
        mAliPlayer.setOnCompletionListener(new OnAVPCompletionListener(this));
        mAliPlayer.setOnTrackChangedListener(new OnAVPTrackChangedListener(this));
        mAliPlayer.setOnSeekCompleteListener(new OnAVPSeekCompleteListener(this));
        mAliPlayer.setOnVideoRenderedListener(new OnAVPVideoRenderedListener(this));
        mAliPlayer.setOnLoadingStatusListener(new OnAVPLoadingStatusListener(this));
        mAliPlayer.setOnRenderingStartListener(new OnAVPRenderingStartListener(this));
        mAliPlayer.setOnStateChangedListener(new OnAVPStateChangedListener(this));
        mAliPlayer.setOnVideoSizeChangedListener(new OnAVPVideoSizeChangedListener(this));
    }

    private static class MyRenderViewCallback implements IRenderView.IRenderCallback {

        private WeakReference<AliLivePlayerView> weakReference;

        private MyRenderViewCallback(AliLivePlayerView aliLivePlayerView) {
            weakReference = new WeakReference<>(aliLivePlayerView);
        }

        @Override
        public void onSurfaceCreate(Surface surface) {
            AliLivePlayerView aliLivePlayerView = weakReference.get();
            if (aliLivePlayerView != null && aliLivePlayerView.mAliPlayer != null) {
                aliLivePlayerView.mSurface = surface;
                aliLivePlayerView.mAliPlayer.setSurface(surface);
            }
        }

        @Override
        public void onSurfaceChanged(int width, int height) {
            AliLivePlayerView aliLivePlayerView = weakReference.get();
            if (aliLivePlayerView != null && aliLivePlayerView.mAliPlayer != null) {
                //                aliLivePlayerView.mAliPlayer.surfaceChanged();
            }
        }

        @Override
        public void onSurfaceDestroyed() {
            AliLivePlayerView aliLivePlayerView = weakReference.get();
            if (aliLivePlayerView != null && aliLivePlayerView.mAliPlayer != null) {
                aliLivePlayerView.mAliPlayer.setSurface(null);
            }
        }
    }

    /**
     * OnPrepared
     */
    private static class OnAVPPreparedListener implements IPlayer.OnPreparedListener {

        private WeakReference<AliLivePlayerView> weakReference;

        private OnAVPPreparedListener(AliLivePlayerView avpLivePlayer) {
            weakReference = new WeakReference<>(avpLivePlayer);
        }


        @Override
        public void onPrepared() {
            AliLivePlayerView avpLivePlayer = weakReference.get();
            if (avpLivePlayer != null) {
                avpLivePlayer.onPrepared();
            }
        }
    }


    /**
     * 纯音频、纯视频流监听
     */
    public interface OnVideoStreamTrackTypeListener {
        //纯视频
        void onVideoOnlyType();

        //纯音频
        void onAudioOnlyType();
    }

    private OnVideoStreamTrackTypeListener mOnVideoStreamTrackTypeListener;

    public void setOnVideoStreamTrackType(OnVideoStreamTrackTypeListener listener) {
        this.mOnVideoStreamTrackTypeListener = listener;
    }

    private IPlayer.OnPreparedListener mOnPreparedListener;

    public void setOnPreparedListener(IPlayer.OnPreparedListener listener) {
        this.mOnPreparedListener = listener;
    }

    private void onPrepared() {
        if (mOnPreparedListener != null) {
            mOnPreparedListener.onPrepared();
        }
        if (mOnVideoStreamTrackTypeListener != null) {
            TrackInfo trackVideo = mAliPlayer.currentTrack(TrackInfo.Type.TYPE_VIDEO);
            TrackInfo trackAudio = mAliPlayer.currentTrack(TrackInfo.Type.TYPE_AUDIO);
            if (trackVideo == null && trackAudio != null) {
                mOnVideoStreamTrackTypeListener.onAudioOnlyType();
            } else if (trackVideo != null && trackAudio == null) {
                mOnVideoStreamTrackTypeListener.onVideoOnlyType();
            }
        }

    }


    /**
     * OnVideoRenderedListener
     */
    private static class OnAVPVideoRenderedListener implements IPlayer.OnVideoRenderedListener {

        private WeakReference<AliLivePlayerView> weakReference;

        private OnAVPVideoRenderedListener(AliLivePlayerView avpLivePlayer) {
            weakReference = new WeakReference<>(avpLivePlayer);
        }

        @Override
        public void onVideoRendered(long timeMs, long pts) {
            AliLivePlayerView avpLivePlayer = weakReference.get();
            if (avpLivePlayer != null) {
                avpLivePlayer.onVideoRendered(timeMs, pts);
            }
        }
    }

    private IPlayer.OnVideoRenderedListener mOnVideoRenderedListener;

    public void setOnVideoRenderedListener(IPlayer.OnVideoRenderedListener listener) {
        this.mOnVideoRenderedListener = listener;
    }

    private void onVideoRendered(long timeMs, long pts) {
        if (mOnVideoRenderedListener != null) {
            mOnVideoRenderedListener.onVideoRendered(timeMs, pts);
        }
    }

    /**
     * OnRenderingStartListener
     */
    private static class OnAVPRenderingStartListener implements IPlayer.OnRenderingStartListener {

        private WeakReference<AliLivePlayerView> weakReference;

        private OnAVPRenderingStartListener(AliLivePlayerView avpLivePlayer) {
            weakReference = new WeakReference<>(avpLivePlayer);
        }

        @Override
        public void onRenderingStart() {
            AliLivePlayerView avpLivePlayer = weakReference.get();
            if (avpLivePlayer != null) {
                //取消起播重试机制
                avpLivePlayer.mAVPLivePlayerHandler.removeMessages(START_RETRY_WHAT);
                //重试成功,取消对SDK的onError回调拦截
                avpLivePlayer.mEnableErrorCallback = true;
                avpLivePlayer.mErrorInfo = null;
                //首帧渲染,标志为正在播放的状态
                avpLivePlayer.mCurrentPlayerStatus = AliLivePlayerStatus.PLAYING;
                avpLivePlayer.onRenderingStart();
            }
        }
    }

    private IPlayer.OnRenderingStartListener mOnRenderingStartListener;

    public void setOnRenderingStartListener(IPlayer.OnRenderingStartListener listener) {
        this.mOnRenderingStartListener = listener;
    }

    private void onRenderingStart() {
        //mEnableRenderStart判断是否拦首帧回调
        if (mOnRenderingStartListener != null && mEnableRenderStartCallback) {
            mOnRenderingStartListener.onRenderingStart();
        }
    }

    /**
     * OnStateChangedListner
     */
    private static class OnAVPStateChangedListener implements IPlayer.OnStateChangedListener {

        private WeakReference<AliLivePlayerView> weakReference;

        public OnAVPStateChangedListener(AliLivePlayerView aliLivePlayerView) {
            weakReference = new WeakReference<>(aliLivePlayerView);
        }

        @Override
        public void onStateChanged(int i) {
            AliLivePlayerView aliLivePlayerView = weakReference.get();
            if (aliLivePlayerView != null) {
                aliLivePlayerView.onStateChangedListener(i);
            }
        }
    }

    private IPlayer.OnStateChangedListener mOnStateChangedListener;

    public void setOnStateChangedListener(IPlayer.OnStateChangedListener listener) {
        this.mOnStateChangedListener = listener;
    }

    private void onStateChangedListener(int newState) {
        if (mOnStateChangedListener != null) {
            mOnStateChangedListener.onStateChanged(newState);
        }
    }

    /**
     * OnVideoSizeChangedListener
     */
    private static class OnAVPVideoSizeChangedListener implements IPlayer.OnVideoSizeChangedListener {

        private WeakReference<AliLivePlayerView> weakReference;

        public OnAVPVideoSizeChangedListener(AliLivePlayerView aliLivePlayerView) {
            weakReference = new WeakReference<>(aliLivePlayerView);
        }

        @Override
        public void onVideoSizeChanged(int width, int height) {
            AliLivePlayerView aliLivePlayerView = weakReference.get();
            if (aliLivePlayerView != null) {
                aliLivePlayerView.onVideoSizeChanged(width, height);
            }
        }
    }

    private IPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;

    public void setOnVideoSizeChangedListener(IPlayer.OnVideoSizeChangedListener listener) {
        this.mOnVideoSizeChangedListener = listener;
    }

    private void onVideoSizeChanged(int width, int height) {
        if (mOnVideoSizeChangedListener != null) {
            mOnVideoSizeChangedListener.onVideoSizeChanged(width, height);
        }
    }

    /**
     * OnInfoListener
     */
    private static class OnAVPInfoListener implements IPlayer.OnInfoListener {

        private WeakReference<AliLivePlayerView> weakReference;

        private OnAVPInfoListener(AliLivePlayerView avpLivePlayer) {
            weakReference = new WeakReference<>(avpLivePlayer);
        }

        @Override
        public void onInfo(InfoBean infoBean) {
            AliLivePlayerView avpLivePlayer = weakReference.get();
            if (avpLivePlayer != null) {
                avpLivePlayer.onInfo(infoBean);
            }
        }
    }

    private IPlayer.OnInfoListener mOnInfoListener;

    public void setOnInfoListener(IPlayer.OnInfoListener listener) {
        this.mOnInfoListener = listener;
    }

    private void onInfo(InfoBean infoBean) {
        if (mOnInfoListener != null) {
            mOnInfoListener.onInfo(infoBean);
        }
    }

    /**
     * OnLoadingStatusListener
     */
    private static class OnAVPLoadingStatusListener implements IPlayer.OnLoadingStatusListener {

        private WeakReference<AliLivePlayerView> weakReference;

        private OnAVPLoadingStatusListener(AliLivePlayerView avpLivePlayer) {
            weakReference = new WeakReference<>(avpLivePlayer);
        }

        @Override
        public void onLoadingBegin() {
            AliLivePlayerView avpLivePlayer = weakReference.get();
            if (avpLivePlayer != null) {
                //发送loadding重试机制
                avpLivePlayer.mAVPLivePlayerHandler.removeMessages(LOADING_RETRY_WHAT);
                //重试阶段,拦截SDK的onError回调
                avpLivePlayer.mEnableErrorCallback = false;
                avpLivePlayer.mErrorInfo = null;
                avpLivePlayer.mAVPLivePlayerHandler.sendEmptyMessageDelayed(LOADING_RETRY_WHAT, avpLivePlayer.mLoadingRetryTimeout);
                avpLivePlayer.onLoadingBegin();
            }
        }

        @Override
        public void onLoadingProgress(int percent, float netSpeed) {
            AliLivePlayerView avpLivePlayer = weakReference.get();
            if (avpLivePlayer != null) {
                avpLivePlayer.onLoadingProgress(percent, netSpeed);
            }
        }

        @Override
        public void onLoadingEnd() {
            AliLivePlayerView avpLivePlayer = weakReference.get();
            if (avpLivePlayer != null) {
                //取消loadding重试机制
                avpLivePlayer.mAVPLivePlayerHandler.removeMessages(LOADING_RETRY_WHAT);
                //重试成功,取消对SDK的onError回调拦截
                avpLivePlayer.mEnableErrorCallback = true;
                avpLivePlayer.mErrorInfo = null;
                avpLivePlayer.onLoadingEnd();
            }
        }
    }

    private IPlayer.OnLoadingStatusListener mOnLoadingStatusListener;

    public void setOnLoadingStatusListener(IPlayer.OnLoadingStatusListener listener) {
        this.mOnLoadingStatusListener = listener;
    }

    private void onLoadingBegin() {
        if (mOnLoadingStatusListener != null) {
            mOnLoadingStatusListener.onLoadingBegin();
        }
    }

    private void onLoadingProgress(int percent, float netSpeed) {
        if (mOnLoadingStatusListener != null) {
            mOnLoadingStatusListener.onLoadingProgress(percent, netSpeed);
        }
    }

    private void onLoadingEnd() {
        if (mOnLoadingStatusListener != null) {
            mOnLoadingStatusListener.onLoadingEnd();
        }
    }


    /**
     * OnSnapShotListener
     */
    private static class OnAVPSnapShotListener implements IPlayer.OnSnapShotListener {

        private WeakReference<AliLivePlayerView> weakReference;

        private OnAVPSnapShotListener(AliLivePlayerView avpLivePlayer) {
            weakReference = new WeakReference<>(avpLivePlayer);
        }

        @Override
        public void onSnapShot(Bitmap bitmap, int with, int height) {
            AliLivePlayerView avpLivePlayer = weakReference.get();
            if (avpLivePlayer != null) {
                avpLivePlayer.onSnapShot(bitmap, with, height);
            }
        }
    }

    private IPlayer.OnSnapShotListener mOnSnapShotListener;

    public void setOnSnapShotListener(IPlayer.OnSnapShotListener listener) {
        this.mOnSnapShotListener = listener;
    }

    private void onSnapShot(Bitmap bitmap, int with, int height) {
        if (mOnSnapShotListener != null) {
            mOnSnapShotListener.onSnapShot(bitmap, with, height);
        }
    }

    /**
     * OnCompletionListener
     */
    private static class OnAVPCompletionListener implements IPlayer.OnCompletionListener {

        private WeakReference<AliLivePlayerView> weakReference;

        private OnAVPCompletionListener(AliLivePlayerView avpLivePlayer) {
            weakReference = new WeakReference<>(avpLivePlayer);
        }

        @Override
        public void onCompletion() {
            AliLivePlayerView avpLivePlayer = weakReference.get();
            if (avpLivePlayer != null) {
                avpLivePlayer.onCompletion();
            }
        }
    }

    private IPlayer.OnCompletionListener mOnCompletionListener;

    public void setOnCompletionListener(IPlayer.OnCompletionListener listener) {
        this.mOnCompletionListener = listener;
    }

    private void onCompletion() {
        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion();
        }
    }

    /**
     * OnSeekCompleteListener
     */
    private static class OnAVPSeekCompleteListener implements IPlayer.OnSeekCompleteListener {

        private WeakReference<AliLivePlayerView> weakReference;

        private OnAVPSeekCompleteListener(AliLivePlayerView avpLivePlayer) {
            weakReference = new WeakReference<>(avpLivePlayer);
        }

        @Override
        public void onSeekComplete() {
            AliLivePlayerView avpLivePlayer = weakReference.get();
            if (avpLivePlayer != null) {
                avpLivePlayer.onSeekComplete();
            }
        }
    }

    private IPlayer.OnSeekCompleteListener mOnSeekCompleteListener;

    public void setOnSeekCompleteListener(IPlayer.OnSeekCompleteListener listener) {
        this.mOnSeekCompleteListener = listener;
    }

    private void onSeekComplete() {
        if (mOnSeekCompleteListener != null) {
            mOnSeekCompleteListener.onSeekComplete();
        }
    }

    /**
     * OnTrackChangedListener
     */
    private static class OnAVPTrackChangedListener implements IPlayer.OnTrackChangedListener {

        private WeakReference<AliLivePlayerView> weakReference;

        private OnAVPTrackChangedListener(AliLivePlayerView avpLivePlayer) {
            weakReference = new WeakReference<>(avpLivePlayer);
        }

        @Override
        public void onChangedSuccess(TrackInfo trackInfo) {
            AliLivePlayerView avpLivePlayer = weakReference.get();
            if (avpLivePlayer != null) {
                avpLivePlayer.onChangedSuccess(trackInfo);
            }
        }

        @Override
        public void onChangedFail(TrackInfo trackInfo, ErrorInfo errorInfo) {
            AliLivePlayerView avpLivePlayer = weakReference.get();
            if (avpLivePlayer != null) {
                avpLivePlayer.onChangedFail(trackInfo, errorInfo);
            }
        }
    }

    private IPlayer.OnTrackChangedListener mOnTrackChangedListener;

    public void setOnTrackChangedListener(IPlayer.OnTrackChangedListener listener) {
        this.mOnTrackChangedListener = listener;
    }

    private void onChangedSuccess(TrackInfo trackInfo) {
        if (mOnTrackChangedListener != null) {
            mOnTrackChangedListener.onChangedSuccess(trackInfo);
        }
    }

    private void onChangedFail(TrackInfo trackInfo, ErrorInfo errorInfo) {
        if (mOnTrackChangedListener != null) {
            mOnTrackChangedListener.onChangedFail(trackInfo, errorInfo);
        }
    }

    /**
     * OnErrorListener
     */
    private static class OnAVPErrorListener implements IPlayer.OnErrorListener {

        private WeakReference<AliLivePlayerView> weakReference;

        private OnAVPErrorListener(AliLivePlayerView avpLivePlayer) {
            weakReference = new WeakReference<>(avpLivePlayer);
        }

        @Override
        public void onError(ErrorInfo errorInfo) {
            AliLivePlayerView avpLivePlayer = weakReference.get();
            if (avpLivePlayer != null) {
                //错误回调,标志为停止状态
                avpLivePlayer.mCurrentPlayerStatus = AliLivePlayerStatus.STOP;
                avpLivePlayer.onError(errorInfo);
            }
        }
    }

    private IPlayer.OnErrorListener mOnErrorListener;

    public void setOnErrorListener(IPlayer.OnErrorListener listener) {
        this.mOnErrorListener = listener;
    }

    private void onError(ErrorInfo errorInfo) {
        //视频硬解码失败,转换成软解,重新prepare,并拦截onError回调
        if (errorInfo.getCode() == ErrorCode.ERROR_DECODE_VIDEO && mCurrentEnableHardwareDecoder) {
            enableHardwareDecoder(false);
            mCurrentPlayerStatus = AliLivePlayerStatus.STOP;
            start();
            return;
        }
        this.mErrorInfo = errorInfo;
        //mEnableErrorCallback为判断是否拦截SDK的回调,当处于重试过程中,不应该有错误回调
        if (mOnErrorListener != null && mEnableErrorCallback) {
            mOnErrorListener.onError(errorInfo);
        }
    }

    public enum SurfaceType {
        /**
         * TextureView
         */
        TEXTURE_VIEW,
        /**
         * SurfacView
         */
        SURFACE_VIEW
    }

    /**
     * 获取真正的播放器实例对象
     */
    public AliPlayer getAliPlayer() {
        return mAliPlayer;
    }

    /**
     * 该方法需要在创建播放器完成后,prepare前调用
     *
     * @param surfaceType Surface的类型
     */
    public void setSurfaceType(SurfaceType surfaceType) {
        if (surfaceType == SurfaceType.TEXTURE_VIEW && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)) {
            mIRenderView = new TextureRenderView(mContext);
        } else {
            mIRenderView = new SurfaceRenderView(mContext);
        }
        initListener();
        addView(mIRenderView.getView());
    }

    private void initListener() {
        mIRenderView.addRenderCallback(new MyRenderViewCallback(this));
    }


    /**
     * 软硬解开关
     *
     * @param enableHardwareDecoder true:硬解,false:软解
     */
    public void enableHardwareDecoder(boolean enableHardwareDecoder) {
        if (mAliPlayer != null) {
            mCurrentEnableHardwareDecoder = enableHardwareDecoder;
            mAliPlayer.enableHardwareDecoder(enableHardwareDecoder);
        }
    }

    /**
     * 是否支持后台播放,默认不支持
     *
     * @param keepPlay true:支持,false:不支持
     */
    public void enterBackground(boolean keepPlay) {
        this.mKeepPlay = keepPlay;
    }

    /**
     * 获取是否支持后台播放
     */
    public boolean enableBackgroundKeepPlay() {
        return mKeepPlay;
    }

    /**
     * loading重试机制
     *
     * @param time  重试时间,单位ms
     * @param count 重试次数
     */
    public void setLoadingTimeout(int time, int count) {
        this.mLoadingRetryTimeout = time;
        this.mLoadingRetryCount = count;
    }

    /**
     * 起播重试机制
     *
     * @param time  重试时间,单位ms
     * @param count 重试次数
     */
    public void setStartTimeout(int time, int count) {
        this.mStartRetryTimeout = time;
        this.mStartRetryCount = count;
    }

    /**
     * 设置播放源
     *
     * @param url 播放源地址
     */
    public void setDataSource(String url) {
        mCurrentPlayerStatus = AliLivePlayerStatus.STOP;
        UrlSource urlSource = new UrlSource();
        mDataSourceIsFlv = (url.contains(".flv") || url.contains(".FLV"));
        urlSource.setUri(url);
        if(mAliPlayer != null){
            mAliPlayer.setDataSource(urlSource);
        }
        mEnableRenderStartCallback = true;
        start();
    }

    /**
     * 设置播放源,需要手动调用Start()
     *
     * @param url 播放源地址
     */
    public void setDataSourceWithoutStart(String url) {
        mCurrentPlayerStatus = AliLivePlayerStatus.STOP;
        UrlSource urlSource = new UrlSource();
        mDataSourceIsFlv = (url.contains(".flv") || url.contains(".FLV"));
        urlSource.setUri(url);
        mAliPlayer.setDataSource(urlSource);
        mEnableRenderStartCallback = true;
    }

    /**
     * 设置播放源
     *
     * @param url 播放源地址
     */
    public void changeDataSource(String url) {
        stop();
        UrlSource urlSource = new UrlSource();
        urlSource.setUri(url);
        mAliPlayer.setDataSource(urlSource);
        mEnableRenderStartCallback = true;
        start();
    }

    /**
     * 播放器是否正在播放
     *
     * @return true:是,false:不是
     */
    public boolean isPlaying() {
        return mCurrentPlayerStatus == AliLivePlayerStatus.PLAYING;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onStart() {
        start();
    }

    /**
     * 如果支持后台播放,该方法不会调用stop,否则会调用stop
     */
    public void onStop() {
        if (!mKeepPlay) {
            privateStop();
        }
    }

    @Override
    public void onPause() {
        pause();
    }

    @Override
    public void onDestroy() {
        release();
        mNetWatchDog.setNetChangeListener(null);
        mNetWatchDog.stopWatch();
    }

    /**
     * 设置缩放模式
     */
    public void setScaleModel(IPlayer.ScaleMode scaleMode) {
        if (mAliPlayer != null) {
            mAliPlayer.setScaleMode(scaleMode);
        }
    }

    /**
     * 获取当前缩放模式
     */
    public IPlayer.ScaleMode getScaleModel() {
        if (mAliPlayer != null) {
            return mAliPlayer.getScaleMode();
        }
        return IPlayer.ScaleMode.SCALE_ASPECT_FIT;
    }

    /**
     * 设置旋转模式
     */
    public void setRotateModel(IPlayer.RotateMode rotateModel) {
        if (mAliPlayer != null) {
            mAliPlayer.setRotateMode(rotateModel);
        }
    }

    /**
     * 获取当前旋转模式
     */
    public IPlayer.RotateMode getRotateModel() {
        if (mAliPlayer != null) {
            return mAliPlayer.getRotateMode();
        }
        return IPlayer.RotateMode.ROTATE_0;
    }

    /**
     * 设置镜像模式
     */
    public void setMirrorMode(IPlayer.MirrorMode mirrorMode) {
        if (mAliPlayer != null) {
            mAliPlayer.setMirrorMode(mirrorMode);
        }
    }

    /**
     * 获取当前镜像模式
     */
    public IPlayer.MirrorMode getMirrorMode() {
        if (mAliPlayer != null) {
            mAliPlayer.getMirrorMode();
        }
        return IPlayer.MirrorMode.MIRROR_MODE_NONE;
    }

    /**
     * 设置PlayerConfig
     */
    public void setPlayerConfig(PlayerConfig playerConfig) {
        if (mAliPlayer != null) {
            mAliPlayer.setConfig(playerConfig);
        }
    }

    /**
     * 获取PlayerConfig
     */
    public PlayerConfig getPlayerConfig() {
        if (mAliPlayer != null) {
            return mAliPlayer.getConfig();
        }
        return null;
    }

    /**
     * 开始播放
     */
    public void start() {
        if (mAliPlayer == null) {
            return;
        }
        if (mCurrentPlayerStatus == AliLivePlayerStatus.PAUSE) {
            mAliPlayer.start();
            mCurrentPlayerStatus = AliLivePlayerStatus.PLAYING;
        } else if (mCurrentPlayerStatus == AliLivePlayerStatus.STOP) {
            mAliPlayer.prepare();
            //发送起播重试机制
            mAVPLivePlayerHandler.removeMessages(START_RETRY_WHAT);
            //在重试阶段,拦截SDK的onError回调
            mErrorInfo = null;
            mEnableErrorCallback = false;
            mAVPLivePlayerHandler.sendEmptyMessageDelayed(START_RETRY_WHAT, mStartRetryTimeout);
        }
    }

    /**
     * 内部stop调用,和stop()的区别是拦截SDK的首帧回调
     */
    private void privateStop() {
        mEnableRenderStartCallback = false;
        mAliPlayer.stop();
        mCurrentPlayerStatus = AliLivePlayerStatus.STOP;
    }

    /**
     * 停止播放
     */
    public void stop() {
        mAliPlayer.stop();
        mCurrentPlayerStatus = AliLivePlayerStatus.STOP;
    }

    /**
     * 暂停播放,直播流不建议使用
     */
    public void pause() {
        mAliPlayer.pause();
        //增加判断,防止stop后再调用pause,导致无法重新prepare
        if (mCurrentPlayerStatus == AliLivePlayerStatus.PLAYING) {
            //标志为暂停状态
            mCurrentPlayerStatus = AliLivePlayerStatus.PAUSE;
        }

    }

    /**
     * 网络监听
     */
    private static class PlayerNetChangeListener implements NetWatchdog.NetChangeListener {

        private WeakReference<AliLivePlayerView> weakReference;

        private PlayerNetChangeListener(AliLivePlayerView avpLivePlayer) {
            weakReference = new WeakReference<>(avpLivePlayer);
        }

        @Override
        public void onWifiTo4G() {
            AliLivePlayerView avpLivePlayer = weakReference.get();
            if (avpLivePlayer != null) {
                avpLivePlayer.onWifiTo4G();
            }
        }

        @Override
        public void on4GToWifi() {
            AliLivePlayerView avpLivePlayer = weakReference.get();
            if (avpLivePlayer != null) {
                avpLivePlayer.on4GTowifi();
            }
        }

        @Override
        public void onNetDisconnected() {
            AliLivePlayerView avpLivePlayer = weakReference.get();
            if (avpLivePlayer != null) {
                avpLivePlayer.onNetDisconnected();
            }
        }
    }

    /**
     * wifi切换到4G
     */
    public void onWifiTo4G(){
//        if(!mDataSourceIsFlv){
//            return ;
//        }
        start();
        mNetWorkingMode = NetWorkingMode.MOBILE_NETWORK;
    }

    /**
     * 4G切换到wifi
     */
    public void on4GTowifi(){
//        if(!mDataSourceIsFlv){
//            return ;
//        }
        //如果是从4g/无网路状态切换到wifi模式,重新prepare
        if(mNetWorkingMode == NetWorkingMode.MOBILE_NETWORK || mNetWorkingMode == NetWorkingMode.NO_NETWORK){
            //置位stop状态才会prepare
            stop();
            start();
        }
        mNetWorkingMode = NetWorkingMode.WIFI_NETWORK;
    }

    /**
     * 网络断开
     */
    public void onNetDisconnected(){
        if(!mDataSourceIsFlv){
            return ;
        }
        stop();
        mNetWorkingMode = NetWorkingMode.NO_NETWORK;
    }

    public void release() {
        if (mAliPlayer != null) {
            stop();
            mAliPlayer.setSurface(null);
            mAliPlayer.release();
            mAliPlayer = null;
        }
        mSurface = null;
    }
}
