package com.aliyun.rtc.videoliveroom.ui;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alivc.rtc.device.utils.StringUtils;
import com.aliyun.rtc.alivcrtcviewcommon.listener.OnTipsDialogListener;
import com.aliyun.rtc.alivcrtcviewcommon.widget.RTCDialogHelper;
import com.aliyun.rtc.videoliveroom.R;
import com.aliyun.rtc.videoliveroom.api.impl.VideoLiveRoomModelFactory;
import com.aliyun.rtc.videoliveroom.api.impl.RTCVideoLiveRoomApiImpl;
import com.aliyun.rtc.videoliveroom.api.net.OkhttpClient;
import com.aliyun.rtc.videoliveroom.bean.IResponse;
import com.aliyun.rtc.videoliveroom.bean.UserInfo;
import com.aliyun.rtc.videoliveroom.constant.Constant;
import com.aliyun.rtc.videoliveroom.rtc.BaseRTCVideoLiveRoom;
import com.aliyun.rtc.videoliveroom.rtc.RTCVideoLiveRoomDelegate;
import com.aliyun.rtc.videoliveroom.rtc.SimpleRTCVideoLiveRoomDelegate;
import com.aliyun.rtc.videoliveroom.ui.base.BaseActivity;
import com.aliyun.rtc.videoliveroom.utils.DoubleClickUtil;
import com.aliyun.rtc.videoliveroom.utils.SPUtil;
import com.aliyun.rtc.videoliveroom.utils.ScreenUtil;
import com.aliyun.rtc.videoliveroom.utils.UIHandlerUtil;
import com.aliyun.rtc.videoliveroom.utils.UserHelper;
import com.aliyun.rtc.videoliveroom.view.BeautyView;
import com.aliyun.rtc.videoliveroom.view.RTCBottomDialog;
import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;
import com.aliyun.svideo.common.utils.image.ImageLoaderOptions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RtcJoinChannelActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = RtcJoinChannelActivity.class.getSimpleName();
    private LinearLayout mLlLiveTitle;
    private LinearLayout mLlInputLiveTitle;
    private FrameLayout mLocalPreview;
    private EditText mEtLiveTitle;
    private String mChannelId;
    private UserInfo mUserInfo;
    private RTCVideoLiveRoomApiImpl mVideoLiveRoomApi;
    private String mCoverUrl;
    private ImageView mIvLiveCover;
    private RTCDialogHelper mRtcDialogHelper;
    private RTCBottomDialog mBottomDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        BaseRTCVideoLiveRoom.sharedInstance().setDelegate(mVideoLiveRoomDelegate);
        mVideoLiveRoomApi = VideoLiveRoomModelFactory.createRTCVideoLiveApi();
        mChannelId = String.valueOf(System.currentTimeMillis());
        mCoverUrl = SPUtil.getInstance().getString(Constant.PICTURE_URL,"");
        if(!TextUtils.isEmpty(mCoverUrl)){
            ImageLoaderOptions imageLoaderOptions = new ImageLoaderOptions.Builder()
                    .roundCorner()
                    .radius(8f)
                    .build();
            new ImageLoaderImpl().loadImage(RtcJoinChannelActivity.this, mCoverUrl, imageLoaderOptions).into(mIvLiveCover);
        } else {
            getLiveCover();
        }
    }

    private void getLiveCover() {
        mVideoLiveRoomApi.randomCoverUrl(mChannelId, new OkhttpClient.BaseHttpCallBack<IResponse<String>>() {
            @Override
            public void onSuccess(IResponse<String> data) {
                if (data != null) {
                    mCoverUrl = data.getData();
                    UIHandlerUtil.getInstance().postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            SPUtil.getInstance().putString(Constant.PICTURE_URL, mCoverUrl);
                            ImageLoaderOptions imageLoaderOptions = new ImageLoaderOptions.Builder()
                                    .roundCorner()
                                    .radius(8f)
                                    .build();
                            new ImageLoaderImpl().loadImage(RtcJoinChannelActivity.this, mCoverUrl, imageLoaderOptions).into(mIvLiveCover);
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMsg) {
                Log.i(TAG, "onError: " + errorMsg);
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.rtc_videoliveroom_activity_join_channel;
    }

    private void initView() {
        mLocalPreview = findViewById(R.id.rtc_videoliveroom_surfaceview_preview);
        ImageView ivClose = findViewById(R.id.rtc_videoliveroom_iv_close_join_channel);
        mIvLiveCover = findViewById(R.id.rtc_videoliveroom_iv_live_cover_join_channel);
        LinearLayout llBeauty = findViewById(R.id.rtc_videoliveroom_ll_beauty_join_channel);
        LinearLayout llSwitchCamera = findViewById(R.id.rtc_videoliveroom_ll_swtichcamera_join_channel);
        mLlLiveTitle = findViewById(R.id.rtc_videoliveroom_ll_live_title);
        mLlInputLiveTitle = findViewById(R.id.rtc_videoliveroom_ll_input_live_title);
        TextView tvJoinLive = findViewById(R.id.rtc_videoliveroom_tv_join_live);
        mEtLiveTitle = findViewById(R.id.rtc_videoliveroom_et_live_title);
        RelativeLayout rlContent = findViewById(R.id.rtc_videoliveroom_rl_content_join_channel);
        rlContent.setPadding(0, ScreenUtil.getStatusBarHeight(this), 0, 0);
        ivClose.setOnClickListener(this);
        llBeauty.setOnClickListener(this);
        llSwitchCamera.setOnClickListener(this);
        mLlLiveTitle.setOnClickListener(this);
        tvJoinLive.setOnClickListener(this);
        mLocalPreview.setOnClickListener(this);

        mEtLiveTitle.addTextChangedListener(new UserNameTextWatcher());
    }

    @Override
    protected void onResume() {
        super.onResume();
        BaseRTCVideoLiveRoom.sharedInstance().startCameraPreView(mLocalPreview);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.rtc_videoliveroom_iv_close_join_channel) {
            finish();
        } else if (id == R.id.rtc_videoliveroom_ll_beauty_join_channel) {
            showBeautyDialog();
        } else if (id == R.id.rtc_videoliveroom_ll_swtichcamera_join_channel) {
            BaseRTCVideoLiveRoom.sharedInstance().switchCamera();
        } else if (id == R.id.rtc_videoliveroom_ll_live_title) {
            mLlLiveTitle.setVisibility(View.GONE);
            mLlInputLiveTitle.setVisibility(View.VISIBLE);
            mLlInputLiveTitle.requestFocus();
            showSoftInput(mEtLiveTitle);
        } else if (id == R.id.rtc_videoliveroom_tv_join_live) {
            if (DoubleClickUtil.isDoubleClick(v, 500)) {
                showToastInCenter(getString(R.string.rtc_videoliveroom_string_hint_double_click));
                return;
            }
            //进入房间前检查权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int res = RtcJoinChannelActivity.this.checkSelfPermission(Manifest.permission.CAMERA);
                if(res != 0){
                    showToastInCenter(getString(R.string.alivc_common_no_permission));
                    return;
                }
            }

            if (TextUtils.isEmpty(getLiveTitle())) {
                showTitleEmptyDialog();
                return;
            }
            showLoading();
            mUserInfo = UserHelper.getInstance().obtainUserInfo();
            BaseRTCVideoLiveRoom.sharedInstance().createRoom(mChannelId, mUserInfo.getUserId(), mUserInfo.getUserName());
        } else if (id == R.id.rtc_videoliveroom_surfaceview_preview) {
            if (TextUtils.isEmpty(getLiveTitle())) {
                mLlLiveTitle.setVisibility(View.VISIBLE);
                mLlInputLiveTitle.setVisibility(View.GONE);
            }
            hideSoftInput();
        }
    }

    /**
     * 标题输入框的文字监听器
     */
    private class UserNameTextWatcher implements TextWatcher {
        private static final int MAX_LENGTH = 20;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String changedStr = s.toString().substring(start, start + count);
            //String regex = "[`~!@#$%^&*()_\\-+=|{}':;',\\[\\].<>/?~！@#￥%……&*（） ——+|{}【】‘；：”“’。，、？]";
            String regex = "[^[\\u4e00-\\u9fa5a-zA-Z0-9]]";
            Pattern pattern = Pattern.compile(regex);
            if (pattern.matcher(changedStr).find() ) {
                Toast toast = Toast.makeText(RtcJoinChannelActivity.this, getString(R.string.rtc_videoliveroom_string_live_title_tips), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                //showToastInCenter(getString(R.string.rtc_videoliveroom_string_live_title_tips));
                mEtLiveTitle.setText(s.toString().substring(0, start));
                mEtLiveTitle.setSelection(start);
            }
            //字数限制最多输入20个字符
            if (s.toString().length() > MAX_LENGTH) {
                showToastInCenter(getString(R.string.rtc_videoliveroom_string_out_of_title_max_length));
                mEtLiveTitle.setText(s.toString().substring(0, MAX_LENGTH));
                mEtLiveTitle.setSelection(mEtLiveTitle.getText().toString().length());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }


    private void showTitleEmptyDialog() {
        //提示展示
        mRtcDialogHelper = RTCDialogHelper.getInstance();

        mRtcDialogHelper.setTitle(getString(R.string.rtc_videoliveroom_string_title_live_title_empty));
        mRtcDialogHelper.setTipsTitle(getString(R.string.rtc_videoliveroom_string_des_live_title_empty));
        mRtcDialogHelper.setConfirmText(getString(R.string.rtc_videoliveroom_string_know));

        mRtcDialogHelper.setOnTipsDialogListener(new OnTipsDialogListener() {
            @Override
            public void onCancel() {
                mRtcDialogHelper.hideAll();
            }

            @Override
            public void onComfirm() {
                mRtcDialogHelper.hideAll();
            }
        });

        mRtcDialogHelper.showCustomTipsView(this);
        mRtcDialogHelper.hideCancelText();
    }

    private void showBeautyDialog() {
        if (mBottomDialog == null) {
            mBottomDialog = new RTCBottomDialog(this);
            mBottomDialog.setTitle(getString(R.string.rtc_videoliveroom_string_beauty));
            BeautyView beautyView = new BeautyView(this);
            mBottomDialog.setContentView(beautyView);
        }

        if (!mBottomDialog.isShowing()) {
            mBottomDialog.show();
        }
    }

    private String getLiveTitle() {
        return mEtLiveTitle.getText().toString().trim();
    }

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    private void showSoftInput(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editText, 0);
        }
    }

    /**
     * 权限申请成功
     */
    @Override
    public void onPermissionGranted() {

    }

    /**
     * 权限申请失败
     */
    @Override
    public void onPermissionCancel() {
        showToastInCenter(getString(R.string.rtc_permission));
    }

    private void toChatActivity() {
        RtcChatActivity.start(this, true, mChannelId, getLiveTitle(), mCoverUrl);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //放到destory中会因为生命周期调用问题导致刚开始预览就停止了
        BaseRTCVideoLiveRoom.sharedInstance().stopCameraPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRtcDialogHelper != null) {
            mRtcDialogHelper.release();
        }
    }

    private RTCVideoLiveRoomDelegate mVideoLiveRoomDelegate = new SimpleRTCVideoLiveRoomDelegate() {
        @Override
        public void onJoinChannelResult(int result) {
            super.onJoinChannelResult(result);
            hideLoading();
            if (result == 0) {
                //先暂停预览
                BaseRTCVideoLiveRoom.sharedInstance().stopCameraPreview();
                mVideoLiveRoomApi.startMPUTask(mChannelId, mUserInfo.getUserId(), mCoverUrl, getLiveTitle(), new OkhttpClient.BaseHttpCallBack<IResponse<String>>() {
                    @Override
                    public void onSuccess(IResponse<String> data) {
                        if (StringUtils.equals(data.getCode(), String.valueOf(200))) {
                            toChatActivity();
                        } else {
                            showToastInCenter(getString(R.string.rtc_videoliveroom_string_hint_join_room_faild));
                            //失败需要退出房间
                            BaseRTCVideoLiveRoom.sharedInstance().destroyRoom();
                        }
                    }

                    @Override
                    public void onError(String errorMsg) {
                        showToastInCenter(getString(R.string.rtc_videoliveroom_string_hint_join_room_faild));
                        //失败需要退出房间
                        BaseRTCVideoLiveRoom.sharedInstance().destroyRoom();
                    }
                });
            }
        }
    };
}
