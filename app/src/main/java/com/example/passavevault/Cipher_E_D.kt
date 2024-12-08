package com.example.passavevault

import java.security.KeyStore
import java.util.Properties
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class Cipher_E_D {
    companion object
    {
        // function for generating the aes key
        fun Generate_AESKEY(keySize: Int): SecretKey
        {
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(keySize)
                return keyGenerator.generateKey()
        }
    }

    fun storeKey(SecretKeyPassed: SecretKey)
    {
        var AndroidKeyStore = KeyStore.getInstance("AndroidKeyStore")
            .setKeyEntry("secret_key",SecretKeyPassed,null,null)
    }


    fun encryptInfo(dataSent: ByteArray, SecretKey: SecretKey): ByteArray
    {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivParameterSpec = IvParameterSpec(dataSent)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKey,ivParameterSpec)
        return cipher.doFinal(dataSent)
    }

    fun decryptInfo()
    {

    }
}