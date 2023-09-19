package com.example.finalproject;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;


public class Business extends User {
    private String businessName;
    private String businessPlace;
    private ArrayList positions;
    private String status; // open to customers or not
    private LatLng location; // save lat lng of business

    public Business(String userName, String password, String name, String email, String businessName,
                    String businessPlace, ArrayList positions, String status, LatLng location) {
        super(userName, password, name, email);
        this.businessName = businessName;
        this.businessPlace = businessPlace;
        this.positions = positions;
        this.status = status;
        this.location = location;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getBusinessPlace() {
        return businessPlace;
    }

    public ArrayList getPositions() {
        return positions;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void setBusinessPlace(String businessPlace) {
        this.businessPlace = businessPlace;
    }

    public void setPositions(ArrayList positions) {
        this.positions = positions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Business{" +
                "businessName='" + businessName + '\'' +
                ", businessPlace='" + businessPlace + '\'' +
                '}';
    }
}
