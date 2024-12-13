package com.example.passavevault

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(private val Cursor: Cursor, private val applicationContext: Context): RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
    interface ItemAdapterListener
    {
        fun clicked(position: Int)
    }
    class ViewHolder(itemView: View, private val cursorPassed: Cursor, val storedPassContext: Context): RecyclerView.ViewHolder(itemView) {
        private val LinearLayoutPass: LinearLayout = itemView.findViewById(R.id.ContainerPasswordRecycler)
        private val textViewPassword: TextView = itemView.findViewById(R.id.TextViewRecyclerPassword)
        private val textViewSource: TextView = itemView.findViewById(R.id.TextViewRecyclerPasswordSource)

        init {
            itemView.setOnClickListener{
                val position = adapterPosition
                cursorPassed.moveToPosition(position)
            }
        }


        fun update(item: Cursor, positionID: Int)
        {
            var column = item.getColumnIndex("passwordEncrypted")
            textViewPassword.text = item.getBlob(column).toString()
            val encryptedStringPass = item.getBlob(column)

            column = item.getColumnIndex("source_site_password")
            textViewSource.text = item.getString(column)
            val encryptedStringSource = item.getString(column)

            column = item.getColumnIndex("Password_id")
            val IDOfCurrentPass = item.getInt(column)


            LinearLayoutPass.setOnClickListener {
//                column = cursorPassed.getColumnIndex("Password_id")
//                var PasswordID = cursorPassed.getInt(column)

                println("Your Password Pressed" + encryptedStringPass)
                println("Your Source Pressed" + encryptedStringSource)
//                println("PasswordID is ${positionID+1}") // position starts at index 0

                val IntentSendDecrypt = Intent(storedPassContext,ActivityDecrypt::class.java)
                ActivityDecrypt.EncryptedString = encryptedStringPass
                IntentSendDecrypt.putExtra("PasswordEncrypted", encryptedStringPass)
                IntentSendDecrypt.putExtra("PasswordSourceEncrypted", encryptedStringSource)
                IntentSendDecrypt.putExtra("PasswordID", IDOfCurrentPass)

                storedPassContext.startActivity(IntentSendDecrypt)
            }
        }
    }

    // inflates layout and passes back to view holder in order to access viewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_layout, parent, false)
        return ViewHolder(view, Cursor, applicationContext)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Cursor.moveToPosition(position)
        holder.update(Cursor, position)
    }

    override fun getItemCount(): Int {
        // check amount of items/size of data model
        return Cursor.count
    }
}
