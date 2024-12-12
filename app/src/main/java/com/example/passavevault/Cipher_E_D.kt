package com.example.passavevault

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.OutputStream
import java.security.KeyStore
import java.security.KeyStore.Entry
import java.security.KeyStoreSpi
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.jvm.internal.Ref.ByteRef
import kotlin.math.log

class Cipher_E_D {
    companion object
    {
        // function for generating the aes secret key
        fun Generate_AESKEY(Alias: String): SecretKey? {
//            var AndroidKeyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore")
            var AndroidKeyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)

//            var keyParameterSpec = KeyGenParameterSpec.Builder(Alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
//                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
//                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
//                .setUserAuthenticationRequired(false)
//                .setKeySize(256)
//                .build()
//            AndroidKeyGen.init(keyParameterSpec)

            AndroidKeyGen.generateKey()





            val secretKeyVar = AndroidKeyGen.generateKey()
            println("Secret key is: $secretKeyVar")
            return secretKeyVar
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createIV(): IvParameterSpec
    {
        var RandomSecureIV : SecureRandom = SecureRandom.getInstanceStrong()
        var CipherInstance = Cipher.getInstance("AES/CBC/PKCS5Padding")
        var iv : ByteArray = ByteArray(CipherInstance.blockSize)
        RandomSecureIV.nextBytes(iv)

        return IvParameterSpec(iv)
    }


    fun encryptInfo(dataSent: ByteArray, SecretKey: SecretKey): ByteArray
    {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

        val ivParameterSpecVar = IvParameterSpec(ByteArray(16))
        println("IV is: $ivParameterSpecVar")
        cipher.init(Cipher.ENCRYPT_MODE,SecretKey, ivParameterSpecVar)
        return cipher.doFinal(dataSent)
//        var finalIV = ""
//        val resultIV = cipher.iv
//        val ivString = Base64.encode(resultIV)
//        finalIV += ivString + " "
//
//        return finalIV.toByteArray()

    }

    fun decryptInfo(dataSent: ByteArray, SecretKeyPassed: SecretKey): ByteArray
    {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivParameterSpec = IvParameterSpec(ByteArray(16))
        cipher.init(Cipher.DECRYPT_MODE,SecretKeyPassed, ivParameterSpec)
        return cipher.doFinal(dataSent)
    }
}