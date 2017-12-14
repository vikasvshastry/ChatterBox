package com.chatterbox.chatterbox;

public class CBMessage {
    private String messageBody;
    private String time;
    private String date;
    private String msgId;
    private String senderId;

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String cmtId) {
        this.msgId = cmtId;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public CBMessage(){

    }

}
