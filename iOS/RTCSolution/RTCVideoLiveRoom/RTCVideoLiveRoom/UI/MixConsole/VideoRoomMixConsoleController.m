//
//  VideoRoomMixConsoleController.m
//  RTCVideoLiveRoom
//
//  Created by aliyun on 2020/8/12.
//

#import "VideoRoomMixConsoleController.h"
#import "NSBundle+RTCVideoLiveRoom.h"
#import "JXCategoryView.h"
#import "JXCategoryListContainerView.h"
#import "RTCMacro.h"
#import "VideoRoomBackgrounMusicController.h"
#import "VideoRoomSoundEffectController.h"
#import "VideoRoomAudioReverbController.h"
#import "VideoRoomMixSetting.h"

@interface VideoRoomMixConsoleController ()<JXCategoryViewDelegate,JXCategoryListContainerViewDelegate>


@property (nonatomic, strong) NSArray *titles;

@property (nonatomic, strong) JXCategoryTitleView *categoryView;

@property (nonatomic, strong) JXCategoryListContainerView *listContainerView;

@property (strong, nonatomic) VideoRoomBackgroundMusic *music;

@property (strong, nonatomic) VideoRoomSoundEffect *effect1;

@property (strong, nonatomic) VideoRoomSoundEffect *effect2;

@property (strong, nonatomic) VideoRoomMixSetting *mixSetting;

@property (strong,nonatomic) VideoRoomBackgrounMusicController *musicVC;

@property (strong,nonatomic) VideoRoomSoundEffectController *effectVC;

@property (strong,nonatomic) VideoRoomAudioReverbController *audioReverbVC;


@end

@implementation VideoRoomMixConsoleController

- (instancetype)initWithMusic:(VideoRoomBackgroundMusic *)music
                  effectLaugh:(VideoRoomSoundEffect *)laugh
               effectApplause:(VideoRoomSoundEffect *)applause
                   mixSetting:(VideoRoomMixSetting *)mixSetting
{
    UIStoryboard *storyboard = [NSBundle RVLR_storyboard];
    self =  [storyboard instantiateViewControllerWithIdentifier:@"VideoRoomMixConsoleController"];
    self.music = music;
    self.effect1 = laugh;
    self.effect2 = applause;
    self.mixSetting = mixSetting;
    self.modalPresentationStyle = UIModalPresentationOverFullScreen;
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
     self.titles = @[@"伴奏", @"音效", @"混响"];
     
    
    self.categoryView.titles = self.titles;
    
    [self.view addSubview:self.listContainerView];

    self.categoryView.listContainer = self.listContainerView;
    self.categoryView.delegate = self;
    [self.view addSubview:self.categoryView];
    
}


- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];
    
    self.categoryView.frame = CGRectMake(0, RTCScreenHeight - RTCSafeBottom - [self preferredCategoryViewHeight], self.view.bounds.size.width, [self preferredCategoryViewHeight]);
    self.listContainerView.frame = CGRectMake(0,0, self.view.bounds.size.width, self.view.bounds.size.height);
}
 

- (JXCategoryBaseView *)preferredCategoryView {
    JXCategoryTitleView *titleView = [[JXCategoryTitleView alloc] init];
    titleView.titleFont = [UIFont systemFontOfSize:12];
    titleView.titleColor = RTCRGBA(255, 255, 255, 0.72);
    titleView.titleSelectedColor = RTCRGBA(255, 255, 255, 0.72);
    titleView.backgroundColor = RTCRGBA(46, 49, 56, 1);
    titleView.averageCellSpacingEnabled = NO;
    titleView.cellWidth = 40;
    titleView.cellSpacing = 20;
    JXCategoryIndicatorLineView *lineView = [[JXCategoryIndicatorLineView alloc] init];
//    lineView.indicatorWidth = 40;
    lineView.indicatorColor = [UIColor whiteColor];
    titleView.indicators = @[lineView];
     
    
    return titleView;
}


- (CGFloat)preferredCategoryViewHeight {
    return 40;
}

- (JXCategoryTitleView *)categoryView {
    if (_categoryView == nil) {
        _categoryView = (JXCategoryTitleView *)[self preferredCategoryView];
    }
    return _categoryView;
}

- (JXCategoryListContainerView *)listContainerView {
    if (_listContainerView == nil) {
        _listContainerView = [[JXCategoryListContainerView alloc] initWithType:JXCategoryListContainerType_ScrollView delegate:self];
        _listContainerView.scrollView.scrollEnabled = NO;
    }
    return _listContainerView;
}




#pragma mark - JXCategoryViewDelegate

- (void)categoryView:(JXCategoryBaseView *)categoryView didSelectedItemAtIndex:(NSInteger)index {
    //侧滑手势处理
    self.navigationController.interactivePopGestureRecognizer.enabled = (index == 0);
    NSLog(@"%@", NSStringFromSelector(_cmd));
}

- (void)categoryView:(JXCategoryBaseView *)categoryView didScrollSelectedItemAtIndex:(NSInteger)index {
    NSLog(@"%@", NSStringFromSelector(_cmd));
}

#pragma mark - JXCategoryListContainerViewDelegate

- (id<JXCategoryListContentViewDelegate>)listContainerView:(JXCategoryListContainerView *)listContainerView initListForIndex:(NSInteger)index {
    if (index == 0) {
        return self.musicVC;
    } else if(index == 1) {
        return self.effectVC;
    } else {
        return self.audioReverbVC;
    }
}

- (NSInteger)numberOfListsInlistContainerView:(JXCategoryListContainerView *)listContainerView {
    return self.titles.count;
}


- (VideoRoomBackgrounMusicController *)musicVC {
    if (!_musicVC) {
        _musicVC = [[VideoRoomBackgrounMusicController alloc] initWithMusic:self.music];
    }
    return _musicVC;
}

- (VideoRoomSoundEffectController *)effectVC {
    if (!_effectVC) {
        _effectVC = [[VideoRoomSoundEffectController alloc] initWithEffect1:self.effect1 effect2:self.effect2];
    }
    return _effectVC;
}

- (VideoRoomAudioReverbController *)audioReverbVC {
    if (!_audioReverbVC) {
        _audioReverbVC = [[VideoRoomAudioReverbController alloc] initWithMixSetting:self.mixSetting];
    }
    return _audioReverbVC;
}

- (void)reloadDataAtIndex:(NSInteger)index {
    if (index == 0) {
        [self.musicVC reloadData];
    } else if (index == 1) {
        [self.effectVC reloadData];
    } else {
        [self.audioReverbVC reloadData];
    }
    
}

@end
