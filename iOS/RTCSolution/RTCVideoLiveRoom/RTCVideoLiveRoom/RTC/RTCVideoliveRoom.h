//
//  RTCAudioliveRoomManager.h
//  LectureHall
//
//  Created by Aliyun on 2020/6/15.
//  Copyright © 2020 alibaba. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AliRTCSdk/AliRTCSdk.h>
#import <AVPLivePlayerHelper.h>

NS_ASSUME_NONNULL_BEGIN

@protocol RTCVideoliveRoomDelegate <NSObject>

- (void)onJoinChannelResult:(int)result
                   authInfo:(AliRtcAuthInfo *)authInfo;
 
 
- (void)onLeaveChannelResult:(int)result;

- (void)onNetworkQualityChanged:(NSString *)uid
               upNetworkQuality:(AliRtcNetworkQuality)upQuality
             downNetworkQuality:(AliRtcNetworkQuality)downQuality;

- (void)onOccurWarning:(int)warn;


- (void)onOccurError:(int)error;

- (void)onAudioPlayingStateChanged:(AliRtcAudioPlayingStateCode)playState
                         errorCode:(AliRtcAudioPlayingErrorCode)errorCode;
/// 远端用户上麦通知
/// @param userId 麦序
- (void)onEnterSeat:(NSString *)userId;

/// 远端用户下线通知
/// @param userId 麦序
- (void)onLeaveSeat:(NSString *)userId;

/// 房间被销毁通知
- (void)onRoomdestroy;

/// 被踢出房间
- (void)onkickedOut;

@end

@interface RTCVideoliveRoom : NSObject

@property (nonatomic, weak) id<RTCVideoliveRoomDelegate> delegate;

//MARK: - 美颜设置相关
/// 美白等级
@property (assign, nonatomic) NSInteger whiteLevel;
/// 磨皮等级
@property (assign, nonatomic) NSInteger smoothLevel;



//MARK: - 单例创建与销毁
//////////////////////////////////////////////////////////////
///
///        单例创建与销毁
///
//////////////////////////////////////////////////////////////
///
/// @brief 获取单例
/// @return RTCAudioliveRoomManager 单例对象
+ (RTCVideoliveRoom *) sharedInstance;

/// 销毁RTCSDK
- (void)destroySharedInstance;

//MARK: - 房间管理相关
//////////////////////////////////////////////////////////////
///
///        房间管理相关
///
//////////////////////////////////////////////////////////////

/// 加入频道
/// @param channelId   频道名称
/// @param userName   任意用于显示的用户名称。不是User ID
/// @param userId   角色
/// @param handler   回调
#warning 参数
- (void)createRoom:(NSString *)channelId
          userName:(NSString *)userName
            userId:(NSString *)userId
          complete:(void(^)(AliRtcAuthInfo *authInfo,
                            NSInteger errorCode))handler;

/// 主播调用 销毁房间
/// @param handler 回调
- (void)destroyRoom:(void(^)(NSInteger result))handler;;

/// 踢出房间的其他用户
/// @param handler 回调
- (void)kickout:(void(^)(NSInteger result))handler;

//MARK: - 观众连麦相关
//////////////////////////////////////////////////////////////
///
///        观众连麦相关
///
//////////////////////////////////////////////////////////////


/// 加入房间
/// @param channelId  频道
/// @param userId 观众id
/// @param userName 观众昵称
/// @param preview 预览view
/// @param handler 回调
- (void)joinRoom:(NSString *)channelId
          userId:(NSString *)userId
        userName:(NSString *)userName
         preview:(UIView *)preview
        complete:(void(^)(NSInteger result))handler;

/// 观众调用 退出直播
/// @param handler 回调
- (void)leaveRoom:(void(^)(NSInteger result))handler;

/// 上麦
/// @param handler 回调
- (void)enterSeat:(void(^)(NSInteger result))handler;

/// 下麦
/// @param handler 回调
- (void)leaveSeat:(void(^)(NSInteger result))handler;


//MARK: - 播放预览相关
//////////////////////////////////////////////////////////////
///
///        播放预览相关
///
//////////////////////////////////////////////////////////////

/// 开启本地预览
/// @param preview 预览View
- (void)startCameraPreView:(UIView *)preview;


/// 停止本地预览
- (void)stopCameraPreview;


/// 切换摄像头
- (void)switchCamera;

/// 播放远端画面
/// @param preview     预览的view
/// @param userId  对方的userId
- (void)startPlay:(UIView *)preview
           userId:(NSString *)userId;


//MARK: - 音视频控制相关
//////////////////////////////////////////////////////////////
///
///       音视频控制相关
///
//////////////////////////////////////////////////////////////


/// 是否开启耳返
/// @param enable YES/NO
- (int)enableEarBack:(BOOL)enable;

/// 播放背景音乐
/// @param filePath 文件路径
/// @param publish 是否推送远端
- (int)startAudioAccompanyWithFile:(NSString *)filePath
                           publish:(BOOL)publish;

/// 停止播放背景音乐
- (int)stopAudioAccompany;

/// 设置背景音乐音量
/// @param volume 音量 0~100
- (int)setAudioAccompanyVolume:(NSInteger)volume;

/// 播放音效
/// @param soundId 音效id
/// @param filePath 资源路径
/// @param publish 是否推送远端
- (int)playEffectSoundtWithSoundId:(NSInteger)soundId
                          filePath:(NSString *)filePath
                           publish:(BOOL)publish;

/// 停止播放音效
/// @param soundId 音效id
- (int)stopAudioEffectWithSoundId:(NSInteger)soundId;

/// 设置音效的音量
/// @param soundId 音效id
/// @param volume 音量 0~100
- (int)setAudioEffectVolumeWithSoundId:(NSInteger)soundId
                                volume:(NSInteger)volume;

/// 设置音效混响模式
/// @param mode 混响模式
- (int)setAudioEffectReverbMode:(AliRtcAudioEffectReverbMode)mode;





@end

NS_ASSUME_NONNULL_END
