package sanju.newproject.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessageEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String sender; // "user" or "ai"
    private String message;
    private long timestamp;
    private String language;

    public ChatMessageEntity(String sender, String message, long timestamp, String language) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
        this.language = language;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}
