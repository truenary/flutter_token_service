#import "TokenServicePlugin.h"
#import <token_service/token_service-Swift.h>

@implementation TokenServicePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftTokenServicePlugin registerWithRegistrar:registrar];
}
@end
