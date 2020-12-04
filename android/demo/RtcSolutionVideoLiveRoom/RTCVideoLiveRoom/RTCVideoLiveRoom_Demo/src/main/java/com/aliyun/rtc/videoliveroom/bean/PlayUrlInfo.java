package com.aliyun.rtc.videoliveroom.bean;

public class PlayUrlInfo {

    private PlayUrlBean playUrl;

    public PlayUrlBean getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(PlayUrlBean playUrl) {
        this.playUrl = playUrl;
    }

    public static class PlayUrlBean {

        private String rtmp;
        private String flv;
        private String m3u8;

        public String getRtmp() {
            return rtmp;
        }

        public void setRtmp(String rtmp) {
            this.rtmp = rtmp;
        }

        public String getFlv() {
            return flv;
        }

        public void setFlv(String flv) {
            this.flv = flv;
        }

        public String getM3u8() {
            return m3u8;
        }

        public void setM3u8(String m3u8) {
            this.m3u8 = m3u8;
        }
    }
}
