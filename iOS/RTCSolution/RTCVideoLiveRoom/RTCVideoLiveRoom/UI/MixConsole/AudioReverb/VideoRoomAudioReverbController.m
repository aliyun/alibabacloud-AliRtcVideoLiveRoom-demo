//
//  VideoRoomAudioReverbController.m
//  RTCVideoLiveRoom
//
//  Created by aliyun on 2020/8/12.
//

#import "VideoRoomAudioReverbController.h"
#import "NSBundle+RTCVideoLiveRoom.h"
#import "VideoRoomMixSetting.h"
#import "RTCVideoliveRoom.h"
#import "VideoRoomReverbCell.h"

@interface VideoRoomAudioReverbController ()<UICollectionViewDelegate,
                                            UICollectionViewDataSource,
                                        UICollectionViewDelegateFlowLayout>

@property (strong,nonatomic) VideoRoomMixSetting *mixSetting;
@property (strong, nonatomic) NSArray *reverbModes;
@property (assign, nonatomic) NSInteger selectedReverbModeIndex;
@property (unsafe_unretained, nonatomic) IBOutlet UICollectionView *collectionView;
@property (unsafe_unretained, nonatomic) IBOutlet UISwitch *earbackSwitch;
@property (unsafe_unretained, nonatomic) IBOutlet UILabel *earBackLabel;

@end

@implementation VideoRoomAudioReverbController


- (instancetype)initWithMixSetting:(VideoRoomMixSetting *)mixSetting
{
    UIStoryboard *storyboard = [NSBundle RVLR_storyboard];
    self = [storyboard instantiateViewControllerWithIdentifier:@"VideoRoomAudioReverbController"];
    self.mixSetting = mixSetting;
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setupUI];
}


- (void)setupUI
{
    self.selectedReverbModeIndex = self.mixSetting.mixType;
    self.earbackSwitch.on = self.mixSetting.earBack;
    self.earBackLabel.text =self.earbackSwitch.on ? @"开启":@"关闭";
    
}

- (void)reloadData {
    [self setupUI];
    [self.collectionView reloadData];
}

- (void)viewDidLayoutSubviews
{
    NSIndexPath *indexPath = [NSIndexPath indexPathForItem:self.selectedReverbModeIndex inSection:0];
       [self.collectionView scrollToItemAtIndexPath:indexPath atScrollPosition:UICollectionViewScrollPositionLeft animated:NO];
}

#pragma mark - collectionView delegate
- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView
{
    return 1;
}
- (NSInteger)collectionView:(UICollectionView *)collectionView
     numberOfItemsInSection:(NSInteger)section
{
    return self.reverbModes.count;
}

- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView
                           cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    VideoRoomReverbCell *tagCell = [collectionView dequeueReusableCellWithReuseIdentifier:@"VideoRoomReverbCell"
                                                                             forIndexPath:indexPath];
    tagCell.imageName = [NSString stringWithFormat:@"reverb%ld",indexPath.item];
    tagCell.picked = indexPath.item == self.selectedReverbModeIndex;
    tagCell.tagStr = self.reverbModes[indexPath.item];
    return tagCell;
}

- (CGSize)collectionView:(UICollectionView *)collectionView
                  layout:(UICollectionViewLayout *)collectionViewLayout
  sizeForItemAtIndexPath:(NSIndexPath *)indexPath
{
    return CGSizeMake(70,70);
}



- (void)collectionView:(UICollectionView *)collectionView  didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
 if (self.selectedReverbModeIndex!= indexPath.item)
   {
       
       VideoRoomReverbCell * cell = (VideoRoomReverbCell *)[collectionView
                                                            cellForItemAtIndexPath:[NSIndexPath indexPathForItem:self.selectedReverbModeIndex
                                                                         inSection:0]];
       cell.picked = NO;
       self.selectedReverbModeIndex = indexPath.item;
       
       VideoRoomReverbCell * selectedCell = (VideoRoomReverbCell *)[collectionView cellForItemAtIndexPath:indexPath];
       selectedCell.picked = YES;
      
       self.mixSetting.mixType = self.selectedReverbModeIndex;
      
        [[RTCVideoliveRoom sharedInstance] setAudioEffectReverbMode:self.selectedReverbModeIndex];
       
   }
}


- (IBAction)earback:(UISwitch *)sender
{
    self.mixSetting.earBack = sender.on;
    if (sender.on) {
        self.earBackLabel.text = @"开启";
    }else {
        self.earBackLabel.text = @"关闭";
    }
    
    [[RTCVideoliveRoom sharedInstance] enableEarBack:sender.on];
}

#pragma mark - setter & getter

- (NSArray *)reverbModes
{
    if (!_reverbModes)
    {
        _reverbModes = @[@"无效果",@"人声I",@"人声II",@"澡堂",@"明亮小房间",@"黑暗小房间",@"中等房间",@"大房间",@"教堂走廊",@"大教堂"];
    }
    
    return _reverbModes;
}

- (IBAction)close:(id)sender {
      [self dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark - JXCategoryListContentViewDelegate

- (UIView *)listView {
    return self.view;
}

@end
