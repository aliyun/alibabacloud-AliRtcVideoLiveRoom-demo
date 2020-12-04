//
//  VideoRoomSoundEffectController.h
//  RTCVideoLiveRoom
//
//  Created by aliyun on 2020/8/12.
//

#import <UIKit/UIKit.h>
#import "JXCategoryListContainerView.h"
#import "VideoRoomSoundEffect.h"

NS_ASSUME_NONNULL_BEGIN

@interface VideoRoomSoundEffectController : UIViewController<JXCategoryListContentViewDelegate>

- (instancetype)initWithEffect1:(VideoRoomSoundEffect *)effect1
                        effect2:(VideoRoomSoundEffect *)effect2;

- (void)reloadData;

@end

NS_ASSUME_NONNULL_END
