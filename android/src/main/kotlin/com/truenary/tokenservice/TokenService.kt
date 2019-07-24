package com.truenary.tokenservice

import android.content.Context
import com.truenary.tokenservice.KeyStoreHelper
import com.truenary.tokenservice.SettingsRepository
import java.io.IOException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException


public class tokenservice(_serverName: String, _accessTokenLabel: String, _refreshTokenLabel: String, _context: Context) {

    private var keyStoreHelper: KeyStoreHelper? = null

    var accessTokenLabel: String
    var refreshTokenLabel: String
    var context: Context;


    init {

        accessTokenLabel = _accessTokenLabel
        refreshTokenLabel = _refreshTokenLabel
        context = _context


        try {
            keyStoreHelper = KeyStoreHelper()
        } catch (e: CertificateException) {
            e.printStackTrace()
            throw e
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            throw e
        } catch (e: KeyStoreException) {
            e.printStackTrace()
            throw e
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }

    }

    public fun getAccessToken(): String?{

        try {

            val ls = keyStoreHelper?.getAllAliasesInTheKeystore()
            val token = ls?.firstOrNull { s -> s.equals(accessTokenLabel) }

            if (token != null) {
                val info = SettingsRepository.getProperty(accessTokenLabel, context)
                val result = keyStoreHelper?.decryptData(accessTokenLabel, info.data!!, info.iv!!)
                return result
            }

            return null
        }
        catch (e: Exception){
            return null
        }
    }

    public fun addOrUpdateAccessToken(token: String): Number{
        try {
            val result = keyStoreHelper?.encryptText(accessTokenLabel, token);
            SettingsRepository.setProperty(accessTokenLabel, result!!, context)
            return 1;
        }
        catch (e:Exception)
        {
            return 0;
        }

    }

    public fun deleteAccessToken(): Number{
        try {
            keyStoreHelper?.deleteKey(accessTokenLabel)
            SettingsRepository.deleteProperty(accessTokenLabel, context)
            return 1
        }
        catch(e:Exception)
        {
            return 0
        }
    }

    public fun getRefreshToken(): String?{

        try {

            val ls = keyStoreHelper?.getAllAliasesInTheKeystore()
            val token = ls?.firstOrNull { s -> s.equals(refreshTokenLabel) }

            if (token != null) {
                val info = SettingsRepository.getProperty(refreshTokenLabel, context)
                val result = keyStoreHelper?.decryptData(refreshTokenLabel, info.data!!, info.iv!!)
                return result
            }

            return null
        }
        catch (e: Exception){
            return null
        }
    }

    public fun addOrUpdateRefreshToken(token: String): Number{
        try {
            val result = keyStoreHelper?.encryptText(refreshTokenLabel, token);
            SettingsRepository.setProperty(refreshTokenLabel, result!!, context)
            return 1;
        }
        catch (e:Exception)
        {
            return 0;
        }

    }

    public fun deleteRefreshToken(): Number{
        try {
            keyStoreHelper?.deleteKey(refreshTokenLabel)
            SettingsRepository.deleteProperty(refreshTokenLabel, context)
            return 1
        }
        catch(e:Exception)
        {
            return 0
        }
    }
}
