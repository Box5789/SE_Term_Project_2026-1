package com.se2026.ims.controller;

import com.se2026.ims.model.*;
import com.se2026.ims.service.IssueService;
import com.se2026.ims.service.ProjectService;
import com.se2026.ims.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class IMSController {
    private final UserService userService;
    private final ProjectService projectService;
    private final IssueService issueService;
    private User currentUser;

    public IMSController(UserService userService, ProjectService projectService, IssueService issueService) {
        this.userService = userService;
        this.projectService = projectService;
        this.issueService = issueService;
    }

    public boolean login(String id, String password) {
        Optional<User> user = userService.login(id, password);
        if (user.isPresent()) {
            this.currentUser = user.get();
            return true;
        }
        return false;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    public List<Project> getAllProjects() {
        return projectService.getAllProjects();
    }

    public List<Issue> searchIssues(String projectId, String reporterId, String assigneeId, IssueStatus status) {
        return issueService.searchIssues(projectId, reporterId, assigneeId, status);
    }

    public Optional<Issue> getIssueById(String id) {
        return issueService.getIssueById(id);
    }

    public void createIssue(String id, String title, String description, String projectId) {
        if (currentUser == null) return;
        issueService.createIssue(id, title, description, projectId, currentUser.getId());
    }

    public void addComment(String issueId, String content) {
        if (currentUser == null) return;
        issueService.addComment(issueId, currentUser.getId(), content);
    }

    public void updateStatus(String issueId, IssueStatus status, String message) {
        if (currentUser == null) return;
        issueService.updateStatus(issueId, status, currentUser.getId(), message);
    }

    public void assignIssue(String issueId, String assigneeId) {
        if (currentUser == null) return;
        issueService.assignIssue(issueId, assigneeId, currentUser.getId());
    }

    public List<String> getRecommendations(String issueId) {
        return issueService.recommendAssignee(issueId);
    }

    public void addUser(String id, String name, String password, Role role) {
        userService.addUser(id, name, password, role);
    }

    public void addProject(String id, String name, String description) {
        projectService.addProject(id, name, description);
    }
    
    public Map<String, Long> getStatistics(String projectId) {
        List<Issue> issues = issueService.searchIssues(projectId, null, null, null);
        return issues.stream()
                .collect(Collectors.groupingBy(i -> i.getReportedDate().toLocalDate().toString(), Collectors.counting()));
    }
}
