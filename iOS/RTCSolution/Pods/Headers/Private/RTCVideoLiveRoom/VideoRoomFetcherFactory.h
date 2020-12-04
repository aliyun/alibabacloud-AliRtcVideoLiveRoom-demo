//
//  VideoRoomSeatsLoaderFactory.h
//  Pods
//
//  Created by Aliyun on 2020/7/22.
//

#import <Foundation/Foundation.h>
#import "VideoRoomFetcherProtocal.h"
 
#define KVideoRoomFetherDefault @"VideoRoomFetherDefault"

NS_ASSUME_NONNULL_BEGIN

@interface VideoRoomFetcherFactory : NSObject
 

+ (id<VideoRoomFetcherProtocal>)getFetcher:(NSString *)type;



@end

NS_ASSUME_NONNULL_END
