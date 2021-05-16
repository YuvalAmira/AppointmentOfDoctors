package com.example.appointmentofdoctors;


public class Doctor {
    private String id;
    private String userName;
    private Boolean availability;
    private Long timeToBeAvailable;

    public Doctor() {
    }

    public Doctor(String id, String userName, Boolean availability, Long timeToBeAvailable) {
        this.id = id;
        this.userName = userName;
        this.availability = availability;
        this.timeToBeAvailable = timeToBeAvailable;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getAvailability() {
        return availability;
    }

    public void setAvailability(Boolean availability) {
        this.availability = availability;
    }

    public Long getTimeToBeAvailable() {
        return timeToBeAvailable;
    }

    public void setTimeToBeAvailable(Long timeToBeAvailable) {
        this.timeToBeAvailable = timeToBeAvailable;
    }
}
