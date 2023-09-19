package com.example.finalproject;

public class Employed extends User {
    private double age;
    private String gender;
    private String phoneNumber;
    private String address;
    private String status; // for employedList in firebase - confirm / decline
    private double rate; // for rating module - rate the business

    public Employed(String userName, String password, String name, String email, double age,
                    String gender, String phoneNumber, String address, String status, double rate) {
        super(userName, password, name, email);
        this.age = age;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.status = status;
        this.rate = rate;
    }

    /**
     * create employed in employees without rate attribute - in Employeds firebase
     * @param userName
     * @param password
     * @param name
     * @param email
     * @param age
     * @param gender
     * @param phoneNumber
     * @param address
     * @param status
     */
    public Employed(String userName, String password, String name, String email, double age,
                    String gender, String phoneNumber, String address, String status) {
        super(userName, password, name, email);
        this.age = age;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.status = status;
    }

    public double getAge() {
        return age;
    }

    public void setAge(double age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        return super.toString() +
                "Employed{" +
                "age=" + age +
                ", gender='" + gender + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
