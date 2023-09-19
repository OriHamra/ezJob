package com.example.finalproject;

import java.util.ArrayList;

public class Position {
    private String positionName;
    private String scopeJob;
    private ArrayList<String> shiftTypes;
    private String trail;
    private double age;
    private double hourSalary;
    private ArrayList<Employed> employedList; // list of employeds who want to get the position

    public Position(String positionName, String scopeJob, ArrayList<String> shiftTypes, String trail
            , double age, double hourSalary, ArrayList<Employed> employedList) {
        this.positionName = positionName;
        this.scopeJob = scopeJob;
        this.shiftTypes = shiftTypes;
        this.trail = trail;
        this.age = age;
        this.hourSalary = hourSalary;
        this.employedList = employedList;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getScopeJob() {
        return scopeJob;
    }

    public void setScopeJob(String scopeJob) {
        this.scopeJob = scopeJob;
    }

    public ArrayList<String> getShiftTypes() {
        return shiftTypes;
    }

    public void setShiftTypes(ArrayList<String> shiftTypes) {
        this.shiftTypes = shiftTypes;
    }

    public String getTrail() {
        return trail;
    }

    public void setTrail(String trail) {
        this.trail = trail;
    }

    public double getAge() {
        return age;
    }

    public void setAge(double age) {
        this.age = age;
    }

    public double getHourSalary() {
        return hourSalary;
    }

    public void setHourSalary(double hourSalary) {
        this.hourSalary = hourSalary;
    }

    public ArrayList<Employed> getEmployedList() {
        return employedList;
    }

    public void setEmployedList(ArrayList<Employed> employedList) {
        this.employedList = employedList;
    }
}
