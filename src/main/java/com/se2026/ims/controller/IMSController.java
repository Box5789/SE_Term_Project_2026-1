package com.se2026.ims.controller;

import com.se2026.ims.model.*;
import com.se2026.ims.service.IssueService;
import com.se2026.ims.service.ProjectService;
import com.se2026.ims.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
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
        requireLoggedIn();
        requireRole(Role.TESTER);
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Issue ID cannot be empty");
        }
        String trimmedId = id.trim();
        if (issueService.getIssueById(trimmedId).isPresent()) {
            throw new IllegalArgumentException("Issue ID already exists: " + trimmedId);
        }
        if (projectService.getProjectById(projectId).isEmpty()) {
            throw new IllegalArgumentException("Invalid Project ID: " + projectId);
        }
        issueService.createIssue(trimmedId, title, description, projectId, currentUser.getId());
    }

    public void addComment(String issueId, String content) {
        requireLoggedIn();
        issueService.addComment(issueId, currentUser.getId(), content);
    }

    public void updateStatus(String issueId, IssueStatus status, String message) {
        requireLoggedIn();
        Issue issue = issueService.getIssueById(issueId)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found: " + issueId));
        if (status == IssueStatus.FIXED) {
            requireRole(Role.DEV);
            if (!currentUser.getId().equals(issue.getAssigneeId())) {
                throw new SecurityException("Only the assigned developer can fix this issue");
            }
            issueService.fixIssue(issueId, currentUser.getId(), message);
        } else if (status == IssueStatus.RESOLVED || status == IssueStatus.REOPENED) {
            requireRole(Role.TESTER);
            if (!currentUser.getId().equals(issue.getReporterId())) {
                throw new SecurityException("Only the reporter can resolve or reopen this issue");
            }
            issueService.updateStatus(issueId, status, currentUser.getId(), message);
        } else if (status == IssueStatus.CLOSED) {
            requireRole(Role.PL);
            issueService.updateStatus(issueId, status, currentUser.getId(), message);
        } else if (status == IssueStatus.ASSIGNED) {
            throw new IllegalArgumentException("Use assignIssue to assign an issue with an assignee");
        } else {
            issueService.updateStatus(issueId, status, currentUser.getId(), message);
        }
    }

    public void assignIssue(String issueId, String assigneeId) {
        requireLoggedIn();
        requireRole(Role.PL);
        Optional<User> assignee = userService.getUserById(assigneeId);
        if (assignee.isEmpty()) {
            throw new IllegalArgumentException("Invalid Assignee ID: " + assigneeId);
        }
        if (assignee.get().getRole() != Role.DEV) {
            throw new IllegalArgumentException("Assignee must be a developer: " + assigneeId);
        }
        issueService.assignIssue(issueId, assigneeId, currentUser.getId());
    }

    public List<String> getRecommendations(String issueId) {
        requireLoggedIn();
        requireRole(Role.PL);
        return issueService.recommendAssignee(issueId);
    }

    public void addUser(String id, String name, String password, Role role) {
        requireLoggedIn();
        requireRole(Role.ADMIN);
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        String trimmedId = id.trim();
        if (userService.getUserById(trimmedId).isPresent()) {
            throw new IllegalArgumentException("User ID already exists: " + trimmedId);
        }
        userService.addUser(trimmedId, name, password, role);
    }

    public void addProject(String id, String name, String description) {
        requireLoggedIn();
        requireRole(Role.ADMIN);
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Project ID cannot be empty");
        }
        String trimmedId = id.trim();
        if (projectService.getProjectById(trimmedId).isPresent()) {
            throw new IllegalArgumentException("Project ID already exists: " + trimmedId);
        }
        projectService.addProject(trimmedId, name, description);
    }

    public void updateProject(String id, String name, String description) {
        requireLoggedIn();
        requireRole(Role.ADMIN);
        projectService.updateProject(id, name, description);
    }

    public void deleteProject(String id) {
        requireLoggedIn();
        requireRole(Role.ADMIN);
        projectService.deleteProject(id);
    }

    public void updateUser(String id, String name, String password, Role role) {
        requireLoggedIn();
        requireRole(Role.ADMIN);
        userService.updateUser(id, name, password, role);
    }

    public void deleteUser(String id) {
        requireLoggedIn();
        requireRole(Role.ADMIN);
        userService.deleteUser(id);
    }

    public void updateIssue(String id, String title, String description, String projectId, Priority priority) {
        requireLoggedIn();
        requireRole(Role.ADMIN);
        issueService.updateIssue(id, title, description, projectId, priority);
    }

    public void deleteIssue(String id) {
        requireLoggedIn();
        requireRole(Role.ADMIN);
        issueService.deleteIssue(id);
    }
    
    public Map<String, Long> getStatistics(String projectId) {
        List<Issue> issues = issueService.searchIssues(projectId, null, null, null);
        return issues.stream()
                .collect(Collectors.groupingBy(i -> i.getReportedDate().toLocalDate().toString(), TreeMap::new, Collectors.counting()));
    }

    public Map<String, Long> getMonthlyStatistics(String projectId) {
        List<Issue> issues = issueService.searchIssues(projectId, null, null, null);
        return issues.stream()
                .collect(Collectors.groupingBy(i -> i.getReportedDate().getYear() + "-" +
                        String.format("%02d", i.getReportedDate().getMonthValue()), TreeMap::new, Collectors.counting()));
    }

    private void requireLoggedIn() {
        if (currentUser == null) {
            throw new SecurityException("Login is required");
        }
    }

    private void requireRole(Role role) {
        if (currentUser == null || currentUser.getRole() != role) {
            throw new SecurityException("This action requires " + role + " role");
        }
    }
}
