package com.truenary.tokenservice

import android.content.Context
import android.content.SharedPreferences

// import com.truenary.tokenservice.KeyStoreHelper
import com.truenary.tokenservice.SettingsRepository
import java.io.IOException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException


public class tokenservice(_serverName: String, _accessTokenLabel: String, _refreshTokenLabel: String, _context: Context) {

    var accessTokenLabel: String
    var refreshTokenLabel: String
    var context: Context;
    var encryptor: IKeyStoreHelper;
    var preferences: SharedPreferences;

    init {
        accessTokenLabel = _accessTokenLabel
        refreshTokenLabel = _refreshTokenLabel
        context = _context
        try {
            preferences = context.getSharedPreferences("TokenService", Context.MODE_PRIVATE);
            encryptor = KeyStoreHelper(preferences = preferences, keyWrapper = RsaKeyStoreKeyWrapper(context));
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
            val info = SettingsRepository.getProperty(accessTokenLabel, context)
            val result = encryptor.decrypt(info.data)
            return result

            return null
        }
        catch (e: Exception){
            return null
        }
    }

    public fun addOrUpdateAccessToken(token: String): Number{
        try {
            val result = encryptor.encrypt(token);
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
            val info = SettingsRepository.getProperty(refreshTokenLabel, context)
            val result = encryptor.decrypt(info.data)

            return result
        }
        catch (e: Exception){
            return null
        }
    }

    public fun addOrUpdateRefreshToken(token: String): Number{
        try {
            val result = encryptor.encrypt(token);
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
            SettingsRepository.deleteProperty(refreshTokenLabel, context)
            return 1
        }
        catch(e:Exception)
        {
            return 0
        }
    }
}
