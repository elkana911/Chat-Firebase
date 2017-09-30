package com.example.eric.chat_firebase;

import java.util.Date;

/**
 * Created by Eric on 23-Sep-17.
 */

public class ChatMessage {
    private String idSender;
    private String idReceiver;
    private String text;
    private long timestamp;

    public ChatMessage(){}

    public ChatMessage(String idSender, String idReceiver, String text) {
        this.idSender = idSender;
        this.idReceiver = idReceiver;
        this.text = text;
        this.timestamp = new Date().getTime();
    }

    public String getIdSender() {
        return idSender;
    }

    public void setIdSender(String idSender) {
        this.idSender = idSender;
    }

    public String getIdReceiver() {
        return idReceiver;
    }

    public void setIdReceiver(String idReceiver) {
        this.idReceiver = idReceiver;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
