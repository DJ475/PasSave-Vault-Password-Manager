package com.example.passavevault

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Base64
import android.util.Log

class ItemAdapter(private val cursor: Cursor, applicationContextPassed: Context): RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    class ViewHolder(itemView: View, private var cursor: Cursor, private var applicationContextPassed: Context ): RecyclerView.ViewHolder(itemView) {
        private val textViewPass: TextView = itemView.findViewById(R.id.TextViewPass)
        private val textViewSource: TextView = itemView.findViewById(R.id.TextViewSource)
        private val linearLayoutClicked : LinearLayout = itemView.findViewById(R.id.RecyclerLayoutContainer)

        init {
            itemView.setOnClickListener{
                val position = adapterPosition
                cursor.moveToPosition(position)
                var columns = cursor.getColumnIndex("source_site_password")
                var stringSources = cursor.getString(columns)

                columns = cursor.getColumnIndex("passwordEncrypted")

                var stringPassword = cursor.getString(columns)

                var decodedInfo = Base64.decode(stringPassword, Base64.DEFAULT)

                var ivposition = decodedInfo.indexOf(0x3A.toByte())

                var extractPass = decodedInfo.copyOfRange(0,ivposition)
                var extractIV = decodedInfo.copyOfRange(ivposition+1,decodedInfo.size)

                var intentDetail = Intent(applicationContextPassed, DetailedPassDecrypt::class.java)
                intentDetail.putExtra("extractedPassword", extractPass)
                intentDetail.putExtra("extractedIV", extractIV)
                applicationContextPassed.startActivity(intentDetail)
            }
        }


        fun update(item: Boolean)
        {
            var column = cursor.getColumnIndexOrThrow("passwordEncrypted")
            textViewPass.text = cursor.getString(column)
            textViewPass.textAlignment = TEXT_ALIGNMENT_CENTER

            column = cursor.getColumnIndexOrThrow("source_site_password")
            textViewSource.text = cursor.getString(column)
            textViewSource.textAlignment = TEXT_ALIGNMENT_CENTER
        }
    }

    // inflates layout and passes back to view holder in order to access viewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerlayout, parent, false)
        return ViewHolder(view, cursor, parent.context)
    }

    override fun onBindViewHolder(holder: ItemAdapter.ViewHolder, position: Int) {
        val item = cursor.moveToPosition(position)
        holder.update(item)
    }

    override fun getItemCount(): Int {
        // check amount of items/size of data model
        return cursor.count
    }
}
