package com.chat.omar.simplechat;

public class Room {
    private String sender;
    private String chat;
    private String msg;
    private String suid;
    private String time;
    private String avatar;

    public Room() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public String getTime() {
        return time;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getSuid() {
        return suid;
    }

    public String getSender() {
        return sender;
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

}
