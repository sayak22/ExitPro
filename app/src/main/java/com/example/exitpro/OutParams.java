package com.example.exitpro;

public class OutParams {
    int roll_number;
    String destination;

    public OutParams(int roll_number, String destination) {
        this.roll_number = roll_number;
        this.destination = destination;
    }

    public int getRoll_number() {
        return roll_number;
    }

    public String getDestination() {
        return destination;
    }

    public void setRoll_number(int roll_number) {
        this.roll_number = roll_number;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
