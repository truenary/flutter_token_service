package com.truenary.tokenservice

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import java.util.*
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec

/**
 * Created by Sovit on 12/26/2018.
 */
class KeyStoreHelper{

    private var keyStore: KeyStore? = null

    init {
        initKeyStore()
    }

    public fun getAllAliasesInTheKeystore(): ArrayList<String> {
        return Collections.list(keyStore?.aliases())
    }

    public fun deleteKey(alias:String) {
        keyStore?.deleteEntry(alias)
    }


    @Throws(KeyStoreException::class, CertificateException::class, NoSuchAlgorithmException::class, IOException::class)
    private fun initKeyStore() {
        keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore!!.load(null)
    }

    @Throws(UnrecoverableEntryException::class, NoSuchAlgorithmException::class, KeyStoreException::class,
            NoSuchProviderException::class, NoSuchPaddingException::class, InvalidKeyException::class,
            IOException::class, BadPaddingException::class, IllegalBlockSizeException::class,
            InvalidAlgorithmParameterException::class)
    fun decryptData(alias: String, encryptedData: ByteArray, encryptionIv: ByteArray): String {

        val secretKeyEntry = keyStore?.getEntry(alias, null) as KeyStore.SecretKeyEntry

        val secretKey = secretKeyEntry.secretKey

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, encryptionIv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val result = cipher.doFinal(encryptedData)

        return String(result, Charsets.UTF_8)
    }

    @Throws(UnrecoverableEntryException::class, NoSuchAlgorithmException::class, KeyStoreException::class,
            NoSuchProviderException::class, NoSuchPaddingException::class, InvalidKeyException::class,
            IOException::class, InvalidAlgorithmParameterException::class,
            SignatureException::class, BadPaddingException::class, IllegalBlockSizeException::class)

    fun encryptText(alias: String, textToEncrypt: String): EncryptedInfo {

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(alias))


        val iv = cipher.getIV()

        val encryptedByte = cipher.doFinal(textToEncrypt.toByteArray(Charsets.UTF_8))

        val info = EncryptedInfo()
        info.iv = iv
        info.data = encryptedByte
        return info
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class, InvalidAlgorithmParameterException::class)
    private fun getSecretKey(alias: String): SecretKey {

        val keyGenerator = KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        keyGenerator.init(KeyGenParameterSpec.Builder(alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build())

        return keyGenerator.generateKey()
    }

    companion object {

        private val TRANSFORMATION = "AES/GCM/NoPadding"
        private val ANDROID_KEY_STORE = "AndroidKeyStore"
    }
}