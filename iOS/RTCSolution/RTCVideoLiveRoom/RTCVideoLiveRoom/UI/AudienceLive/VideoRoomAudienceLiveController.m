//
//  VideoRoomAudienceLiveController.m
//  RTCVideoLiveRoom
//
//  Created by aliyun on 2020/8/14.
//

#import "VideoRoomAudienceLiveController.h"
#import "NSBundle+RTCVideoLiveRoom.h"
#import "RTCCommon.h"
#import "UIImageView+WebCache.h"
#import "RTCVideoliveRoom.h"
#import "VideoRoomApi.h"
#import "RTCCommonView.h"

@interface VideoRoomAudienceLiveController ()<RTCVideoliveRoomDelegate>
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
//房间名称
@property (unsafe_unretained, nonatomic) IBOutlet UILabel *channelName;
//退出按钮
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *exitButton;
@property (unsafe_unretained, nonatomic) IBOutlet UIImageView *titleBackground;
//连麦按钮
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *connectBtn;
//翻转摄像头按钮
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *flipBtn;
//旁路预览view
@property (weak, nonatomic)  UIView *playerView;
//用户id
@property (copy, nonatomic) NSString *myId;
//用户昵称
@property (copy, nonatomic) NSString *displayName;
//用户进入RTC频道后的在线用户数组
@property (strong, nonatomic) NSMutableArray *onlineUsers;
//是否在RTC频道中
@property (assign, nonatomic) BOOL isInRTC;

@end

@implementation VideoRoomAudienceLiveController

#pragma mark - life cycle

- (instancetype)init
{
    UIStoryboard *storyboard = [NSBundle RVLR_storyboard];
    return [storyboard instantiateViewControllerWithIdentifier:@"VideoRoomAudienceLiveController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setupUI];
    [RTCVideoliveRoom sharedInstance].delegate = self;
    
    //  加入房间处理
    [[RTCVideoliveRoom sharedInstance] joinRoom:self.channelId
                                         userId:self.myId
                                        userName:self.displayName
                                        preview:self.playerView
                                       complete:^(NSInteger result){
        if (result!= 0) {
            [RTCHUD showHud:@"加入房间失败" inView:self.view];
        }
    }];
    
}

- (void)dealloc
{
    [[RTCVideoliveRoom sharedInstance] stopCameraPreview];
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
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [self.navigationController setNavigationBarHidden:NO];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}
#pragma mark - UI 操作
/// 初始化视图
- (void)setupUI
{
    UIView *playerView = [[UIView alloc] initWithFrame:self.view.bounds];
    [self.view addSubview:playerView];
    [self.view sendSubviewToBack:playerView];
    self.playerView = playerView;
    
    self.channelName.text = self.name;
    self.avatar.layer.cornerRadius = 20;
    [self.avatar sd_setImageWithURL:[NSURL URLWithString:self.coverURL]
                   placeholderImage:[NSBundle RVLR_pngImageWithName:@"1"]];
    self.titleBackground.image = [NSBundle RVLR_pngImageWithName:@"rectangle"];
    
    [self.exitButton setImage:[NSBundle RVLR_pngImageWithName:@"close"]
                     forState:UIControlStateNormal];
    [self.exitButton setImage:[NSBundle RVLR_pngImageWithName:@"close"]
                     forState:UIControlStateHighlighted];
    
    [self.connectBtn setImage:[NSBundle RVLR_pngImageWithName:@"connect"]
                     forState:UIControlStateNormal];
    [self.flipBtn setImage:[NSBundle RVLR_pngImageWithName:@"camera_flip_selected"]
                  forState:UIControlStateNormal];
    
    self.anchorPreview.hidden = YES;
    self.firstAudiencePreview.hidden = YES;
    self.secondAudiencePreview.hidden = YES;
    
}

/// 根据房间的人数 调整视图位置
- (void)updatePreviewFrames
{
    NSInteger userCount = self.onlineUsers.count;
    
    self.flipBtn.hidden = !userCount;
    
    if (userCount == 1) {
        self.anchorPreview = self.previews.firstObject;
        self.anchorPreview.frame = self.view.bounds;
        self.flipBtn.hidden = NO;
    } else if (userCount == 2) {
        self.anchorPreview = self.previews[0];
        self.firstAudiencePreview = self.previews[1];
        
        CGFloat w_h_ratio = 0.5622;
        CGFloat width = RTCScreenWith * 0.5;
        CGFloat height = width/w_h_ratio;
       
        
        self.anchorPreview.bounds = CGRectMake(0, 0, width, height);
        self.anchorPreview.center = CGPointMake(width * 0.5, RTCScreenHeight * 0.5);
        
        self.firstAudiencePreview.bounds = CGRectMake(0, 0, width, height);
        self.firstAudiencePreview.center = CGPointMake(width * 1.5, RTCScreenHeight * 0.5);
       
        self.flipBtn.hidden = NO;
        
    } else if (userCount == 3) {
        self.anchorPreview = self.previews[0];
        self.firstAudiencePreview = self.previews[1];
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
       

        self.flipBtn.hidden = NO;
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

/// 当前用户再RTC频道中 收到的用户上线通知
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
    
    
}
/// 当前用户再RTC频道中 收到的用户下线通知
/// @param userId 用户id
- (void)onLeaveSeat:(NSString *)userId
{
    //主播离开
    if([userId isEqualToString:self.anchorId])
    {
        [self onRoomdestroy];
        return;
    }
    
    
    NSUInteger index = [self.onlineUsers indexOfObject:userId];

    //思路，如果是中间的用户离开 则把view移动到最后
    if (index != self.onlineUsers.count -1) {
        UIView *view = [self.previews objectAtIndex:index];
        [self.previews removeObject:view];
        [self.previews addObject:view];
    }
    
    [self.onlineUsers removeObject:userId];
    
    [self updatePreviewFrames];
}

/// 当前用户被踢出房间
- (void)onkickedOut
{
    [RTCHUD showHud:@"主播已经断开连麦" inView:self.view];
    [[RTCVideoliveRoom sharedInstance] leaveSeat:^(NSInteger result) {
        if (result != 0) {
            //错误处理
            [RTCHUD showHud:@"加入房间失败" inView:self.view];
        } else {
            [self.onlineUsers removeAllObjects];
            [self updatePreviewFrames];
            self.connectBtn.selected = NO;
            self.isInRTC = NO;
        }
    }];
    
}

//房间被销毁了 体验结束
- (void)onRoomdestroy
{
    RTCSingleActionAlertController *vc = [[RTCSingleActionAlertController alloc]
                                          initWithTitle:@"提示"
                                          message:@"直播已结束"
                                          actionTitle:@"确定"
                                          action:^{
        [[RTCVideoliveRoom sharedInstance] leaveRoom:^(NSInteger errorCode) {
            if (errorCode == 0) {
                [self.navigationController popViewControllerAnimated:YES];
            }
        }];
        
    }];
    
      [self presentViewController:vc animated:YES completion:nil];
}


/// 用户离开RTC频道
/// @param result 结果
- (void)onLeaveChannelResult:(int)result
{
    [self updateMpuLayout];
}


/// 用户加入RTC频道的通知
/// @param result 结果
/// @param authInfo 授权信息
- (void)onJoinChannelResult:(int)result
                   authInfo:(AliRtcAuthInfo *)authInfo
{
    [self updateMpuLayout];
}

/// 更新旁路布局回调
- (void)updateMpuLayout
{
    [VideoRoomApi updateMPULayout:self.channelId
                         complete:^(NSString * _Nonnull error) {}];
}

- (void)onError:(AliPlayer *)player errorModel:(AVPErrorModel *)errorModel
{
    [RTCHUD showHud:errorModel.message inView:self.view];
}

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
        [self.navigationController popToRootViewControllerAnimated:YES];
    }];
    
    [self presentViewController:vc animated:YES completion:nil];
}

#pragma mark - 上下麦
- (void)enterSeat
{
    [VideoRoomApi getUserList:self.channelId
                     complete:^(NSArray * _Nonnull userList,
                                NSString * _Nonnull error) {
        if (userList.count<=2) {
            [[RTCVideoliveRoom sharedInstance] enterSeat:^(NSInteger errorCode)
             {
                if (errorCode == 0) {
                    //上麦成功
                    self.connectBtn.selected = YES;
                    self.flipBtn.hidden = NO;
                    self.isInRTC = YES;
                }
            }];
        }else{
            //人数超过限制
            [RTCHUD showHud:@"连麦人数已满，请稍后再试" inView:self.view];
        }
    }];
}

- (void)leaveSeat
{
    RTCDoubleActionsAlertController *vc = [[RTCDoubleActionsAlertController alloc]
                                           initWithTitle:@"提示"
                                           message:@"您正在连麦中，是否确认断开连麦"
                                           leftActionTitle:@"确认"
                                           leftAction:^{
        [[RTCVideoliveRoom sharedInstance] leaveSeat:^(NSInteger result) {
            if (result == 0) { 
                    [self.onlineUsers removeAllObjects];
                    [self updatePreviewFrames];
                    self.connectBtn.selected = NO;
                    self.isInRTC = NO;
                    self.flipBtn.hidden = YES;

            }else{
                [RTCHUD showHud:@"退出连麦失败" inView:self.view];
            }
        }];
    }
                                           rightActionTitle:@"取消"
                                           rightAction:^{}];
    
    [self presentViewController:vc animated:YES completion:nil];
}
#pragma mark - actions
- (IBAction)connectClicked:(UIButton *)sender
{
//    self.flipBtn.hidden = YES;
    if (sender.selected) {
        //下麦
        [self leaveSeat];
    } else {
        //上麦
        [self enterSeat];
    }
}

- (IBAction)flipCameraClicked:(id)sender
{
    [[RTCVideoliveRoom sharedInstance] switchCamera];
}

- (IBAction)exit:(id)sender
{
    if(!self.isInRTC){
        [[RTCVideoliveRoom sharedInstance] leaveRoom:^(NSInteger result)
        {
             [self.navigationController popViewControllerAnimated:YES];
        }];
       
    } else {
        RTCDoubleActionsAlertController *vc = [[RTCDoubleActionsAlertController alloc] initWithTitle:@"退出直播"
                                                                                             message:@"您确定要退出直播间吗？"
                                                                                     leftActionTitle:@"确认"
                                                                                          leftAction:^{
            
            [[RTCVideoliveRoom sharedInstance] leaveRoom:^(NSInteger result)
            {
                 [self.navigationController popViewControllerAnimated:YES];
            }];
           
        }
                                                                                    rightActionTitle:@"取消"
                                                                                         rightAction:^{
        }];
        [self presentViewController:vc animated:YES completion:nil];
    }
}



#pragma mark -setter & getter
- (NSString *)myId {
    if (_myId == nil) {
        _myId =  [NSString stringWithFormat:@"%u",arc4random()];
    }
    return _myId;
}

- (NSString *)displayName
{
    if (_displayName == nil) {
        _displayName = [NSString stringWithFormat:@"%u",arc4random()];
    }
    return _displayName;
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


@end
