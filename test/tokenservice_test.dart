import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:token_service/token_service.dart';

const String _serverName = "tokenservice.truenary.com";

void main() {
  String accessToken;
  String refreshToken;

  const MethodChannel channel = MethodChannel('$_serverName/tokens');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      if (methodCall.method == 'addOrUpdateAccessToken') {
        accessToken = methodCall.arguments.toString();
        return null;
      }
      if (methodCall.method == 'getAccessToken') return accessToken;

      if (methodCall.method == 'deleteAccessToken') {
        accessToken = null;
        return null;
      }


      if (methodCall.method == 'addOrUpdateRefreshToken') {
        refreshToken = methodCall.arguments.toString();
        return null;
      }
      if (methodCall.method == 'getRefreshToken') return refreshToken;

      if (methodCall.method == 'deleteRefreshToken') {
        refreshToken = null;
        return null;
      }
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  group('Access Token Service Tests', () {
    test('addOrUpdateAccessToken', () async {
      await TokenService().addOrUpdateAccessToken('access-token');
      expect(await TokenService().getAccessToken(), 'access-token');
    });
    test('getAccessToken', () async {
      expect(await TokenService().getAccessToken(), 'access-token');
    });
    test('deleteAccessToken', () async {
      await TokenService().deleteAccessToken();
      expect(await TokenService().getAccessToken(), null);
    });
  });


  group('Refresh Token Service Tests', () {
    test('addOrUpdateRefreshToken', () async {
      await TokenService().addOrUpdateRefreshToken('refresh-token');
      expect(await TokenService().getRefreshToken(), 'refresh-token');
    });
    test('getRefreshToken', () async {
      expect(await TokenService().getRefreshToken(), 'refresh-token');
    });
    test('deleteRefreshToken', () async {
      await TokenService().deleteRefreshToken();
      expect(await TokenService().getRefreshToken(), null);
    });
  });
}
