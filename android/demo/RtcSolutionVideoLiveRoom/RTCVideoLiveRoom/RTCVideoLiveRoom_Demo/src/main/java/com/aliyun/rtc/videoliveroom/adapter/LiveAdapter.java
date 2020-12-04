package com.aliyun.rtc.videoliveroom.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.rtc.videoliveroom.R;
import com.aliyun.rtc.videoliveroom.bean.LiveInfo;
import com.aliyun.svideo.common.utils.image.GlideRoundedCornersTransform;
import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;
import com.aliyun.svideo.common.utils.image.ImageLoaderOptions;

import java.util.List;

public class LiveAdapter extends RecyclerView.Adapter {

    private List<LiveInfo> mLiveInfos;
    private Context mContext;
    private OnItemClickedListener mClickedListener;
    private ImageLoaderImpl mImageLoader;

    public LiveAdapter(List<LiveInfo> liveInfos, Context context) {
        mLiveInfos = liveInfos;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.rtc_videoliveroom_item_live, parent, false);
        return new LiveViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((LiveViewHolder) holder).bindView(position);
    }

    @Override
    public int getItemCount() {
        return mLiveInfos == null ? 0 : mLiveInfos.size();
    }

    private class LiveViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIvLiveCover;
        private final TextView mTvLiveTitle;

        public LiveViewHolder(View inflate) {
            super(inflate);
            mIvLiveCover = inflate.findViewById(R.id.rtc_videoliveroom_iv_live_cover);
            mTvLiveTitle = inflate.findViewById(R.id.rtc_videoliveroom_tv_live_title);
        }

        public void bindView(final int position) {
            LiveInfo liveInfo = mLiveInfos.get(position);
            if (liveInfo == null) {
                return;
            }
            if (mImageLoader == null) {
                mImageLoader = new ImageLoaderImpl();
            }
            ImageLoaderOptions imageLoaderOptions = new ImageLoaderOptions.Builder()
                    .roundCorner()
                    .radius(8f)
                    .build();
            mImageLoader.loadImage(mContext, liveInfo.getCoverUrl(), imageLoaderOptions).into(mIvLiveCover);
            mTvLiveTitle.setText(liveInfo.getTitle());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mClickedListener != null) {
                        mClickedListener.onClicked(position);
                    }
                }
            });
        }
    }

    public void setClickedListener(OnItemClickedListener clickedListener) {
        mClickedListener = clickedListener;
    }

    public interface OnItemClickedListener {
        void onClicked(int position);
    }
}
