package com.chat.omar.simplechat;

public class Room {
    private String sender;
    private String chat;
    private String msg;

    public Room() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getChat() {
        return chat;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
