package com.example.passavevault

import android.app.Activity
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


import org.mindrot.jbcrypt.BCrypt
import java.security.KeyStore


class MainActivity : AppCompatActivity() {
    private lateinit var sqLiteDatabase: SQLiteDatabase
    private lateinit var passSaveDatabaseHelper : PassSaveDatabaseHelper
    private lateinit var CurrentUser: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbarMain)
        setSupportActionBar(toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        passSaveDatabaseHelper = PassSaveDatabaseHelper(applicationContext)
        sqLiteDatabase = passSaveDatabaseHelper.readableDatabase

//        try {
//            // Load the AndroidKeyStore instance
//            val keyStore = KeyStore.getInstance("AndroidKeyStore")
//            keyStore.load(null) // Initialize with default values
//
//            // Enumerate all aliases in the keystore
//            val aliases = keyStore.aliases()
//            while (aliases.hasMoreElements()) {
//                val alias = aliases.nextElement()
//                // Delete each entry
//                keyStore.deleteEntry(alias)
//                println("Deleted key with alias: $alias")
//            }
//            println("Keystore cleared successfully.")
//        } catch (e: Exception) {
//            e.printStackTrace()
//            println("Error clearing keystore: ${e.message}")
//        }

        fragmentChangeFun(LoginFragment())

        // Destroying the Secret Key


//        println("Password Hashed is now: " + password)
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Source: https://stackoverflow.com/questions/69068588/how-to-disable-an-item-from-a-menu-in-kotlin
        // make add icon disappear when add is pressed
        // can also make items appear, i use this to make add icon and list icon disappear
        // as well as making cancel icon appear, giving the option to create or cancel making an account

        val toolbar = findViewById<Toolbar>(R.id.toolbarMain)
        val ItemAddIcon = toolbar.menu.findItem(R.id.add_Account)
        val ItemCancelIcon = toolbar.menu.findItem(R.id.cancel_Registration)

        return when(item.itemId)
        {
            R.id.add_Account-> {
                println("Adding New Account")
                fragmentChangeFun(NewUserFragment())

                ItemAddIcon.isVisible = false
                ItemCancelIcon.isVisible = true

                true
            }

            R.id.cancel_Registration->{
                println("Canceling account registration")
                fragmentChangeFun(LoginFragment())
                ItemAddIcon.isVisible = true
                ItemCancelIcon.isVisible = false
                true
            }

            else->true
        }
    }

    fun fragmentChangeFun(fragemntNameSwitch: Fragment)
    {
        val ft = supportFragmentManager.beginTransaction() // required
        ft.replace(R.id.currentUiFrameLayout, fragemntNameSwitch) // required
        ft.addToBackStack(null)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.commit()
    }

    // Using an onStart allows for all users to be logged out when start of application
    // This way when the program is about to become visible it logs every one out
    // This is good for security specifically in my app:
    //      * Because Access to the next activity's recycler view has to be done by logging in or creating a new user
    override fun onStart() {
        super.onStart()
        println("Now Logging out all Users")

        val contentValuesAllUser = ContentValues().apply {
            put("login_status","Logged Out")
        }

        sqLiteDatabase.update("User",contentValuesAllUser,null,null)

    }
}