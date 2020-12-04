//
//  VideoRoomChannelCell.m
//  Pods
//
//  Created by aliyun on 2020/8/7.
//

#import "VideoRoomChannelCell.h"
#import "NSBundle+RTCVideoLiveRoom.h"
#import "UIImageView+WebCache.h"

@interface VideoRoomChannelCell()

@property (unsafe_unretained, nonatomic) IBOutlet UILabel *titleLabel;
@property (unsafe_unretained, nonatomic) IBOutlet UIImageView *backgroundImageView;
@end

@implementation VideoRoomChannelCell

 
- (void)awakeFromNib
{
    [super awakeFromNib];
    self.backgroundImageView.layer.cornerRadius = 8;
}


- (void)setChannel:(VideoRoomChannel *)channel
{
    _channel = channel;
    if (_channel) {
        [self.backgroundImageView sd_setImageWithURL:[NSURL URLWithString:channel.coverUrl]];
        self.titleLabel.text = channel.title;
    }
}

@end
