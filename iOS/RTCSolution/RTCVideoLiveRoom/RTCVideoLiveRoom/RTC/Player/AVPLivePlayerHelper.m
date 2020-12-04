//
//  AVPLivePlayerDemo.m
//  AliPlayerDemo
//
//  Created by ToT on 2020/4/9.
//  Copyright © 2020 com.alibaba. All rights reserved.
//

#import "AVPLivePlayerHelper.h"
#import "Reachability.h" 
//用于后台播放h过程中，收到来电的处理
#import <CoreTelephony/CTCallCenter.h>
#import <CoreTelephony/CTCall.h>

typedef NS_ENUM(NSInteger, PlayerStatus) {
    PlayerStatusFree,
    PlayerStatusPlaying,
    PlayerStatusPause,
};

@interface AVPLivePlayerHelper()<AVPDelegate>

//@property (nonatomic,assign)BOOL isFLV;
@property (nonatomic,strong)Reachability *reachability;
@property (nonatomic,assign)NetworkStatus currentNetworkStatus;//当前网络状态
@property (nonatomic,assign)BOOL needPlayIntofore;//回到前台需要播放
@property (nonatomic,assign)PlayerStatus currentPlayerStatus;//当前播放器状体啊
@property (nonatomic,assign)NSInteger loadingCount;//loading超时已经尝试次数
@property (nonatomic,assign)NSInteger prepareCount;//prepare超时已经尝试次数
//@property (nonatomic,assign)BOOL saveError;
//@property (nonatomic,strong)AVPErrorModel *errorModel;
@property (nonatomic,assign)BOOL hasFirstRender;//是否回掉过FirstRender
@property (nonatomic,assign)BOOL isSoftwarePlayback;//是否收到过切换成软解回掉
@property (nonatomic,strong)CTCallCenter *callCenter;//来电中心
@property (nonatomic,assign)BOOL startByCall;//来电导致的中断需要重新播放

@end

@implementation AVPLivePlayerHelper

- (void)dealloc {
    [[NSNotificationCenter defaultCenter]removeObserver:self];
    NSLog(@"%s",__func__);
}

- (instancetype)init {
    self = [super init];
    if (self) {
        
        self.loadingTimeOut = 10;
        self.loadingRetryCount = 1;
        self.prepareTimeOut = 10;
        self.prepareRetryCount = 1;
        self.loadingCount = 0;
        self.prepareCount = 0;
        
        self.aliPlayer = [[AliPlayer alloc]init];
        self.aliPlayer.autoPlay = YES;
        self.aliPlayer.loop = YES;
        self.aliPlayer.delegate = self;
        self.aliPlayer.scalingMode = AVP_SCALINGMODE_SCALEASPECTFILL;
        //这里设置保留最后一帧
        AVPConfig *config = [self.aliPlayer getConfig];
        config.clearShowWhenStop = NO;
        config.maxDelayTime = 2000;
        [self.aliPlayer setConfig:config];
        
        // 添加检测app进入后台的观察者
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationEnterBackground) name: UIApplicationWillResignActiveNotification object:nil];
        // 添加检测app进入前台的观察者
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationDidBecomeActive) name: UIApplicationDidBecomeActiveNotification object:nil];
        // 监听网络变化
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(networkStateChange) name:kReachabilityChangedNotification object:nil];
        self.reachability = [Reachability reachabilityForInternetConnection];
        [self.reachability startNotifier];
        
        [self monitorTelephoneCall];
        
    }
    return self;
}

- (void)monitorTelephoneCall {
    __weak AVPLivePlayerHelper *weakSelf = self;
    self.callCenter = [[CTCallCenter alloc] init];
    self.callCenter.callEventHandler = ^(CTCall * call) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if ([call.callState isEqualToString:CTCallStateDisconnected]) {
                //挂断
                if (weakSelf.startByCall) {
                    weakSelf.startByCall = NO;
                    //这里不用判断网络，保持原来的状态
                    [weakSelf start];
                }
            }else {
                //其他状态，都需要打断播放
                if (weakSelf.currentPlayerStatus == PlayerStatusPlaying) {
                    [weakSelf privateStop];
                    weakSelf.startByCall = YES;
                }
            }
        });
    };
}

- (void)setSourceWithString:(NSString *)string {
//    [self changeIsFlv:string];
    AVPUrlSource *source = [[AVPUrlSource alloc]urlWithString:string];
    [self.aliPlayer setUrlSource:source];
    [self tryToStart];
}

- (void)changeSourceWithString:(NSString *)string {
    //切换资源播放，需要清除首帧状态
    [self stop];
    AVPUrlSource *source = [[AVPUrlSource alloc]urlWithString:string];
    [self.aliPlayer setUrlSource:source];
    [self tryToStart];
}

//尝试播放
- (void)tryToStart {
//    if ([self.reachability currentReachabilityStatus] == ReachableViaWWAN && !self.allowWWAN) {
//        if (self.delegate && [self.delegate respondsToSelector:@selector(onNetworkWWANWantToPlay:)]) {
//            [self.delegate onNetworkWWANWantToPlay:self];
//        }
//    }else {
        [self start];
//    }
}

//开始播放
- (void)start {
    if (self.currentPlayerStatus == PlayerStatusPause) {
        [self.aliPlayer start];
    }else {
        [self.aliPlayer prepare];
        [self performSelector:@selector(prepareTimeOutFunc) withObject:nil afterDelay:self.prepareTimeOut];
    }
    self.currentPlayerStatus = PlayerStatusPlaying;
}

- (void)pause {
    if (self.currentPlayerStatus == PlayerStatusPlaying) {
        self.currentPlayerStatus = PlayerStatusPause;
        [self.aliPlayer pause];
    }
}

//外部调用的stop，需要清除全部状态
- (void)stop {
    self.loadingCount = 0;
    self.prepareCount = 0;
    self.hasFirstRender = NO;
    self.isSoftwarePlayback = NO;
    [self privateStop];
}

//内部stop，不清除首帧状态
- (void)privateStop {
    self.currentPlayerStatus = PlayerStatusFree;
    self.needPlayIntofore= NO;
    [self.aliPlayer stop];
}

- (void)retry {
    self.loadingCount = 0;
    self.prepareCount = 0;
    [self tryToStart];
}

- (void)destroy
{
    if (_aliPlayer) {
        [_aliPlayer stop];
        [_aliPlayer destroy];
        _aliPlayer = nil;
    }
}

//进入后台
- (void)applicationEnterBackground {
    if (!self.supportBackGroundPlay && self.currentPlayerStatus == PlayerStatusPlaying) {
        [self privateStop];
        self.needPlayIntofore = YES;
    }
}

//进入前台
- (void)applicationDidBecomeActive {
    if ( self.needPlayIntofore == YES ) {
        self.needPlayIntofore = NO;
        [self tryToStart];
    }
}

//网络状态切换
- (void)networkStateChange {
    NetworkStatus status = [self.reachability currentReachabilityStatus];
    switch (status) {
        case NotReachable: {
                //无网停止播放
                [self privateStop];
        }
            break;
        case ReachableViaWiFi: {
                //切换网络停止播放
                [self privateStop];
                //wifi网络直接进行播放,不需要判断
                [self start];
        }
            break;
        case ReachableViaWWAN: {
                //切换网络停止播放
                [self privateStop];
                //移动网络尝试播放
                [self tryToStart];
        }
            break;
    }
    self.currentNetworkStatus = status;
}

//prepare超时
- (void)prepareTimeOutFunc {
    if (self.prepareRetryCount > self.prepareCount) {
        NSLog(@"%s",__func__);
        self.prepareCount ++;
        //重试不进行网络判断
        [self start];
    }else {
        if ([self.delegate respondsToSelector:@selector(onLivePlayRetryError )]) {
            [self.delegate onLivePlayRetryError];
        }
    }
}

//loading超时
- (void)loadingTimeOutFunc {
    if (self.loadingRetryCount > self.loadingCount) {
        NSLog(@"%s",__func__);
        self.loadingCount ++;
        //重试不进行网络判断
        [self start];
    }else {
        if ([self.delegate respondsToSelector:@selector(onLivePlayRetryError)]) {
            [self.delegate onLivePlayRetryError];
        }
    }
}

#pragma mark AVPDelegate

-(void)onPlayerEvent:(AliPlayer*)player eventWithString:(AVPEventWithString)eventWithString description:(NSString *)description {
    if (eventWithString == EVENT_SWITCH_TO_SOFTWARE_DECODER) {
        self.isSoftwarePlayback = YES;
    }
}

-(void)onPlayerEvent:(AliPlayer*)player eventType:(AVPEventType)eventType {
    switch (eventType) {
        case AVPEventFirstRenderedStart: {
            self.hasFirstRender = YES;
            self.prepareCount = 0;
            [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(prepareTimeOutFunc) object:nil];
        }
            break;
        case AVPEventLoadingStart: {
            [self performSelector:@selector(loadingTimeOutFunc) withObject:nil afterDelay:self.loadingTimeOut];
        }
            break;
        case AVPEventLoadingEnd: {
            self.loadingCount = 0;
            [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(loadingTimeOutFunc) object:nil];
        }
            break;
        default:
            break;
    }
    //内部重试，不回掉AVPEventFirstRenderedStart
    if (self.hasFirstRender && eventType == AVPEventFirstRenderedStart) {
        return;
    }
//    if (self.delegate && [self.delegate respondsToSelector:@selector(onPlayerEvent:eventType:)]) {
//        [self.delegate onPlayerEvent:self eventType:eventType];
//    }
}

- (void)onPlayerStatusChanged:(AliPlayer*)player oldStatus:(AVPStatus)oldStatus newStatus:(AVPStatus)newStatus {
    
}

- (void)onError:(AliPlayer*)player errorModel:(AVPErrorModel *)errorModel {
    self.currentPlayerStatus = PlayerStatusFree;

    if (self.delegate && [self.delegate respondsToSelector:@selector(onError:)]) {
        [self.delegate onError:errorModel];
    }
}

@end
