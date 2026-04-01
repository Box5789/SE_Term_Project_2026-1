package com.se2026.ims.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Issue implements Identifiable {
    private String id;
    private String title;
    private String description;
    private String projectId;
    private String reporterId;
    private String assigneeId;
    private String fixerId;
    private Priority priority = Priority.MAJOR;
    private IssueStatus status = IssueStatus.NEW;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportedDate;
    
    private List<Comment> comments = new ArrayList<>();

    public Issue() {}

    @JsonCreator
    public Issue(@JsonProperty("id") String id, 
                 @JsonProperty("title") String title, 
                 @JsonProperty("description") String description,
                 @JsonProperty("projectId") String projectId,
                 @JsonProperty("reporterId") String reporterId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.projectId = projectId;
        this.reporterId = reporterId;
        this.reportedDate = LocalDateTime.now();
        this.status = IssueStatus.NEW;
        this.priority = Priority.MAJOR;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getReporterId() { return reporterId; }
    public void setReporterId(String reporterId) { this.reporterId = reporterId; }

    public String getAssigneeId() { return assigneeId; }
    public void setAssigneeId(String assigneeId) { this.assigneeId = assigneeId; }

    public String getFixerId() { return fixerId; }
    public void setFixerId(String fixerId) { this.fixerId = fixerId; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public IssueStatus getStatus() { return status; }
    public void setStatus(IssueStatus status) { this.status = status; }

    public LocalDateTime getReportedDate() { return reportedDate; }
    public void setReportedDate(LocalDateTime reportedDate) { this.reportedDate = reportedDate; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }
    
    public void addComment(Comment comment) {
        if (this.comments == null) {
            this.comments = new ArrayList<>();
        }
        this.comments.add(comment);
    }

    @Override
    public String toString() {
        return "Issue{id='" + id + "', title='" + title + "', status=" + status + "}";
    }
}
