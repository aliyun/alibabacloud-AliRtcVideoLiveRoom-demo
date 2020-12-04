package com.aliyun.rtc.videoliveroom.adapter;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;


public class AutoItemDecoration extends RecyclerView.ItemDecoration {


    private int mChildCount;

    public AutoItemDecoration(int childCount) {
        mChildCount = childCount;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        int marginRight = 0;
        int marginLeft = 0;
        int marginBottom = 0;
        int measuredWidth = parent.getMeasuredWidth();
        if (layoutManager instanceof LinearLayoutManager && ((LinearLayoutManager) layoutManager).getOrientation() == OrientationHelper.HORIZONTAL && mChildCount > 0) {
            int childViewWidth = getChildViewWidth(view, measuredWidth);
            if (mChildCount * childViewWidth < measuredWidth && mChildCount > 1 && parent.getChildCount() < mChildCount) {
                marginRight = (measuredWidth - (mChildCount * childViewWidth)) / (mChildCount - 1);
            }
            outRect.right = marginRight;
        }else if (layoutManager instanceof GridLayoutManager && ((GridLayoutManager) layoutManager).getOrientation() == OrientationHelper.VERTICAL ){
            int spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
            int childViewWidth = getChildViewWidth(view, measuredWidth / spanCount);
            if (spanCount * childViewWidth < measuredWidth) {
                marginBottom = marginLeft = marginRight = (measuredWidth - (spanCount * childViewWidth)) / (spanCount + 1);
            }
            if (parent.getChildAdapterPosition(view) % spanCount == (spanCount -1)){
                marginRight = marginRight / 2;
            }else if (parent.getChildAdapterPosition(view) % spanCount == 0){
                marginLeft = marginLeft / 2;
            }
            outRect.set(new Rect(marginRight, 0, marginLeft, marginBottom));
        }
    }

    private int getChildViewWidth(View view, int maxWidth) {
        int childViewWidth = view.getMeasuredWidth();
        if (childViewWidth == 0) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            int widthMeasureSpace = getChildWidthMeasureSpace(layoutParams, maxWidth);
            int heightMeasureSpace = getChilHeightdMeasureSpace(layoutParams, maxWidth);
            view.measure(widthMeasureSpace, heightMeasureSpace);
        }
        childViewWidth = view.getMeasuredWidth();
        return childViewWidth;
    }

    private int getChildWidthMeasureSpace(ViewGroup.LayoutParams layoutParams, int maxWidth){
        int width = layoutParams.width;
        int widthMode = 0;
        int widthSize = 0;
        if (width > 0){
            widthMode = View.MeasureSpec.EXACTLY;
            widthSize = width;
        }else if (width == -1){
            widthMode = View.MeasureSpec.EXACTLY;
            widthSize = maxWidth;
        }else if (width == -2){
            widthMode = View.MeasureSpec.AT_MOST;
            widthSize = maxWidth;
        }
        return View.MeasureSpec.makeMeasureSpec(widthSize, widthMode);
    }

    private int getChilHeightdMeasureSpace(ViewGroup.LayoutParams layoutParams, int maxWidth){
        int height = layoutParams.height;
        int heightMode = 0;
        int heightSize = 0;
        if (height > 0){
            heightMode = View.MeasureSpec.EXACTLY;
            heightSize = height;
        }else if (height == -1){
            heightMode = View.MeasureSpec.EXACTLY;
            heightSize = maxWidth;
        }else if (height == -2){
            heightMode = View.MeasureSpec.AT_MOST;
            heightSize = maxWidth;
        }
        return View.MeasureSpec.makeMeasureSpec(heightSize, heightMode);
    }
}
