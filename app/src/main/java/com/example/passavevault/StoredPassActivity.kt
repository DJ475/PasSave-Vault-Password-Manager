package com.example.passavevault

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StoredPassActivity : AppCompatActivity() {
    private lateinit var SQLiteDatabase: SQLiteDatabase
    private lateinit var passSaveDatabaseHelper: PassSaveDatabaseHelper

    private lateinit var usernameGet: String

    private lateinit var textViewLoginDetails : TextView

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

        usernameGet = intent.getStringExtra("usernameValue").toString()

        println("Username Now Logged in is $usernameGet")

        if(usernameGet.isEmpty())
        {
            textViewLoginDetails.visibility - View.VISIBLE
            textViewLoginDetails.text = "Login Failed User Not Found"
        }
        else
        {
            textViewLoginDetails.visibility - View.VISIBLE
            textViewLoginDetails.text = "Login Success"
        }
    }

    fun StoredPassActivity.onBackButtonPressed(callback: (() -> Boolean)) {
        (this as? FragmentActivity)?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(!callback())
                {
                    remove()
                    performBackPress()
                }
            }

        })
    }

    fun StoredPassActivity.performBackPress()
    {
        (this as? FragmentActivity)?.onBackPressedDispatcher?.onBackPressed()
        println("Back Pressed")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_store,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId)
        {
            R.id.Logout_Icon->{
                var contentValuesLogOut = ContentValues().apply {
                    put("login_status","LOGGED OUT")
                }

                CoroutineScope(Dispatchers.IO).launch{
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
            else->true
        }
    }

    private suspend fun UpdateLoginOperation()
    {
//        PassSaveDatabaseHelper(applicationContext).UpdateUserLoginStatus(usernameGet,contentValuesLogOut)
        var contentValuesLogOut = ContentValues().apply {
            put("login_status","LOGGED OUT")
        }
        SQLiteDatabase?.update("User", contentValuesLogOut,"username = ?", arrayOf(usernameGet))
        println("$usernameGet Updating to login status : $contentValuesLogOut")
    }

    override fun onStop() {
        super.onStop()
        println("Logging every one out not")
    }
}