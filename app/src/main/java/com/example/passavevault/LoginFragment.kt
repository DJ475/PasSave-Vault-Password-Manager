package com.example.passavevault

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast


class LoginFragment : Fragment() {
    private lateinit var ButtonLogin : Button
    private lateinit var EditTextUsername: EditText
    private lateinit var EditTextPassword: EditText

    private lateinit var UserDatabaseHelper: PassSaveDatabaseHelper
    private lateinit var sqLiteDatabase: SQLiteDatabase

//    private lateinit var

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        EditTextUsername = view.findViewById(R.id.usernameEditText)
        EditTextPassword = view.findViewById(R.id.passwordEditText)
        ButtonLogin = view.findViewById(R.id.loginButton)

        UserDatabaseHelper = PassSaveDatabaseHelper(requireContext())
        sqLiteDatabase = UserDatabaseHelper.readableDatabase

        ButtonLogin.setOnClickListener{
            if (EditTextUsername.text.isEmpty() || EditTextPassword.text.isEmpty()) {
                Toast.makeText(requireContext(), "User Entered A Invalid Input", Toast.LENGTH_LONG).show()
                println("Empty or Bad Input Detected, Please Make Sure to Enter For Both Fields")
                // check users database, if found in query then login and change to stored password activity
                // else its not found and should say no user exists with that name
            }
            else
            {
                println("User Entered: ${EditTextUsername.text.toString()}")
                println("User Entered: ${EditTextPassword.text.toString()}")

                var usernameString = EditTextUsername.text.toString()
                var passwordString = EditTextPassword.text.toString()

                val cursorSelect = sqLiteDatabase.query(
                    "User",
                    arrayOf("User_id","username","password","login_status"), // select all columns
                    "username = ?", // where username in database matches user's input of username
                    arrayOf(usernameString),
                    null,
                    null,
                    null,
                    null // limit query to return only 1 user at a time
                )
                // going through all of the cursor from the database in order to make sure username and password is valid
                // when valid loop condition finds matching value from DB and starts storePasswordActivity
                // when loop condition not reached then username was not found in database
                while(cursorSelect.moveToNext()) {
                    var column_username = cursorSelect.getColumnIndex("username")
                    var column_pass = cursorSelect.getColumnIndex("password")
                    if (cursorSelect.getString(column_username) == usernameString && cursorSelect.getString(column_pass) == passwordString) {
                        println("Username Found Logging In now")

                        val contentValuesLoggedIn = ContentValues().apply {
                            put("login_status","LOGGED IN")
                        }

                        UserDatabaseHelper.UpdateUserLoginStatus(usernameString,contentValuesLoggedIn)

                        var intent_PassStoreAct = Intent(requireContext(),StoredPassActivity()::class.java)
                        intent_PassStoreAct.putExtra("usernameValue",usernameString)
                        startActivity(intent_PassStoreAct)
                    }
                    else
                    {
                        break
                    }
                }
                println("Username or Password Not Found Please Make Sure The Information Above Is Correct")
                EditTextUsername.text.clear()
                EditTextPassword.text.clear()
            }
        }
        return view
    }
}