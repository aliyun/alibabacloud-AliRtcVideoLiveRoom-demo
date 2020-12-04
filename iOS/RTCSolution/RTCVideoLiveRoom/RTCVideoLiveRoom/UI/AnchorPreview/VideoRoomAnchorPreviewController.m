//
//  VideoRoomAnchorPreviewController.m
//  Pods
//
//  Created by aliyun on 2020/8/7.
//

#import "VideoRoomAnchorPreviewController.h"
#import "NSBundle+RTCVideoLiveRoom.h"
#import "RTCHUD.h"
#import "VideoRoomBeautyPanelController.h"
#import "RTCVideoliveRoom.h"
#import "VideoRoomAnchorLiveController.h"
#import "VideoRoomApi.h"
#import "UIImageView+WebCache.h"
#import "RTCCommonView.h"

@interface VideoRoomAnchorPreviewController ()<UITextViewDelegate>
@property (unsafe_unretained, nonatomic) IBOutlet UIImageView *navigatorBackgroundImageView;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *closeButton;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *beautyBtn;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *cameraFlipBtn;
@property (unsafe_unretained, nonatomic) IBOutlet UIImageView *coverImageView;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *loginBtn;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *titlePlaceHolderBtn;
@property (unsafe_unretained, nonatomic) IBOutlet UILabel *titleLabel;
@property (unsafe_unretained, nonatomic) IBOutlet UITextView *titleTextView;
@property (unsafe_unretained, nonatomic) IBOutlet UIView *titleBottomLine;
@property (copy, nonatomic) NSString *coverUrl;

@end

@implementation VideoRoomAnchorPreviewController

- (instancetype)init
{
    UIStoryboard *storyboard = [NSBundle RVLR_storyboard];
    return [storyboard instantiateViewControllerWithIdentifier:@"VideoRoomAnchorPreviewController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self setupUI];
    NSString *coverUrl = [[NSUserDefaults standardUserDefaults] objectForKey:@"coverUrl"];
    if (coverUrl) {
        self.coverUrl = coverUrl;
        [self.coverImageView sd_setImageWithURL:[NSURL URLWithString:coverUrl] placeholderImage:
         [NSBundle RVLR_pngImageWithName:@"liveCover"]];
    }else {
        [VideoRoomApi randomCoverUrl:^(NSString * _Nonnull coverUrl, NSString * _Nonnull error) {
            if (!error) {
                self.coverUrl = coverUrl;
                [self.coverImageView sd_setImageWithURL:[NSURL URLWithString:coverUrl] placeholderImage:
                 [NSBundle RVLR_pngImageWithName:@"liveCover"]];
                [[NSUserDefaults standardUserDefaults] setObject:coverUrl forKey:@"coverUrl"];
                [[NSUserDefaults standardUserDefaults] synchronize];
            }
        }];
    }
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(textchanged:)
                                                 name:UITextViewTextDidChangeNotification
                                               object:nil];
    
    [self startPreview];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [self.navigationController setNavigationBarHidden:NO];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
}
- (void)dealloc
{
    [self stopPreview];
    NSLog(@"VideoRoomAnchorPreviewController dealloc");
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

#pragma mark - UI functions

- (void)setupUI
{
    self.navigatorBackgroundImageView.image = [NSBundle RVLR_pngImageWithName:@"rectangle"];
    
    [self.closeButton setImage:[NSBundle RVLR_pngImageWithName:@"close"] forState:UIControlStateNormal];
    [self.beautyBtn setImage:[NSBundle RVLR_pngImageWithName:@"beauty"] forState:UIControlStateNormal];
    [self.cameraFlipBtn setImage:[NSBundle RVLR_pngImageWithName:@"camera_flip"] forState:UIControlStateNormal];
    
    self.loginBtn.layer.cornerRadius = 24;
    self.loginBtn.layer.masksToBounds = YES;
    
    self.coverImageView.layer.cornerRadius = 8;
    self.coverImageView.image = [NSBundle RVLR_pngImageWithName:@"liveCover"];
    
    [self.titlePlaceHolderBtn setImage:[NSBundle RVLR_pngImageWithName:@"titleEdit"] forState:UIControlStateNormal];
    [self.titlePlaceHolderBtn setImage:[NSBundle RVLR_pngImageWithName:@"titleEdit"] forState:UIControlStateHighlighted];
    
    self.titleTextView.delegate = self;
    
    [self hideEditor];
}

#pragma mark - clicking actions

/// 关闭
- (IBAction)close:(id)sender
{
    [self stopPreview];
    [self.navigationController popViewControllerAnimated:YES];
}

/// 美颜
- (IBAction)beauty:(id)sender
{
    VideoRoomBeautyPanelController *vc = [[VideoRoomBeautyPanelController alloc] init];
    vc.modalPresentationStyle = UIModalPresentationOverFullScreen;
    [self presentViewController:vc animated:YES completion:nil];
}
// 翻转摄像头
- (IBAction)flip:(id)sender
{
    [[RTCVideoliveRoom sharedInstance] switchCamera];
}
// 创建房间
- (IBAction)login:(id)sender
{
    NSString *title = self.titleTextView.text;
    if(title.length == 0){
        [self showAlert];
        return;
    }
    BOOL result = [self validateTitle:title];
    
    if(!result) {
        [RTCHUD showHud:@"直播标题仅支持中英文和数字" inView:self.view];
        return;
    }
    self.loginBtn.enabled  = NO;
    //加入房间
    NSString *channelId = [NSString stringWithFormat:@"%u",arc4random()];
    NSString *name = [NSString stringWithFormat:@"%u",arc4random()];
    NSString *userId = [NSString stringWithFormat:@"%u",arc4random()];
    NSString *coverUrl = self.coverUrl;
    NSLog(@"channel:%@",channelId);
    [[RTCVideoliveRoom sharedInstance] createRoom:channelId
                                         userName:name
                                           userId:userId
                                         complete:^(AliRtcAuthInfo * _Nonnull authInfo, NSInteger errorCode) {
        if (errorCode == 0) {
            [VideoRoomApi startMPUTask:channelId
                                userId:userId
                              coverUrl:coverUrl
                                 title:title
                              complete:^(NSString * _Nonnull error)
             {
                if (!error) {
                    self.loginBtn.enabled = YES;
                    VideoRoomAnchorLiveController *vc = [[VideoRoomAnchorLiveController alloc] init];
                    vc.name = title;
                    vc.channelId = channelId;
                    vc.coverURL = coverUrl;
                    [self.navigationController pushViewController:vc animated:YES];
                }else{
                    [self dealwithError:@"开启旁路直播失败,请重新创建频道"];
                }
            }];
        } else {
            [self dealwithError:@"加入房间失败"];
        }
    }];
}

- (void)dealwithError:(NSString *)error
{
    self.loginBtn.enabled = YES;
    [RTCHUD showHud:error inView:self.view];
    [[RTCVideoliveRoom sharedInstance] destroyRoom:^(NSInteger result) {
        if (result != 0) {
            [RTCHUD showHud:@"房间销毁失败" inView:self.view];
        }
    }];
}

#pragma mark - titleTextViewFunctions

- (IBAction)titleButtonClicked:(id)sender {
    [self beginEdit];
}

- (IBAction)viewTapped:(id)sender {
    [self endEdit];
}

- (BOOL)validateTitle:(NSString *)textString
{
    NSString* number=@"^[\u4e00-\u9fa5a-zA-Z0-9]{0,20}$";
    NSPredicate *numberPre = [NSPredicate predicateWithFormat:@"SELF MATCHES %@",number];
    return [numberPre evaluateWithObject:textString];
}

- (void)beginEdit
{
    [self showEditor];
    [self.titleTextView becomeFirstResponder];
}

- (void)endEdit
{
    [self.titleTextView resignFirstResponder];
    if (!self.titleTextView.text.length) {
        [self hideEditor];
    }
}


- (void)showEditor
{
    self.titleLabel.hidden = NO;
    self.titleTextView.hidden = NO;
    self.titleBottomLine.hidden = NO;
    self.titlePlaceHolderBtn.hidden = YES;
}

- (void)hideEditor
{
    self.titleLabel.hidden = YES;
    self.titleTextView.hidden = YES;
    self.titleBottomLine.hidden = YES;
    self.titlePlaceHolderBtn.hidden = NO;
}

- (void)textchanged:(id)notification
{
    if(self.titleTextView.text.length > 20  ) {
        self.titleTextView.text = [self.titleTextView.text substringToIndex:20];
        [RTCHUD showHud:@"标题文字最多为20个字" inView:self.view];
        [self endEdit];
    }
}
- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)text
{
//    BOOL result = [self validateTitle:text];
//    if (!result) {
//        [RTCHUD showHud:@"直播标题仅支持中英文和数字" inView:self.view];
//    }
//    return result;
    NSLog(@"%@",text);
    return YES;
}

- (void)showAlert
{
    RTCSingleActionAlertController *vc = [[RTCSingleActionAlertController alloc] initWithTitle:@"直播标题不能为空"
                                                                                       message:@"您的直播标题不能为空，请先进行填写才能开启直播"
                                                                                   actionTitle:@"我知道了"
                                                                                        action:^{}];
    [self presentViewController:vc animated:YES completion:nil];
    
}

#pragma mark -本地摄像头预览

- (void)startPreview
{
    [[RTCVideoliveRoom sharedInstance] startCameraPreView:self.view];
}


- (void)stopPreview
{
    [[RTCVideoliveRoom sharedInstance] stopCameraPreview];
}


@end
