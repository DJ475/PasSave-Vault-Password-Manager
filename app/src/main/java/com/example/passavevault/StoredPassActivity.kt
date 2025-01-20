package com.example.passavevault

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var EditTextUsername: EditText

    private lateinit var ButtonSubmit: Button

    private lateinit var buttonGeneratePass: Button

    private lateinit var RecyclerView : RecyclerView

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
//        EditTextUsername = findViewById(R.id.EditTextUsername)

        ButtonSubmit = findViewById(R.id.ButtonEncrypt)

        buttonGeneratePass = findViewById(R.id.ClickStrongPassGEN)

        RecyclerView = findViewById(R.id.RecyclerViewPass)

        usernameGet = intent.getStringExtra("usernameValue").toString()
        userID = intent.getIntExtra("userID",0)

        ReloadItemAdapter(SQLiteDatabase)

        val secretKey = Cipher_E_D.Generate_AESKEY("secretKeyAlias")
        if(secretKey == null)
        {
            Log.d("Error DJ", "Secret Key is null")
        }

        ButtonSubmit.setOnClickListener {
            println("Print Here")

            if(EditTextEncryptPass.text.isNullOrEmpty() || EditTextEncryptSource.text.isNullOrEmpty())
            {
                Toast.makeText(this,"Please Enter Password and Source to Continue",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (secretKey != null) {
                val encrypted = Cipher_E_D().encryptInfo(
                    Base64.encodeToString(EditTextEncryptPass.text.toString().toByteArray(), Base64.DEFAULT),
                    EditTextEncryptSource.text.toString(),
                    userID,
                    secretKey,
                    applicationContext,
                    false
                )
                println("Encrypted String: $encrypted")
            } else {
                Log.e("EncryptionError", "Failed to encrypt. Secret key is null.")
                Toast.makeText(this, "Encryption failed due to missing secret key.", Toast.LENGTH_SHORT).show()
            }

            EditTextEncryptPass.text.clear()
            EditTextEncryptSource.text.clear()

            ReloadItemAdapter(SQLiteDatabase)
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
                if(StringUserInLengthPass.toInt() < 13)
                {
                    Toast.makeText(applicationContext,"Most Secure Passwords Are a Length of 13 or more Characters\n\nPlease Retry",Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
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

    fun ReloadItemAdapter(databaseInstance: SQLiteDatabase)
    {
        var cursor = passSaveDatabaseHelper.SelectSpecific(userID)

        RecyclerView = findViewById(R.id.RecyclerViewPass)

        RecyclerView.adapter = ItemAdapter(cursor)
        RecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        RecyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        ReloadItemAdapter(SQLiteDatabase)
    }
}