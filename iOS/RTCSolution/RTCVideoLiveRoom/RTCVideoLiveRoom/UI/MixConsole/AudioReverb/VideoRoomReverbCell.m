//
//  VideoRoomReverbCell.m
//  RTCVideoLiveRoom
//
//  Created by aliyun on 2020/8/13.
//

#import "VideoRoomReverbCell.h"
#import "NSBundle+RTCVideoLiveRoom.h"
#import "UIColor+Hex.h"
@interface VideoRoomReverbCell()
@property (unsafe_unretained, nonatomic) IBOutlet UIImageView *iconView;
@property (unsafe_unretained, nonatomic) IBOutlet UILabel *label;

@end
@implementation VideoRoomReverbCell

- (void)awakeFromNib {
    [super awakeFromNib];
    self.iconView.image = [NSBundle RVLR_pngImageWithName:@"1"];
    self.iconView.layer.cornerRadius = 24;
    self.iconView.layer.borderColor = [UIColor colorWithHex:0x18BFFF].CGColor;
    
}

- (void)setTagStr:(NSString *)tagStr
{
    _tagStr = tagStr;
    self.label.text = tagStr;
}

- (void)setPicked:(BOOL)picked
{
    _picked = picked;
    if (picked) {
        self.iconView.layer.borderWidth = 2;
        self.label.textColor = [UIColor colorWithHex:0x18BFFF];
    } else {
        self.iconView.layer.borderWidth = 0;
        self.label.textColor = [UIColor whiteColor];
    }
    
}

- (void)setImageName:(NSString *)imageName
{
    _imageName = imageName;
    self.iconView.image = [NSBundle RVLR_pngImageWithName:imageName];
}

@end
