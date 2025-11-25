package com.example.exitpro

import android.app.Application
import com.example.exitpro.data.model.LateStudent

class GlobalVariables : Application() {
    var lateList: ArrayList<LateStudent> = ArrayList()
}
