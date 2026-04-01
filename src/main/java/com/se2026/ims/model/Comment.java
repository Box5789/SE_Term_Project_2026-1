package com.se2026.ims.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class Comment {
    private String authorId;
    private String content;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public Comment() {}

    @JsonCreator
    public Comment(@JsonProperty("authorId") String authorId, 
                   @JsonProperty("content") String content, 
                   @JsonProperty("timestamp") LocalDateTime timestamp) {
        this.authorId = authorId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
