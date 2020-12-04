//
//  NSBundle+RTCVideoLiveRoom.m
//  Pods
//
//  Created by aliyun on 2020/8/6.
//

#import "NSBundle+RTCVideoLiveRoom.h"
#import "VideoRoomChannelListController.h"

#import "NSBundle+RTCCommonView.h"
//#import "RTCCommonView.h"

@implementation NSBundle (RTCVideoLiveRoom)

+ (instancetype)RTCVideoLiveRoomBundle
{
    static NSBundle *bundel = nil;
    if (bundel == nil) {
        NSString *bundlePath = [[NSBundle bundleForClass:[VideoRoomChannelListController class]] pathForResource:@"RTCVideoLiveRoom" ofType:@"bundle"];
        bundel = [NSBundle bundleWithPath:bundlePath];
    }
    return bundel;
}

+ (UIImage *)RVLR_imageWithName:(NSString *)name type:(NSString *)type
{
    int scale = [[UIScreen mainScreen] scale] <= 2 ? 2 : 3;
    NSString *fullName = [NSString stringWithFormat:@"%@@%dx",name,scale];
    NSString *path =  [[NSBundle RTCVideoLiveRoomBundle] pathForResource:fullName ofType:type];
    UIImage *image = [UIImage imageNamed:path];
    //如果不存在 则直接加载name.type
    if (!image) {
        path =  [[NSBundle RTCCommonViewBundle] pathForResource:name ofType:type];
        image = [UIImage imageNamed:path];
    }
    return image;
}

+ (UIImage *)RVLR_pngImageWithName:(NSString *)name
{
    UIImage *image = [NSBundle  RVLR_imageWithName:name type:@"png"];
    //从commonView中查找
    if (!image) {
        image = [NSBundle RCV_pngImageWithName:name];
    }
    return image;
}

+ (UIStoryboard *)RVLR_storyboard
{
    return [UIStoryboard storyboardWithName:@"RTCVideoLiveRoom"
                                     bundle:[NSBundle bundleForClass:[VideoRoomChannelListController class]]];
}

+ (NSString *)RVLR_musicPathForResource:(NSString *)name {
    return [NSBundle RCV_pathForResource:name];
}

@end
