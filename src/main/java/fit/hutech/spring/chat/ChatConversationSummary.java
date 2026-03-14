package fit.hutech.spring.chat;

import fit.hutech.spring.user.User;

import java.time.Instant;

public class ChatConversationSummary {
    private final User customer;
    private final String lastMessage;
    private final Instant lastMessageAt;
    private final long totalMessages;

    public ChatConversationSummary(User customer, String lastMessage, Instant lastMessageAt, long totalMessages) {
        this.customer = customer;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.totalMessages = totalMessages;
    }

    public User getCustomer() {
        return customer;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public long getTotalMessages() {
        return totalMessages;
    }
}
