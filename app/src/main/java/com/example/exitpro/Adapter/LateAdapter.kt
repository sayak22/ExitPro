package com.example.exitpro.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.exitpro.Model.LateStudent
import com.example.exitpro.R
import com.example.exitpro.Utils.CallUtil

class LateAdapter(
    private val context: Context,
    private var dataList: MutableList<LateStudent>
) : RecyclerView.Adapter<LateAdapter.ViewHolder>() {

    // Initializing the adapter and logging the size of the dataList
    init {
        Log.d("LateAdapter", "DataList size: ${dataList.size}")
    }

    // Method to update the dataList with a filtered list and notify the adapter
    fun setFilteredList(filteredList: MutableList<LateStudent>) {
        this.dataList = filteredList
        Log.d("Filtered data", "DataList size: ${dataList.size}")
        notifyDataSetChanged()
    }

    // Inflate the item layout and create a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.late_item, parent, false)
        return ViewHolder(view)
    }

    // Bind data to ViewHolder for the given position
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = dataList[position]
        holder.bind(student)
    }

    // Return the size of the data list
    override fun getItemCount(): Int = dataList.size

    // ViewHolder class to hold the views for each item
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Initialize views from the item layout
        private val textName: TextView = view.findViewById(R.id.stuName)
        private val textRollNumber: TextView = view.findViewById(R.id.rollNumber)
        private val textDestination: TextView = view.findViewById(R.id.destination)
        private val textOutTime: TextView = view.findViewById(R.id.exitTime)

        // Bind student data to the views
        fun bind(student: LateStudent) {
            textName.text = student.name
            textRollNumber.text = student.rollNumber.toString()
            textDestination.text = student.destination
            textOutTime.text = "${student.day}-${student.month}-${student.year} at ${student.hour}:${student.minute}"

            // Set click listener for the item view to make a call and hide the keyboard
            itemView.setOnClickListener {
                // Hide the keyboard
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(itemView.windowToken, 0)

                // Make the call if phoneNumber is not null
                student.phoneNumber?.let { phoneNumber -> CallUtil.makeCall(context, phoneNumber) }
            }
        }
    }
}
