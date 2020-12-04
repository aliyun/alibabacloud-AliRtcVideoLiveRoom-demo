package com.aliyun.rtc.videoliveroom.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aliyun.rtc.videoliveroom.R;
import com.aliyun.rtc.videoliveroom.utils.NetUtils;
import com.aliyun.rtc.videoliveroom.utils.NetWatchdogUtils;
import com.aliyun.svideo.common.utils.ToastUtils;

public class NetWorkErrorActivity extends AppCompatActivity implements NetWatchdogUtils.NetChangeListener, View.OnClickListener {

    private ImageView mIvClose;
    private TextView mTvReTry;
    private NetWatchdogUtils mNetWatchdogUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rtc_videoliveroom_activity_net_work_error);

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNetWatchdogUtils == null) {
            //添加网络监听
            mNetWatchdogUtils = new NetWatchdogUtils(this);
            mNetWatchdogUtils.setNetChangeListener(this);
            mNetWatchdogUtils.startWatch();
        }
    }

    private void initView() {
        mIvClose = findViewById(R.id.iv_close_net_error);
        mTvReTry = findViewById(R.id.tv_retry_net_error);

        mIvClose.setOnClickListener(this);
        mTvReTry.setOnClickListener(this);
    }


    @Override
    public void onWifiTo4G() {

    }

    @Override
    public void on4GToWifi() {

    }

    @Override
    public void onReNetConnected(boolean isReconnect) {
        if (isReconnect) {
            finish();
        }
    }

    @Override
    public void onNetUnConnected() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNetWatchdogUtils != null) {
            mNetWatchdogUtils.stopWatch();
            mNetWatchdogUtils.setNetChangeListener(null);
            mNetWatchdogUtils = null;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_close_net_error){
            finish();
        }else if (id == R.id.tv_retry_net_error){
            boolean networkConnected = NetUtils.isNetworkConnected(NetWorkErrorActivity.this);
            if (networkConnected) {
                finish();
            } else {
                ToastUtils.showInCenter(NetWorkErrorActivity.this, getString(R.string.rtc_videoliveroom_string_network_conn_error));
            }
        }
    }
}
