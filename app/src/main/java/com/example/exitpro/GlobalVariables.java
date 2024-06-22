package com.example.exitpro;

import android.app.Application;

import com.example.exitpro.Model.LateStudent;

import java.util.ArrayList;

public class GlobalVariables extends Application {
    private ArrayList<LateStudent> lateList;

    public ArrayList<LateStudent> getLateList() {
        if (lateList == null) {
            lateList = new ArrayList<>();
        }
        return lateList;
    }

    public void setLateList(ArrayList<LateStudent> lateList) {
        this.lateList = lateList;
    }
}
