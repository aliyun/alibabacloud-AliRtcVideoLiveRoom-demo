//
//  VideoRoomSeatsLoaderFactory.m
//  Pods
//
//  Created by Aliyun on 2020/7/22.
//

#import "VideoRoomFetcherFactory.h"
#import "VideoRoomFether.h"

@implementation VideoRoomFetcherFactory

+ (id<VideoRoomFetcherProtocal>)getFetcher:(NSString *)type
{
    if ([type isEqualToString:KVideoRoomFetherDefault])
    {
        return [[VideoRoomFether alloc]init];
    }
    
    return nil;
}
@end
