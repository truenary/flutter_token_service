import 'dart:async';

import 'package:flutter/services.dart';

const String _serverName = "tokenservice.truenary.com";

abstract class TokenServiceBase {
  Future<String> getAccessToken();
  Future<String> getRefreshToken();
  Future<void> addOrUpdateAccessToken(String token);
  Future<void> addOrUpdateRefreshToken(String token);
  Future<void> deleteAccessToken();
  Future<void> deleteRefreshToken();
}

class TokenService implements TokenServiceBase {
  static MethodChannel tokenChannel = MethodChannel('$_serverName/tokens');

  Future<String> getAccessToken() async {
    final String accessToken =
        await tokenChannel.invokeMethod('getAccessToken');
    return accessToken;
  }

  Future<String> getRefreshToken() async {
    final String refreshToken =
        await tokenChannel.invokeMethod('getRefreshToken');
    return refreshToken;
  }

  Future<void> addOrUpdateAccessToken(String token) async {
    await tokenChannel.invokeMethod('addOrUpdateAccessToken', token);
    return;
  }

  Future<void> addOrUpdateRefreshToken(String token) async {
    await tokenChannel.invokeMethod('addOrUpdateRefreshToken', token);
    return;
  }

  Future<void> deleteAccessToken() async {
    await tokenChannel.invokeMethod('deleteAccessToken');
    return;
  }

  Future<void> deleteRefreshToken() async {
    await tokenChannel.invokeMethod('deleteRefreshToken');
    return;
  }
}
