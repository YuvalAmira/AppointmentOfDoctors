package com.example.appointmentofdoctors;

public class Patient {
    private String id;
    private String userName;
    private long appointmentime;

    public Patient() {
    }

    public Patient(String id, String userName, long appointmentime) {
        this.id = id;
        this.userName = userName;
        this.appointmentime = appointmentime;
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

    public long getAppointmentime() {
        return appointmentime;
    }

    public void setAppointmentime(long appointmentime) {
        this.appointmentime = appointmentime;
    }
}
