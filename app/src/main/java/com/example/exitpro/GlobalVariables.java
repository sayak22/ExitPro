package com.example.exitpro;
import android.app.Application;

import com.example.exitpro.Model.LateStudent;

import java.util.ArrayList;

public class GlobalVariables extends Application {
    private ArrayList<LateStudent> lateList;

    public ArrayList<LateStudent> getlateList() {
        if (lateList == null) {
            lateList = new ArrayList<>();
        }
        return lateList;
    }

    public void setlateList(ArrayList<LateStudent> lateList) {
        this.lateList = lateList;
    }
}
