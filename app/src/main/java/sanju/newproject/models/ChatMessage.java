package sanju.newproject.models;

public class ChatMessage {
    private String sender; // "user" or "ai"
    private String message;
    private long timestamp;
    private String language;
    private String imageUri;

    public ChatMessage(String sender, String message, long timestamp, String language) {
        this(sender, message, timestamp, language, null);
    }

    public ChatMessage(String sender, String message, long timestamp, String language, String imageUri) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
        this.language = language;
        this.imageUri = imageUri;
    }

    public String getSender() { return sender; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public String getLanguage() { return language; }
    public String getImageUri() { return imageUri; }
}
