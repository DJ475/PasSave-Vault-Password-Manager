package com.example.passavevault

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.annotation.RequiresApi
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

class Cipher_E_D {
    private lateinit var SQLiteDatabase: SQLiteDatabase
    private lateinit var passSaveDatabaseHelper: PassSaveDatabaseHelper
    companion object
    {
        // function for generating the aes secret key
        fun Generate_AESKEY(Alias: String): SecretKey? {
            var AndroidKeyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

            var keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            // Check if the key already exists
            if (keyStore.containsAlias(Alias)) {
                println("Key already exists with alias: $Alias")
                val key = keyStore.getKey(Alias, null)
                if (key is SecretKey) {
                    return key
                } else {
                    println("Key retrieved is not a SecretKey")
                    return null
                }
            }

            var keyParameterSpec = KeyGenParameterSpec.Builder(Alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(false)
                .setKeySize(256)
                .build()
            AndroidKeyGen.init(keyParameterSpec)

            val secretKeyVar = AndroidKeyGen.generateKey()
            return secretKeyVar
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun encryptInfo(
        dataSent: String,
        Source: String,
        userId: Int,
        SecretKey: SecretKey,
        ContextStoredActivity: Context?
    ): ByteArray
    {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")

        cipher.init(Cipher.ENCRYPT_MODE,SecretKey)

        var encryptedStringFinal = cipher.doFinal(dataSent.encodeToByteArray())

        var ivGetKeyStore = cipher.iv

        var delimeterColon = byteArrayOf(0x3A)

        var combinedPassword = encryptedStringFinal + delimeterColon + ivGetKeyStore

        var stringRepresentation = Base64.encodeToString(combinedPassword,Base64.DEFAULT)

        val contentValues = ContentValues().apply {
            put("passwordEncrypted",stringRepresentation)
            put("source_site_password",Source)
            put("User_id",userId)
        }

        passSaveDatabaseHelper = ContextStoredActivity?.let { PassSaveDatabaseHelper(it) }!!
        SQLiteDatabase = passSaveDatabaseHelper.readableDatabase

        SQLiteDatabase.insert("UserPassword",null, contentValues)

        return combinedPassword
    }

    fun decryptInfo(dataSent: ByteArray,iv: ByteArray): String
    {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")

        var keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        var secretKey = keyStore.getKey("secretKeyAlias",null) as SecretKey

        var ivParamSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE,secretKey, ivParamSpec)

        Log.d("Decrypt Info", "Data to decrypt: ${Base64.encodeToString(dataSent, Base64.DEFAULT)}")
        Log.d("Decrypt Info", "IV: ${Base64.encodeToString(iv, Base64.DEFAULT)}")

        var cipherFinalDecrypt = cipher.doFinal(dataSent)
        return String(Base64.decode(cipherFinalDecrypt,Base64.DEFAULT))
    }
}