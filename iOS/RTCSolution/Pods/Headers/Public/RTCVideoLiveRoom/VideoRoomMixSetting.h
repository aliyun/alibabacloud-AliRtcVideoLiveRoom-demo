//
//  VideoRoomMixSetting.h
//  RTCVideoLiveRoom
//
//  Created by aliyun on 2020/8/13.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface VideoRoomMixSetting : NSObject

/// 耳返
@property (assign, nonatomic) BOOL earBack;

/// 混响类型
@property (assign, nonatomic) NSInteger mixType;

@end

NS_ASSUME_NONNULL_END
