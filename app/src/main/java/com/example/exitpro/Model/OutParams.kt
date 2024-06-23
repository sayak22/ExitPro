package com.example.exitpro.Model;

public class OutParams {
    private int rollNumber;
    private String destination;

    // Constructor
    public OutParams(int rollNumber, String destination) {
        this.rollNumber = rollNumber;
        this.destination = destination;
    }

    // Getter for rollNumber
    public int getRollNumber() {
        return rollNumber;
    }

    // Setter for rollNumber
    public void setRollNumber(int rollNumber) {
        this.rollNumber = rollNumber;
    }

    // Getter for destination
    public String getDestination() {
        return destination;
    }

    // Setter for destination
    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "OutParams{" +
                "rollNumber=" + rollNumber +
                ", destination='" + destination + '\'' +
                '}';
    }
}
