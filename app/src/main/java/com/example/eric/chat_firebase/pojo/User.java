package com.example.eric.chat_firebase.pojo;

/**
 * Created by Eric on 23-Sep-17.
 */

public class User {
    private String uid; //diperlukan krn buat update di listview
    private String name;
    private String status;
    private String firebaseToken;  //utk push notif
    private boolean online;

    public User() {
    }

    public User(String uid, String name, String status, boolean online, String firebaseToken) {
        this.uid = uid;
        this.name = name;
        this.status = status;
        this.online = online;
        this.firebaseToken = firebaseToken;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }
}
