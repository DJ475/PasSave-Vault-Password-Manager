package com.example.passavevault

import android.content.ContentValues
import android.database.Cursor
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
import javax.crypto.Cipher
import javax.crypto.SecretKey

class StoredPassActivity : AppCompatActivity(), ItemAdapter.ItemAdapterListener {
    private lateinit var SQLiteDatabase: SQLiteDatabase
    private lateinit var passSaveDatabaseHelper: PassSaveDatabaseHelper

    private lateinit var usernameGet: String
    private var userID: Int = 0

    private lateinit var textViewLoginDetails: TextView

    private lateinit var EditTextEncryptPass: EditText
    private lateinit var EditTextEncryptSource: EditText

    private lateinit var ButtonSubmit: Button

    private lateinit var buttonGeneratePass: Button

    lateinit var RecyclerView: RecyclerView

    lateinit var itemAdapter: ItemAdapter

    private lateinit var cursor: Cursor

    private lateinit var secretKey: SecretKey

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

        RecyclerView = findViewById(R.id.RecyclerViewPass)


        usernameGet = intent.getStringExtra("usernameValue").toString()
        userID = intent.getIntExtra("userID",0)

        val secretKey = Cipher_E_D.Generate_AESKEY("secretKeyAlias")
        if (secretKey != null) {
            ActivityDecrypt.secretKeyPassed = secretKey
        }

        println("Secret key passing to Activity Decrypt: $secretKey")

        cursor = SQLiteDatabase.query(
            "UserPassword",
            arrayOf("Password_id","passwordEncrypted","source_site_password","User_id"),
            "User_id = ?",
            arrayOf(userID.toString()),
            null, // where statements
            null, // where statements
            null,
            null
        )

        cursor.moveToFirst()

        // pass cursor to item adapter as well as item adapter listener
        updateItemAdapter()

        ButtonSubmit.setOnClickListener {
            if(EditTextEncryptSource.text.toString().isEmpty())
            {
                Toast.makeText(applicationContext,"Source/Website of Password", Toast.LENGTH_LONG).show()
                println("Source/Website of Password")
                return@setOnClickListener
            }
            if(EditTextEncryptPass.text.toString().isEmpty())
            {
                Toast.makeText(applicationContext,"Password Entry Is Empty, Please Enter Valid Input", Toast.LENGTH_LONG).show()
                println("Password Entry Is Empty, Please Enter Valid Input")
                return@setOnClickListener
            }
            val EncryptedStringPass = secretKey?.let { it1 -> Cipher_E_D().encryptInfo(EditTextEncryptPass.text.toString().toByteArray(), it1, applicationContext, userID) }

//            val cipherDe = EncryptedStringPass?.let { it1 -> Cipher_E_D().decryptInfo(it1, secretKey) }
//            println("Decrypted String is ${cipherDe.toString()}")

            val contentValues = ContentValues().apply {
                put("passwordEncrypted",EncryptedStringPass)
                put("source_site_password",EditTextEncryptSource.text.toString())
                put("User_id",userID)
            }

            SQLiteDatabase.insert("UserPassword",null, contentValues)

            updateItemAdapter()

            clearUserInput()

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
            builder.setTitle("Please Enter Length Of Strong Password\n")
            builder.setMessage("Note that the Lowest is 7 Characters")

            val viewEditText = layoutInflater.inflate(R.layout.edit_text_layout, null)

            val getUserIN = viewEditText.findViewById<EditText>(R.id.EditTextStrongPassLen)

            getUserIN.setTextColor(R.color.white)
            getUserIN.setBackgroundColor(R.color.black)

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
                CoroutineScope(Dispatchers.IO).launch {
                    UpdateLoginOperation()
                }

                clearUserInput()

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

    fun clearUserInput()
    {
        EditTextEncryptPass.text.clear()
        EditTextEncryptSource.text.clear()
    }

    fun updateItemAdapter()
    {
        // pass cursor to item adapter as well as item adapter listener
        var newSelect = PassSaveDatabaseHelper(applicationContext).SelectSpecific(userID)

        itemAdapter = ItemAdapter(newSelect, this)
        RecyclerView.adapter = itemAdapter
        RecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        itemAdapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        updateItemAdapter()
    }

    override fun clicked(position: Int) {
//        var encryptedStringSource = cursorFind.getString(column)
//
//        println("Your Password Pressed" + encryptedStringPass)
//        println("Your Source Pressed" + encryptedStringSource)
//
//        var decryptedStringSource = encryptedStringSource?.let { it2 -> Cipher_E_D().decryptInfo(it2.toByteArray(), secretKey) }
//
//        println("Decrypted String is: " + decryptedStringSource.toString())
        //            var DecryptedStringPassword =
//
//
//
//            println("Decrypted String PAss: $DecryptedStringPassword")
//            var decyrpt_text = java.lang.String(DecryptedStringPassword)
//            println("Decrypt TExt: $decyrpt_text")
    }
}