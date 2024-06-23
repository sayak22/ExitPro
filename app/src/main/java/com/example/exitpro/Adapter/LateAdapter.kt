package com.example.exitpro.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.exitpro.Model.LateStudent
import com.example.exitpro.R
import com.example.exitpro.Utils.CallUtil

class LateAdapter(
    private val context: Context,
    private val dataList: List<LateStudent>
) : RecyclerView.Adapter<LateAdapter.ViewHolder>() {

    init {
        // Log the size of dataList when the adapter is initialized
        Log.d("LateAdapter", "DataList size: ${dataList.size}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the item layout and create a ViewHolder
        val view = LayoutInflater.from(parent.context).inflate(R.layout.late_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Bind data to ViewHolder for the given position
        val student = dataList[position]

        // Set data to views in ViewHolder
        holder.bind(student)
    }

    override fun getItemCount(): Int {
        return dataList.size // Return the size of the data list
    }

    // ViewHolder class to hold the views for each item
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Initialize views from the item layout
        private val textName: TextView = view.findViewById(R.id.stuName)
        private val textRollNumber: TextView = view.findViewById(R.id.rollNumber)
        private val textDestination: TextView = view.findViewById(R.id.destination)
        private val textOutTime: TextView = view.findViewById(R.id.exitTime)

        fun bind(student: LateStudent) {
            textName.text = student.name
            textRollNumber.text = student.rollNumber.toString()
            textDestination.text = student.destination
            textOutTime.text = "${student.day}-${student.month}-${student.year} at ${student.hour}:${student.minute}"

            // Set click listener for the item view to make a call
            itemView.setOnClickListener {
                student.phoneNumber?.let { it1 -> CallUtil.makeCall(context, it1) } // here ? operator checks if phoneNumber is null or not, if it is not null the only it1 references phoneNumber and then gets called by makeCall()
            }
        }
    }
}
