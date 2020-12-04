//
//  VideoRoomAudioReverbController.h
//  RTCVideoLiveRoom
//
//  Created by aliyun on 2020/8/12.
//

#import <UIKit/UIKit.h>
#import "JXCategoryListContainerView.h"
#import "VideoRoomMixSetting.h"
NS_ASSUME_NONNULL_BEGIN

@interface VideoRoomAudioReverbController : UIViewController<JXCategoryListContentViewDelegate>

- (instancetype)initWithMixSetting:(VideoRoomMixSetting *)mixSetting;

- (void)reloadData;

@end

NS_ASSUME_NONNULL_END
