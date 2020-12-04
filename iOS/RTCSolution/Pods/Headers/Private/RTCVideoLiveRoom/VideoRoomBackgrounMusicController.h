//
//  VideoRoomBackgrounMusicController.h
//  RTCVideoLiveRoom
//
//  Created by aliyun on 2020/8/12.
//

#import <UIKit/UIKit.h>
#import "JXCategoryListContainerView.h"
#import "VideoRoomBackgroundMusic.h"

NS_ASSUME_NONNULL_BEGIN

@interface VideoRoomBackgrounMusicController : UIViewController<JXCategoryListContentViewDelegate>

- (instancetype)initWithMusic:(VideoRoomBackgroundMusic *)music;

- (void)reloadData;

@end

NS_ASSUME_NONNULL_END
