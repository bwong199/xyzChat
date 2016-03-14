package com.benwong.geochat;

import com.shaded.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

/**
 * Created by benwong on 2016-03-08.
 */
public class Message implements Comparable<Message> {
    private String senderId;
    private String recipientId;
    private String message;
    private Date date;
    private String widgetMessage;

    @JsonIgnoreProperties(ignoreUnknown=true)
    public Message() {
    }

    public String getWidgetMessage() {
        return widgetMessage;
    }

    public void setWidgetMessage(String widgetMessage) {
        this.widgetMessage = widgetMessage;
    }

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
        return other.getDate().compareTo(getDate());
    }


}
