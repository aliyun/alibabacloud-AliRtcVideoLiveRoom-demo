//
//  VideoRoomReverbCell.h
//  RTCVideoLiveRoom
//
//  Created by aliyun on 2020/8/13.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface VideoRoomReverbCell : UICollectionViewCell

@property (copy,nonatomic) NSString *tagStr;

@property (assign,nonatomic) BOOL picked;

@property (copy,nonatomic) NSString *imageName;


@end

NS_ASSUME_NONNULL_END
