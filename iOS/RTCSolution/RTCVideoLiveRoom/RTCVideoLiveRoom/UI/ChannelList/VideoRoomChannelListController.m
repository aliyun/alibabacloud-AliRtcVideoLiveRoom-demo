//
//  VideoRoomChannelListController.m
//  Pods
//
//  Created by aliyun on 2020/8/6.
//

#import "VideoRoomChannelListController.h"
#import "NSBundle+RTCVideoLiveRoom.h"
#import "RTCCommonView.h"
#import "RTCCommon.h"
#import "VideoRoomChannelCell.h"
#import "MJRefresh.h"
#import "VideoRoomAnchorPreviewController.h"
#import "VideoRoomApi.h"
#import "VideoRoomAudienceLiveController.h"
#import "AFNetworkReachabilityManager.h"
#import "RTCOffLineController.h"
#import "RTCVideoliveRoom.h"

@interface VideoRoomChannelListController ()<UICollectionViewDelegate,
                                            UICollectionViewDataSource,
                                        UICollectionViewDelegateFlowLayout>

@property (unsafe_unretained, nonatomic) IBOutlet UICollectionView *collectionView;
@property (unsafe_unretained, nonatomic) IBOutlet UIImageView *bgImageView;
@property (unsafe_unretained, nonatomic) IBOutlet UIButton *startBroadcastBtn;
@property (strong,nonatomic) NSMutableArray *channelList;

@end

@implementation VideoRoomChannelListController

#pragma mark - life cycle
- (instancetype)init
{
    UIStoryboard *storyboard = [NSBundle RVLR_storyboard];
    return [storyboard instantiateViewControllerWithIdentifier:@"VideoRoomChannelListController"];
}


-(void)dealloc
{
    [[RTCVideoliveRoom sharedInstance] destroySharedInstance];
    NSLog(@"VideoRoomChannelListController dealloc");
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self seupUI];
    [self addNetworkingObserver];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [self loadData:nil];
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
    if (@available(iOS 13.0, *)) {
        return UIStatusBarStyleDarkContent;
    } else {
        return UIStatusBarStyleDefault;
    }
}

#pragma mark - UI 操作
- (void)seupUI
{
    //navigationbar
    self.navigationController.navigationBar.tintColor = [UIColor blackColor];
    UIImage *image = [UIImage imageWithColor:[[UIColor whiteColor]colorWithAlphaComponent:0]];
    [self.navigationController.navigationBar setBackgroundImage:image forBarMetrics:UIBarMetricsDefault];
    [self.navigationController.navigationBar setShadowImage:image];
    
    UIBarButtonItem *leftItem = [[UIBarButtonItem alloc] initWithImage:[NSBundle RCV_pngImageWithName:@"angle_left"] style:UIBarButtonItemStylePlain target:self action:@selector(back)];
    self.navigationItem.leftBarButtonItem = leftItem;
    UILabel *titleView = [[UILabel alloc] init];
    titleView.text = @"视频互动直播";
    titleView.font = [UIFont systemFontOfSize:17];
    titleView.textColor = [UIColor colorWithHex:0x161A23];
    [titleView sizeToFit];
    self.navigationItem.titleView = titleView;
    
    //background
    self.bgImageView.image = [NSBundle RCV_pngImageWithName:@"background"];
    [self.startBroadcastBtn setImage:[NSBundle RVLR_pngImageWithName:@"StartBroadcast"] forState:UIControlStateNormal];
    
    //setup layout
    UICollectionViewFlowLayout *layout =(UICollectionViewFlowLayout *) self.collectionView.collectionViewLayout;
    layout.sectionInset = UIEdgeInsetsMake(0, 18, 0, 18);
    layout.minimumInteritemSpacing = 0;
    
    __weak typeof(self) weakSelf = self;
    //add refresh
    self.collectionView.mj_header = [MJRefreshNormalHeader headerWithRefreshingBlock:^{
        [weakSelf loadData:nil];
    }];
    
    
    self.collectionView.mj_footer = [MJRefreshAutoFooter footerWithRefreshingBlock:^{
        VideoRoomChannel *channel = self.channelList.lastObject;
        if (channel) {
            [weakSelf loadData:channel.channelId];
        } else {
            [weakSelf.collectionView.mj_footer endRefreshing];
        }
    }];
    
}
#pragma mark -网络监听
- (void)addNetworkingObserver {
    __weak typeof(self) weakSelf = self;
    [[AFNetworkReachabilityManager sharedManager] setReachabilityStatusChangeBlock:^(AFNetworkReachabilityStatus status) {
        if (status == AFNetworkReachabilityStatusNotReachable) {
            //显示断网页面
            RTCOffLineController *vc = [[RTCOffLineController alloc] initWithcloseAction:^{
                [weakSelf dismissViewControllerAnimated:YES completion:nil];
                [weakSelf.navigationController popToRootViewControllerAnimated:NO];
            } retryAction:^{
                NSLog(@"");
            }];
            [weakSelf presentViewController:vc animated:YES completion:nil];
        } else {
            //关闭断网页面
            [weakSelf dismissViewControllerAnimated:YES completion:nil];
        }
    }];
    
    [[AFNetworkReachabilityManager sharedManager] startMonitoring];
}



- (void)back
{
    [self.navigationController popViewControllerAnimated:YES];
}

- (IBAction)buttonClicked:(id)sender {
    VideoRoomAnchorPreviewController *vc = [[VideoRoomAnchorPreviewController alloc] init];
    [self.navigationController pushViewController:vc animated:YES];
    
}

#pragma mark - collectionViewdelegate
- (NSInteger)collectionView:(UICollectionView *)collectionView
     numberOfItemsInSection:(NSInteger)section {
    return self.channelList.count;
}


- (__kindof UICollectionViewCell *)collectionView:(UICollectionView *)collectionView
                           cellForItemAtIndexPath:(NSIndexPath *)indexPath{
    VideoRoomChannelCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"VideoRoomChannelCell"
                                                                           forIndexPath:indexPath];
    VideoRoomChannel *channel = self.channelList[indexPath.item];
    cell.channel = channel;
    return cell;
}


- (CGSize)collectionView:(UICollectionView *)collectionView
                  layout:(UICollectionViewLayout *)collectionViewLayout
  sizeForItemAtIndexPath:(NSIndexPath *)indexPath
{
    return CGSizeMake(165, 210);
}


- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath {
    VideoRoomChannel *channel = self.channelList[indexPath.item];
    VideoRoomAudienceLiveController *vc = [[VideoRoomAudienceLiveController alloc] init];
    vc.name = channel.title;
    vc.coverURL = channel.coverUrl;
    vc.anchorId = channel.ownerId;
    vc.channelId = channel.channelId;
    
    [self.navigationController pushViewController:vc animated:YES];
}

#pragma mark - 网络请求
- (void)loadData:(NSString *)lastChannelId
{
    __weak typeof(self) weakSelf = self;
//    NSString *lastChannelId = nil;
//    if (self.channelList.count) {
//        VideoRoomChannel *channel = self.channelList.lastObject;
//        lastChannelId = channel.channelId;
//    }
    [VideoRoomApi channelList:lastChannelId
                     complete:^(NSArray * _Nonnull channels, NSString * _Nonnull error) {
        
        [weakSelf.collectionView.mj_header endRefreshing];
        [weakSelf.collectionView.mj_footer endRefreshing];
        
        if (!error) {
            [weakSelf dealWithChannels:channels lastChannelId:lastChannelId];
        }
    }];
}

- (void)dealWithChannels:(NSArray *)channels lastChannelId:(NSString *)lastChannelId
{
    if (!lastChannelId) {
        //下拉刷新
        [self.channelList removeAllObjects];
    }
    [self.channelList addObjectsFromArray:channels];
    
    if(self.channelList.count == 0){
        [RTCHUD showHud:@"当前没有主播开播" inView:self.view];
    }
     
    [self.collectionView reloadData];
}

- (NSMutableArray *)channelList
{
    if (!_channelList) {
        _channelList = @[].mutableCopy;
    }
    return _channelList;
}
@end
