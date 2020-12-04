//
//  VideoRoomSoundEffect.m
//  RTCVideoLiveRoom
//
//  Created by aliyun on 2020/8/13.
//

#import "VideoRoomSoundEffect.h"
#import "NSBundle+RTCVideoLiveRoom.h"

@implementation VideoRoomSoundEffect

- (instancetype)initWithEffectId:(NSInteger)effectId fileName:(NSString *)fileName
{
    if (self = [super init]) {
        self.effectId = effectId;
        self.path =[NSBundle RVLR_musicPathForResource:fileName];
        self.volume = 100;
        self.testing = NO;
        self.publishing = NO;
    }
    
    return self;
}

- (void)resetData
{
    self.testing = NO;
    self.publishing = NO;
}

@end
