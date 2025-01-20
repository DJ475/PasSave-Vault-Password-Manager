package com.example.passavevault

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.security.KeyStore
import javax.crypto.SecretKey

class DetailedPassDecrypt : AppCompatActivity() {
    private lateinit var editTextPassword: EditText
    private lateinit var editTextSource: EditText
    private lateinit var buttonDelete: Button

    private lateinit var passSaveDatabaseHelper: PassSaveDatabaseHelper
    private lateinit var SQLiteDatabase: SQLiteDatabase

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detailed_pass_decrypt)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        editTextSource = findViewById(R.id.IdTextviewSource)
        editTextPassword = findViewById(R.id.IdTextviewDecrypted)
        buttonDelete = findViewById(R.id.ButtonDelete)

        passSaveDatabaseHelper = PassSaveDatabaseHelper(applicationContext)
        SQLiteDatabase = passSaveDatabaseHelper.readableDatabase

        var getPass = intent.getByteArrayExtra("extractedPassword")
        var getIV = intent.getByteArrayExtra("extractedIV")
        var Password_id = intent.getIntExtra("passPSWD_ID",0)
        var source = intent.getStringExtra("source")
        var userID = intent.getIntExtra("passUser_ID",0)

        // Set click listener for the delete button
        buttonDelete.setOnClickListener {
            // Get the password ID to delete
            println("Password ID to delete: $Password_id")
            passSaveDatabaseHelper.DeleteSpecific(Password_id)
            finish()
        }

        if(getPass != null && getIV != null)
        {
            var decryptString = Cipher_E_D().decryptInfo(getPass,getIV)

            if(decryptString.isEmpty())
            {
                Toast.makeText(this, "Password and Source Not Found", Toast.LENGTH_SHORT).show()
            }
            else
            {
                editTextPassword.setText(decryptString)
                editTextSource.setText(source)
            }
        }
        else
        {
            Toast.makeText(this, "Password or Iv Ran into an Issue", Toast.LENGTH_SHORT).show()
        }
    }
}