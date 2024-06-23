package com.example.exitpro.Model

class OutParams // Constructor
    (
    var rollNumber: Int,
    var destination: String
) {
    override fun toString(): String {
        return "OutParams{" +
                "rollNumber=" + rollNumber +
                ", destination='" + destination + '\'' +
                '}'
    }
}
