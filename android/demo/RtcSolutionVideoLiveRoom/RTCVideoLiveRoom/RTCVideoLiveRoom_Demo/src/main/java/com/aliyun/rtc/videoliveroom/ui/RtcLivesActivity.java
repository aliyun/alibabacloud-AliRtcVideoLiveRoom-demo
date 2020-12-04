package com.aliyun.rtc.videoliveroom.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.aliyun.rtc.videoliveroom.R;
import com.aliyun.rtc.videoliveroom.adapter.AutoItemDecoration;
import com.aliyun.rtc.videoliveroom.adapter.LiveAdapter;
import com.aliyun.rtc.videoliveroom.api.impl.VideoLiveRoomModelFactory;
import com.aliyun.rtc.videoliveroom.api.impl.RTCVideoLiveRoomApiImpl;
import com.aliyun.rtc.videoliveroom.api.net.OkhttpClient;
import com.aliyun.rtc.videoliveroom.bean.IResponse;
import com.aliyun.rtc.videoliveroom.bean.LiveInfo;
import com.aliyun.rtc.videoliveroom.rtc.BaseRTCVideoLiveRoom;
import com.aliyun.rtc.videoliveroom.ui.base.BaseActivity;
import com.aliyun.rtc.videoliveroom.utils.ScreenUtil;
import com.aliyun.rtc.videoliveroom.utils.UIHandlerUtil;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.*;

public class RtcLivesActivity extends BaseActivity implements OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = RtcLivesActivity.class.getSimpleName();
    private List<LiveInfo> mLiveInfos = new ArrayList<>();
    private LiveAdapter mLiveAdapter;
    private SwipeRefreshLayout mSwipeRefresh;
    private RTCVideoLiveRoomApiImpl mVideoLiveRoomApi;
    private String lastChannelId = "";
    private final int pageSize = 10;


    @Override
    public int getLayoutId() {
        return R.layout.rtc_videoliveroom_activity_lives;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        mVideoLiveRoomApi = VideoLiveRoomModelFactory.createRTCVideoLiveApi();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getChannelList();
    }

    private void getChannelList() {
        mVideoLiveRoomApi.getChannelList(lastChannelId, pageSize, new OkhttpClient.BaseHttpCallBack<IResponse<List<LiveInfo>>>() {
            @Override
            public void onSuccess(IResponse<List<LiveInfo>> data) {
                if (data != null) {
                    if (TextUtils.isEmpty(lastChannelId)) {
                        mLiveInfos.clear();
                    }
                    mLiveInfos.addAll(data.getData());
                    notifyLives();
                    if (data.getData() == null || data.getData().size() == 0){
                        showToastInCenter(getString(R.string.rtc_videoliveroom_string_lives_empty));
                    }
                }
                hideRefresh();
            }

            @Override
            public void onError(String errorMsg) {
                hideRefresh();
                Log.i(TAG, "onError: errorMsg : " + errorMsg);
            }
        });
    }

    private void hideRefresh() {
        UIHandlerUtil.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefresh.isRefreshing()) {
                    mSwipeRefresh.setRefreshing(false);
                }
            }
        });
    }

    private void notifyLives() {
        UIHandlerUtil.getInstance().postRunnable(new Runnable() {
            @Override
            public void run() {
                mLiveAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initView() {
        ImageView ivBack = findViewById(R.id.rtc_videoliveroom_iv_back);
        ImageView ivBeginLive = findViewById(R.id.rtc_videoliveroom_iv_begin_live);
        mSwipeRefresh = findViewById(R.id.rtc_videoliveroom_swiperefresh_lives);
        final RecyclerView rcyLives = findViewById(R.id.rtc_videoliveroom_rcy_lives);
        RelativeLayout rlContent = findViewById(R.id.rtc_videoliveroom_rl_content_live_List);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        rcyLives.setLayoutManager(gridLayoutManager);
        rcyLives.addItemDecoration(new AutoItemDecoration(2));
        mLiveAdapter = new LiveAdapter(mLiveInfos, this);
        rcyLives.setAdapter(mLiveAdapter);
        ivBack.setOnClickListener(this);
        ivBeginLive.setOnClickListener(this);
        mSwipeRefresh.setOnRefreshListener(this);
        //适配水滴屏
        rlContent.setPadding(0, ScreenUtil.getStatusBarHeight(RtcLivesActivity.this), 0, 0);

        rcyLives.addOnScrollListener(new OnScrollListener() {
            private boolean loadMore;
            int previousY;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int lastVisibleItemPosition = gridLayoutManager.findLastVisibleItemPosition();
                if (lastVisibleItemPosition == (mLiveInfos.size() - 1) && newState == SCROLL_STATE_IDLE && loadMore) {
                    Log.i(TAG, "onScrollStateChanged: 滑动到最后一条" + ", newState : " + newState);
                    if (mLiveInfos.size() > 0) {
                        lastChannelId = mLiveInfos.get(mLiveInfos.size() - 1).getChannelId();
                        getChannelList();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy - previousY > 0) {
                    loadMore = true;
                } else {
                    loadMore = false;
                }
                previousY = dy;
            }
        });

        mLiveAdapter.setClickedListener(new LiveAdapter.OnItemClickedListener() {
            @Override
            public void onClicked(int position) {
                toChatActivity(mLiveInfos.get(position));
            }
        });
    }

    private void toChatActivity(LiveInfo liveInfo) {
        RtcChatActivity.start(this, false, liveInfo.getChannelId(), liveInfo.getTitle(), liveInfo.getCoverUrl());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.rtc_videoliveroom_iv_back) {
            finish();
        } else if (id == R.id.rtc_videoliveroom_iv_begin_live) {
            startActivity(new Intent(this, RtcJoinChannelActivity.class));
        }
    }

    @Override
    public void onRefresh() {
        Log.i(TAG, "onRefresh: ");
        lastChannelId = "";
        getChannelList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BaseRTCVideoLiveRoom.sharedInstance().destroySharedInstance();
    }
}
