package com.aliyun.rtc.videoliveroom.api.impl;

import com.aliyun.rtc.videoliveroom.api.BaseRTCVideoLiveRoomApi;
import com.aliyun.rtc.videoliveroom.api.net.OkHttpCientManager;
import com.aliyun.rtc.videoliveroom.api.net.OkhttpClient;
import com.aliyun.rtc.videoliveroom.bean.IResponse;
import com.aliyun.rtc.videoliveroom.constant.Constant;

import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;


public class RTCVideoLiveRoomApiImpl extends BaseRTCVideoLiveRoomApi {


    @Override
    public <T> void getChannelList(String channelId, int pageSize, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack) {
        String url = Constant.getChannelListUrl();
        Map<String, Object> params = new HashMap<>();
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_LASTCHANNELID, channelId);
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_PAGESIZE, pageSize);
        OkHttpCientManager.getInstance().doGet(url, params, callBack);
    }

    @Override
    public <T> void getRtcAuth(String channelId, String userId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack) {
        String url = Constant.getRtcAuthUrl();
        Map<String, Object> params = new HashMap<>();
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, channelId);
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_USERID, userId);
        OkHttpCientManager.getInstance().doGet(url, params, callBack);
    }

    @Override
    public <T> void startMPUTask(String channelId, String userId, String coverUrl, String title, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack) {
        String url = Constant.getStartMPUTaskUrl();
        FormBody formBody = new FormBody.Builder()
                .add(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, channelId)
                .add(Constant.NEW_TOKEN_PARAMS_KEY_USERID, userId)
                .add(Constant.NEW_TOKEN_PARAMS_KEY_COVER_URL, coverUrl)
                .add(Constant.NEW_TOKEN_PARAMS_KEY_TITLE, title)
                .build();
        OkHttpCientManager.getInstance().doPost(url, formBody, callBack);
    }

    @Override
    public <T> void getPlayUrl(String channelId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack) {
        String url = Constant.getPlayUrl();
        Map<String, Object> params = new HashMap<>();
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, channelId);
        OkHttpCientManager.getInstance().doGet(url, params, callBack);
    }

    @Override
    public <T> void getUserList(String channelId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack) {
        String url = Constant.getUserListUrl();
        Map<String, Object> params = new HashMap<>();
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, channelId);
        OkHttpCientManager.getInstance().doGet(url, params, callBack);
    }

    @Override
    public <T> void updateMPULayout(String channelId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack) {
        String url = Constant.getUpdateMPULayoutUrl();
        FormBody formBody = new FormBody.Builder()
                .add(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, channelId)
                .build();
        OkHttpCientManager.getInstance().doPost(url, formBody, callBack);
    }

    @Override
    public <T> void stopMPUTask(String channelId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack) {
        String url = Constant.getStopMPUTaskUrl();
        FormBody formBody = new FormBody.Builder()
                .add(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, channelId)
                .build();
        OkHttpCientManager.getInstance().doPost(url, formBody, callBack);
    }

    @Override
    public <T> void describeChannelUsers(String channelId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack) {
        String url = Constant.getDescribeChannelUsersUrl();
        Map<String, Object> params = new HashMap<>();
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, channelId);
        OkHttpCientManager.getInstance().doGet(url, params, callBack);
    }

    @Override
    public <T> void describeRtcChannelMetric(String channelId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack) {
        String url = Constant.getDescribeRtcChannelMetricUrl();
        Map<String, Object> params = new HashMap<>();
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, channelId);
        OkHttpCientManager.getInstance().doGet(url, params, callBack);
    }

    @Override
    public <T> void removeTerminals(String channelId, String operatorId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack) {
        String url = Constant.getRemoveTerminalsUrl();
        FormBody formBody = new FormBody.Builder()
                .add(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, channelId)
                .add(Constant.NEW_TOKEN_PARAMS_KEY_OPERATORID, operatorId)
                .build();
        OkHttpCientManager.getInstance().doPost(url, formBody, callBack);
    }

    @Override
    public <T> void randomCoverUrl(String channelId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack) {
        String url = Constant.getRandomCoverUrl();
        Map<String, Object> params = new HashMap<>();
        params.put(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, channelId);
        OkHttpCientManager.getInstance().doGet(url, params, callBack);
    }

//    @Override
//    public <T> void interactiveOnJoinSuccess(String channelId, String userId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack) {
//        String url = Constant.getJoinSuccessUrl();
//        FormBody formBody = new FormBody.Builder()
//                .add(Constant.NEW_TOKEN_PARAMS_KEY_CHANNELID, channelId)
//                .add(Constant.NEW_TOKEN_PARAMS_KEY_USERID, userId)
//                .build();
//        OkHttpCientManager.getInstance().doPost(url, formBody, callBack);
//    }

}
