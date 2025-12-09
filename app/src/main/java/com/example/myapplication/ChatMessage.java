package com.example.myapplication;

public class ChatMessage {
    private final String sender;
    private final String message;
    private final boolean isSentByUser;

    public ChatMessage(String sender, String message, boolean isSentByUser) {
        this.sender = sender;
        this.message = message;
        this.isSentByUser = isSentByUser;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSentByUser() {
        return isSentByUser;
    }
}
