package common;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String sender;
    private String content;
    private Date timestamp;
    private MessageType type;
    
    public enum MessageType {
        TEXT, CONNECT, DISCONNECT, ERROR, INFO
    }
    
    // Constructeurs
    public Message(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timestamp = new Date();
        this.type = MessageType.TEXT;
    }
    
    public Message(String sender, String content, MessageType type) {
        this.sender = sender;
        this.content = content;
        this.timestamp = new Date();
        this.type = type;
    }
    
    // Getters et setters
    public String getSender() {
        return sender;
    }
    
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    // Méthode d'affichage
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = sdf.format(timestamp);
        
        switch (type) {
            case CONNECT:
                return "[" + time + "] " + "🔵 " + content;
            case DISCONNECT:
                return "[" + time + "] " + "🔴 " + content;
            case ERROR:
                return "[" + time + "] " + "❌ ERREUR: " + content;
            case INFO:
                return "[" + time + "] " + "ℹ️ INFO: " + content;
            default:
                return "[" + time + "] " + sender + ": " + content;
        }
    }
}