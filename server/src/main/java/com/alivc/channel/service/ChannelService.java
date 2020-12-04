package com.alivc.channel.service;

import com.alivc.channel.pojo.Channel;

import java.time.LocalDateTime;
import java.util.List;

public interface ChannelService {


    void insertChannel(Channel channel);

    void deleteChannel(String channelId);


    List<Channel> getChannelList(String lastChannelId, Integer pageSize);

    Channel getChannel(String channelId);

    void endChannel(String channelId, LocalDateTime endTime);
}
