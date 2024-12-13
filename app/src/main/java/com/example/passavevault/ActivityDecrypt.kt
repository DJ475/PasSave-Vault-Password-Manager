package com.example.passavevault

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.crypto.SecretKey
import android.util.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class ActivityDecrypt : AppCompatActivity() {
    private lateinit var TextViewPasswordDecrypt: TextView
    private lateinit var TextViewSourceDecrypt: TextView

    private lateinit var SQLiteDatabase: SQLiteDatabase
    private lateinit var passDatabaseHelper: PassSaveDatabaseHelper

    private var getPasswordID: Int = 0

    companion object{
        lateinit var secretKeyPassed: SecretKey
        var userIdPassed : Int = 0
        lateinit var EncryptedString: ByteArray
    }

    @OptIn(ExperimentalEncodingApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_decrypt)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarDecrypt)
        setSupportActionBar(toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        TextViewPasswordDecrypt = findViewById(R.id.TextDecryptPassword)
        TextViewSourceDecrypt = findViewById(R.id.TextDecryptSource)

        passDatabaseHelper = PassSaveDatabaseHelper(applicationContext)
        SQLiteDatabase = passDatabaseHelper.readableDatabase

//        var getPasswordDecrypt = intent.getByteArrayExtra("PasswordEncrypted")
//        getPasswordDecrypt = EncryptedString
        var getPasswordID = intent.getIntExtra("PasswordID",0)

        println("Password passed to ActivtyDecrypt is now: $EncryptedString")

        TextViewPasswordDecrypt.text = "$EncryptedString\nYour id is: $getPasswordID"


        val secretKeyInDecrypt = secretKeyPassed
        println("The Secret Key in ActivityDecrypt used for decryption is $secretKeyInDecrypt")

//        var cursorgetIV = SQLiteDatabase.query(
//            "Iv_Table",
//            arrayOf("iv_id, iv, user_id"),
//            "iv_id = ?",
//            arrayOf(getPasswordID.toString()),
//            null,
//            null,
//            null
//        )

        println("Id of user logged in in decrypt is: $userIdPassed")
        println("ID in Activity Decrypt is : $getPasswordID")

        CoroutineScope(Dispatchers.IO).launch {

//            val cursorgetIV = SQLiteDatabase.rawQuery("" +
//                    "SELECT User.username, UserPassword.passwordEncrypted, Iv_Table.iv\n" +
//                    "FROM User\n" +
//                    "JOIN UserPassword ON User.User_id = UserPassword.User_id\n" +
//                    "JOIN Iv_Table ON UserPassword.Password_id = Iv_Table.password_id\n" +
//                    "WHERE User.user_id = ?", arrayOf(userIdPassed.toString())
//            )

//            val cursorgetIV = SQLiteDatabase.rawQuery("SELECT * " + "FROM Iv_Table " + "WHERE Password_id = ?", arrayOf(userIdPassed.toString()))

            var cursorgetIV = SQLiteDatabase.query(
                "Iv_Table",
                arrayOf("iv_id, iv, Password_id"),
                "iv_id = ?",
                arrayOf(getPasswordID.toString()),
                null,
                null,
                null
            )

            if (cursorgetIV.moveToFirst()) {
                cursorgetIV.moveToFirst()
                var column = cursorgetIV.getColumnIndex("iv")
                var specific_IV = cursorgetIV.getBlob(column)
                println("Cursor came back with: $specific_IV")


                //Cipher_E_D.ivAll = specific_IV
                println("Before Decrypt Functions: $specific_IV")

                var decryptedCipher = Cipher_E_D().decryptInfo(EncryptedString, secretKeyPassed)
                var String_Cipher = String(decryptedCipher)
                println("String Cipher Decrypted is $String_Cipher")

            }
            else{
                println("Cursor empty")
            }
        }

//        TextViewPasswordDecrypt.setOnClickListener {
//            println("Wanna Edit $getPasswordDecrypt")
//
//            val decryptedStringPassword = getPasswordDecrypt?.let { it1 -> Cipher_E_D().decryptInfo(it1.toByteArray(),secretKeyDecryptNew) }
//
//            println("Your Decryped Message is ${decryptedStringPassword.toString()}")
//        }
//
//        TextViewSourceDecrypt.setOnClickListener {
//            println("Wanna Edit ID $getPasswordID")
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_decrypt,menu)
        return true
    }

//    fun getSecretKey(secretKey: SecretKey)
//    {
//        this.secrektKeyDecrypt = secretKey
//        println("It was Passed and the Secret key is: $secretKey.")
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId)
        {
            R.id.DeleteEntry->{
                println("Delete This Password")
                passDatabaseHelper.DeleteSpecific(getPasswordID)
                finish()
                true
            }
            else -> true
        }
    }
}