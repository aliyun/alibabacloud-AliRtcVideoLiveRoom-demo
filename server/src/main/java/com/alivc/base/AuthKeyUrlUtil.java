package com.alivc.base;

import org.apache.commons.codec.digest.DigestUtils;

public class AuthKeyUrlUtil {

    public static String getAuthedPath(String path, String authKey, Long timestamp, String rand) {

        String param = timestamp + "-" + rand + "-0-";

        String md5Hash = DigestUtils.md5Hex(path + "-" + param + authKey);

        String url = path + "?auth_key=" + param + md5Hash;

        return url;

    }


}
