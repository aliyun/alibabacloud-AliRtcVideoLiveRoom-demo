//
//  VideoRoomAudienceLiveController.h
//  RTCVideoLiveRoom
//
//  Created by aliyun on 2020/8/14.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface VideoRoomAudienceLiveController : UIViewController

@property (copy,nonatomic) NSString *name;
@property (copy,nonatomic) NSString *coverURL;
@property (copy,nonatomic) NSString *channelId;
@property (copy,nonatomic) NSString *anchorId;

@end

NS_ASSUME_NONNULL_END
