package com.example.exitpro.Model

class LateStudent {
    // Getters and setters for each attribute
    var name: String? = null
    var rollNumber: Int = 0
    var destination: String? = null
    var phoneNumber: String? = null
    var year: Int = 0
    var month: Int = 0
    var day: Int = 0
    var hour: String? = null
    var minute: String? = null
    var second: String? = null

    override fun toString(): String {
        return "LateStudent{" +
                "name='" + name + '\'' +
                ", rollNumber=" + rollNumber +
                ", destination='" + destination + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour='" + hour + '\'' +
                ", minute='" + minute + '\'' +
                ", second='" + second + '\'' +
                '}'
    }
}
