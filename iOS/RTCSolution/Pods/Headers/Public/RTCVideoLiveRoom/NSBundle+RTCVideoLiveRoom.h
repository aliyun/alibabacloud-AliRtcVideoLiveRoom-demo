//
//  NSBundle+RTCVideoLiveRoom.h
//  Pods
//
//  Created by aliyun on 2020/8/6.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface NSBundle (RTCVideoLiveRoom)

+ (instancetype)RTCVideoLiveRoomBundle;

+ (UIImage *)RVLR_imageWithName:(NSString *)name type:(NSString *)type;

+ (UIImage *)RVLR_pngImageWithName:(NSString *)name;

+ (UIStoryboard *)RVLR_storyboard;

+ (NSString *)RVLR_musicPathForResource:(NSString *)name;

@end

NS_ASSUME_NONNULL_END
