//
//  VideoRoomMixSetting.m
//  RTCVideoLiveRoom
//
//  Created by aliyun on 2020/8/13.
//

#import "VideoRoomMixSetting.h"

@implementation VideoRoomMixSetting
- (instancetype)init
{
    if (self = [super init])
    {
        self.mixType = 0;
        self.earBack = NO;
    }
    return self;
}

- (void)resetData
{
    self.mixType = 0;
    self.earBack = NO;
}
@end
