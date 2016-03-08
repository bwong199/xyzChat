package com.benwong.geochat;

import java.util.Date;

/**
 * Created by benwong on 2016-03-08.
 */
public class Message implements Comparable<Message> {
    private String senderId;
    private String recipientId;
    private String message;
    private Date date;

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    @Override
    public int compareTo(Message other) {
        return getDate().compareTo(other.getDate());
    }
}
