package com.truenary.tokenservice

import android.os.Build;

import android.content.Context
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.embedding.engine.plugins.FlutterPlugin

class TokenServicePlugin: MethodCallHandler, FlutterPlugin {

  private val serverName = "tokenservice.truenary.com"
  private val accessTokenLabel = "ACCESS_TOKEN"
  private val refreshTokenLabel = "REFRESH_TOKEN"
  private var methodChannel: MethodChannel? = null
  private var applicationContext: Context? = null

  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val pluginInstance = TokenServicePlugin()
      pluginInstance.onAttachedToEngine(registrar.context(), registrar.messenger())
    }
  }

  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    onAttachedToEngine(binding.getApplicationContext(), binding.getBinaryMessenger())
  }

  private fun onAttachedToEngine(context: Context, messenger: BinaryMessenger) {
    applicationContext = context
    methodChannel = MethodChannel(messenger, "tokenservice.truenary.com/tokens")
    methodChannel!!.setMethodCallHandler(this)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    applicationContext = null
    methodChannel!!.setMethodCallHandler(null)
    methodChannel = null
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    handlePlatformSpecificCall(call, result)
  }


  private fun handlePlatformSpecificCall(call: MethodCall, result:Result):Result {
    try {

      val tokenservice: tokenservice = tokenservice(serverName, accessTokenLabel, refreshTokenLabel, applicationContext!!)

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
