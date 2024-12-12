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
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

// DJ's Directory Database:
// C:\Users\djibr\Desktop\Mobile Dev\CS 435\FinalProject\FINAL

class NewUserFragment : Fragment() {
    private lateinit var passSaveDatabaseHelper : PassSaveDatabaseHelper
    private lateinit var sqLiteDatabase: SQLiteDatabase

    private lateinit var editTextNewUsername : EditText
    private lateinit var editTextNewPassword : EditText
    private lateinit var TextLoginStatus: TextView

    private lateinit var buttonAddUser : Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_new_user, container, false)

        editTextNewUsername = view.findViewById(R.id.newUsernameEditText)
        editTextNewPassword = view.findViewById(R.id.newPasswordEditText)
        TextLoginStatus = view.findViewById(R.id.TextViewLoginStatus)

        buttonAddUser = view.findViewById(R.id.AddUserButton)

        passSaveDatabaseHelper = PassSaveDatabaseHelper(requireContext())
        sqLiteDatabase = passSaveDatabaseHelper.readableDatabase

        buttonAddUser.setOnClickListener {
            if(!editTextNewUsername.text.toString().isNullOrEmpty() || !editTextNewUsername.text.toString().isDigitsOnly() || !editTextNewPassword.text.isNullOrEmpty() || !editTextNewPassword.text.isDigitsOnly())
            {

                // create salt for more security
                val hash_salt = BCrypt.gensalt()

                val ValueLoggedIN = ContentValues().apply {
                    put("username",editTextNewUsername.text.toString())
                    put("password",BCrypt.hashpw(editTextNewPassword.text.toString(), hash_salt))
                    put("login_status","LOGGED IN")
                }

                CoroutineScope(Dispatchers.IO).launch{
                    DatabaseOperation(ValueLoggedIN)
                }
            }
            else
            {
                Toast.makeText(requireContext(),"The Username Entered Is Invalid\n",Toast.LENGTH_LONG).show()
                println("Entry From User Is Invalid")
            }
        }
        return view
    }

    private suspend fun DatabaseOperation(ValueLoggedIN: ContentValues)
    {

        val addNewUser = sqLiteDatabase.insertWithOnConflict("User",null,ValueLoggedIN,SQLiteDatabase.CONFLICT_IGNORE)


        // insertWithOnConflict comes back as -1 when fail
        if(addNewUser.toInt() == -1){
            println("This Username is Already Taken, Please Choose a unique username")
        }
        else
        {

            val cursorID = sqLiteDatabase.query(
                "User",
                arrayOf("User_id","username","password","login_status"),
            "username = ?",
                arrayOf(editTextNewUsername.text.toString()),
                    null,
                    null,
                    null,
                null
            )

            cursorID.moveToFirst()
            var column = cursorID.getColumnIndex("User_id")
            var stringIDFetch = cursorID.getInt(column)

            println("The UserID that came back right now is: $stringIDFetch")

            // when new user added then go look for new user added in database
            // and log them in, logging out all other users

            val intent_LoggedIn = Intent(requireContext(), StoredPassActivity::class.java)
            intent_LoggedIn.putExtra("usernameValue",editTextNewUsername.text.toString())
            intent_LoggedIn.putExtra("userID",stringIDFetch)
            startActivity(intent_LoggedIn)

            editTextNewUsername.text.clear()
            editTextNewPassword.text.clear()
        }
    }
}