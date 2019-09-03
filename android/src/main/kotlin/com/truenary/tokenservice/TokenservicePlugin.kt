package com.truenary.tokenservice

import android.os.Build;

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

class TokenServicePlugin: MethodCallHandler {

  private val serverName = "tokenservice.truenary.com"
  private val accessTokenLabel = "ACCESS_TOKEN"
  private val refreshTokenLabel = "REFRESH_TOKEN"



  companion object {
    @JvmStatic
    public var instance: Registrar? = null

    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "tokenservice.truenary.com/tokens")
      channel.setMethodCallHandler(TokenServicePlugin())
      instance = registrar
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    handlePlatformSpecificCall(call, result)
  }


  private fun handlePlatformSpecificCall(call: MethodCall, result:Result):Result {
    try {

      val tokenservice: tokenservice = tokenservice(serverName, accessTokenLabel, refreshTokenLabel, instance!!.context())

      if (call.method == "getAccessToken") {
        result.success(tokenservice.getAccessToken())
      }

      else if (call.method == "addOrUpdateAccessToken") {
        result.success(tokenservice.addOrUpdateAccessToken(call.arguments.toString()))
      }

      else if (call.method == "deleteAccessToken") {
        result.success(tokenservice.deleteAccessToken())
      }

      else if (call.method == "getRefreshToken") {
        result.success(tokenservice.getRefreshToken())
      }

      else if (call.method == "addOrUpdateRefreshToken") {
        result.success(tokenservice.addOrUpdateRefreshToken(call.arguments.toString()))
      }

      else if (call.method == "deleteRefreshToken") {
        result.success(tokenservice.deleteRefreshToken())
      }

      else {
        result.notImplemented()
      }
    }

    catch (ex: Exception)
    {
      result.error("502", "Token Error", null)
    }

    return result
  }
}
