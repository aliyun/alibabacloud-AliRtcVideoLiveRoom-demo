package com.alivc.channel.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Channel {

    private String channelId;

    private String ownerId;

    private String coverUrl;

    private String title;

    private LocalDateTime createDateTime;

    private LocalDateTime endTime;

}
