package com.truenary.tokenservice_example

import android.os.Bundle

import io.flutter.app.FlutterActivity
import io.flutter.plugins.GeneratedPluginRegistrant
import com.truenary.tokenservice.TokenServicePlugin

class EmbedderV1Activity: FlutterActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    TokenServicePlugin.registerWith(registrarFor("com.truenary.tokenservice.TokenServicePlugin"));
  }
}
