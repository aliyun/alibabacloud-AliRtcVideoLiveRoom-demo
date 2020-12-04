package com.aliyun.rtc.videoliveroom.api;


import com.aliyun.rtc.videoliveroom.api.net.OkhttpClient;
import com.aliyun.rtc.videoliveroom.bean.IResponse;

public abstract class BaseRTCVideoLiveRoomApi {

    /**
     * 获取房间列表 get请求
     *
     * @param channelId 起始房间编号，已经请求过的最大房间id
     * @param pageSize  返回的房间数量
     * @param <T>       返回值泛型
     */
    public abstract <T> void getChannelList(String channelId, int pageSize, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack);

    /**
     * 获取鉴权信息 get请求
     *
     * @param channelId 房间编号
     * @param userId       用户id
     * @param <T>       返回值泛型
     */
    public abstract <T> void getRtcAuth(String channelId, String userId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack);

    /**
     * 开启旁路直播 post请求
     *
     * @param channelId 房间编号
     * @param userId       用户id
     * @param <T>       返回值泛型
     */
    public abstract <T> void startMPUTask(String channelId, String userId, String coverUrl, String title, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack);

    /**
     * 获取直播链接 get请求
     *
     * @param channelId 房间号
     * @param <T>       返回值泛型
     */
    public abstract <T> void getPlayUrl(String channelId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack);

    /**
     * 获取用户列表 get请求
     *
     * @param channelId 房间号
     * @param <T>返回值泛型
     */
    public abstract <T> void getUserList(String channelId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack);

    /**
     * 更新布局，主播在观众连麦时调用  post请求
     *
     * @param channelId 房间号
     * @param <T>返回值泛型
     */
    public abstract <T> void updateMPULayout(String channelId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack);

    /**
     * 停止旁路直播 post请求
     *
     * @param channelId 房间号
     * @param <T>返回值泛型
     */
    public abstract <T> void stopMPUTask(String channelId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack);

    /**
     * 查询房间人数 get请求
     *
     * @param channelId 房间号
     * @param <T>返回值泛型
     */
    public abstract <T> void describeChannelUsers(String channelId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack);

    /**
     * 查询通信记录,直播结束后获取直播时间 get请求
     *
     * @param channelId 房间号
     * @param <T>返回值泛型
     */
    public abstract <T> void describeRtcChannelMetric(String channelId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack);

    /**
     * 断开连麦用户 post请求
     *
     * @param channelId  房间号
     * @param operatorId 操作者的UserId
     * @param <T>返回值泛型
     */
    public abstract <T> void removeTerminals(String channelId, String operatorId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack);

    /**
     * 获取随机封面 get请求
     * @param channelId 房间号
     */
    public abstract <T> void randomCoverUrl(String channelId, OkhttpClient.BaseHttpCallBack<IResponse<T>> callBack);
}
