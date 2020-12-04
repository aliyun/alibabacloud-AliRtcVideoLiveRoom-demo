package com.alivc.channel.dao;

import com.alivc.channel.pojo.Channel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;


@Mapper
public interface ChannelDao {

    void insertChannel(Channel channel);

    void deleteChannel(String channelId);

    List<Channel> getChannelList(@Param("lastChannelId") String lastChannelId, @Param("pageSize") Integer pageSize);

    Channel getChannel(String channelId);

    void endChannel(@Param("channelId") String channelId, @Param("endTime") LocalDateTime endTime);
}
