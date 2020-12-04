package com.aliyun.rtc.videoliveroom.view;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.alivc.rtc.AliRtcEngine;
import com.aliyun.rtc.videoliveroom.R;
import com.aliyun.rtc.videoliveroom.adapter.RtcBgmAdapter;
import com.aliyun.rtc.videoliveroom.bean.RtcAudioFileInfo;
import com.aliyun.rtc.videoliveroom.bean.RtcFunctionBean;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class RTCAudioEffectView extends FrameLayout {

    private RtcFunctionBean mFunctionBean;
    private ViewPager mVpContent;
    private Context mContext;
    private TabLayout mTabLayout;
    private String[] mFunctions;
    private SparseArray<View> mItemViews;
    private RtcBgmAdapter.AudioPlayingListener mMusicPlayListener;

    public RTCAudioEffectView(@NonNull Context context) {
        this(context, null);
    }

    public RTCAudioEffectView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RTCAudioEffectView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RTCAudioEffectView(RtcFunctionBean functionBean, Context context) {
        super(context);
        mContext = context;
        mFunctionBean = functionBean;
        mItemViews = new SparseArray<>();
        mFunctions = getContext().getResources().getStringArray(R.array.functions);
        initView();
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.rtc_videoliveroom_layout_audio_effect, this, true);
        mVpContent = findViewById(R.id.rtc_videoliveroom_vp_content_bottom_dialog);
        mTabLayout = findViewById(R.id.rtc_videoliveroom_tablayout_bottom_dialog);
        initViewPager();
        initTabLayout();
    }

    private void initTabLayout() {
        mTabLayout.setupWithViewPager(mVpContent, true);
    }

    private void initViewPager() {
        mVpContent.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return mFunctions.length;
            }

            //判断是否page view与 instantiateItem(ViewGroup, int)返回的object的key 是否相同，以提供给其他的函数使用
            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            //instantiateItem该方法的功能是创建指定位置的页面视图。finishUpdate(ViewGroup)返回前，页面应该保证被构造好
            //返回值：返回一个对应该页面的object，这个不一定必须是View，但是应该是对应页面的一些其他容器
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View functionView = getFunctionView(position);
                container.addView(functionView);
                return functionView;
            }

            //该方法的功能是移除一个给定位置的页面。
            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mFunctions[position];
            }
        });
    }

    private View getFunctionView(int position) {
        View view = mItemViews.get(position);
        if (view != null) {
            return view;
        }
        switch (position) {
            case 0:
                BgmListView bgmListView = new BgmListView(mFunctionBean.getBgmFiles(), getContext());
                bgmListView.setListener(mMusicPlayListener);
                view = bgmListView;
                mItemViews.put(position, bgmListView);
                break;
            case 1:
                BgmListView audioEffectView = new BgmListView(mFunctionBean.getAudioEffectFiles(), getContext());
                audioEffectView.setListener(mMusicPlayListener);
                view = audioEffectView;
                mItemViews.put(position, audioEffectView);
                break;
            case 2:
                EffectReverbModeView effectReverbModeView = new EffectReverbModeView(getContext(), mFunctionBean.isEarBack(), mFunctionBean.getEffectReverbMode());
                effectReverbModeView.setEffectReverbModeListener(new EffectReverbModeView.EffectReverbModeListener() {
                    @Override
                    public void onReverbModeSelectedChanged(int selectedPosition) {
                        mFunctionBean.setEffectReverbMode(selectedPosition);
                    }

                    @Override
                    public void onCheckedChanged(boolean enableEarBack) {
                        mFunctionBean.setEarBack(enableEarBack);
                    }
                });
                view = effectReverbModeView;
                break;
            default:
        }
        return view;
    }

    public void setListener(RtcBgmAdapter.AudioPlayingListener listener) {
        mMusicPlayListener = listener;
    }

    public void notifyItemChanged(RtcAudioFileInfo currAudioFileInfo) {
        View view = mItemViews.get(0);
        if (view instanceof BgmListView) {
            ((BgmListView) view).notifyItemChanged(currAudioFileInfo);
        }
    }

}
