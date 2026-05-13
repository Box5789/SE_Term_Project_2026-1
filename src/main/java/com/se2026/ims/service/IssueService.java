package com.se2026.ims.service;

import com.se2026.ims.model.*;
import com.se2026.ims.repository.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

public class IssueService {
    private final Repository<Issue> issueRepository;

    public IssueService(Repository<Issue> issueRepository) {
        this.issueRepository = issueRepository;
    }

    public Issue createIssue(String id, String title, String description, String projectId, String reporterId) {
        requireText(id, "Issue ID cannot be empty");
        requireText(title, "Issue title cannot be empty");
        requireText(description, "Issue description cannot be empty");
        requireText(projectId, "Project ID cannot be empty");
        requireText(reporterId, "Reporter ID cannot be empty");
        if (issueRepository.findById(id.trim()).isPresent()) {
            throw new IllegalArgumentException("Issue ID already exists: " + id.trim());
        }
        Issue issue = new Issue(id.trim(), title.trim(), description.trim(), projectId.trim(), reporterId.trim());
        issueRepository.save(issue);
        return issue;
    }

    public void addComment(String issueId, String authorId, String content) {
        Issue issue = requireIssue(issueId);
        requireText(authorId, "Author ID cannot be empty");
        requireText(content, "Comment cannot be empty");
        Comment comment = new Comment(authorId, content.trim(), LocalDateTime.now());
        issue.addComment(comment);
        issueRepository.update(issue);
    }

    public void updateStatus(String issueId, IssueStatus newStatus, String commentAuthorId, String statusMessage) {
        Issue issue = requireIssue(issueId);
        validateTransition(issue.getStatus(), newStatus);
        issue.setStatus(newStatus);
        if (newStatus == IssueStatus.REOPENED) {
            issue.setAssigneeId(null);
            issue.setFixerId(null);
        }
        if (statusMessage != null && !statusMessage.trim().isEmpty()) {
            issue.addComment(new Comment(commentAuthorId, "Status changed to " + newStatus + ": " + statusMessage.trim(), LocalDateTime.now()));
        }
        issueRepository.update(issue);
    }

    public void assignIssue(String issueId, String assigneeId, String commentAuthorId) {
        Issue issue = requireIssue(issueId);
        requireText(assigneeId, "Assignee ID cannot be empty");
        validateTransition(issue.getStatus(), IssueStatus.ASSIGNED);
        issue.setAssigneeId(assigneeId.trim());
        issue.setStatus(IssueStatus.ASSIGNED);
        issue.addComment(new Comment(commentAuthorId, "Assigned to " + assigneeId.trim(), LocalDateTime.now()));
        issueRepository.update(issue);
    }

    public void fixIssue(String issueId, String fixerId, String comment) {
        Issue issue = requireIssue(issueId);
        requireText(fixerId, "Fixer ID cannot be empty");
        validateTransition(issue.getStatus(), IssueStatus.FIXED);
        issue.setFixerId(fixerId.trim());
        issue.setStatus(IssueStatus.FIXED);
        if (comment != null && !comment.trim().isEmpty()) {
            issue.addComment(new Comment(fixerId.trim(), comment.trim(), LocalDateTime.now()));
        }
        issueRepository.update(issue);
    }

    public void updateIssue(String id, String title, String description, String projectId, Priority priority) {
        Issue issue = requireIssue(id);
        requireText(title, "Issue title cannot be empty");
        requireText(description, "Issue description cannot be empty");
        requireText(projectId, "Project ID cannot be empty");
        issue.setTitle(title.trim());
        issue.setDescription(description.trim());
        issue.setProjectId(projectId.trim());
        issue.setPriority(priority == null ? Priority.MAJOR : priority);
        issueRepository.update(issue);
    }

    public void deleteIssue(String id) {
        issueRepository.delete(id);
    }

    public Optional<Issue> getIssueById(String id) {
        return issueRepository.findById(id);
    }

    public List<Issue> searchIssues(String projectId, String reporterId, String assigneeId, IssueStatus status) {
        return issueRepository.findAll().stream()
                .filter(issue -> (projectId == null || Objects.equals(issue.getProjectId(), projectId)))
                .filter(issue -> (reporterId == null || Objects.equals(issue.getReporterId(), reporterId)))
                .filter(issue -> (assigneeId == null || Objects.equals(issue.getAssigneeId(), assigneeId)))
                .filter(issue -> (status == null || issue.getStatus() == status))
                .collect(Collectors.toList());
    }

    public List<String> recommendAssignee(String issueId) {
        Optional<Issue> targetIssueOpt = issueRepository.findById(issueId);
        if (targetIssueOpt.isEmpty()) return Collections.emptyList();
        
        Issue targetIssue = targetIssueOpt.get();
        String title = targetIssue.getTitle();

        // 해결된 이슈들 중에서 유사도가 높은 이슈의 Fixer 추천
        List<Issue> resolvedIssues = issueRepository.findAll().stream()
                .filter(i -> i.getStatus() == IssueStatus.RESOLVED || i.getStatus() == IssueStatus.CLOSED)
                .filter(i -> i.getFixerId() != null)
                .collect(Collectors.toList());

        Map<String, Integer> fixerScores = new HashMap<>();
        for (Issue resolved : resolvedIssues) {
            int score = calculateSimilarity(title, resolved.getTitle());
            if (score > 0) {
                fixerScores.put(resolved.getFixerId(), fixerScores.getOrDefault(resolved.getFixerId(), 0) + score);
            }
        }

        return fixerScores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private int calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        // 간단한 키워드 매칭 기반 유사도 측정 (실제로는 더 복잡한 알고리즘 가능)
        String[] words1 = s1.toLowerCase().split("\\s+");
        String[] words2 = s2.toLowerCase().split("\\s+");
        Set<String> set1 = new HashSet<>(Arrays.asList(words1));
        Set<String> set2 = new HashSet<>(Arrays.asList(words2));
        
        int matchCount = 0;
        for (String w : set1) {
            if (set2.contains(w)) matchCount++;
        }
        return matchCount;
    }

    private Issue requireIssue(String issueId) {
        requireText(issueId, "Issue ID cannot be empty");
        String trimmedId = issueId.trim();
        return issueRepository.findById(trimmedId)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found: " + trimmedId));
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validateTransition(IssueStatus current, IssueStatus next) {
        if (current == next) return;
        boolean allowed =
                (current == IssueStatus.NEW && next == IssueStatus.ASSIGNED) ||
                (current == IssueStatus.ASSIGNED && next == IssueStatus.FIXED) ||
                (current == IssueStatus.FIXED && (next == IssueStatus.RESOLVED || next == IssueStatus.REOPENED)) ||
                (current == IssueStatus.RESOLVED && next == IssueStatus.CLOSED) ||
                (current == IssueStatus.REOPENED && next == IssueStatus.ASSIGNED);
        if (!allowed) {
            throw new IllegalStateException("Invalid status transition: " + current + " -> " + next);
        }
    }
}
