package com.example.passavevault

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.security.keystore.KeyProperties
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import android.util.Base64
import java.security.SecureRandom

class Cipher_E_D {
    lateinit var SQLiteDatabase : SQLiteDatabase
    lateinit var passSaveDatabaseHelper: PassSaveDatabaseHelper


    companion object
    {
        lateinit var ivAll: IvParameterSpec

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
            ActivityDecrypt.secretKeyPassed = secretKeyVar
            return secretKeyVar
        }

//        lateinit var ivAll?: IvParameterSpec
    }

    fun encryptInfo(
        dataSent: ByteArray,
        SecretKey: SecretKey,
        applicationContext: Context,
        userID: Int
    ): ByteArray
    {
        passSaveDatabaseHelper = PassSaveDatabaseHelper(applicationContext)
        SQLiteDatabase = passSaveDatabaseHelper.readableDatabase

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

        val ivParameterEmptyByte = ByteArray(16)
        SecureRandom().nextBytes(ivParameterEmptyByte)

        println("Password ID founnnddd $userID")

        var ivParameterSpecVar = IvParameterSpec(ivParameterEmptyByte)
        ivAll = ivParameterSpecVar
        val conentValsIV = ContentValues().apply {
            put("iv", ivParameterEmptyByte)
            put("Password_id", userID)
        }

        // insert iv in database for decryption
        SQLiteDatabase.insert("Iv_Table",null,conentValsIV)

        cipher.init(Cipher.ENCRYPT_MODE,SecretKey, IvParameterSpec(ivParameterEmptyByte))
        return cipher.doFinal(dataSent)
    }

    fun decryptInfo(dataSent: ByteArray, SecretKeyPassed: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        println("IV in decrypt function is: $ivAll")
        cipher.init(Cipher.DECRYPT_MODE,SecretKeyPassed, ivAll)
        return cipher.doFinal(dataSent)
    }
}