package com.aliyun.rtc.videoliveroom.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.aliyun.rtc.videoliveroom.constant.Constant;
import com.aliyun.rtc.videoliveroom.bean.UserInfo;

import java.util.UUID;


public class UserHelper {

    private UserHelper() {
    }

    private static UserHelper instance;

    private UserInfo userInfo;

    public static UserHelper getInstance() {
        if (instance == null) {
            synchronized (UserHelper.class) {
                if (instance == null) {
                    instance = new UserHelper();
                }
            }
        }
        return instance;
    }

    public UserInfo obtainUserInfo() {

        if (!checkUserInfoHasData(userInfo)) {
            return loadUserInfoBySp();
        }

        if (checkUserInfoHasData(userInfo)) {
            saveUserInfo(userInfo);
            return userInfo;
        }

        userInfo = createRandomUser();

        return userInfo;
    }

    private UserInfo createRandomUser() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(UUID.randomUUID().toString());
        userInfo.setUserName(userInfo.getUserId());
        saveUserInfo(userInfo);
        return userInfo;
    }


    private UserInfo loadUserInfoBySp() {
        String userInfoString = SPUtil.getInstance().getString(Constant.RTC_SP_KEY_USER_INFO, "");
        if (!TextUtils.isEmpty(userInfoString)) {
            try {
                userInfo = new Gson().fromJson(userInfoString, UserInfo.class);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        userInfo = createRandomUser();
        return userInfo;
    }

    //    public interface UserInfoResultListener {
    //        void onSuccess(UserInfo userInfo);
    //
    //        void onFaild(String errorMsg);
    //    }

    private void saveUserInfo(UserInfo userInfo) {
        if (userInfo != null) {
            this.userInfo = userInfo;
            String userInfoString = new Gson().toJson(this.userInfo, UserInfo.class);
            SPUtil.getInstance().putString(Constant.RTC_SP_KEY_USER_INFO, userInfoString);
        }
    }

    private boolean checkUserInfoHasData(UserInfo userInfo) {
        return userInfo != null && !TextUtils.isEmpty(userInfo.getUserId());
    }
}
