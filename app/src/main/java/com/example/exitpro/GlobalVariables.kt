package com.example.exitpro

import android.app.Application
import com.example.exitpro.Model.LateStudent

class GlobalVariables : Application() {
    var lateList: ArrayList<LateStudent>? = null
        get() {
            if (field == null) {
                field = ArrayList<LateStudent>()
            }
            return field
        }
}
