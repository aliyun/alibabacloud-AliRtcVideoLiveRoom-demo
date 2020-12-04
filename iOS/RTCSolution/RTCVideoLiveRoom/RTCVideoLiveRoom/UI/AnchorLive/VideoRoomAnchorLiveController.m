//
//  VideoRoomAnchorLiveController.m
//  AFNetworking
//
//  Created by aliyun on 2020/8/11.
//

#import "VideoRoomAnchorLiveController.h"
#import "RTCCommon.h"
#import "NSBundle+RTCVideoLiveRoom.h"
#import "RTCVideoliveRoom.h"
#import "VideoRoomBeautyPanelController.h"
#import "VideoRoomMixConsoleController.h"
#import "VideoRoomBackgroundMusic.h"
#import "VideoRoomSoundEffect.h"
#import "VideoRoomMixSetting.h"
#import "UIImageView+WebCache.h"
#import "VideoRoomApi.h"
#import "RTCCommonView.h"

@interface VideoRoomAnchorLiveController ()<RTCVideoliveRoomDelegate>
//主播预览View
@property (unsafe_unretained, nonatomic) IBOutlet UIView *anchorPreview;
//第一个观众预览view
@property (unsafe_unretained, nonatomic) IBOutlet UIView *firstAudiencePreview;
//第二个观众预览view
@property (unsafe_unretained, nonatomic) IBOutlet UIView *secondAudiencePreview;


// 存放anchorPreview , firstAudiencePreview , secondAudiencePreview
@property (strong,nonatomic) NSMutableArray *previews;
//房间头像
@property (unsafe_unretained, nonatomic) IBOutlet UIImageView *avatar;
//计时label
@property (unsafe_unretained, nonatomic) IBOutlet UILabel *timeLabel;
//频道名称
@property (unsafe_unretained, nonatomic) IBOutlet UILabel *channelName;
//退出按钮
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *exitButton;
//标题背景
@property (unsafe_unretained, nonatomic) IBOutlet UIImageView *titleBackground;
//美颜按钮
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *beautyBtn;
//翻转摄像头
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *flipBtn;
//调音台
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *mixBtn;
//挂断按钮
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *hungupBtn;
//背景音乐
@property (strong, nonatomic) VideoRoomBackgroundMusic *music;
//音效1
@property (strong, nonatomic) VideoRoomSoundEffect *laugh;
//音效2
@property (strong, nonatomic) VideoRoomSoundEffect *applause;
//混音
@property (strong, nonatomic) VideoRoomMixSetting *mixSetting;
//频道中的用户
@property (strong, nonatomic) NSMutableArray *onlineUsers;
//定时器
@property (strong, nonatomic) NSTimer *timer;
//通话时间
@property (assign, nonatomic) NSUInteger seconds;

@property (strong, nonatomic) VideoRoomMixConsoleController *mixConoleVC;

@property (assign, nonatomic) BOOL hasDestroy;

@end

@implementation VideoRoomAnchorLiveController

#pragma mark - life cycle
- (instancetype)init
{
    UIStoryboard *storyboard = [NSBundle RVLR_storyboard];
    return [storyboard instantiateViewControllerWithIdentifier:@"VideoRoomAnchorLiveController"];
}

- (void)dealloc
{
    NSLog(@"VideoRoomAnchorLiveController dealloc");
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self setupUI];
    [RTCVideoliveRoom sharedInstance].delegate = self;
    [self startTimer];
}

- (BOOL)shouldAutorotate
{
    return NO;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations
{
    return  UIInterfaceOrientationMaskPortrait;
}

- (UIStatusBarStyle) preferredStatusBarStyle
{
    return UIStatusBarStyleLightContent;
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES];
    [[RTCVideoliveRoom sharedInstance] startCameraPreView:self.anchorPreview];
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
}
- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [self.navigationController setNavigationBarHidden:NO];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}
#pragma mark - UI操作

- (void)setupUI
{
    self.channelName.text = self.name;
    self.avatar.layer.cornerRadius = 20;
    
    [self.avatar sd_setImageWithURL:[NSURL URLWithString:self.coverURL]
                   placeholderImage:[NSBundle RVLR_pngImageWithName:@"1"]];
    
    self.titleBackground.image = [NSBundle RVLR_pngImageWithName:@"rectangle"];
    
    [self.exitButton setImage:[NSBundle RVLR_pngImageWithName:@"close"]
                     forState:UIControlStateNormal];
    [self.exitButton setImage:[NSBundle RVLR_pngImageWithName:@"close"]
                     forState:UIControlStateHighlighted];
    [self.beautyBtn setImage:[NSBundle RVLR_pngImageWithName:@"beauty_selected"]
                    forState:UIControlStateNormal];
    [self.flipBtn setImage:[NSBundle RVLR_pngImageWithName:@"camera_flip_selected"]
                  forState:UIControlStateNormal];
    [self.mixBtn setImage:[NSBundle RVLR_pngImageWithName:@"mix_panel"]
                 forState:UIControlStateNormal];
    [self.hungupBtn setImage:[NSBundle RVLR_pngImageWithName:@"hung_up"]
                    forState:UIControlStateNormal];
}

- (void)updatePreviewFrames
{
    NSInteger userCount = self.onlineUsers.count;
    self.hungupBtn.hidden = userCount <= 1;
    if (userCount == 1) {
        self.anchorPreview = self.previews.firstObject;
        self.anchorPreview.frame = self.view.bounds;
      
        
    } else if (userCount == 2) {
        self.firstAudiencePreview = self.previews[1];
        CGFloat w_h_ratio = 0.5622;
        CGFloat width = RTCScreenWith * 0.5;
        CGFloat height = width/w_h_ratio;
      
        self.anchorPreview.bounds = CGRectMake(0, 0, width, height);
        self.anchorPreview.center = CGPointMake(width * 0.5, RTCScreenHeight * 0.5);
        self.firstAudiencePreview.bounds = CGRectMake(0, 0, width, height);
        self.firstAudiencePreview.center = CGPointMake(width * 1.5, RTCScreenHeight * 0.5);
    } else if (userCount == 3){
        self.secondAudiencePreview = self.previews[2];
        CGFloat w_h_ratio = 0.84;
        CGFloat width = RTCScreenWith * 0.5;
        CGFloat height = width/w_h_ratio;
        CGFloat anchorPreview_y = 100;
        CGFloat anchorPreview_x = width * 0.5;
        CGFloat audience_y = anchorPreview_y + height;
       
        self.anchorPreview.frame = CGRectMake(anchorPreview_x, anchorPreview_y, width, height);
        self.firstAudiencePreview.frame = CGRectMake(0, audience_y, width, height);
        self.secondAudiencePreview.frame = CGRectMake(width, audience_y, width, height);
    }
    
    //控制视图的显示 隐藏 
    for (int i = 0; i < self.previews.count; i++) {
        UIView *preview = self.previews[i];
        if (i<=userCount -1) {
            preview.hidden = NO;
            //调整子view大小
            for (UIView *view in preview.subviews) {
                if([view isKindOfClass:[AliRenderView class]]) {
                    view.frame = preview.bounds;
                }
                break;
            }
        }else {
            preview.hidden = YES;
        }
    }
}

#pragma mark - RTCVideoRoomDelegate

/// 用户上线
/// @param userId 用户id
- (void)onEnterSeat:(NSString *)userId
{
    //如果在线人数超过了3 则不处理
    if(self.onlineUsers.count >=3){
        return;
    }
    [self.onlineUsers addObject:userId];
    
    NSInteger count = self.onlineUsers.count;
    
    [self updatePreviewFrames];
    
    [[RTCVideoliveRoom sharedInstance] startPlay:self.previews[count -1] userId:userId];

    [self updateMpuLayout];
}

/// 用户下线
/// @param userId 用户id
- (void)onLeaveSeat:(NSString *)userId
{
    //思路，如果是中间的用户离开 则把view移动到最后
    NSUInteger index = [self.onlineUsers indexOfObject:userId];
    if (index != self.onlineUsers.count -1) {
        //exchange 2 & 3
//        UIView *tempAudience1 = self.firstAudiencePreview;
//        self.firstAudiencePreview = self.secondAudiencePreview;
//        self.secondAudiencePreview = tempAudience1;
        
        UIView *view = [self.previews objectAtIndex:index];
        [self.previews removeObject:view];
        [self.previews addObject:view];
    }
    
    [self.onlineUsers removeObject:userId];
    [self updatePreviewFrames];
    [self updateMpuLayout];
    
}

//房间被销毁了 体验结束
- (void)onRoomdestroy
{
    if (self.hasDestroy) {
        return;
    }
    self.hasDestroy = YES;
    [[RTCVideoliveRoom sharedInstance] destroySharedInstance];
    [self destroyTimer];
    [self timeoutAlert];
    
}

- (void)onkickedOut{}

- (void)onOccurError:(int)error
{
    //连接超时
    if(error == AliRtcErrIceConnectionReconnectFail)
    {
        [[RTCVideoliveRoom sharedInstance] destroySharedInstance];
        return;
    }
    
    NSString *errMsg = @"";
    if (error == AliRtcErrSdkInvalidState) {
        errMsg = @"sdk 状态错误";
    }else if (error == AliRtcErrIceConnectionHeartbeatTimeout) {
        errMsg = @"信令心跳超时";
    }else if (error == AliRtcErrSessionRemoved) {
        errMsg = @"Session 已经被移除，Session 不存在";
    }
    //发生以上错误的时候 需要销毁manager
    if (errMsg.length) {
        [[RTCVideoliveRoom sharedInstance] destroySharedInstance];
        [self showSdkError:errMsg];
        return;
    }
    errMsg = [NSString stringWithFormat:@"错误码:%d",error];
    [RTCHUD showHud:errMsg inView:self.view];
}


- (void)showSdkError:(NSString *)errorMsg
{
    RTCSingleActionAlertController *vc = [[RTCSingleActionAlertController alloc] initWithTitle:@"错误提示"
                                                                                       message:errorMsg
                                                                                   actionTitle:@"返回首页"
                                                                                        action:^{
        [[RTCVideoliveRoom sharedInstance] destroySharedInstance];
        [self.navigationController popToRootViewControllerAnimated:YES];
    }];
    [self showVC:vc];
//    [self presentViewController:vc animated:YES completion:nil];
}

- (void)onAudioPlayingStateChanged:(AliRtcAudioPlayingStateCode)playState errorCode:(AliRtcAudioPlayingErrorCode)errorCode
{
    if (playState == AliRtcAudioPlayingEnded)
    {
        self.music.publishing = NO;
        self.music.testing = NO;
        
        [self.mixConoleVC reloadDataAtIndex:0];
    }
}


#pragma mark - button actions

/// 调整旁路布局
- (void)updateMpuLayout
{
    [VideoRoomApi updateMPULayout:self.channelId
                         complete:^(NSString * _Nonnull error) {
        
    }];
}


/// 离开频道
- (IBAction)exit:(id)sender
{
    RTCDoubleActionsAlertController *vc = [[RTCDoubleActionsAlertController alloc]
                                           initWithTitle:@"退出直播"
                                           message:@"您的本次体验时长未满可继续体验"
                                           leftActionTitle:@"确认退出"
                                           leftAction:^{
        [self leave:^(NSInteger errorCode) {
            if (errorCode == 0)
            {
                [self showChannelInfo:self.seconds];
            }
        }];
    }
                                           rightActionTitle:@"继续体验"
                                           rightAction:^{}];
    [self showVC:vc];
//    [self presentViewController:vc animated:YES completion:nil];
}

/// 美颜
- (IBAction)beautyCliked:(id)sender
{
    VideoRoomBeautyPanelController *vc = [[VideoRoomBeautyPanelController alloc] init];
    vc.modalPresentationStyle = UIModalPresentationOverFullScreen;
    [self showVC:vc];
//    [self presentViewController:vc animated:YES completion:nil];
}
/// 翻转摄像头
- (IBAction)flipClicked:(id)sender
{
    [[RTCVideoliveRoom sharedInstance] switchCamera];
}
/// 混音
- (IBAction)mixClicked:(id)sender
{
    VideoRoomMixConsoleController *vc = [[VideoRoomMixConsoleController alloc] initWithMusic:self.music
                                                                                 effectLaugh:self.laugh
                                                                              effectApplause:self.applause
                                                                                  mixSetting:self.mixSetting];
    self.mixConoleVC = vc;
    [self showVC:vc];
//    [self presentViewController:vc animated:YES completion:nil];
}
/// 挂断其他用户
- (IBAction)hungupClicked:(id)sender
{
    
    RTCDoubleActionsAlertController *vc = [[RTCDoubleActionsAlertController alloc]
                                           initWithTitle:@"断开连麦"
                                           message:@"关闭连麦后将断开全部连麦观众"
                                           leftActionTitle:@"确认断开"
                                           leftAction:^{
        [[RTCVideoliveRoom sharedInstance] kickout:^(NSInteger result) {
            NSLog(@"%ld",result);
        }];
    }
                                           rightActionTitle:@"继续连麦"
                                           rightAction:^{}];
    [self showVC:vc];
//    [self presentViewController:vc animated:YES completiosn:nil];
}

/// 离开房间
/// @param handler 结果
- (void)leave:(void(^)(NSInteger result))handler;
{
    [self destroyTimer];
    [[RTCVideoliveRoom sharedInstance] destroyRoom:^(NSInteger result) {
        if (result == 0) {
            [VideoRoomApi stopMPUTask:self.channelId
                             complete:^(NSString * _Nonnull error)
             {
                if (!error) {
                    handler(0);
                } else {
                    handler(-1);
                }
            }];
        }else{
            handler(-1);
        }
    }];
   
}
/// 展示频道通话时长
- (void)showChannelInfo:(NSInteger)duration
{
    NSString *message = [NSString stringWithFormat:@"直播时长  %02d:%02d:%02d",duration/3600,duration/60,duration%60];
    RTCSingleActionAlertController *vc = [[RTCSingleActionAlertController alloc] initWithTitle:@"直播结束"
                                                                                       message:message
                                                                                   actionTitle:@"返回首页"
                                                                                        action:^{
        [[RTCVideoliveRoom sharedInstance] destroySharedInstance];
        [self.navigationController popToRootViewControllerAnimated:YES];
    }];
    [self showVC:vc];
//    [self presentViewController:vc animated:YES completion:nil];
}
// 超时提示
- (void)timeoutAlert
{
    RTCSingleActionAlertController *vc = [[RTCSingleActionAlertController alloc] initWithTitle:@"体验时间已到" message:@"您的本次体验时长已满如需再次体验，请重新创建通话" actionTitle:@"我知道了" action:^{
//        [self.navigationController popViewControllerAnimated:YES];
        [self back];
    }];
    [self showVC:vc];
//    [self presentViewController:vc animated:YES completion:nil];
}

#pragma mark -timer
- (void)startTimer {
    self.timer = [NSTimer scheduledTimerWithTimeInterval:1
                                                  target:self
                                                selector:@selector(count:)
                                                userInfo:nil
                                                 repeats:YES];
    [self.timer fire];
}

- (void)count:(NSTimer *) timer
{
    self.seconds++;
    self.timeLabel.text = [NSString stringWithFormat:@"%02d:%02d:%02d",
                                                        self.seconds/3600,
                                                        self.seconds/60,
                                                        self.seconds%60];
    
    
//    if(self.seconds == 600) {
//        [self leave:^(NSInteger result) {
//            [self onRoomdestroy];
//        }];
//        
//    }
}

- (void)destroyTimer
{
    [self.timer invalidate];
    self.timer = nil;
}

#pragma mark - setter & getter

- (VideoRoomBackgroundMusic *)music
{
    if (!_music) {
        _music = [[VideoRoomBackgroundMusic alloc] init];
    }
    return _music;
}


- (VideoRoomSoundEffect *)laugh
{
    if (!_laugh)
    {
        _laugh = [[VideoRoomSoundEffect alloc] initWithEffectId:1000
                                                       fileName:@"smile.mp3"];
    }
    return _laugh;
}

- (VideoRoomSoundEffect *)applause
{
    if (!_applause) {
        _applause = [[VideoRoomSoundEffect alloc] initWithEffectId:2000
                                                          fileName:@"Clapping.mp3"];
    }
    return _applause;
}


- (VideoRoomMixSetting *)mixSetting
{
    if (!_mixSetting) {
        _mixSetting = [[VideoRoomMixSetting alloc] init];
    }
    return _mixSetting;
}

- (NSMutableArray *)onlineUsers
{
    if (_onlineUsers == nil) {
        _onlineUsers = @[].mutableCopy;
    }
    return _onlineUsers;
}


- (NSMutableArray *)previews {
    if (!_previews) {
        _previews = [@[] mutableCopy];
        [_previews addObject:self.anchorPreview];
        [_previews addObject:self.firstAudiencePreview];
        [_previews addObject:self.secondAudiencePreview];
    }
    return _previews;
}

- (void)back {
    [[RTCVideoliveRoom sharedInstance] destroySharedInstance];
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)showVC:(UIViewController *)VC {
    [self dismissViewControllerAnimated:NO completion:nil];
    [self presentViewController:VC animated:YES completion:nil];
}
@end
