package fit.hutech.spring.chat;

import java.time.Instant;

public class ChatMessageDto {
    private Long id;
    private String senderName;
    private String content;
    private Instant createdAt;
    private boolean fromMe;

    public ChatMessageDto(Long id, String senderName, String content, Instant createdAt, boolean fromMe) {
        this.id = id;
        this.senderName = senderName;
        this.content = content;
        this.createdAt = createdAt;
        this.fromMe = fromMe;
    }

    public Long getId() {
        return id;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isFromMe() {
        return fromMe;
    }
}
