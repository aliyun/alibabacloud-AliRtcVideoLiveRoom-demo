//
//  VideoRoomBackgroundMusic.h
//  RTCVideoLiveRoom
//
//  Created by aliyun on 2020/8/13.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface VideoRoomBackgroundMusic : NSObject

/// 音量
@property (assign, nonatomic) NSInteger volume;

/// 路径
@property (copy, nonatomic) NSString *path;

/// 测试播放中
@property (assign, nonatomic) BOOL testing;

/// 推送中
@property (assign, nonatomic) BOOL publishing;

- (void)resetData;
@end

NS_ASSUME_NONNULL_END
