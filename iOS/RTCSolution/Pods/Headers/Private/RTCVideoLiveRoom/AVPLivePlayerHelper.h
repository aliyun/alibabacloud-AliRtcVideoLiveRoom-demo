//
//  AVPLivePlayerHelper.h
//  AliPlayerHelper
//
//  Created by ToT on 2020/4/9.
//  Copyright © 2020 com.alibaba. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AliyunPlayer/AliyunPlayer.h>

@class AVPLivePlayerHelper;
@protocol AVPLivePlayerHelperDelegate <NSObject>

@optional
 

/// 播放失败的回调
/// @param playerHelper
/// @param errorModel 错误
- (void)onError:(AVPErrorModel *)errorModel;

/// 播放器拉不到流后 重试3次还是拉不到数据时的回调
- (void)onLivePlayRetryError;



@end


@interface AVPLivePlayerHelper : NSObject

@property (nonatomic, weak) id<AVPLivePlayerHelperDelegate> delegate;
@property (nonatomic,assign)BOOL supportBackGroundPlay;//支持后台播放
@property (nonatomic,assign)BOOL allowWWAN;//移动数据自动播放
@property (nonatomic,strong)AliPlayer* aliPlayer;
@property (nonatomic,assign)NSInteger loadingTimeOut;//loading超时时间
@property (nonatomic,assign)NSInteger loadingRetryCount;//loading超时重试次数
@property (nonatomic,assign)NSInteger prepareRetryCount;//prepare超时重试次数
@property (nonatomic,assign)NSInteger prepareTimeOut;//prepare超时时间

//设置源播放
- (void)setSourceWithString:(NSString *)string;

//切换源播放
- (void)changeSourceWithString:(NSString *)string;

- (void)start;

- (void)pause;

- (void)stop;

- (void)retry;

- (void)destroy;

@end


