package com.example.passavevault

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Base64

class ItemAdapter(
    private var cursor: Cursor,
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    class ViewHolder(
        itemView: View,
        private var cursor: Cursor,
        private val applicationContextPassed: Context,
    ) : RecyclerView.ViewHolder(itemView) {

        private val textViewPass: TextView = itemView.findViewById(R.id.TextViewPass)
        private val textViewSource: TextView = itemView.findViewById(R.id.TextViewSource)


        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                cursor.moveToPosition(position)



                var columns = cursor.getColumnIndex("source_site_password")
                var stringSources = cursor.getString(columns)

                columns = cursor.getColumnIndex("passwordEncrypted")
                var stringPassword = cursor.getBlob(columns)

                columns = cursor.getColumnIndex("Password_id")
                var passwordID = cursor.getInt(columns)

                columns = cursor.getColumnIndex("User_id")
                var userID = cursor.getInt(columns)

                var decodedInfo = Base64.decode(stringPassword, Base64.DEFAULT)

                val ivposition = decodedInfo.indexOf(0x3A.toByte())
                val extractPass = decodedInfo.copyOfRange(0, ivposition)
                val extractIV = decodedInfo.copyOfRange(ivposition + 1, decodedInfo.size)

                val intentDetail = Intent(applicationContextPassed, DetailedPassDecrypt::class.java)
                intentDetail.putExtra("extractedPassword", extractPass)
                intentDetail.putExtra("extractedIV", extractIV)
                intentDetail.putExtra("source", stringSources)
                intentDetail.putExtra("passPSWD_ID", passwordID)
                intentDetail.putExtra("passUser_ID", userID)
                applicationContextPassed.startActivity(intentDetail)
            }
        }

        // Update UI elements
        fun update() {
            var column = cursor.getColumnIndexOrThrow("passwordEncrypted")
            textViewPass.text = cursor.getBlob(column).toString()
            textViewPass.textAlignment = TEXT_ALIGNMENT_CENTER

            column = cursor.getColumnIndexOrThrow("source_site_password")
            textViewSource.text = cursor.getString(column)
            textViewSource.textAlignment = TEXT_ALIGNMENT_CENTER
        }

    }

    // Inflates the layout and returns the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_layout, parent, false)
        return ViewHolder(view, cursor, parent.context) // Pass the listener
    }

    // Binds the data to the views in the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        cursor.moveToPosition(position)
        holder.update() // Update the item at the current position
    }

    // Return the number of items
    override fun getItemCount(): Int {
        return cursor.count
    }

    fun updateCursor(updatedCursor: Cursor) {
        cursor.close()
        cursor = updatedCursor
        notifyDataSetChanged()
    }
}