package com.truenary.tokenservice

import android.content.Context
import android.preference.PreferenceManager
import android.util.Base64

/**
 * Created by Sovit on 12/26/2018.
 */


object SettingsRepository {
    fun getProperty(key: String, context: Context): EncryptedInfo {
        val info = EncryptedInfo()

        val result = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key, null)

        info.data = Base64.decode(result,Base64.DEFAULT)

        val iv = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("${key}_iv", null)

        info.iv = Base64.decode(iv,Base64.DEFAULT)

        return info
    }

    fun setProperty(key: String, info: EncryptedInfo, context: Context) {
        val ivString = Base64.encodeToString(info.iv, Base64.DEFAULT)
        val dataString = Base64.encodeToString(info.data, Base64.DEFAULT)

        val settingPref = PreferenceManager.getDefaultSharedPreferences(context).edit()
        settingPref.putString(key, dataString)
        settingPref.apply()

        val settingIvPref = PreferenceManager.getDefaultSharedPreferences(context).edit()
        settingIvPref.putString("${key}_iv", ivString )
        settingIvPref.apply()
    }

    fun deleteProperty(key: String, context: Context) {
        val settingPref = PreferenceManager.getDefaultSharedPreferences(context).edit()
        settingPref.remove(key)
        settingPref.apply()
    }
}