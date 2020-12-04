//
//  VideoRoomBackgrounMusicController.m
//  RTCVideoLiveRoom
//
//  Created by aliyun on 2020/8/12.
//

#import "VideoRoomBackgrounMusicController.h"
#import "NSBundle+RTCVideoLiveRoom.h"
#import "RTCVideoliveRoom.h"

@interface VideoRoomBackgrounMusicController ()

@property (unsafe_unretained, nonatomic) IBOutlet UISlider *sliderBar;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *auditionBtn; //试听按钮
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *publishBtn;  //推送按钮

@property (nonatomic,strong) VideoRoomBackgroundMusic *music;

@end

@implementation VideoRoomBackgrounMusicController

- (instancetype)initWithMusic:(VideoRoomBackgroundMusic *)music
{
    UIStoryboard *storyboard = [NSBundle RVLR_storyboard];
    self = [storyboard instantiateViewControllerWithIdentifier:@"VideoRoomBackgrounMusicController"];
    self.music = music;
    return self;
}


- (void)viewDidLoad
{
    [super viewDidLoad];
    [self setupUI];
    [self reloadData];
}

- (void)setupUI
{
    [self.auditionBtn setImage:[NSBundle RVLR_pngImageWithName:@"test"]
                      forState:UIControlStateNormal];
    [self.auditionBtn setImage:[NSBundle RVLR_pngImageWithName:@"pause"]
                      forState:UIControlStateSelected];
    [self.publishBtn setImage:[NSBundle RVLR_pngImageWithName:@"play"]
                     forState:UIControlStateNormal];
    [self.publishBtn setImage:[NSBundle RVLR_pngImageWithName:@"pause"]
                     forState:UIControlStateSelected];
}

- (void)reloadData
{
    self.sliderBar.value = self.music.volume;
    self.auditionBtn.selected =  self.music.testing;
    self.publishBtn.selected = self.music.publishing;
}

- (void)playControl
{
    if (self.music.testing|| self.music.publishing )
    {
        [[RTCVideoliveRoom sharedInstance] startAudioAccompanyWithFile:self.music.path
                                                               publish:self.music.publishing ];
        [[RTCVideoliveRoom sharedInstance] setAudioAccompanyVolume: self.music.volume];
    } else {
        [[RTCVideoliveRoom sharedInstance] stopAudioAccompany];
    }
}

- (IBAction)test:(UIButton *)sender
{
    self.music.testing = !self.music.testing;
    self.music.publishing = NO;
    [self reloadData];
    [self playControl];
}
- (IBAction)publish:(UIButton *)sender
{
    self.music.testing = NO;
    self.music.publishing = !self.music.publishing;
    [self reloadData];
    [self playControl];
}

- (IBAction)chaneVolum:(UISlider *)sender
{
    self.music.volume = sender.value;
    [[RTCVideoliveRoom sharedInstance] setAudioAccompanyVolume:self.music.volume];
}

- (IBAction)close:(id)sender {
    [self dismissViewControllerAnimated:YES
                             completion:nil];
}

#pragma mark - JXCategoryListContentViewDelegate

- (UIView *)listView {
    return self.view;
}

@end
