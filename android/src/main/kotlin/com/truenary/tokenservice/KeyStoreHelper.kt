package com.truenary.tokenservice

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.*
import java.security.spec.AlgorithmParameterSpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal


interface KeyWrapper {
    @Throws(Exception::class)
    fun wrap(key: Key): ByteArray;

    @Throws(Exception::class)
    fun unwrap(wrappedKey: ByteArray, algorithm: String): Key;
}

class RsaKeyStoreKeyWrapper(context: Context) : KeyWrapper {

    private val keyAlias: String
    private val context: Context
    private val TYPE_RSA = "RSA"
    private val KEYSTORE_PROVIDER_ANDROID = "AndroidKeyStore"

    init {
        this.keyAlias = context.packageName
        this.context = context
        createRSAKeysIfNeeded()
    }

    @Throws(Exception::class)
    override fun wrap(key: Key): ByteArray {
        val publicKey = getPublicEntry()
        val cipher = getRSACipher()
        cipher.init(Cipher.WRAP_MODE, publicKey)
        return cipher.wrap(key)
    }

    @Throws(Exception::class)
    override fun unwrap(wrappedKey: ByteArray, algorithm: String): Key {
        val privateKey = getPrivateEntry()
        val cipher = getRSACipher()
        cipher.init(Cipher.UNWRAP_MODE, privateKey)

        return cipher.unwrap(wrappedKey, algorithm, Cipher.SECRET_KEY)
    }

    @Throws(Exception::class)
    fun encrypt(input: ByteArray): ByteArray {
        val publicKey = getPublicEntry()
        val cipher = getRSACipher()
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        return cipher.doFinal(input)
    }

    @Throws(Exception::class)
    fun decrypt(input: ByteArray): ByteArray {
        val privateKey = getPrivateEntry()
        val cipher = getRSACipher()
        cipher.init(Cipher.DECRYPT_MODE, privateKey)

        return cipher.doFinal(input)
    }

    @Throws(Exception::class)
    private fun getPublicEntry(): PublicKey {
        val ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID)
        ks.load(null)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ks.getCertificate(keyAlias).publicKey
        }
        val asymmetricKey = ks.getEntry(keyAlias, null) as KeyStore.PrivateKeyEntry
        return asymmetricKey.certificate.publicKey


    }

    @Throws(Exception::class)
    private fun getPrivateEntry(): PrivateKey {
        val ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID)
        ks.load(null)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ks.getKey(keyAlias, null) as PrivateKey
        }
        val asymmetricKey = ks.getEntry(keyAlias, null) as KeyStore.PrivateKeyEntry
        return asymmetricKey.privateKey

    }

    @Throws(Exception::class)
    private fun getRSACipher(): Cipher {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL") // error in android 6: InvalidKeyException: Need RSA private or public key
        } else {
            Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidKeyStoreBCWorkaround") // error in android 5: NoSuchProviderException: Provider not available: AndroidKeyStoreBCWorkaround
        }
    }

    @Throws(Exception::class)
    private fun createRSAKeysIfNeeded() {
        val ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID)
        ks.load(null)

        var entry: Key? = null
        for (i in 1..5) {
            try {
                entry = getPrivateEntry();
                break;
            } catch (ignored: Exception) {
            }
        }

        if (entry == null) {
            createKeys();
            try {
                entry = getPrivateEntry();
            } catch (ignored: Exception) {
                ks.deleteEntry(keyAlias);
            }
            if (entry == null) {
                createKeys();
            }
        }
    }

    @SuppressLint("NewApi")
    @Throws(Exception::class)
    private fun createKeys() {
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 25)

        val kpGenerator = KeyPairGenerator.getInstance(TYPE_RSA, KEYSTORE_PROVIDER_ANDROID)

        val spec: AlgorithmParameterSpec

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

      spec = android.security.KeyPairGeneratorSpec.Builder( context)
              .setAlias(keyAlias)
              .setSubject(X500Principal("CN=$keyAlias"))
              .setSerialNumber(BigInteger.valueOf(1))
              .setStartDate(start.time)
              .setEndDate(end.time)
              .build()
    } else {
        spec = KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT)
                .setCertificateSubject(X500Principal("CN=$keyAlias"))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setUserAuthenticationRequired(false)
                .setCertificateSerialNumber(BigInteger.valueOf(1))
                .setCertificateNotBefore(start.time)
                .setCertificateNotAfter(end.time)
                .build()
    }
        kpGenerator.initialize(spec)
        kpGenerator.generateKeyPair()
    }

}

interface IKeyStoreHelper {
    @Throws(Exception::class)
    fun encrypt(input: String): EncryptedInfo?

    @Throws(Exception::class)
    fun decrypt(input: ByteArray?): String?
}

class KeyStoreHelper
@Throws(Exception::class) constructor(preferences: SharedPreferences, keyWrapper: KeyWrapper) : IKeyStoreHelper {

    private val ivSize = 16
    private val keySize = 16
    private val KEY_ALGORITHM = "AES"
    private val WRAPPED_AES_KEY_ITEM = "W0n5hlJtrAH0K8mIreDGxtG"
    private val charset: Charset = Charset.forName("UTF-8")
    private val secureRandom: SecureRandom = SecureRandom()

    private var secretKey: Key
    private val cipher: Cipher

    init {
        val wrappedAesKey = preferences.getString(WRAPPED_AES_KEY_ITEM, null)

        if (wrappedAesKey == null) {
            secretKey = createKey(preferences, keyWrapper)
        } else {
            val encrypted = Base64.decode(wrappedAesKey, Base64.DEFAULT)
            try {
                secretKey = keyWrapper.unwrap(encrypted, KEY_ALGORITHM)
            } catch (ingnored: Exception) {
                secretKey = createKey(preferences, keyWrapper)
            }
        }
        cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
    }

    fun createKey(preferences: SharedPreferences, keyWrapper: KeyWrapper): Key {
        val key = ByteArray(keySize)
        secureRandom.nextBytes(key)
        val secretKey = SecretKeySpec(key, KEY_ALGORITHM)
        preferences
                .edit()
                .putString(WRAPPED_AES_KEY_ITEM, Base64.encodeToString(keyWrapper.wrap(secretKey), Base64.DEFAULT))
                .commit()
        return secretKey
    }

    @Throws(Exception::class)
    override fun encrypt(input: String): EncryptedInfo {

        val iv = ByteArray(ivSize)
        secureRandom.nextBytes(iv)

        val ivParameterSpec = IvParameterSpec(iv)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)

        val payload = cipher.doFinal(input.toByteArray(charset))
        val combined = ByteArray(iv.size + payload.size)

        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(payload, 0, combined, iv.size, payload.size)

        val info = EncryptedInfo()
        info.iv = iv
        info.data = combined
        return info

        // return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    @Throws(Exception::class)
    override fun decrypt(inputBytes: ByteArray?): String? {

        if(inputBytes == null){
            return null
        }
        val iv = ByteArray(ivSize)
        System.arraycopy(inputBytes, 0, iv, 0, iv.size)
        val ivParameterSpec = IvParameterSpec(iv)

        val payloadSize = inputBytes.size - ivSize
        val payload = ByteArray(payloadSize)
        System.arraycopy(inputBytes, iv.size, payload, 0, payloadSize)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
        val outputBytes = cipher.doFinal(payload)
        return String(outputBytes, charset)
    }
}
