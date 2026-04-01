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
        Issue issue = new Issue(id, title, description, projectId, reporterId);
        issueRepository.save(issue);
        return issue;
    }

    public void addComment(String issueId, String authorId, String content) {
        issueRepository.findById(issueId).ifPresent(issue -> {
            Comment comment = new Comment(authorId, content, LocalDateTime.now());
            issue.addComment(comment);
            issueRepository.update(issue);
        });
    }

    public void updateStatus(String issueId, IssueStatus newStatus, String commentAuthorId, String statusMessage) {
        issueRepository.findById(issueId).ifPresent(issue -> {
            issue.setStatus(newStatus);
            if (statusMessage != null && !statusMessage.isEmpty()) {
                addComment(issueId, commentAuthorId, "Status changed to " + newStatus + ": " + statusMessage);
            }
            issueRepository.update(issue);
        });
    }

    public void assignIssue(String issueId, String assigneeId, String commentAuthorId) {
        issueRepository.findById(issueId).ifPresent(issue -> {
            issue.setAssigneeId(assigneeId);
            issue.setStatus(IssueStatus.ASSIGNED);
            addComment(issueId, commentAuthorId, "Assigned to " + assigneeId);
            issueRepository.update(issue);
        });
    }

    public void fixIssue(String issueId, String fixerId, String comment) {
        issueRepository.findById(issueId).ifPresent(issue -> {
            issue.setFixerId(fixerId);
            issue.setStatus(IssueStatus.FIXED);
            addComment(issueId, fixerId, comment);
            issueRepository.update(issue);
        });
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
}
