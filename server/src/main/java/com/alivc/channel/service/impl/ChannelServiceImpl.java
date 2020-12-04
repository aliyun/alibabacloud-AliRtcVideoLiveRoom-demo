package com.alivc.channel.service.impl;

import com.alivc.channel.dao.ChannelDao;
import com.alivc.channel.pojo.Channel;
import com.alivc.channel.service.ChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ChannelServiceImpl implements ChannelService {


    @Resource
    private ChannelDao channelDao;

    @Override
    public void insertChannel(Channel channel) {
        channelDao.insertChannel(channel);
    }

    @Override
    public void deleteChannel(String channelId) {
        channelDao.deleteChannel(channelId);

    }

    @Override
    public List<Channel> getChannelList(String lastChannelId, Integer pageSize) {
        return channelDao.getChannelList(lastChannelId, pageSize);
    }

    @Override
    public Channel getChannel(String channelId) {
        return channelDao.getChannel(channelId);
    }

    @Override
    public void endChannel(String channelId, LocalDateTime endTime) {
        channelDao.endChannel(channelId, endTime);

    }


}

