package com.alivc.liveclass.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alivc.base.AuthKeyUrlUtil;
import com.alivc.base.ConfigMapUtil;
import com.alivc.base.ResponseResult;
import com.alivc.base.RtcOpenAPI;
import com.alivc.channel.pojo.Channel;
import com.alivc.channel.service.ChannelService;
import com.aliyuncs.rtc.model.v20180111.DescribeChannelUsersResponse;
import com.aliyuncs.rtc.model.v20180111.DescribeUserInfoInChannelResponse;
import com.aliyuncs.rtc.model.v20180111.StartMPUTaskResponse;
import com.aliyuncs.rtc.model.v20180111.StopMPUTaskRequest;
import com.aliyuncs.rtc.model.v20180111.StopMPUTaskResponse;
import com.aliyuncs.rtc.model.v20180111.UpdateMPULayoutRequest;
import com.aliyuncs.rtc.model.v20180111.UpdateMPULayoutResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Slf4j
@RestController
public class RtcController {

    @Resource
    private ChannelService channelService;

    @RequestMapping(value = "/getChannelList", method = RequestMethod.GET)
    public ResponseResult getChannelList(String lastChannelId, Integer pageSize) {
        if (StringUtils.isBlank(lastChannelId)) {
            lastChannelId = null;
        }

        if (pageSize == null || pageSize < 0 || pageSize > 100) {
            pageSize = 10;
        }

        List<Channel> channelList = channelService.getChannelList(lastChannelId, pageSize);

        String appId = ConfigMapUtil.getValueByKey("rtc.liveclass.appId");

        ResponseResult responseResult = new ResponseResult();

        try {

            Iterator<Channel> channelIterator = channelList.iterator();
            while (channelIterator.hasNext()) {
                Channel channel = channelIterator.next();
                String channelId = channel.getChannelId();


                DescribeChannelUsersResponse response = RtcOpenAPI.describeChannelUsers(appId, channelId);

                if (CollectionUtils.isEmpty(response.getUserList())) {
                    channelService.deleteChannel(channelId);
                    channelIterator.remove();
                }

            }

            responseResult.setData(channelList);

            return responseResult;
        } catch (Exception e) {
            responseResult.setResult("false");
            responseResult.setCode("500");
            log.error("getChannelList error", e);
        }
        return responseResult;

    }

    @RequestMapping(value = "/getRtcAuth", method = RequestMethod.GET)
    public ResponseResult getRtcAuth(String channelId, String userId) {

        ResponseResult responseResult = new ResponseResult();

        if (StringUtils.isBlank(userId)) {
            userId = UUID.randomUUID().toString();
        }

        try {
            JSONObject rtcToken = RtcOpenAPI.createToken(channelId, userId);

            responseResult.setData(rtcToken);

            String appId = ConfigMapUtil.getValueByKey("rtc.liveclass.appId");

            ScheduledDeleteChannel.addChannel(appId, channelId);


            return responseResult;
        } catch (Exception e) {
            responseResult.setResult("false");
            responseResult.setCode("500");
            log.error("getRtcAuth error", e);
        }
        return responseResult;

    }

    @RequestMapping(value = "/startMPUTask", method = RequestMethod.POST)
    public ResponseResult onJoinSuccess(String channelId, String userId, String coverUrl, String title) {

        String appId = ConfigMapUtil.getValueByKey("rtc.liveclass.appId");

        String livePushDomain = ConfigMapUtil.getValueByKey("live_push_domain");
        String livePushDomainAuthKey = ConfigMapUtil.getValueByKey("live_push_domain_auth_key");

        String appStream = ConfigMapUtil.getValueByKey("live_app_stream_name");

        Long timestamp = System.currentTimeMillis() / 1000;
        String rand = UUID.randomUUID().toString().replace("-", "");

        String pushUrl = "rtmp://" + livePushDomain + AuthKeyUrlUtil.getAuthedPath(appStream + channelId, livePushDomainAuthKey, timestamp, rand);


        ResponseResult responseResult = new ResponseResult();


        try {

            StartMPUTaskResponse response = RtcOpenAPI.startMPUTask(channelId, pushUrl, channelId, appId, userId);

            Channel channel = new Channel();
            channel.setChannelId(channelId);
            channel.setOwnerId(userId);
            channel.setCoverUrl(coverUrl);
            channel.setTitle(title);
            channel.setCreateDateTime(LocalDateTime.now());
            channelService.insertChannel(channel);

            return responseResult;
        } catch (Exception e) {
            responseResult.setResult("false");
            responseResult.setCode("500");
            log.error("startMPUTask error", e);
        }
        return responseResult;

    }

    @RequestMapping(value = "/getPlayUrl", method = RequestMethod.GET)
    public ResponseResult getPlayUrl(String channelId) {

        String livePlayDomain = ConfigMapUtil.getValueByKey("live_play_domain");
        String livePlayDomainAuthKey = ConfigMapUtil.getValueByKey("live_play_domain_auth_key");

        String appStream = ConfigMapUtil.getValueByKey("live_app_stream_name");

        Long timestamp = System.currentTimeMillis() / 1000;
        String rand = UUID.randomUUID().toString().replace("-", "");

        ResponseResult responseResult = new ResponseResult();

        // 原画
        String playUrlRtmp = "rtmp://" + livePlayDomain + AuthKeyUrlUtil.getAuthedPath(appStream + channelId, livePlayDomainAuthKey, timestamp, rand);
        String playUrlFlv = "https://" + livePlayDomain + AuthKeyUrlUtil.getAuthedPath(appStream + channelId + ".flv", livePlayDomainAuthKey, timestamp, rand);
        String playUrlM3u8 = "https://" + livePlayDomain + AuthKeyUrlUtil.getAuthedPath(appStream + channelId + ".m3u8", livePlayDomainAuthKey, timestamp, rand);

        JSONObject playUrl = new JSONObject();
        playUrl.put("rtmp", playUrlRtmp);
        playUrl.put("flv", playUrlFlv);
        playUrl.put("m3u8", playUrlM3u8);

        JSONObject data = new JSONObject();
        data.put("playUrl", playUrl);

        responseResult.setData(data);

        return responseResult;

    }

    @RequestMapping(value = "/updateMPULayout", method = RequestMethod.POST)
    public ResponseResult uploadLayout(String channelId) {
        ResponseResult responseResult = new ResponseResult();

        try {
            List<String> userList = getSortedUserList(channelId);

            UpdateMPULayoutRequest request = new UpdateMPULayoutRequest();

            String appId = ConfigMapUtil.getValueByKey("rtc.liveclass.appId");

            request.setAppId(appId);

            request.setTaskId(channelId);

            List<UpdateMPULayoutRequest.UserPanes> userPanesList = new LinkedList<>();
            for (int i = 0; i < userList.size(); i++) {
                UpdateMPULayoutRequest.UserPanes userPanes = new UpdateMPULayoutRequest.UserPanes();
                userPanes.setUserId(userList.get(i));
                userPanes.setPaneId(i);
                userPanes.setSourceType("camera");
                userPanesList.add(userPanes);
            }

            request.setUserPaness(userPanesList);

            String layout1Id = ConfigMapUtil.getValueByKey("layout_1");
            String layout2Id = ConfigMapUtil.getValueByKey("layout_2");
            String layout3Id = ConfigMapUtil.getValueByKey("layout_3");
            List layoutIdArr = new ArrayList();
            layoutIdArr.add(layout1Id);
            layoutIdArr.add(layout2Id);
            layoutIdArr.add(layout3Id);
            request.setLayoutIdss(layoutIdArr);

            UpdateMPULayoutResponse response = RtcOpenAPI.updateMPULayout(request);
            log.info(JSON.toJSONString(response));

            return responseResult;
        } catch (Exception e) {
            responseResult.setResult("false");
            responseResult.setCode("500");
            log.error("updateMPULayout error", e);
        }
        return responseResult;

    }


    @RequestMapping(value = "/stopMPUTask", method = RequestMethod.POST)
    public ResponseResult stopMPUTask(String channelId) {
        StopMPUTaskRequest request = new StopMPUTaskRequest();

        String appId = ConfigMapUtil.getValueByKey("rtc.liveclass.appId");

        request.setAppId(appId);
        request.setTaskId(channelId);

        ResponseResult responseResult = new ResponseResult();

        try {

            StopMPUTaskResponse response = RtcOpenAPI.stopMPUTaskRequest(request);
            RtcOpenAPI.deleteChannel(appId, channelId);

            channelService.endChannel(channelId, LocalDateTime.now());

            return responseResult;
        } catch (Exception e) {
            responseResult.setResult("false");
            responseResult.setCode("500");
            log.error("stopMPUTask error", e);
        }
        return responseResult;

    }

    private List<String> getSortedUserList(String channelId) throws Exception {

        Channel channel = channelService.getChannel(channelId);
        String appId = ConfigMapUtil.getValueByKey("rtc.liveclass.appId");

        DescribeChannelUsersResponse response = RtcOpenAPI.describeChannelUsers(appId, channelId);
        List<String> userList = response.getUserList();

        Map<String, Integer> createDatetime = getJoinTime(channelId, userList);

        userList.sort((o1, o2) -> {
            if (o1.equals(channel.getOwnerId())) {
                return -1;
            } else if (o2.equals(channel.getOwnerId())) {
                return 1;
            } else {
                return createDatetime.get(o1).compareTo(createDatetime.get(o2));
            }
        });
        return userList;

    }

    private Map<String, Integer> getJoinTime(String channelId, List<String> userIdList) {
        String appId = ConfigMapUtil.getValueByKey("rtc.liveclass.appId");

        try {
            Map<String, Integer> joinTime = new HashMap<>();
            for (String userId : userIdList) {
                DescribeUserInfoInChannelResponse response = RtcOpenAPI.describeUserInfoInChannel(appId, channelId, userId);
                Integer join = response.getProperty().get(0).getJoin();
                joinTime.put(userId, join);
            }

            return joinTime;

        } catch (Exception e) {
            log.error("", e);
            return null;
        }

    }

    @RequestMapping(value = "/getUserList", method = RequestMethod.GET)
    public ResponseResult getUserList(String channelId) {

        ResponseResult responseResult = new ResponseResult();
        List<String> userList;
        try {
            userList = getSortedUserList(channelId);
            responseResult.setData(userList);
        } catch (Exception e) {
            userList = new ArrayList<>();
            log.error("getUserList error", e);
        }
        return responseResult;

    }

    @RequestMapping(value = "/describeChannelUsers", method = RequestMethod.GET)
    public ResponseResult describeChannelUsers(String channelId) {

        String appId = ConfigMapUtil.getValueByKey("rtc.liveclass.appId");

        ResponseResult responseResult = new ResponseResult();

        try {
            DescribeChannelUsersResponse response = RtcOpenAPI.describeChannelUsers(appId, channelId);
            responseResult.setData(response);
            return responseResult;
        } catch (Exception e) {
            responseResult.setResult("false");
            responseResult.setCode("500");
            log.error("describe Channel Users error", e);
        }
        return responseResult;

    }

    @RequestMapping(value = "/getChannelStartTime", method = RequestMethod.GET)
    public ResponseResult getChannelStartTime(String channelId) {

        ResponseResult responseResult = new ResponseResult();

        try {
            Channel channel = channelService.getChannel(channelId);
            String startTime = channel.getCreateDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
            String endTime = channel.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));

            JSONObject channelStartTime = new JSONObject();
            channelStartTime.put("startTime", startTime);
            channelStartTime.put("endTime", endTime);
            responseResult.setData(channelStartTime);
            return responseResult;
        } catch (Exception e) {
            responseResult.setResult("false");
            responseResult.setCode("500");
            log.error("getChannelStartTime error", e);
        }
        return responseResult;

    }

    @RequestMapping(value = "/removeTerminals", method = RequestMethod.POST)
    public ResponseResult describeChannelUsers(String channelId, String operatorId) {

        String appId = ConfigMapUtil.getValueByKey("rtc.liveclass.appId");

        ResponseResult responseResult = new ResponseResult();

        try {
            DescribeChannelUsersResponse response = RtcOpenAPI.describeChannelUsers(appId, channelId);

            List<String> userList = response.getUserList();
            userList.remove(operatorId);

            RtcOpenAPI.removeTerminals(appId, channelId, userList);

            responseResult.setData(response);
            return responseResult;
        } catch (Exception e) {
            responseResult.setResult("false");
            responseResult.setCode("500");
            log.error("remove Users error", e);
        }
        return responseResult;

    }

    @RequestMapping(value = "/randomCoverUrl", method = RequestMethod.GET)
    public ResponseResult randomCoverUrl() {
        String[] coverUrlArr = {
                "https://alivc-demo-vod.aliyuncs.com/image/default/663D208AEFDC47849CB214C39F34A5D7-6-2.jpg",
                "https://alivc-demo-vod.aliyuncs.com/image/default/D75C10C16E0A482AAA70C24A159A660F-6-2.jpg",
                "https://alivc-demo-vod.aliyuncs.com/image/default/F149C58041BC4D85B1185B2140CFE3EF-6-2.jpg",
                "https://alivc-demo-vod.aliyuncs.com/image/default/E84D11BE7A1D4ADEB9004C6DB6B14AB8-6-2.jpg",
                "https://alivc-demo-vod.aliyuncs.com/image/default/9A700776A4A141B4B43B9F36A66FF5B4-6-2.jpg",
                "https://alivc-demo-vod.aliyuncs.com/image/default/0DBE811BEB3B4300B4BE9EBAD9A625A6-6-2.jpg"
        };


        String coverUrl = coverUrlArr[new Random().nextInt(coverUrlArr.length)];

        ResponseResult responseResult = new ResponseResult();

        try {
            responseResult.setData(coverUrl);
            return responseResult;
        } catch (Exception e) {
            responseResult.setResult("false");
            responseResult.setCode("500");
            log.error(" random coverUrl error", e);
        }
        return responseResult;

    }

}
