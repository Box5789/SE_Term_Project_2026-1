package com.se2026.ims.view.swing;

import com.se2026.ims.controller.IMSController;
import com.se2026.ims.model.*;
import com.se2026.ims.view.IMSView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SwingView implements IMSView {
    private IMSController controller;
    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    // 패널들
    private JPanel loginPanel;
    private JPanel dashboardPanel;
    private JPanel issueListPanel;
    private JPanel issueDetailPanel;
    private JPanel statsPanel;
    private JPanel adminPanel;

    @Override
    public void setController(IMSController controller) {
        this.controller = controller;
    }

    @Override
    public void start() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            frame = new JFrame("Issue Management System (IMS)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);

            cardLayout = new CardLayout();
            mainPanel = new JPanel(cardLayout);

            initLoginPanel();
            initDashboardPanel();
            initIssueListPanel();
            initIssueDetailPanel();
            initStatsPanel();
            initAdminPanel();

            mainPanel.add(loginPanel, "LOGIN");
            mainPanel.add(dashboardPanel, "DASHBOARD");
            mainPanel.add(issueListPanel, "ISSUE_LIST");
            mainPanel.add(issueDetailPanel, "ISSUE_DETAIL");
            mainPanel.add(statsPanel, "STATS");
            mainPanel.add(adminPanel, "ADMIN");

            frame.add(mainPanel);
            showPanel("LOGIN");
            frame.setVisible(true);
        } catch (Exception e) {
            System.err.println("Cannot start Swing UI: " + e.getMessage());
        }
    }

    private void showPanel(String name) {
        cardLayout.show(mainPanel, name);
    }

    private void initLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("IMS Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; loginPanel.add(new JLabel("ID:"), gbc);
        JTextField idField = new JTextField(15);
        gbc.gridx = 1; loginPanel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; loginPanel.add(new JLabel("Password:"), gbc);
        JPasswordField pwField = new JPasswordField(15);
        gbc.gridx = 1; loginPanel.add(pwField, gbc);

        JButton loginBtn = new JButton("Login");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        loginPanel.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> {
            String id = idField.getText();
            String pw = new String(pwField.getPassword());
            if (controller.login(id, pw)) {
                updateDashboard();
                showPanel("DASHBOARD");
            } else {
                showMessage("Invalid credentials");
            }
        });
    }

    private JLabel welcomeLabel;
    private JPanel menuPanel;

    private void initDashboardPanel() {
        dashboardPanel = new JPanel(new BorderLayout());
        welcomeLabel = new JLabel("Welcome", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        dashboardPanel.add(welcomeLabel, BorderLayout.NORTH);

        menuPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));
        
        JButton browseIssuesBtn = new JButton("Browse Issues");
        browseIssuesBtn.addActionListener(e -> {
            updateIssueList();
            showPanel("ISSUE_LIST");
        });
        
        JButton statsBtn = new JButton("View Statistics");
        statsBtn.addActionListener(e -> {
            updateStats();
            showPanel("STATS");
        });

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            controller.logout();
            showPanel("LOGIN");
        });

        menuPanel.add(browseIssuesBtn);
        menuPanel.add(statsBtn);
        menuPanel.add(logoutBtn);
        dashboardPanel.add(menuPanel, BorderLayout.CENTER);
    }

    private void updateDashboard() {
        User user = controller.getCurrentUser();
        welcomeLabel.setText("Welcome, " + user.getName() + " (" + user.getRole() + ")");
        
        // Admin의 경우 관리 메뉴 추가
        if (user.getRole() == Role.ADMIN) {
            JButton adminBtn = new JButton("System Administration");
            adminBtn.addActionListener(e -> showPanel("ADMIN"));
            menuPanel.add(adminBtn, 0);
        }
    }

    private JTable issueTable;
    private DefaultTableModel issueTableModel;

    private void initIssueListPanel() {
        issueListPanel = new JPanel(new BorderLayout());
        
        // 검색 필터
        JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Project ID:"));
        JTextField projectFilter = new JTextField(8);
        filterPanel.add(projectFilter);
        
        JButton searchBtn = new JButton("Search");
        filterPanel.add(searchBtn);
        
        JButton createBtn = new JButton("Create New Issue");
        filterPanel.add(createBtn);
        
        JButton backBtn = new JButton("Back");
        filterPanel.add(backBtn);
        
        issueListPanel.add(filterPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Title", "Project", "Status", "Assignee"};
        issueTableModel = new DefaultTableModel(cols, 0);
        issueTable = new JTable(issueTableModel);
        issueListPanel.add(new JScrollPane(issueTable), BorderLayout.CENTER);

        JButton viewDetailBtn = new JButton("View Detail");
        issueListPanel.add(viewDetailBtn, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> {
            String pid = projectFilter.getText().isEmpty() ? null : projectFilter.getText();
            List<Issue> issues = controller.searchIssues(pid, null, null, null);
            refreshIssueTable(issues);
        });

        createBtn.addActionListener(e -> showCreateIssueDialog());
        backBtn.addActionListener(e -> showPanel("DASHBOARD"));
        viewDetailBtn.addActionListener(e -> {
            int row = issueTable.getSelectedRow();
            if (row >= 0) {
                String id = (String) issueTable.getValueAt(row, 0);
                updateIssueDetail(id);
                showPanel("ISSUE_DETAIL");
            }
        });
    }

    private void refreshIssueTable(List<Issue> issues) {
        issueTableModel.setRowCount(0);
        for (Issue i : issues) {
            issueTableModel.addRow(new Object[]{i.getId(), i.getTitle(), i.getProjectId(), i.getStatus(), i.getAssigneeId()});
        }
    }

    private void updateIssueList() {
        refreshIssueTable(controller.searchIssues(null, null, null, null));
    }

    private JTextArea issueInfoArea;
    private JPanel commentPanel;
    private JPanel actionPanel;
    private String currentIssueId;

    private void initIssueDetailPanel() {
        issueDetailPanel = new JPanel(new BorderLayout());
        
        issueInfoArea = new JTextArea();
        issueInfoArea.setEditable(false);
        issueDetailPanel.add(new JScrollPane(issueInfoArea), BorderLayout.NORTH);

        commentPanel = new JPanel();
        commentPanel.setLayout(new BoxLayout(commentPanel, BoxLayout.Y_AXIS));
        issueDetailPanel.add(new JScrollPane(commentPanel), BorderLayout.CENTER);

        actionPanel = new JPanel();
        JButton addCommentBtn = new JButton("Add Comment");
        JButton changeStatusBtn = new JButton("Change Status");
        JButton backBtn = new JButton("Back to List");

        actionPanel.add(addCommentBtn);
        actionPanel.add(changeStatusBtn);
        actionPanel.add(backBtn);
        issueDetailPanel.add(actionPanel, BorderLayout.SOUTH);

        backBtn.addActionListener(e -> showPanel("ISSUE_LIST"));
        addCommentBtn.addActionListener(e -> {
            String content = JOptionPane.showInputDialog(frame, "Enter comment:");
            if (content != null && !content.isEmpty()) {
                controller.addComment(currentIssueId, content);
                updateIssueDetail(currentIssueId);
            }
        });

        changeStatusBtn.addActionListener(e -> showStatusChangeDialog());
    }

    private void updateIssueDetail(String issueId) {
        this.currentIssueId = issueId;
        Optional<Issue> opt = controller.getIssueById(issueId);
        if (opt.isEmpty()) return;
        
        Issue issue = opt.get();
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(issue.getId()).append("\n");
        sb.append("Title: ").append(issue.getTitle()).append("\n");
        sb.append("Description: ").append(issue.getDescription()).append("\n");
        sb.append("Status: ").append(issue.getStatus()).append("\n");
        sb.append("Reporter: ").append(issue.getReporterId()).append("\n");
        sb.append("Assignee: ").append(issue.getAssigneeId()).append("\n");
        sb.append("Fixer: ").append(issue.getFixerId()).append("\n");
        issueInfoArea.setText(sb.toString());

        commentPanel.removeAll();
        for (Comment c : issue.getComments()) {
            commentPanel.add(new JLabel(c.getAuthorId() + " [" + c.getTimestamp() + "]: " + c.getContent()));
        }
        
        // PL 추천 기능 표시
        if (controller.getCurrentUser().getRole() == Role.PL) {
            List<String> recs = controller.getRecommendations(issueId);
            if (!recs.isEmpty()) {
                JLabel recLabel = new JLabel("Recommended Assignees: " + String.join(", ", recs));
                recLabel.setForeground(Color.BLUE);
                commentPanel.add(recLabel);
            }
        }
        
        issueDetailPanel.revalidate();
        issueDetailPanel.repaint();
    }

    private void showStatusChangeDialog() {
        IssueStatus[] statuses = IssueStatus.values();
        IssueStatus selected = (IssueStatus) JOptionPane.showInputDialog(frame, "Select New Status", "Status Change",
                JOptionPane.QUESTION_MESSAGE, null, statuses, statuses[0]);
        
        if (selected != null) {
            if (selected == IssueStatus.ASSIGNED) {
                String assignee = JOptionPane.showInputDialog(frame, "Enter Assignee ID:");
                if (assignee != null) controller.assignIssue(currentIssueId, assignee);
            } else {
                String msg = JOptionPane.showInputDialog(frame, "Enter status message:");
                controller.updateStatus(currentIssueId, selected, msg);
            }
            updateIssueDetail(currentIssueId);
        }
    }

    private void showCreateIssueDialog() {
        JTextField idField = new JTextField();
        JTextField titleField = new JTextField();
        JTextArea descField = new JTextArea(5, 20);
        JTextField projectField = new JTextField();

        Object[] message = {
            "Issue ID:", idField,
            "Title:", titleField,
            "Description:", new JScrollPane(descField),
            "Project ID:", projectField
        };

        int option = JOptionPane.showConfirmDialog(frame, message, "Create New Issue", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            controller.createIssue(idField.getText(), titleField.getText(), descField.getText(), projectField.getText());
            updateIssueList();
        }
    }

    private JTextArea statsArea;
    private void initStatsPanel() {
        statsPanel = new JPanel(new BorderLayout());
        statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsPanel.add(new JScrollPane(statsArea), BorderLayout.CENTER);
        
        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> showPanel("DASHBOARD"));
        statsPanel.add(backBtn, BorderLayout.SOUTH);
    }

    private void updateStats() {
        Map<String, Long> stats = controller.getStatistics(null);
        StringBuilder sb = new StringBuilder("--- Issue Statistics (Daily Counts) ---\n");
        stats.forEach((date, count) -> sb.append(date).append(": ").append(count).append(" issues\n"));
        statsArea.setText(sb.toString());
    }

    private void initAdminPanel() {
        adminPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        adminPanel.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));

        JButton addUserBtn = new JButton("Add New User");
        addUserBtn.addActionListener(e -> showAddUserDialog());

        JButton addProjectBtn = new JButton("Add New Project");
        addProjectBtn.addActionListener(e -> showAddProjectDialog());

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> showPanel("DASHBOARD"));

        adminPanel.add(addUserBtn);
        adminPanel.add(addProjectBtn);
        adminPanel.add(backBtn);
    }

    private void showAddUserDialog() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField pwField = new JTextField();
        Role[] roles = Role.values();
        JComboBox<Role> roleCombo = new JComboBox<>(roles);

        Object[] message = {
            "User ID:", idField,
            "Name:", nameField,
            "Password:", pwField,
            "Role:", roleCombo
        };

        int option = JOptionPane.showConfirmDialog(frame, message, "Add User", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            controller.addUser(idField.getText(), nameField.getText(), pwField.getText(), (Role)roleCombo.getSelectedItem());
            showMessage("User added successfully");
        }
    }

    private void showAddProjectDialog() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();

        Object[] message = {
            "Project ID:", idField,
            "Project Name:", nameField,
            "Description:", descField
        };

        int option = JOptionPane.showConfirmDialog(frame, message, "Add Project", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            controller.addProject(idField.getText(), nameField.getText(), descField.getText());
            showMessage("Project added successfully");
        }
    }

    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }
}
