package com.example.passavevault

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class StoredPassActivity : AppCompatActivity() {
    private lateinit var SQLiteDatabase: SQLiteDatabase
    private lateinit var passSaveDatabaseHelper: PassSaveDatabaseHelper

    private lateinit var usernameGet: String
    private var userID: Int = 0

    private lateinit var textViewLoginDetails: TextView

    private lateinit var EditTextEncryptPass: EditText
    private lateinit var EditTextEncryptSource: EditText

    private lateinit var ButtonSubmit: Button

    private lateinit var buttonGeneratePass: Button

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stored_pass)

        var toobar2 = findViewById<Toolbar>(R.id.toolbarActivityStore)
        setSupportActionBar(toobar2)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        passSaveDatabaseHelper = PassSaveDatabaseHelper(applicationContext)
        SQLiteDatabase = passSaveDatabaseHelper.readableDatabase

        textViewLoginDetails = findViewById(R.id.TextViewLoginDetails)

        EditTextEncryptPass = findViewById(R.id.EncryptPassword)
        EditTextEncryptSource = findViewById(R.id.EncryptSourcePassword)
        ButtonSubmit = findViewById(R.id.ButtonEncrypt)

        buttonGeneratePass = findViewById(R.id.ClickStrongPassGEN)

        usernameGet = intent.getStringExtra("usernameValue").toString()
        userID = intent.getIntExtra("userID",0)

        val secretKey = Cipher_E_D.Generate_AESKEY("secretKeyAlias")
        println("Secret key: $secretKey")

        ButtonSubmit.setOnClickListener {
            println("Print Here")

            val EncryptedStringPass = secretKey?.let { it1 ->
                Cipher_E_D().encryptInfo(
                    EditTextEncryptPass.text.toString().toByteArray(),
                    it1
                )
            }

            val EncryptedSource = secretKey?.let { it1 ->
                Cipher_E_D().encryptInfo(
                    EditTextEncryptSource.text.toString().toByteArray(),
                    it1
                )
            }

            println("Encrypted String PAss: $EncryptedStringPass")

            val contentValues = ContentValues().apply {
                put("passwordEncrypted",EncryptedStringPass.toString())
                put("source_site_password",EncryptedSource.toString())
                put("User_id",userID)
            }

            SQLiteDatabase.insert("UserPassword",null, contentValues)


//            var DecryptedStringPassword =
//                EncryptedStringPass?.let { it1 -> Cipher_E_D().decryptInfo(it1, secretKey) }
//
//
//
//            println("Decrypted String PAss: $DecryptedStringPassword")
//            var decyrpt_text = java.lang.String(DecryptedStringPassword)
//            println("Decrypt TExt: $decyrpt_text")
        }

        buttonGeneratePass.setOnClickListener {
            // Source: https://www.digitalocean.com/community/tutorials/android-alert-dialog-using-kotlin
            // Get EditText for taking userInput from user in alertDialog
            val builder = AlertDialog.Builder(this)
            var StringUserInLengthPass = ""
            builder.setTitle("Please Enter Length Of Strong Password\n(Note That 10 Characters and Above is a Strong Default Password")

            val viewEditText = layoutInflater.inflate(R.layout.edit_text_layout, null)

            val getUserIN = viewEditText.findViewById<EditText>(R.id.EditTextStrongPassLen)

            builder.setView(viewEditText)
            builder.setPositiveButton("Enter") { dialog, which ->
                StringUserInLengthPass = getUserIN.text.toString()
                println(StringUserInLengthPass)

                CoroutineScope(Dispatchers.IO).launch {
                    APIStrongPass(StringUserInLengthPass)
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                println("User Cancelled Action")
            }

            builder.show()
        }

        if (usernameGet.isEmpty()) {
            textViewLoginDetails.visibility - View.VISIBLE
            textViewLoginDetails.text = "Login Failed User Not Found"
        } else {
            textViewLoginDetails.visibility - View.VISIBLE
            textViewLoginDetails.text = "Login Success"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_store, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.Logout_Icon -> {
                var contentValuesLogOut = ContentValues().apply {
                    put("login_status", "LOGGED OUT")
                }

                CoroutineScope(Dispatchers.IO).launch {
                    UpdateLoginOperation()
                }

                finish()
                // look into this for destorying activity and logging all users out
                // finishActivity()

                // Idea: make it so they cant go back and login to new user unless current user is logged out
                // this helps create more security
                true
            }
            // TO DO
            else -> true
        }
    }

    private suspend fun UpdateLoginOperation() {
//        PassSaveDatabaseHelper(applicationContext).UpdateUserLoginStatus(usernameGet,contentValuesLogOut)
        var contentValuesLogOut = ContentValues().apply {
            put("login_status", "LOGGED OUT")
        }
        SQLiteDatabase?.update("User", contentValuesLogOut, "username = ?", arrayOf(usernameGet))
        println("$usernameGet Updating to login status : $contentValuesLogOut")
    }

    override fun onStop() {
        super.onStop()
        println("Logging every one out now")
    }

    suspend fun APIStrongPass(UserInputLengthPass: String) {
        println("Its working Now doing api stuff")

        withContext(Dispatchers.Main)
        {
            println("Length Chosen is : $UserInputLengthPass")

            if(UserInputLengthPass.toInt() <= 7)
            {
                Toast.makeText(applicationContext,"Typically a Strong Password is greater than 7 characters ${UserInputLengthPass.length}",Toast.LENGTH_LONG).show()
                println("Typically a Strong Password is greater than 7 characters $UserInputLengthPass")
            }
        }


        var httpURLConnection : HttpURLConnection? = null


        try {
            var result = ""

            var url = URL("https://api.genratr.com/?length=$UserInputLengthPass&uppercase&lowercase&special&numbers")
            httpURLConnection = url.openConnection() as HttpURLConnection

            httpURLConnection?.requestMethod = "GET" // this can comeback as having a bad request


            if (httpURLConnection?.responseCode != HttpURLConnection.HTTP_OK) {
                result = "BAD CONNECTION"
                println("$result")
            }
            else
            {
                val inputStreamReader = httpURLConnection.inputStream
                val bufferReader = BufferedReader(InputStreamReader(inputStreamReader))
                result = bufferReader.readText()

                val jsonObject = JSONObject(result)
                println(jsonObject)

                var passwordGen = jsonObject.getString("password")

                println("Password Found is $passwordGen")

                withContext(Dispatchers.Main)
                {
                    //change Edit Text for User to Come up With a Secure Password For them
                    EditTextEncryptPass.setText(passwordGen)
                }

                bufferReader.close()
            }
        }
        catch (e: IOException)
        {
            e.printStackTrace()
        }
        finally
        {
            httpURLConnection?.disconnect()
        }
    }

}