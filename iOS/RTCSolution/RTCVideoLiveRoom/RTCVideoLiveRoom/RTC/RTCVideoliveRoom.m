//
//  RTCAudioliveRoomManager.m
//  LectureHall
//
//  Created by Aliyun on 2020/6/15.
//  Copyright © 2020 alibaba. All rights reserved.
//

#import "RTCVideoliveRoom.h"
#import "VideoRoomFetcherFactory.h"
#import "AVPLivePlayerHelper.h"

static dispatch_once_t onceToken;
static RTCVideoliveRoom *manager = nil;

@interface RTCVideoliveRoom()<AliRtcEngineDelegate,AVPLivePlayerHelperDelegate>

/// 用户昵称
@property (copy, nonatomic) NSString *displayName;
/// 频道号
@property (copy, nonatomic) NSString *channelId;
/// 用户Id
@property (copy, nonatomic) NSString *myId;
/// RTC Engine
@property (strong, nonatomic) AliRtcEngine *engine;
/// 鉴权信息
@property (strong, nonatomic) AliRtcAuthInfo * info;
/// 网络请求类
@property (strong, nonatomic) id<VideoRoomFetcherProtocal> fetcher;
/// RTC频道里的用户
@property (strong, nonatomic) NSArray *onlineUsers;
/// 旁路播放器
@property (strong, nonatomic) AVPLivePlayerHelper *playerHelper;
/// 播放器渲染的view
@property (weak, nonatomic) UIView *playerView;
 

@property (copy,nonatomic) void (^leaveChannelHandler)(NSInteger result) ;

@end

@implementation RTCVideoliveRoom

#pragma mark - 单例声明周期相关
+ (RTCVideoliveRoom *) sharedInstance{
    dispatch_once(&onceToken, ^{
        manager = [[super allocWithZone:NULL] init];
    });
    return manager;
}

+ (instancetype)allocWithZone:(struct _NSZone *)zone {
    return [RTCVideoliveRoom sharedInstance];
}

- (id)copyWithZone:(nullable NSZone *)zone {
    return [RTCVideoliveRoom sharedInstance];
}

- (id)mutableCopyWithZone:(nullable NSZone *)zone {
    return [RTCVideoliveRoom sharedInstance];
}

- (instancetype)init {
    self = [super init];
    if (self) {
        self.smoothLevel = 5;
        self.whiteLevel = 5;
    }
    return self;
}

-(void)dealloc
{
    NSLog(@"RTCVideoRoom dealloc");
}

-(void)destroySharedInstance
{
    if(_engine){
        [AliRtcEngine destroy];
        _engine = nil;
    }
    if (_playerHelper) {
        [self destroyPlayer];
    }
    onceToken = 0;
    manager = nil;
}

#pragma mark - private functions

- (void)joinRTC:(NSString *)channelId
           name:(NSString *)name
         userId:(NSString *)userId
       isAnchor:(BOOL)isAnchor
       complete:(void(^)(AliRtcAuthInfo *authInfo,
                         NSInteger errorCode))handler
{
    self.myId = userId;
    self.displayName = name;
    self.channelId = channelId;
    [self.fetcher authInfo:@{@"channelId":channelId,@"userId":userId}
                  complete:^(AliRtcAuthInfo * _Nonnull info,
                             NSString * _Nonnull nickName,
                             NSString * _Nonnull errorMsg) {
        if (!errorMsg)
        {
            self.info = info;
            self.displayName = name;
            self.onlineUsers = nil;
            [self.engine joinChannel:info
                                name:name
                            onResult:^(NSInteger errCode)
             {
                if (errCode != 0)
                {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        handler(nil,errCode);
                    });
                    return;
                }
                dispatch_async(dispatch_get_main_queue(), ^{
                    handler(info,0);
                    [self refreshSeats];
                });
            }];
        } else {
            handler(nil,-1);
        }
    }];
}

// RTC离开频道
- (void)leaveChannel:(void(^)(NSInteger result))handler
{
    self.leaveChannelHandler = handler;
    [self stopCameraPreview];
    if ([self.engine isInCall]) {
        dispatch_async(dispatch_get_global_queue(0, 0), ^{
               [self.engine leaveChannel];
           });
    }else {
        handler(0);
    }
   
}

- (void)destroyPlayer
{
    if (_playerHelper) {
        _playerHelper.delegate = nil;
        _playerView.hidden = YES;
        [_playerHelper destroy];
        _playerHelper = nil;
    }
}


- (void)stopPlayer
{
    if (_playerHelper) {
        _playerView.hidden = YES;
        [_playerHelper stop];
    }
}

- (void)initializeSDK
{
    //高音质模式
    [AliRtcEngine setAudioProfile:AliRtcEngineHighQualityMode
                      audio_scene:AliRtcSeneMediaMode];

//    NSString *version = [AliRtcEngine getSdkVersion];
//
//    NSLog(@"sdk version:%@",version);
    
    // 创建SDK实例，注册delegate，extras可以为空
    _engine = [AliRtcEngine sharedInstance:self
                                    extras:nil];
    
    //使用扬声器
    [_engine enableSpeakerphone:YES];
    //频道模式
    [_engine setChannelProfile:AliRtcCommunication];
    //自动拉流 手动推流
    [_engine setAutoPublish:YES withAutoSubscribe:YES];
    //推流分辨率
    [_engine setVideoProfile:AliRtcVideoProfile_540_960P_15_1200Kb
                    forTrack:AliRtcVideoTrackCamera];
    //摄像头
    AliRtcCameraCapturerConfiguration *config = [[AliRtcCameraCapturerConfiguration alloc] init];
    config.preference = AliRtcCaptureOutputPreferencePreview;
    config.cameraDirection = AliRtcCameraDirectionFront;
    [_engine setCameraCapturerConfiguration:config];
    
    
    [self setBeautyEffect:_whiteLevel smoothLevel:_smoothLevel];
    
}

#pragma mark -主播房间相关操作

/// 主播创建房间
- (void)createRoom:(NSString *)channelId
          userName:(NSString *)userName
            userId:(NSString *)userId
          complete:(void(^)(AliRtcAuthInfo *authInfo,
                            NSInteger errorCode))handler
{
    [self joinRTC:channelId
             name:userName
           userId:userId
         isAnchor:YES
         complete:handler];
}

/// 主播调用销毁房间
- (void)destroyRoom:(void(^)(NSInteger result))handler
{
    [self leaveChannel:^(NSInteger result) {
        handler(result);
    }];
}

// 主播踢人
- (void)kickout:(void(^)(NSInteger result))handler
{
    [self.fetcher kickout:@{
        @"channelId":self.channelId,
        @"operatorId":self.myId
    }
                 complete:^(NSString *error)
     {
        if (error) {
            handler(-1);
        } else {
            handler(0);
        }
    }];
}


#pragma mark -用户房间相关操作

/// 观众离开房间
- (void)leaveRoom:(void(^)(NSInteger result))handler
{
    //如果当前在rtc频道中
    if (_engine && _engine.isInCall)
    {
        [self leaveChannel:^(NSInteger result) {
            handler(result);
        }];
    } else {
        [self destroyPlayer];
        handler(0);
    }
}

///  观众加入房间
- (void)joinRoom:(NSString *)channelId
          userId:(NSString *)userId
        userName:(NSString *)userName
         preview:(UIView *)preview
        complete:(void(^)(NSInteger result))handler
{
    //如果有播放器 则直接prepare
    
     __weak typeof(self) weakSelf = self;
    self.channelId = channelId;
    self.myId = userId;
    self.displayName = userName;
    self.playerView = preview;
    //获取播放地址
    [self.fetcher getplayUrl:@{@"channelId":channelId}
                    complete:^(NSDictionary *result, NSString *errorMsg)
     {
        if (!errorMsg)
        {
            NSString *url = result[@"rtmp"];
            NSLog(@"url:%@",url);
            [weakSelf.playerHelper setSourceWithString:url];
            weakSelf.playerHelper.aliPlayer.playerView = preview;
            preview.hidden = NO;
            handler(0);
        }else{
           handler(-1);
        }
    }];
}


#pragma mark - 上下麦的方法

/// 上麦只需要切换角色 成功的回调是 onUpdateRoleNotifyWithOldRole
- (void)enterSeat:(void(^)(NSInteger result))handler
{
    [self stopPlayer];
    
    [self joinRTC:self.channelId
             name:self.displayName
           userId:self.myId
         isAnchor:NO
         complete:^(AliRtcAuthInfo * _Nonnull authInfo,
                    NSInteger errorCode)
     {
        handler(errorCode);
    }];
}

- (void)leaveSeat:(void(^)(NSInteger result))handler
{
     __weak typeof(self) weakSelf = self;
    
    //有可能被踢出房间
    if(self.engine && self.engine.isInCall)
    {
        [self  leaveChannel:^(NSInteger result) {
            if (result == 0) {
                [weakSelf joinRoom:self.channelId
                            userId:self.myId
                          userName:self.displayName
                           preview:self.playerView
                          complete:^(NSInteger result)
                 {
                    
                    handler(result);
                }];
            }else{
                handler(-1);
            }
        }]; 
    } else {
        [self joinRoom:self.channelId
                userId:self.myId
              userName:self.displayName
               preview:self.playerView
              complete:^(NSInteger result){
            handler(result);
        }];
    }
}


#pragma mark -播放预览相关

/// 开始相机预览
/// @param preview 预览view
- (void)startCameraPreView:(UIView *)preview
{
    //创建renderView
    AliRenderView *renderView = nil;
    
    for (UIView *view in preview.subviews) {
        if ([view isKindOfClass:[AliRenderView class]]) {
            renderView = (AliRenderView *)view;
            break;
        }
    }
    
    if (!renderView) {
        renderView = [[AliRenderView alloc] init];
        [preview addSubview:renderView];
        [preview sendSubviewToBack:renderView];
    }
    
    renderView.frame = preview.bounds;
    
    AliVideoCanvas *canvas = [[AliVideoCanvas alloc] init];
    canvas.renderMode = AliRtcRenderModeAuto;
    canvas.view = renderView;
    [self.engine setLocalViewConfig:canvas
                           forTrack:AliRtcVideoTrackCamera];
    
    [self.engine startPreview];
}


/// 开始播放
/// @param preview 预览view
/// @param userId 用户id
- (void)startPlay:(UIView *)preview
           userId:(NSString *)userId
{
    //如果是自己 显示本地预览
    if([userId isEqualToString:self.myId])
    {
        [self startCameraPreView:preview];
        return;
    }
    //如果不是自己 显示远端画面
    for (UIView *view in preview.subviews) {
        [view removeFromSuperview];
    }
    
    AliRenderView *renderView  = [[AliRenderView alloc] init];
    [preview addSubview:renderView];
    [preview sendSubviewToBack:renderView];
    
    renderView.frame = preview.bounds;
    
    AliVideoCanvas *canvas = [[AliVideoCanvas alloc] init];
    canvas.renderMode = AliRtcRenderModeAuto;
    canvas.view = renderView;
    
    [self.engine setRemoteViewConfig:canvas
                                 uid:userId
                            forTrack:AliRtcVideoTrackCamera];
}

/// 停止相机预览
- (void)stopCameraPreview
{
    if (_engine)
    {
        [self.engine setLocalViewConfig:nil
                               forTrack:AliRtcVideoTrackCamera];
        [self.engine stopPreview];
    }
}

/// 切换摄像头
- (void)switchCamera
{
    [self.engine switchCamera];
}

#pragma mark -音频音效
- (int)startAudioAccompanyWithFile:(NSString *)filePath
                           publish:(BOOL)publish
{
    return [self.engine startAudioAccompanyWithFile:filePath
                                      onlyLocalPlay:!publish
                                         replaceMic:NO
                                         loopCycles:1];
}

- (int)setAudioAccompanyVolume:(NSInteger)volume
{
    [self.engine setAudioAccompanyPlayoutVolume:volume];
    return [self.engine setAudioAccompanyPublishVolume:volume];
}

- (int)stopAudioAccompany {
    return [self.engine stopAudioAccompany];
}

- (int)playEffectSoundtWithSoundId:(NSInteger)soundId
                          filePath:(NSString *)filePath
                           publish:(BOOL)publish
{
    [self.engine preloadAudioEffectWithSoundId:soundId
                                      filePath:filePath];
    
    return [self.engine playAudioEffectWithSoundId:soundId
                                          filePath:filePath
                                            cycles:1
                                           publish:publish];
}

- (int)stopAudioEffectWithSoundId:(NSInteger)soundId
{
    return [self.engine stopAudioEffectWithSoundId:soundId];
}

- (int)setAudioEffectVolumeWithSoundId:(NSInteger)soundId
                                volume:(NSInteger)volume
{
    [self.engine setAudioEffectPlayoutVolumeWithSoundId:soundId
                                                 volume:volume];
    return [self.engine setAudioEffectPublishVolumeWithSoundId:soundId
                                                        volume:volume];
}

- (int)enableEarBack:(BOOL)enable
{
    return [self.engine enableEarBack:enable];
}

- (int)setAudioEffectReverbMode:(AliRtcAudioEffectReverbMode)mode
{
    return [self.engine setAudioEffectReverbMode:mode];
}


#pragma mark - 美颜
- (void)setBeautyEffect:(NSInteger)whiteLevel smoothLevel:(NSInteger)smoothLevel
{
    if (whiteLevel > 10)
    {
        whiteLevel = 10;
    }
    if (whiteLevel < 0)
    {
        whiteLevel = 0;
    }
    if (smoothLevel > 10)
    {
        smoothLevel = 10;
    }
    if (smoothLevel < 0)
    {
        smoothLevel = 0;
    }
    
    AliRtcBeautyConfig *config = [[AliRtcBeautyConfig alloc] init];
    config.whiteningLevel = whiteLevel/10.0;
    config.smoothnessLevel = smoothLevel/10.0;
    _whiteLevel = whiteLevel;
    _smoothLevel = smoothLevel;
    if (_engine) {
        [_engine setBeautyEffect:YES config:config];
    }
}


- (void)setWhiteLevel:(NSInteger)whiteLevel {
    [self setBeautyEffect:whiteLevel smoothLevel:_smoothLevel];
}

- (void)setSmoothLevel:(NSInteger)smoothLevel {
    [self setBeautyEffect:_whiteLevel smoothLevel:smoothLevel];
}

- (AliRtcEngine *)engine {
    if (!_engine) {
        [self initializeSDK];
    }
    return _engine;
}


#pragma mark - AliRtcEngineDelegate

- (void)onBye:(int)code {
    if(code == 1) {
        if ([self.delegate respondsToSelector:@selector(onkickedOut)])
        {
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.delegate onkickedOut];
            });
        }
    } else if (code == 2) {
        if ([self.delegate respondsToSelector:@selector(onRoomdestroy)])
        {
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.delegate onRoomdestroy];
            });
        }
    }
}

- (void)onAudioPlayingStateChanged:(AliRtcAudioPlayingStateCode)playState
                         errorCode:(AliRtcAudioPlayingErrorCode)errorCode {
    if ([self.delegate respondsToSelector:@selector(onAudioPlayingStateChanged:errorCode:)])
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.delegate onAudioPlayingStateChanged:playState
                                            errorCode:errorCode];
        });
    }
}

- (void)onOccurError:(int)error
{
    if ([self.delegate respondsToSelector:@selector(onOccurError:)])
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.delegate onOccurError:error];
        });
    }
}

- (void)onOccurWarning:(int)warn
{
    if ([self.delegate respondsToSelector:@selector(onOccurWarning:)])
    {
        [self.delegate onOccurWarning:warn];
    }
}

- (void)onNetworkQualityChanged:(NSString *)uid
               upNetworkQuality:(AliRtcNetworkQuality)upQuality
             downNetworkQuality:(AliRtcNetworkQuality)downQuality
{
    if ([self.delegate respondsToSelector:@selector(onNetworkQualityChanged:upNetworkQuality:downNetworkQuality:)])
    {
        [self.delegate onNetworkQualityChanged:uid
                              upNetworkQuality:upQuality
                            downNetworkQuality:downQuality];
    }
}

#pragma mark - 麦序发生变化的代理

- (void)onLeaveChannelResult:(int)result
{
    dispatch_async(dispatch_get_main_queue(), ^{
        
        if (self.leaveChannelHandler)
        {
            self.leaveChannelHandler(result);
        }
        
        if ([self.delegate respondsToSelector:@selector(onLeaveChannelResult:)])
        {
            if (result == 0)
            {
                self.onlineUsers = nil;
            }
            [self.delegate onLeaveChannelResult:result];
        }
    });
}

- (void)onJoinChannelResult:(int)result authInfo:(AliRtcAuthInfo *)authInfo
{
    if ([self.delegate respondsToSelector:@selector(onJoinChannelResult:authInfo:)]) {
        dispatch_async(dispatch_get_main_queue(), ^{
            
            [self.delegate onJoinChannelResult:result
                                      authInfo:authInfo];
        });
    }
}

- (void)onRemoteUserOnLineNotify:(NSString *)uid
{
    [self refreshSeats];
}

- (void)onRemoteUserOffLineNotify:(NSString *)uid
{
    [self refreshSeats];
}


- (void)refreshSeats
{
    if([self.fetcher respondsToSelector:@selector(getUserList:complete:)])
    {
        [self.fetcher getUserList:@{@"channelId":self.info.channel}
                         complete:^(NSArray * _Nonnull users,
                                    NSString * _Nonnull error)
         {
            if([NSThread currentThread].isMainThread)
            {
                [self dealwithUsers:users];
            } else {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self dealwithUsers:users];
                    
                });
            }
        }];
    }
}
// 放到主线程中
- (void)dealwithUsers:(NSArray *)users
{
    if (!users) {
        return;
    }
    //下麦seats查找
    //原map中有 新数组中没有
    NSArray *oldSeats = self.onlineUsers;
    NSPredicate *offlinePredicate = [NSPredicate predicateWithFormat:@"NOT (SELF in %@)",users];
    NSArray *offlineSeats = [oldSeats filteredArrayUsingPredicate:offlinePredicate];
    
    //处理下麦user
    for (NSString *userId in offlineSeats)
    {
        //如果是已经上麦的用户
        if ([self.delegate respondsToSelector:@selector(onLeaveSeat:)])
        {
            [self.delegate onLeaveSeat:userId];
        }
    }
    
    //处理上麦user
    NSPredicate *onlinePredicate = [NSPredicate predicateWithFormat:@"NOT (SELF in %@)",oldSeats];
    NSArray *onlineSeats = [users filteredArrayUsingPredicate:onlinePredicate];
    
    for (NSString *userId in onlineSeats) {
        if ([self.delegate respondsToSelector:@selector(onEnterSeat:)]) {
            [self.delegate onEnterSeat:userId];
        }
    }
    
    self.onlineUsers = users;
}

- (void)renotifySeatsInfo {
    @synchronized ([RTCVideoliveRoom class]) {
        for (NSString *userId in self.onlineUsers) {
            if ([self.delegate respondsToSelector:@selector(onEnterSeat:)]) {
                [self.delegate onEnterSeat:userId];
            }
        }
    }
}
#pragma mark setter & getter

- (void)setDelegate:(id<RTCVideoliveRoomDelegate>)delegate {
    _delegate = delegate;
    if (delegate) {
        [self renotifySeatsInfo];
    }
}

- (NSInteger)getWhiteLevel
{
    return _whiteLevel;
}

- (NSInteger)getSmoothLevel{
    return _smoothLevel;
}

- (id<VideoRoomFetcherProtocal>)fetcher
{
#warning 修改 VideoRoomSeatsLoaderFactory 工厂类 创建自己的麦序类
    if (!_fetcher) {
        _fetcher = [VideoRoomFetcherFactory getFetcher:KVideoRoomFetherDefault];
    }
    NSAssert(_fetcher != nil, @"请初始化获取授权信息的对象");
    return _fetcher;
}



#pragma mark - player delegate

- (void)onLivePlayRetryError {
    [self destroyPlayer];
    dispatch_async(dispatch_get_main_queue(), ^{
        if([self.delegate respondsToSelector:@selector(onRoomdestroy)])
        {
            [self.delegate onRoomdestroy];
        }
    });
}

- (AVPLivePlayerHelper *)playerHelper
{
    if (!_playerHelper) {
        _playerHelper = [[AVPLivePlayerHelper alloc]init];
        _playerHelper.loadingRetryCount = 3;
        _playerHelper.prepareRetryCount = 3;
        _playerHelper.delegate = self;
    }
    return _playerHelper;
}

@end
