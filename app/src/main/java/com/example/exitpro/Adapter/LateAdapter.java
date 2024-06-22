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

    private ArrayList<LateStudent> dataList;
//    GlobalVariables globalVariables =new GlobalVariables();
    private Context context;

    public LateAdapter(Context context, ArrayList<LateStudent> dataList) {
        this.context = context;
        this.dataList = dataList;
        Log.d("abhay",String.valueOf(dataList));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.late_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        LateStudent model = dataList.get(position);
        Log.i("Adapter sayak - > " , String.valueOf(position));
        Log.i("Adapter sayak - > " , String.valueOf(dataList.get(position)));

        holder.textName.setText(model.getName());
        holder.textRollNumber.setText(String.valueOf(model.getRollNumber()));
        holder.textDestination.setText(model.getDestination());
        holder.textOutTime.setText(model.getDay()+"-"+model.getMonth()+"-"+model.getYear()+" at "+model.getHour()+":"+model.getMinute());

        holder.itemView.setOnClickListener(v -> {
            // Handle item click, e.g., make a call to the phone number
            String phoneNumber = dataList.get(position).getPhoneNumber();
            CallUtil.makeCall(context, phoneNumber);
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textName;
        public TextView textRollNumber;
        public TextView textDestination;
        public TextView textOutTime;

        public ViewHolder(View view) {
            super(view);
            textName = view.findViewById(R.id.stuName);
            textRollNumber = view.findViewById(R.id.rollNumber);
            textDestination = view.findViewById(R.id.destination);
            textOutTime = view.findViewById(R.id.exitTime);
        }
    }
}

