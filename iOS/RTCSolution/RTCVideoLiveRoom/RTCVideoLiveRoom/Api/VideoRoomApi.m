//
//  AudioRoomLoginApi.m
//  RTCAudioLiveRoom
//
//  Created by Aliyun on 2020/7/7.
//

#import "VideoRoomApi.h"
#import "AppConfig.h"
#import "NetworkManager.h"
#import "MJExtension.h"


@implementation VideoRoomApi


+ (void)channelList:(NSString *)lastChannelId
           complete:(void(^)(NSArray *channels,NSString *error))handler
{
    NSString *url = [kBaseUrl_VideoRoom stringByAppendingString:@"getChannelList"];
    
    NSMutableDictionary *param = @{}.mutableCopy;
    [param setObject:@(10) forKey:@"pageSize"];
    if (lastChannelId) {
        [param setObject:lastChannelId forKey:@"lastChannelId"];
    }
    
    [NetworkManager GET:url
             parameters:param
      completionHandler:^(NSString * _Nullable errString, id  _Nullable result)
     {
        if (errString) {
            handler(nil,errString);
        }else{
            NSArray *channels = [VideoRoomChannel mj_objectArrayWithKeyValuesArray:result];
            handler(channels,nil);
        }
    }];
}

+ (void)randomCoverUrl:(void(^)(NSString *coverUrl,NSString *error))handler
{
    NSString *url = [kBaseUrl_VideoRoom stringByAppendingString:@"randomCoverUrl"];
    
    NSMutableDictionary *param = @{}.mutableCopy;
    
    [NetworkManager GET:url
             parameters:param
      completionHandler:^(NSString * _Nullable errString, id  _Nullable result)
     {
        if (errString) {
            handler(nil,errString);
        }else{
            NSString *coverUrl = result;
            handler(coverUrl,nil);
        }
    }];
}

+ (void)startMPUTask:(NSString *)channelId
              userId:(NSString *)userId
            coverUrl:(NSString *)coverUrl
               title:(NSString *)title
            complete:(void(^)(NSString *error))handler
{
    NSString *url = [kBaseUrl_VideoRoom stringByAppendingString:@"startMPUTask"];
    
    NSMutableDictionary *param = @{}.mutableCopy;
    if (channelId) {
        [param setObject:channelId forKey:@"channelId"];
    }
    if (userId) {
        [param setObject:userId forKey:@"userId"];
    }
    if (coverUrl) {
        [param setObject:coverUrl forKey:@"coverUrl"];
    }
    if (title) {
        [param setObject:title forKey:@"title"];
    }
    
    [NetworkManager POST:url
              parameters:param
       completionHandler:^(NSString * _Nullable errString, id  _Nullable result)
     {
        handler(errString);
    }];
}



+ (void)updateMPULayout:(NSString *)channelId
               complete:(void(^)(NSString *error))handler
{
    
    NSString *url = [kBaseUrl_VideoRoom stringByAppendingString:@"updateMPULayout"];
    
    NSMutableDictionary *param = @{}.mutableCopy;
    if (channelId) {
        [param setObject:channelId forKey:@"channelId"];
    }
    
    [NetworkManager POST:url
              parameters:param
       completionHandler:^(NSString * _Nullable errString, id  _Nullable result)
     {
        handler(errString);
    }];
}


+ (void)getUserList:(NSString *)channelId
           complete:(void(^)(NSArray *userList,NSString *error))handler
{
    NSString *url = [kBaseUrl_VideoRoom stringByAppendingString:@"getUserList"];
    NSMutableDictionary *params = @{}.mutableCopy;
    if (channelId) {
        [params setObject:channelId forKey:@"channelId"];
    }
    [NetworkManager GET:url
             parameters:params
      completionHandler:^(NSString * _Nullable errString, id  _Nullable result)
     {
        if (errString)
        {
            handler(nil,errString);
        } else {
            if ([result isKindOfClass:[NSArray class]])
            {
                NSArray *array = [NSString mj_objectArrayWithKeyValuesArray:result];
                NSLog(@"%@",array);
                handler(array,nil);
            } else {
                handler(nil,@"没有获取到人数");
            }
        }
    }];
}

+ (void)channelInfo:(NSString *)channel
           complete:(void(^)(NSDictionary *info,NSString *error))handler
{
    NSString *url = [kBaseUrl_VideoRoom stringByAppendingString:@"describeRtcChannelMetric"];
    NSMutableDictionary *params = @{}.mutableCopy;
    if (channel) {
        [params setObject:channel forKey:@"channelId"];
    }
    [NetworkManager GET:url
             parameters:params
      completionHandler:^(NSString * _Nullable errString, id  _Nullable result)
     {
        if (errString)
        {
            handler(nil,errString);
        } else {
            
            handler(result,nil);
        }
    }];
}

+ (void)stopMPUTask:(NSString *)channelId
           complete:(void(^)(NSString *error))handler
{
    NSString *url = [kBaseUrl_VideoRoom stringByAppendingString:@"stopMPUTask"];
    
    NSMutableDictionary *param = @{}.mutableCopy;
    if (channelId) {
        [param setObject:channelId forKey:@"channelId"];
    }
    
    [NetworkManager POST:url
              parameters:param
       completionHandler:^(NSString * _Nullable errString, id  _Nullable result)
     {
        handler(errString);
    }];
}

@end
