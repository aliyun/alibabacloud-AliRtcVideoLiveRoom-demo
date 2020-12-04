package com.aliyun.rtc.videoliveroom.constant;


public class Constant {

    public static final String NEW_TOKEN_PARAMS_KEY_USERID = "userId";
    public static final String PATH_ASSETS_BGM = "mp3/bgm.zip";
    public static final String PATH_ASSETS_AUDIOEFFECT = "mp3/audioeffect.zip";
    public static final String PATH_DIR_BGM_OUT = "bgm";
    public static final String PATH_DIR_AUDIOEFFECT_OUT = "audioeffect";
    //背景乐、音效默认音量
    public static final int VALUE_AUDIO_EFFECT_VOLUME = 100;
    public static final String RTC_SP_KEY_USER_INFO = "userInfo";
    //保存随机图片的地址
    public static final String PICTURE_URL = "picture_url";
    /**
     * server端的请求域名，需要用户自己替换成自己server端的域名
     */
    private static final String API_URL = "";
    /**
     * 获取鉴权信息
     */
    private static final String URL_CHANNEK_LIST = API_URL + "/getChannelList";
    //加入房间成功
    private static final String URL_RTC_AUTH = API_URL + "/getRtcAuth";
    //获取麦序
    private static final String URL_START_MPU_TASK = API_URL + "/startMPUTask";

    private static final String URL_JOIN_SUCCESS = API_URL + "/interactiveOnJoinSuccess";

    private static final String URL_GET_PLAY_URL = API_URL + "/getPlayUrl";

    private static final String URL_USER_LIST = API_URL + "/getUserList";

    private static final String URL_UPDATE_MPU_LAYOUT = API_URL + "/updateMPULayout";

    private static final String URL_STOP_MPU_TASK = API_URL + "/stopMPUTask";

    private static final String URL_DESCRIBE_CHANNEL_USERS = API_URL + "/describeChannelUsers";

    private static final String URL_DESCRIBE_CHANNEK_METRIC = API_URL + "/describeRtcChannelMetric";

    private static final String URL_REMOVE_TERMINALS = API_URL + "/removeTerminals";

    private static final String URL_RANDOM_COVER = API_URL + "/randomCoverUrl";

    public static final String NEW_TOKEN_PARAMS_KEY_CHANNELID = "channelId";
    public static final String NEW_TOKEN_PARAMS_KEY_PAGESIZE = "pageSize";
    public static final String NEW_TOKEN_PARAMS_KEY_LASTCHANNELID = "lastChannelId";
    public static final String NEW_TOKEN_PARAMS_KEY_COVER_URL = "coverUrl";
    public static final String NEW_TOKEN_PARAMS_KEY_TITLE = "title";
    public static final String NEW_TOKEN_PARAMS_KEY_OPERATORID = "operatorId";

    //最大麦序数量
    public static final int MAX_SEAT_COUNT = 3;

    public static String getChannelListUrl() {
        return URL_CHANNEK_LIST;
    }

    public static String getRtcAuthUrl() {
        return URL_RTC_AUTH;
    }

    public static String getStartMPUTaskUrl() {
        return URL_START_MPU_TASK;
    }

    public static String getPlayUrl() {
        return URL_GET_PLAY_URL;
    }

    public static String getUserListUrl() {
        return URL_USER_LIST;
    }

    public static String getUpdateMPULayoutUrl() {
        return URL_UPDATE_MPU_LAYOUT;
    }

    public static String getStopMPUTaskUrl() {
        return URL_STOP_MPU_TASK;
    }

    public static String getDescribeChannelUsersUrl() {
        return URL_DESCRIBE_CHANNEL_USERS;
    }

    public static String getDescribeRtcChannelMetricUrl() {
        return URL_DESCRIBE_CHANNEK_METRIC;
    }

    public static String getRemoveTerminalsUrl() {
        return URL_REMOVE_TERMINALS;
    }

    public static String getRandomCoverUrl() {
        return URL_RANDOM_COVER;
    }

    public static String getJoinSuccessUrl(){
        return URL_JOIN_SUCCESS;
    }
}
