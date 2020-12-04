package com.aliyun.rtc.videoliveroom.bean;

import java.util.List;

public class ChannelUsersInfo {

    private String requestId;
    private int timestamp;
    private boolean isChannelExist;
    private int channelProfile;
    private int commTotalNum;
    private int interactiveUserNum;
    private int liveUserNum;
    private List<String> userList;
    private List<String> interactiveUserList;
    private List<String> liveUserList;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isIsChannelExist() {
        return isChannelExist;
    }

    public void setIsChannelExist(boolean isChannelExist) {
        this.isChannelExist = isChannelExist;
    }

    public int getChannelProfile() {
        return channelProfile;
    }

    public void setChannelProfile(int channelProfile) {
        this.channelProfile = channelProfile;
    }

    public int getCommTotalNum() {
        return commTotalNum;
    }

    public void setCommTotalNum(int commTotalNum) {
        this.commTotalNum = commTotalNum;
    }

    public int getInteractiveUserNum() {
        return interactiveUserNum;
    }

    public void setInteractiveUserNum(int interactiveUserNum) {
        this.interactiveUserNum = interactiveUserNum;
    }

    public int getLiveUserNum() {
        return liveUserNum;
    }

    public void setLiveUserNum(int liveUserNum) {
        this.liveUserNum = liveUserNum;
    }

    public List<String> getUserList() {
        return userList;
    }

    public void setUserList(List<String> userList) {
        this.userList = userList;
    }

    public List<String> getInteractiveUserList() {
        return interactiveUserList;
    }

    public void setInteractiveUserList(List<String> interactiveUserList) {
        this.interactiveUserList = interactiveUserList;
    }

    public List<String> getLiveUserList() {
        return liveUserList;
    }

    public void setLiveUserList(List<String> liveUserList) {
        this.liveUserList = liveUserList;
    }

}
