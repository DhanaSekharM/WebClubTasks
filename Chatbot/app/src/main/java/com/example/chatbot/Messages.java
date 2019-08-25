package com.example.chatbot;

/**
 * POJO class for storing user messages and bot reply info
 */
public class Messages {
    private String userMessage, botReply, sendTime, replyTime;

    public Messages(String userMessage, String botReply, String sendTime, String replyTime) {
        this.userMessage = userMessage;
        this.botReply = botReply;
        this.sendTime = sendTime;
        this.replyTime = replyTime;
    }

    public Messages() {
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getBotReply() {
        return botReply;
    }

    public void setBotReply(String botReply) {
        this.botReply = botReply;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getReplyTime() {
        return replyTime;
    }

    public void setReplyTime(String replyTime) {
        this.replyTime = replyTime;
    }
}
