package com.example.passavevault

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf

class PassSaveDatabaseHelper(context: Context): SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object {
        private const val DB_NAME = "PassSaveVault.sqlite"
        private const val DB_VERSION = 1 // newest version of database
    }

    override fun onCreate(db: SQLiteDatabase?) {
        //// Source: https://www.sqlitetutorial.net/sqlite-foreign-key/
        // PARAMETER ALLOWING FOR FOREIGN KEYS in sqlite database is enabled
        val foreignKeyAllow = """
            PRAGMA foreign_keys = ON
            """.trimIndent()
        db?.execSQL(foreignKeyAllow)

        val query = """
            CREATE TABLE User (
                User_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE,
                password TEXT,
                login_status TEXT
            );
        """.trimIndent()
        db?.execSQL(query)

        val query2 = """
            CREATE TABLE UserPassword (
                Password_id INTEGER PRIMARY KEY AUTOINCREMENT,
                passwordEncrypted TEXT,
                source_site_password TEXT,
                User_id INTEGER,
                FOREIGN KEY (User_id) REFERENCES User(User_id)
            );
        """.trimIndent()
        db?.execSQL(query2)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
//        if(oldVersion == 2) // if statement looking for anything above 2
//        {
//            val query = "ALTER TABLE beer ADD COLUMN favorite NUMERIC"
//            db?.execSQL(query)
//        }
//        if(oldVersion == 3) // if statement looking for anything above 2
//        {
//            val query = "ALTER TABLE beer ADD COLUMN favorite NUMERIC"
//            db?.execSQL(query)
//        }
    }

    fun UpdateUserLoginStatus(specificUserID: Int, loginStatusContentVal: ContentValues) {
        val db = this.readableDatabase

        // update specific user added to now be logged in
        db?.update("User", loginStatusContentVal,"User_id = ?", arrayOf(specificUserID.toString()))
        println("$specificUserID Updating to login status : $loginStatusContentVal")

        // other users are then logged out, resulting in a 1 user logged in at a time
        // the reason 1 user is logged in at a time is because it would help protect other users
        // by only allowing 1 user who has to be logged in to be able to encrypt/view their passwords at a time
//        val contentValLogout = ContentValues().apply {
//            put("login_status", "LOGGED OUT")
//        }
//
//        db?.update("User", contentValLogout,"username != ?", arrayOf(usernameSpecified))
//        println("$usernameSpecified Updating to login status : $loginStatusContentVal")


//        return cursor
    }



    fun SelectSpecificUser(cursorPosition: Int, id: Int): Cursor {
        val db = this.readableDatabase

        var cursor = db.query(
            "color",
            arrayOf("_id","color","R","G","B","isfavorite"),
            null,
            null,
            null, // where statements
            null, // where statements
            null,
            null
        )

        cursor.moveToPosition(cursorPosition)

        println(cursorPosition)


        val getFavorite = cursor.getColumnIndex("R")
        val valueSpecific = cursor.getString(getFavorite)

        return cursor
    }

//    fun getAllPasswordsForUser(): Cursor {
//        val db = this.readableDatabase
//
//        val cursor = db.query(
//            "color",
//            arrayOf("_id","color","R","G","B","isfavorite"),
//            null,
//            null,
//            null, // where statements
//            null, // where statements
//            null,
//            null
//        )
//
//        return cursor
//    }

}