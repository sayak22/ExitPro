package com.example.exitpro.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exitpro.Model.LateStudent;
import com.example.exitpro.R;
import com.example.exitpro.Utils.CallUtil;

import java.util.ArrayList;

public class LateAdapter extends RecyclerView.Adapter<LateAdapter.ViewHolder> {

    private ArrayList<LateStudent> dataList; // List to hold late student data
    private Context context; // Context reference for making calls or other context-dependent operations

    /**
     * Constructor to initialize the adapter with context and data list.
     *
     * @param context The context from which the adapter is created.
     * @param dataList The list of LateStudent objects to display.
     */
    public LateAdapter(Context context, ArrayList<LateStudent> dataList) {
        this.context = context;
        this.dataList = dataList;
        Log.d("LateAdapter", "DataList size: " + dataList.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout and create a ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.late_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        // Bind data to ViewHolder for the given position
        LateStudent model = dataList.get(position);

        // Set data to views in ViewHolder
        holder.textName.setText(model.getName());
        holder.textRollNumber.setText(String.valueOf(model.getRollNumber()));
        holder.textDestination.setText(model.getDestination());
        holder.textOutTime.setText(model.getDay() + "-" + model.getMonth() + "-" + model.getYear() + " at " + model.getHour() + ":" + model.getMinute());

        // Set click listener for the item view to make a call
        holder.itemView.setOnClickListener(v -> {
            String phoneNumber = dataList.get(position).getPhoneNumber();
            CallUtil.makeCall(context, phoneNumber); // Utilize CallUtil to make a call
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size(); // Return the size of the data list
    }

    // ViewHolder class to hold the views for each item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textName;
        public TextView textRollNumber;
        public TextView textDestination;
        public TextView textOutTime;

        public ViewHolder(View view) {
            super(view);
            // Initialize views from the item layout
            textName = view.findViewById(R.id.stuName);
            textRollNumber = view.findViewById(R.id.rollNumber);
            textDestination = view.findViewById(R.id.destination);
            textOutTime = view.findViewById(R.id.exitTime);
        }
    }
}
