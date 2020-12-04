//
//  VideoRoomMixConsoleController.h
//  RTCVideoLiveRoom
//
//  Created by aliyun on 2020/8/12.
//

#import <UIKit/UIKit.h>
#import "VideoRoomBackgroundMusic.h"
#import "VideoRoomSoundEffect.h"
#import "VideoRoomMixSetting.h"
NS_ASSUME_NONNULL_BEGIN

@interface VideoRoomMixConsoleController : UIViewController

- (instancetype)initWithMusic:(VideoRoomBackgroundMusic *)music
                  effectLaugh:(VideoRoomSoundEffect *)laugh
               effectApplause:(VideoRoomSoundEffect *)applause
                   mixSetting:(VideoRoomMixSetting *)mixSetting;

- (void)reloadDataAtIndex:(NSInteger)index;

@end

NS_ASSUME_NONNULL_END
