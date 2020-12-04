package com.aliyun.rtc.videoliveroom.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.aliyun.rtc.videoliveroom.R;
import com.aliyun.rtc.videoliveroom.rtc.BaseRTCVideoLiveRoom;
import com.aliyun.rtc.videoliveroom.utils.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

public class RTCUserChatView extends FrameLayout {

    private FrameLayout mFlOneLayout;
    private FrameLayout mFlTwoLayout;
    private FrameLayout mFlThreeLayout;
    private SparseArray<String> mShowingViews;

    public RTCUserChatView(@NonNull Context context) {
        this(context, null);
    }

    public RTCUserChatView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RTCUserChatView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.rtc_videoliveroom_layout_interactive_user, this, true);
        mFlOneLayout = findViewById(R.id.rtc_videoliveroom_fl_one);
        mFlTwoLayout = findViewById(R.id.rtc_videoliveroom_fl_two);
        mFlThreeLayout = findViewById(R.id.rtc_videoliveroom_fl_three);
        mShowingViews = new SparseArray<>();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility != View.VISIBLE) {
            mFlOneLayout.setVisibility(INVISIBLE);
            mFlTwoLayout.setVisibility(INVISIBLE);
            mFlThreeLayout.setVisibility(INVISIBLE);
            //离会之后sdk会将现有的远端预览载体都消除，再次入会开启预览就需要重新设置surfaceview
            mFlOneLayout.removeAllViews();
            mFlTwoLayout.removeAllViews();
            mFlThreeLayout.removeAllViews();

            mShowingViews.clear();
        }
    }

    /**
     * 主播进入页面场景
     */
    public void showOneLayout() {
        mFlOneLayout.setVisibility(VISIBLE);
        mFlTwoLayout.setVisibility(INVISIBLE);
        mFlThreeLayout.setVisibility(INVISIBLE);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        mFlOneLayout.setLayoutParams(layoutParams);
    }

    public void enterSeat(String userId) {
        int seatCount = mShowingViews.size();
        if (mShowingViews.indexOfValue(userId) == -1) {
            seatCount++;
        }
        if (seatCount == 1) {
            mShowingViews.put(0, userId);
            showOneLayout();
            BaseRTCVideoLiveRoom.sharedInstance().startPlay(userId, mFlOneLayout);
        } else if (seatCount == 2) {
            mShowingViews.put(1, userId);
            showTwoLayout();
            BaseRTCVideoLiveRoom.sharedInstance().startPlay(userId, mFlTwoLayout);
        } else if (seatCount == 3) {
            mShowingViews.put(2, userId);
            showThreeLayout();
            BaseRTCVideoLiveRoom.sharedInstance().startPlay(userId, mFlThreeLayout);
        }
    }

    public void leaveSeat(String userId) {
        View seat2 = mFlTwoLayout.getChildAt(0);
        View seat3 = mFlThreeLayout.getChildAt(0);
        int position = mShowingViews.indexOfValue(userId);
        if (mShowingViews.size() == 3) {
            //退出的是中间的人，就把第三个人挪到中间，把第三个view清空
            if (position == 1) {
                mShowingViews.put(1, mShowingViews.get(2));
                mShowingViews.remove(2);
                mFlTwoLayout.removeAllViews();
                mFlThreeLayout.removeAllViews();
                mFlTwoLayout.addView(seat3);
            } else if (position == 0) {
                //退出的是第一个人，后面两个挪过来
                mShowingViews.put(0, mShowingViews.get(1));
                mShowingViews.put(1, mShowingViews.get(2));
                mShowingViews.remove(2);
                mFlOneLayout.removeAllViews();
                mFlTwoLayout.removeAllViews();
                mFlThreeLayout.removeAllViews();
                mFlOneLayout.addView(seat2);
                mFlTwoLayout.addView(seat3);
            }
            showTwoLayout();
        } else {
            //退出的是第一个人，就把第二个人挪到第一个，把第二个view清空
            if (position == 0) {
                mShowingViews.put(0, mShowingViews.get(1));
                mShowingViews.remove(1);
                mFlOneLayout.removeAllViews();
                mFlTwoLayout.removeAllViews();
                mFlOneLayout.addView(seat2);
            }
            showOneLayout();
        }
        if (position >=0 && position < mShowingViews.size()) {
            mShowingViews.remove(mShowingViews.indexOfValue(userId));
        }
    }

    public List<String> getSeat() {
        if (mShowingViews == null) {
            return null;
        }
        ArrayList<String> seats = new ArrayList<>();
        for (int i = 0; i < mShowingViews.size(); i++) {
            seats.add(mShowingViews.get(i));
        }
        return seats;
    }

    /**
     * 二连麦场景（可以是主播，也可以是观众）
     */
    public void showTwoLayout() {
        mFlOneLayout.setVisibility(VISIBLE);
        mFlTwoLayout.setVisibility(VISIBLE);
        mFlThreeLayout.setVisibility(INVISIBLE);
        int width = ScreenUtil.getScreenWidth(((Activity) getContext())) / 2;
        int height = ScreenUtil.getScreenHeight(((Activity) getContext())) / 2;
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(width, height);
        layoutParams1.addRule(RelativeLayout.CENTER_VERTICAL);
        mFlOneLayout.setLayoutParams(layoutParams1);

        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(width, height);
        layoutParams2.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams2.addRule(RelativeLayout.RIGHT_OF, mFlOneLayout.getId());
        mFlTwoLayout.setLayoutParams(layoutParams2);
    }

    /**
     * 三人连麦场景（可以是主播，也可以是观众）
     */
    public void showThreeLayout() {
        mFlOneLayout.setVisibility(VISIBLE);
        mFlTwoLayout.setVisibility(VISIBLE);
        mFlThreeLayout.setVisibility(VISIBLE);

        int width = ScreenUtil.getScreenWidth(((Activity) getContext())) / 2;
        int height = ScreenUtil.getScreenHeight(((Activity) getContext())) / 3;
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(width, height);
        layoutParams1.topMargin = ScreenUtil.getScreenHeight(((Activity) getContext())) / 6;
        layoutParams1.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mFlOneLayout.setLayoutParams(layoutParams1);

        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(width, height);
        layoutParams2.addRule(RelativeLayout.BELOW, mFlOneLayout.getId());
        mFlTwoLayout.setLayoutParams(layoutParams2);

        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(width, height);
        layoutParams3.addRule(RelativeLayout.BELOW, mFlOneLayout.getId());
        layoutParams3.addRule(RelativeLayout.RIGHT_OF, mFlTwoLayout.getId());
        mFlThreeLayout.setLayoutParams(layoutParams3);
    }
}
