//
//  VideoRoomSoundEffectController.m
//  RTCVideoLiveRoom
//
//  Created by aliyun on 2020/8/12.
//

#import "VideoRoomSoundEffectController.h"
#import "NSBundle+RTCVideoLiveRoom.h"
#import "RTCVideoliveRoom.h"
#import "VideoRoomSoundEffect.h"

@interface VideoRoomSoundEffectController ()

@property (unsafe_unretained, nonatomic) IBOutlet UISlider *laughSliderBar;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *laughAuditionButton;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *laughPublishButton;
@property (unsafe_unretained, nonatomic) IBOutlet UISlider *applauseSliderBar;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *applauseAuditonButton;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *applausePublishButton;
@property (strong, nonatomic) VideoRoomSoundEffect *effect1;
@property (strong, nonatomic) VideoRoomSoundEffect *effect2;

@end

@implementation VideoRoomSoundEffectController

- (instancetype)initWithEffect1:(VideoRoomSoundEffect *)effect1
                        effect2:(VideoRoomSoundEffect *)effect2
{
    UIStoryboard *storyboard = [NSBundle RVLR_storyboard];
    self = [storyboard instantiateViewControllerWithIdentifier:@"VideoRoomSoundEffectController"];
    self.effect1 = effect1;
    self.effect2 = effect2;
    return self;
}


- (void)viewDidLoad {
    [super viewDidLoad];
    [self setupUI];
    [self reloadData];
}

- (void)setupUI
{
    [self.laughAuditionButton setImage:[NSBundle RVLR_pngImageWithName:@"test"]
                              forState:UIControlStateNormal];
    
    [self.laughPublishButton setImage:[NSBundle RVLR_pngImageWithName:@"play"]
                             forState:UIControlStateNormal];
    
    [self.applauseAuditonButton setImage:[NSBundle RVLR_pngImageWithName:@"test"]
                                forState:UIControlStateNormal];
    
    [self.applausePublishButton setImage:[NSBundle RVLR_pngImageWithName:@"play"]
                                forState:UIControlStateNormal];
}

- (void)reloadData
{
    self.laughSliderBar.value = self.effect1.volume;
    self.laughAuditionButton.selected = self.effect1.testing;
    self.laughPublishButton.selected = self.effect1.publishing;
    
    self.applauseSliderBar.value = self.effect2.volume;
    self.applauseAuditonButton.selected =self.effect2.testing;
    self.applausePublishButton.selected = self.effect2.publishing;
}

- (void)effect1PlayControl
{
    //    NSString *url = [self.effect1.path stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    NSInteger soundId = self.effect1.effectId;
    
    if (self.effect1.testing || self.effect1.publishing )
    {
        [[RTCVideoliveRoom sharedInstance]  playEffectSoundtWithSoundId:soundId
                                                               filePath:self.effect1.path
                                                                publish:self.effect1.publishing];
        [[RTCVideoliveRoom sharedInstance] setAudioEffectVolumeWithSoundId:soundId
                                                                    volume:self.effect1.volume];
    } else {
        [[RTCVideoliveRoom sharedInstance] stopAudioEffectWithSoundId:soundId];
    }
}

- (void)effect2PlayControl
{
    NSInteger soundId = self.effect2.effectId;
    if (self.effect2.testing || self.effect2.publishing )
    {
        [[RTCVideoliveRoom sharedInstance]  playEffectSoundtWithSoundId:soundId
                                                               filePath:self.effect2.path
                                                                publish:self.effect2.publishing];
        [[RTCVideoliveRoom sharedInstance] setAudioEffectVolumeWithSoundId:soundId
                                                                    volume:self.effect2.volume];
    } else {
        [[RTCVideoliveRoom sharedInstance] stopAudioEffectWithSoundId:soundId];
    }
}

- (IBAction)laughTest:(UIButton *)sender
{
    self.effect1.testing = YES;
    self.effect1.publishing = NO;
    self.effect2.testing = NO;
    self.effect2.publishing = NO;
    
    [self reloadData];
    
    [self effect1PlayControl];
    [self effect2PlayControl];
}


- (IBAction)laughPublish:(UIButton *)sender
{
    self.effect1.testing = YES;
    self.effect1.publishing = YES;
    self.effect2.testing = NO;
    self.effect2.publishing = NO;
    
    [self reloadData];
    
    [self effect1PlayControl];
    [self effect2PlayControl];
}


- (IBAction)applauseTest:(UIButton *)sender
{
    self.effect1.testing = NO;
    self.effect1.publishing = NO;
    self.effect2.testing = YES;
    self.effect2.publishing = NO;
    
    [self reloadData];
    
    [self effect1PlayControl];
    [self effect2PlayControl];
}

- (IBAction)applausePublish:(UIButton *)sender
{
    self.effect1.testing = NO;
    self.effect1.publishing = NO;
    self.effect2.testing = YES;
    self.effect2.publishing = YES;
    
    [self reloadData];
    
    [self effect1PlayControl];
    [self effect2PlayControl];
}

- (IBAction)effect1VolumeChanged:(UISlider *)sender
{
    self.effect1.volume = sender.value;
    NSInteger soundId = self.effect1.effectId;
    [[RTCVideoliveRoom sharedInstance] setAudioEffectVolumeWithSoundId:soundId
                                                                volume:sender.value];
}


- (IBAction)effect2VolumeChanged:(UISlider *)sender
{
    self.effect2.volume = sender.value;
    NSInteger soundId = self.effect2.effectId;
    [[RTCVideoliveRoom sharedInstance] setAudioEffectVolumeWithSoundId:soundId
                                                                volume:sender.value];
}

- (IBAction)close:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}


#pragma mark - JXCategoryListContentViewDelegate

- (UIView *)listView {
    return self.view;
}

@end
