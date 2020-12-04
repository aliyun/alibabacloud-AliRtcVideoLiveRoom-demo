//
//  VideoRoomBackgroundMusic.m
//  RTCVideoLiveRoom
//
//  Created by aliyun on 2020/8/13.
//

#import "VideoRoomBackgroundMusic.h"
#import "NSBundle+RTCVideoLiveRoom.h"

@implementation VideoRoomBackgroundMusic

- (instancetype)init {
    if (self = [super init])
    {
        NSString *path = [NSBundle RVLR_musicPathForResource:@"Yippee.mp3"];
        self.volume = 100;
        self.path =path;
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
