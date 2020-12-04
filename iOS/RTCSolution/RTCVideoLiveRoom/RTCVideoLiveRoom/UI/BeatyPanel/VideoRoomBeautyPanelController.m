//
//  VideoRoomBeautyPanelController.m
//  Pods
//
//  Created by aliyun on 2020/8/10.
//

#import "VideoRoomBeautyPanelController.h"
#import "NSBundle+RTCVideoLiveRoom.h"
#import "RTCVideoliveRoom.h"

@interface VideoRoomBeautyPanelController ()

@property (unsafe_unretained, nonatomic) IBOutlet UISlider *whiteSlider;
@property (unsafe_unretained, nonatomic) IBOutlet UISlider *skinSlider;
@property (unsafe_unretained, nonatomic) IBOutlet UILabel *whiteLabel;
@property (unsafe_unretained, nonatomic) IBOutlet UILabel *skinLabel;


@end

@implementation VideoRoomBeautyPanelController

- (instancetype)init
{
    UIStoryboard *storyboard = [NSBundle RVLR_storyboard];
    return [storyboard instantiateViewControllerWithIdentifier:@"VideoRoomBeautyPanelController"];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self setupUI];
    
}

- (void)setupUI
{
    NSInteger whiteLevel = [[RTCVideoliveRoom sharedInstance] whiteLevel];
    self.whiteSlider.value = whiteLevel;
    self.whiteLabel.text = [NSString stringWithFormat:@"%ld",whiteLevel];
    
    NSInteger smoothLevel = [[RTCVideoliveRoom sharedInstance] smoothLevel];
    self.skinSlider.value = smoothLevel;
    self.skinLabel.text = [NSString stringWithFormat:@"%ld",smoothLevel];
}


- (IBAction)close:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)whiteChanged:(id)sender
{ 
    NSInteger whiteLevel = self.whiteSlider.value;
    [RTCVideoliveRoom sharedInstance].whiteLevel = whiteLevel;
    self.whiteSlider.value = whiteLevel;
    self.whiteLabel.text = [NSString stringWithFormat:@"%ld",whiteLevel];
}

- (IBAction)smoothChanged:(id)sender
{
    NSInteger smoothLevel = self.skinSlider.value;
    [RTCVideoliveRoom sharedInstance].smoothLevel = smoothLevel;
    self.skinSlider.value = smoothLevel;
    self.skinLabel.text = [NSString stringWithFormat:@"%ld",smoothLevel];
}

 
@end
