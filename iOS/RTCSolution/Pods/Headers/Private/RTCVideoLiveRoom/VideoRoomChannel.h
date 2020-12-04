//
//  VideoRoomChannel.h
//  RTCVideoLiveRoom
//
//  Created by aliyun on 2020/8/12.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface VideoRoomChannel : NSObject

@property (copy,nonatomic)   NSString *channelId;
@property (copy,nonatomic)   NSString *ownerId;
@property (copy,nonatomic)   NSString *coverUrl;
@property (copy,nonatomic)   NSString *title;
@property (strong,nonatomic) NSDate   *createDateTime;

@end

NS_ASSUME_NONNULL_END
