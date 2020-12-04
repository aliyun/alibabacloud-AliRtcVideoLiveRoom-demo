#
# Be sure to run `pod lib lint RTCVideoLiveRoom.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'RTCVideoLiveRoom'
  s.version          = '0.1.0'
  s.summary          = 'A short description of RTCVideoLiveRoom.'

# This description is used to generate tags and improve search results.
#   * Think: What does it do? Why did you write it? What is the focus?
#   * Try to keep it short, snappy and to the point.
#   * Write the description between the DESC delimiters below.
#   * Finally, don't worry about the indent, CocoaPods strips it!

  s.description      = <<-DESC
TODO: Add long description of the pod here.
                       DESC

  s.homepage         = 'https://github.com/aliyunvideo/RTCVideoLiveRoom'
  # s.screenshots     = 'www.example.com/screenshots_1', 'www.example.com/screenshots_2'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'aliyunvideo' => '' }
  s.source           = { :git => '', :tag => s.version.to_s }

  s.ios.deployment_target = '9.0'

  s.source_files = 'RTCVideoLiveRoom/**/*.{h,m,mm}'
  
  s.resources  = 'RTCVideoLiveRoom/RTCVideoLiveRoom.bundle','RTCVideoLiveRoom/*.storyboard'
  
  s.pod_target_xcconfig = { 'ENABLE_BITCODE' => 'NO' }

  s.dependency  'RTCCommon'
  s.dependency  'RTCCommonView'
  s.dependency  'AliRTCSdk'
  s.dependency  'MJRefresh'
  s.dependency  'SDWebImage','5.8.1'
  s.dependency  'MJExtension'
  s.dependency  'JXCategoryView','1.5.6'
  s.dependency  'AliPlayerSDK_iOS','4.7.5'
end
