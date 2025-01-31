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
import android.widget.Toast

class Cipher_E_D {
    private lateinit var SQLiteDatabase: SQLiteDatabase
    private lateinit var passSaveDatabaseHelper: PassSaveDatabaseHelper

    private lateinit var dataSent: String
    private lateinit var Source: String
    private var userId: Int = 0
    private lateinit var SecretKey: SecretKey
    private lateinit var ContextActivity: Context

    private lateinit var dataSentDecrypt: ByteArray
    private lateinit var ivDecrypt: ByteArray

    companion object
    {
        // function for generating the aes secret key
        fun Generate_AESKEY(Alias: String): SecretKey? {
            var AndroidKeyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

            var keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            // Check if the key already exists
            if (keyStore.containsAlias(Alias)) {
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
    public fun setDataEncrypt(
        dataSent: String,
        Source: String,
        userId: Int,
        SecretKey: SecretKey,
        ContextActivity: Context?,
    )
    {

        this.dataSent = dataSent
        this.Source = Source
        this.userId = userId
        this.SecretKey = SecretKey
        // encryption is kept to only encrypt when all values are set
        if (ContextActivity != null && dataSent != null && Source != null && userId != null && SecretKey != null) {
            this.ContextActivity = ContextActivity
            encryptionMechanismStart()
        }
        else
        {
            Toast.makeText(ContextActivity, "Data Setting was Unsuccessful, Encryption Failed",Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    // Function used to call encryption Mechanism to do work, in order to add more security
    // and make it so that encryption cannot be used outside of this class
    private fun encryptionMechanismStart(): ByteArray
    {
        return encryptInfo()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun encryptInfo(): ByteArray
    {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")

        cipher.init(Cipher.ENCRYPT_MODE,SecretKey)

        var encryptedStringFinal = cipher.doFinal(dataSent.encodeToByteArray())

        var ivGetKeyStore = cipher.iv

        var delimeterColon = byteArrayOf(0x3A)

        var combinedPassword = encryptedStringFinal + delimeterColon + ivGetKeyStore

        //var stringRepresentation = Base64.encodeToString(combinedPassword,Base64.DEFAULT)


            val contentValues = ContentValues().apply {
                put("passwordEncrypted",combinedPassword)
                put("source_site_password",Source)
                put("User_id",userId)
            }

            passSaveDatabaseHelper = ContextActivity?.let { PassSaveDatabaseHelper(it) }!!
            SQLiteDatabase = passSaveDatabaseHelper.readableDatabase

            SQLiteDatabase.insert("UserPassword",null, contentValues)

        return combinedPassword
    }

    public fun setDataDecrypt(dataSent: ByteArray,iv: ByteArray): String
    {
        var decryptedString = ""
        this.dataSentDecrypt = dataSent
        this.ivDecrypt = iv
        if(dataSent != null && iv != null)
        {
            decryptedString = decryptionMechanismStart()
        }
        else
        {
            Toast.makeText(ContextActivity, "Data Setting was Unsuccessful, Decryption Failed",Toast.LENGTH_LONG).show()
        }

        return decryptedString
    }

    private fun decryptionMechanismStart():String
    {
        return decryptInfo()
    }

    private fun decryptInfo(): String
    {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")

        var keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        var secretKey = keyStore.getKey("secretKeyAlias",null) as SecretKey

        var ivParamSpec = IvParameterSpec(ivDecrypt)
        cipher.init(Cipher.DECRYPT_MODE,secretKey, ivParamSpec)

        Log.d("Decrypt Info", "Data to decrypt: ${Base64.encodeToString(dataSentDecrypt, Base64.DEFAULT)}")
        Log.d("Decrypt Info", "IV: ${Base64.encodeToString(ivDecrypt, Base64.DEFAULT)}")

        var cipherFinalDecrypt = cipher.doFinal(dataSentDecrypt)
        return String(Base64.decode(cipherFinalDecrypt,Base64.DEFAULT))
    }
}