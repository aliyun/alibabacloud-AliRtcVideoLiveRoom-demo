package com.aliyun.rtc.videoliveroom.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.rtc.videoliveroom.R;

import java.util.List;

public class EffectReverbAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<Pair<String, Integer>> mEffectReverbDatas;
    private int selectedPosition;
    private onItemClickListener mClickListener;

    public EffectReverbAdapter(Context context, List<Pair<String, Integer>> effectReverbDatas) {
        mContext = context;
        mEffectReverbDatas = effectReverbDatas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.rtc_videoliveroom_item_effect_reverb, parent, false);
        return new UserInfoHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((UserInfoHolder) holder).bindView(position);
    }

    @Override
    public int getItemCount() {
        return mEffectReverbDatas != null ? mEffectReverbDatas.size() : 0;
    }

    public void setSelectedItem(int selectedPosition) {
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
    }

    private class UserInfoHolder extends RecyclerView.ViewHolder {

        private final TextView mTvName;
        private final ImageView mIvCover;

        public UserInfoHolder(View inflate) {
            super(inflate);
            mIvCover = inflate.findViewById(R.id.rtc_videoliveroom_iv_effect_reverb_cover);
            mTvName = inflate.findViewById(R.id.rtc_videoliveroom_tv_effect_reverb_name);
        }

        public synchronized void bindView(final int position) {
            Pair<String, Integer> effectReverbData = mEffectReverbDatas.get(position);
            if (effectReverbData == null) {
                return;
            }
            mTvName.setText(effectReverbData.first);
            mIvCover.setBackgroundResource(effectReverbData.second);
            if (selectedPosition == position) {
                mIvCover.setImageResource(R.drawable.rtc_videoliveroom_bg_effect_reverb_shape);
            } else {
                mIvCover.setImageResource(0);
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPosition = position;
                    if (mClickListener != null) {
                        mClickListener.onClick(position);
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }

    public onItemClickListener getClickListener() {
        return mClickListener;
    }

    public void setClickListener(onItemClickListener clickListener) {
        mClickListener = clickListener;
    }

    public interface onItemClickListener {
        void onClick(int position);
    }
}
