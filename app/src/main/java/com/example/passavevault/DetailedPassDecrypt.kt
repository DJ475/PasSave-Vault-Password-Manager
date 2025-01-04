package com.example.passavevault

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DetailedPassDecrypt : AppCompatActivity() {
    private lateinit var textViewPassword: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detailed_pass_decrypt)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        textViewPassword = findViewById(R.id.IdTextviewDecrypted)


        var getPass = intent.getByteArrayExtra("extractedPassword")
        var getIV = intent.getByteArrayExtra("extractedIV")

        if(getPass != null && getIV != null)
        {
            var decryptString = Cipher_E_D().decryptInfo(getPass,getIV)

            if(decryptString.isEmpty())
            {
                textViewPassword.text = "Password Not Found"

            }
            else
            {
                textViewPassword.text = decryptString
            }
        }
        else
        {
            textViewPassword.text = "Password or Iv Ran into an Issue"
        }
    }
}