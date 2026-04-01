package com.se2026.ims.view.swing;

import com.se2026.ims.controller.IMSController;
import com.se2026.ims.model.*;
import com.se2026.ims.util.I18n;
import com.se2026.ims.view.IMSView;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.FlatClientProperties;

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

    // 메인 앱 구조용
    private JPanel mainAppPanel;
    private JPanel sidebar;
    private JPanel contentPanel;
    private CardLayout contentCardLayout;

    // 패널들
    private JPanel loginPanel;
    private JPanel dashboardPanel;
    private JPanel issueListPanel;
    private JPanel issueDetailPanel;
    private JPanel statsPanel;
    private JPanel adminPanel;
    private JPanel boardPanel;
    private JButton adminBtn;

    // Issue Detail components
    private JTextArea issueInfoArea;
    private JPanel commentPanel;
    private JPanel actionPanel;
    private String currentIssueId;

    @Override
    public void setController(IMSController controller) {
        this.controller = controller;
    }

    @Override
    public void start() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Apply Apple-style Look and Feel
                if (!FlatMacLightLaf.setup()) {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                
                // UI Global tuning for Apple style
                UIManager.put("Button.arc", 12);
                UIManager.put("Component.arc", 12);
                UIManager.put("TextComponent.arc", 12);
                UIManager.put("ScrollBar.thumbArc", 999);
                UIManager.put("ProgressBar.arc", 12);
                UIManager.put("Table.selectionArc", 8);
                
                frame = new JFrame(I18n.get("app.title"));
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1200, 800);

                cardLayout = new CardLayout();
                mainPanel = new JPanel(cardLayout);

                initLoginPanel();
                initMainAppPanel();

                mainPanel.add(loginPanel, "LOGIN");
                mainPanel.add(mainAppPanel, "MAIN_APP");

                frame.add(mainPanel);
                showPanel("LOGIN");
                frame.setVisible(true);
            } catch (Exception e) {
                System.err.println("Cannot start Swing UI: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Fatal Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showPanel(String name) {
        cardLayout.show(mainPanel, name);
    }

    private void showContent(String name) {
        contentCardLayout.show(contentPanel, name);
    }

    private void initMainAppPanel() {
        mainAppPanel = new JPanel(new BorderLayout());

        // Sidebar
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(245, 245, 247)); // Apple Sidebar Light Gray
        sidebar.setPreferredSize(new Dimension(240, 0)); // Slightly wider
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(210, 210, 212)));

        JLabel logoLabel = new JLabel("Issue Tracker");
        logoLabel.setForeground(new Color(29, 29, 31)); // Apple Text Dark
        logoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        logoLabel.setBorder(BorderFactory.createEmptyBorder(30, 25, 30, 25));
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logoLabel);

        JButton dashBtn = createSidebarButton(I18n.get("nav.dashboard"));
        JButton boardBtn = createSidebarButton(I18n.get("nav.board"));
        JButton issueBtn = createSidebarButton(I18n.get("nav.issue_list"));
        JButton statsBtn = createSidebarButton(I18n.get("nav.stats"));
        adminBtn = createSidebarButton(I18n.get("nav.admin"));
        
        JButton logoutBtn = createSidebarButton(I18n.get("nav.logout"));

        dashBtn.addActionListener(e -> { updateDashboard(); showContent("DASHBOARD"); });
        boardBtn.addActionListener(e -> { updateBoardView(); showContent("BOARD"); });
        issueBtn.addActionListener(e -> { updateIssueList(); showContent("ISSUE_LIST"); });
        statsBtn.addActionListener(e -> { updateStats(); showContent("STATS"); });
        adminBtn.addActionListener(e -> { updateAdminPanel(); showContent("ADMIN"); });
        logoutBtn.addActionListener(e -> {
            controller.logout();
            showPanel("LOGIN");
        });

        sidebar.add(dashBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(boardBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(issueBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(statsBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        sidebar.add(adminBtn);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        mainAppPanel.add(sidebar, BorderLayout.WEST);

        // Content Area
        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.setBackground(Color.WHITE);

        initDashboardPanel();
        initBoardPanel();
        initIssueListPanel();
        initStatsPanel();
        initAdminPanel();

        contentPanel.add(dashboardPanel, "DASHBOARD");
        contentPanel.add(boardPanel, "BOARD");
        contentPanel.add(issueListPanel, "ISSUE_LIST");
        contentPanel.add(statsPanel, "STATS");
        contentPanel.add(adminPanel, "ADMIN");

        mainAppPanel.add(contentPanel, BorderLayout.CENTER);
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(210, 40));
        btn.setPreferredSize(new Dimension(210, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMargin(new Insets(0, 25, 0, 0));
        btn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        btn.setForeground(new Color(60, 60, 67));
        
        btn.putClientProperty("JButton.buttonType", "toolBarButton");
        btn.putClientProperty("JComponent.roundRect", true);
        
        return btn;
    }

    private void initBoardPanel() {
        boardPanel = new JPanel(new BorderLayout());
        boardPanel.setBackground(new Color(242, 242, 247));
        
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        header.setOpaque(false);
        JLabel title = new JLabel(I18n.get("nav.board"));
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        header.add(title);
        
        boardPanel.add(header, BorderLayout.NORTH);
        
        JPanel columnsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        columnsPanel.setOpaque(false);
        columnsPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 20, 15));
        
        boardPanel.add(new JScrollPane(columnsPanel) {{
            setBorder(null);
            getViewport().setOpaque(false);
            setOpaque(false);
        }}, BorderLayout.CENTER);
    }

    private void updateBoardView() {
        if (boardPanel == null) return;
        
        List<Issue> issues = controller.searchIssues(null, null, null, null);
        
        JScrollPane scroll = (JScrollPane) boardPanel.getComponent(1);
        JPanel columnsPanel = (JPanel) scroll.getViewport().getView();
        columnsPanel.removeAll();
        
        columnsPanel.add(createBoardColumn(IssueStatus.NEW, issues));
        columnsPanel.add(createBoardColumn(IssueStatus.ASSIGNED, issues));
        columnsPanel.add(createBoardColumn(IssueStatus.FIXED, issues));
        
        columnsPanel.revalidate();
        columnsPanel.repaint();
    }

    private JPanel createBoardColumn(IssueStatus status, List<Issue> allIssues) {
        JPanel col = new JPanel(new BorderLayout());
        col.setOpaque(false);
        
        String titleKey = "status." + status.name().toLowerCase();
        JLabel titleLabel = new JLabel(I18n.get(titleKey) + " (" + 
                allIssues.stream().filter(i -> i.getStatus() == status).count() + ")");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
        
        JPanel cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setOpaque(false);
        
        allIssues.stream()
            .filter(i -> i.getStatus() == status)
            .forEach(i -> cardsContainer.add(createIssueCard(i)));
        
        cardsContainer.add(Box.createVerticalGlue());
        
        col.add(titleLabel, BorderLayout.NORTH);
        col.add(new JScrollPane(cardsContainer) {{
            setBorder(null);
            getViewport().setOpaque(false);
            setOpaque(false);
            getVerticalScrollBar().setUnitIncrement(16);
        }}, BorderLayout.CENTER);
        
        return col;
    }

    private JPanel createIssueCard(Issue issue) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 0, 10, 0),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 235), 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
            )
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel idLabel = new JLabel(issue.getId());
        idLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        idLabel.setForeground(Color.GRAY);
        
        JLabel titleLabel = new JLabel("<html><body style='width: 150px'>" + issue.getTitle() + "</body></html>");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        
        JLabel priorityLabel = new JLabel(issue.getPriority().name());
        priorityLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        priorityLabel.setOpaque(true);
        priorityLabel.setBackground(getPriorityColor(issue.getPriority()));
        priorityLabel.setForeground(Color.WHITE);
        priorityLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        
        footer.add(priorityLabel, BorderLayout.WEST);
        if (issue.getAssigneeId() != null) {
            JLabel userLabel = new JLabel(issue.getAssigneeId());
            userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            footer.add(userLabel, BorderLayout.EAST);
        }
        
        card.add(idLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(footer);
        
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                updateIssueDetail(issue.getId());
                showContent("ISSUE_LIST"); // 상세 보기 이동
            }
        });
        
        return card;
    }

    private Color getPriorityColor(Priority priority) {
        switch (priority) {
            case BLOCKER: return new Color(255, 59, 48); // Apple Red
            case CRITICAL: return new Color(255, 149, 0); // Apple Orange
            case MAJOR: return new Color(255, 204, 0); // Apple Yellow
            case MINOR: return new Color(0, 122, 255); // Apple Blue
            default: return new Color(142, 142, 147); // Apple Gray
        }
    }

    private void initLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(new Color(242, 242, 247));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 225), 1, true),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel(I18n.get("login.title"));
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 30, 0);
        card.add(titleLabel, gbc);

        gbc.insets = new Insets(10, 0, 5, 0);
        gbc.gridwidth = 2;
        gbc.gridy = 1; 
        JLabel userLabel = new JLabel(I18n.get("login.username"));
        userLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        card.add(userLabel, gbc);
        
        JTextField idField = new JTextField(20);
        idField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your ID");
        gbc.gridy = 2; 
        card.add(idField, gbc);

        gbc.gridy = 3; 
        JLabel pwLabel = new JLabel(I18n.get("login.password"));
        pwLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        card.add(pwLabel, gbc);
        
        JPasswordField pwField = new JPasswordField(20);
        pwField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your password");
        gbc.gridy = 4; 
        card.add(pwField, gbc);

        JButton loginBtn = new JButton(I18n.get("login.submit"));
        loginBtn.setBackground(new Color(0, 122, 255)); // Apple Blue
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        loginBtn.setPreferredSize(new Dimension(0, 40));
        gbc.gridy = 5; 
        gbc.insets = new Insets(30, 0, 0, 0);
        card.add(loginBtn, gbc);

        loginPanel.add(card);

        java.awt.event.ActionListener loginAction = e -> {
            String id = idField.getText().trim();
            String pw = new String(pwField.getPassword());
            if (id.isEmpty()) {
                showMessage(I18n.get("login.error.empty"));
                return;
            }
            if (controller.login(id, pw)) {
                updateDashboard();
                showPanel("MAIN_APP");
                showContent("DASHBOARD");
            } else {
                showMessage(I18n.get("login.error.failed"));
            }
        };

        loginBtn.addActionListener(loginAction);
        idField.addActionListener(loginAction);
        pwField.addActionListener(loginAction);
    }

    private JLabel welcomeLabel;
    private JPanel menuPanel;

    private JTextArea dashboardSummaryArea;

    private void initDashboardPanel() {
        dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(Color.WHITE);
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        welcomeLabel = new JLabel("Dashboard");
        welcomeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        header.add(welcomeLabel, BorderLayout.WEST);
        
        dashboardPanel.add(header, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        dashboardPanel.add(new JScrollPane(centerPanel) {{
            setBorder(null);
        }}, BorderLayout.CENTER);
    }

    private void updateDashboard() {
        if (dashboardPanel == null || controller.getCurrentUser() == null) return;
        User user = controller.getCurrentUser();
        
        JScrollPane scroll = (JScrollPane) dashboardPanel.getComponent(1);
        JPanel centerPanel = (JPanel) scroll.getViewport().getView();
        centerPanel.removeAll();

        // Welcome sub-text
        JLabel welcomeSub = new JLabel(I18n.get("dashboard.welcome", user.getName(), user.getRole()));
        welcomeSub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        welcomeSub.setForeground(Color.GRAY);
        welcomeSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(welcomeSub);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Summary Cards Container
        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        List<Issue> myIssues = controller.searchIssues(null, null, user.getId(), null);
        
        cardsPanel.add(createSummaryCard("My Issues", String.valueOf(myIssues.size()), new Color(0, 122, 255)));
        cardsPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        
        long pending = myIssues.stream().filter(i -> i.getStatus() != IssueStatus.FIXED && i.getStatus() != IssueStatus.CLOSED).count();
        cardsPanel.add(createSummaryCard("Pending", String.valueOf(pending), new Color(255, 149, 0)));
        
        centerPanel.add(cardsPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        // Assigned Issues List
        JLabel listTitle = new JLabel(I18n.get("dashboard.assigned_to_me"));
        listTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        listTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(listTitle);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        if (myIssues.isEmpty()) {
            centerPanel.add(new JLabel(I18n.get("dashboard.no_issues")) {{
                setForeground(Color.GRAY);
                setAlignmentX(Component.LEFT_ALIGNMENT);
            }});
        } else {
            for (Issue issue : myIssues) {
                centerPanel.add(createIssueRow(issue));
                centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
        
        adminBtn.setVisible(user.getRole() == Role.ADMIN);
        sidebar.revalidate();
        sidebar.repaint();

        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private JPanel createSummaryCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 235), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setPreferredSize(new Dimension(220, 120));
        card.setMaximumSize(new Dimension(220, 120));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);
        card.add(titleLabel, BorderLayout.NORTH);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 36));
        valueLabel.setForeground(accentColor);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createIssueRow(Issue issue) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(new Color(248, 248, 250));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(235, 235, 240)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel idLabel = new JLabel(issue.getId());
        idLabel.setPreferredSize(new Dimension(80, 0));
        idLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        
        JLabel titleLabel = new JLabel(issue.getTitle());
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        
        JLabel statusLabel = new JLabel(I18n.get("status." + issue.getStatus().name().toLowerCase()));
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        row.add(idLabel, BorderLayout.WEST);
        row.add(titleLabel, BorderLayout.CENTER);
        row.add(statusLabel, BorderLayout.EAST);

        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                updateIssueDetail(issue.getId());
                showContent("ISSUE_LIST");
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                row.setBackground(new Color(240, 240, 245));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                row.setBackground(new Color(248, 248, 250));
            }
        });

        return row;
    }

    private JTable issueTable;
    private DefaultTableModel issueTableModel;

    private void initIssueListPanel() {
        issueListPanel = new JPanel(new BorderLayout());
        issueListPanel.setBackground(Color.WHITE);
        
        // --- 1. Top Search Panel ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel title = new JLabel(I18n.get("nav.issue_list"));
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        topPanel.add(title, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);
        
        JTextField projectFilter = new JTextField(12);
        projectFilter.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, I18n.get("issue.project_id"));
        filterPanel.add(projectFilter);
        
        JButton searchBtn = new JButton(I18n.get("issue.search"));
        searchBtn.putClientProperty("JButton.buttonType", "roundRect");
        filterPanel.add(searchBtn);
        
        JButton createBtn = new JButton(I18n.get("issue.create"));
        createBtn.putClientProperty("JButton.buttonType", "roundRect");
        createBtn.setBackground(new Color(0, 122, 255));
        createBtn.setForeground(Color.WHITE);
        filterPanel.add(createBtn);
        
        topPanel.add(filterPanel, BorderLayout.EAST);
        issueListPanel.add(topPanel, BorderLayout.NORTH);

        // --- 2. Center Table ---
        String[] cols = {
            I18n.get("issue.id"), 
            I18n.get("issue.title"), 
            I18n.get("issue.project"), 
            I18n.get("issue.status"), 
            I18n.get("issue.assignee")
        };
        issueTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        issueTable = new JTable(issueTableModel);
        issueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        issueTable.setRowHeight(40);
        issueTable.setShowGrid(false);
        issueTable.setIntercellSpacing(new Dimension(0, 0));
        issueTable.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        issueTable.getTableHeader().setBackground(new Color(250, 250, 252));
        
        // 테이블 행 선택 시 상세 정보 업데이트
        issueTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = issueTable.getSelectedRow();
                if (row >= 0) {
                    String id = (String) issueTable.getValueAt(row, 0);
                    updateIssueDetail(id);
                }
            }
        });

        // --- 3. Detail Panel (to be put in JSplitPane) ---
        initIssueDetailSubPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(issueTable) {{
            setBorder(BorderFactory.createEmptyBorder());
            getViewport().setBackground(Color.WHITE);
        }}, issueDetailPanel);
        splitPane.setDividerLocation(300);
        splitPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 235)));
        
        issueListPanel.add(splitPane, BorderLayout.CENTER);

        searchBtn.addActionListener(e -> {
            String pid = projectFilter.getText().isEmpty() ? null : projectFilter.getText();
            List<Issue> issues = controller.searchIssues(pid, null, null, null);
            refreshIssueTable(issues);
        });

        createBtn.addActionListener(e -> showCreateIssueDialog());
    }

    private void initIssueDetailSubPanel() {
        issueDetailPanel = new JPanel(new BorderLayout());
        issueDetailPanel.setBackground(Color.WHITE);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(250, 250, 252));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 235)),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel detailTitle = new JLabel(I18n.get("issue.details"));
        detailTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        headerPanel.add(detailTitle, BorderLayout.WEST);
        
        issueDetailPanel.add(headerPanel, BorderLayout.NORTH);
        
        issueInfoArea = new JTextArea();
        issueInfoArea.setEditable(false);
        issueInfoArea.setLineWrap(true);
        issueInfoArea.setWrapStyleWord(true);
        issueInfoArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        issueInfoArea.setMargin(new Insets(20, 20, 20, 20));
        
        commentPanel = new JPanel();
        commentPanel.setLayout(new BoxLayout(commentPanel, BoxLayout.Y_AXIS));
        commentPanel.setBackground(Color.WHITE);
        commentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JSplitPane detailSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
            new JScrollPane(issueInfoArea) {{ setBorder(null); }}, 
            new JScrollPane(commentPanel) {{ setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(240, 240, 245))); }}
        );
        detailSplit.setDividerLocation(450);
        detailSplit.setBorder(null);

        issueDetailPanel.add(detailSplit, BorderLayout.CENTER);

        actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(240, 240, 245)));
        
        JButton addCommentBtn = new JButton(I18n.get("issue.add_comment"));
        JButton changeStatusBtn = new JButton(I18n.get("issue.change_status"));
        
        addCommentBtn.putClientProperty("JButton.buttonType", "roundRect");
        changeStatusBtn.putClientProperty("JButton.buttonType", "roundRect");

        actionPanel.add(addCommentBtn);
        actionPanel.add(changeStatusBtn);
        issueDetailPanel.add(actionPanel, BorderLayout.SOUTH);

        addCommentBtn.addActionListener(e -> {
            if (currentIssueId == null) return;
            String content = JOptionPane.showInputDialog(frame, I18n.get("dialog.comment.enter"));
            if (content != null && !content.isEmpty()) {
                controller.addComment(currentIssueId, content);
                updateIssueDetail(currentIssueId);
            }
        });

        changeStatusBtn.addActionListener(e -> showStatusChangeDialog());
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

    private void updateIssueDetail(String issueId) {
        this.currentIssueId = issueId;
        Optional<Issue> opt = controller.getIssueById(issueId);
        if (opt.isEmpty()) return;
        
        Issue issue = opt.get();
        StringBuilder sb = new StringBuilder();
        sb.append(issue.getTitle()).append("\n\n");
        sb.append("ID: ").append(issue.getId()).append("\n");
        sb.append("Project: ").append(issue.getProjectId()).append("\n");
        sb.append("Status: ").append(I18n.get("status." + issue.getStatus().name().toLowerCase())).append("\n");
        sb.append("Reporter: ").append(issue.getReporterId()).append("\n");
        sb.append("Assignee: ").append(issue.getAssigneeId() != null ? issue.getAssigneeId() : "Unassigned").append("\n");
        if (issue.getFixerId() != null) sb.append("Fixer: ").append(issue.getFixerId()).append("\n");
        sb.append("\n---\n\n");
        sb.append(issue.getDescription());
        
        issueInfoArea.setText(sb.toString());
        issueInfoArea.setCaretPosition(0);

        commentPanel.removeAll();
        
        // PL 추천 기능 표시
        if (controller.getCurrentUser().getRole() == Role.PL) {
            List<String> recs = controller.getRecommendations(issueId);
            if (!recs.isEmpty()) {
                JPanel recCard = new JPanel(new BorderLayout());
                recCard.setBackground(new Color(232, 242, 255));
                recCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(186, 214, 255), 1, true),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
                recCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
                
                JLabel recLabel = new JLabel("💡 " + I18n.get("issue.detail.recommendations", String.join(", ", recs)));
                recLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                recLabel.setForeground(new Color(0, 102, 204));
                recCard.add(recLabel);
                
                commentPanel.add(recCard);
                commentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        }

        for (Comment c : issue.getComments()) {
            if (c != null) {
                commentPanel.add(createCommentCard(c));
                commentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        
        commentPanel.add(Box.createVerticalGlue());
        
        commentPanel.revalidate();
        commentPanel.repaint();
        issueDetailPanel.revalidate();
        issueDetailPanel.repaint();
    }

    private JPanel createCommentCard(Comment c) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(250, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 235), 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel author = new JLabel(c.getAuthorId());
        author.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        JLabel time = new JLabel(c.getTimestamp().toString());
        time.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        time.setForeground(Color.GRAY);
        
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(author, BorderLayout.WEST);
        header.add(time, BorderLayout.EAST);
        
        JLabel content = new JLabel("<html>" + c.getContent().replace("\n", "<br>") + "</html>");
        content.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        content.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        card.add(header, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }

    private void showStatusChangeDialog() {
        IssueStatus[] statuses = IssueStatus.values();
        IssueStatus selected = (IssueStatus) JOptionPane.showInputDialog(frame, I18n.get("dialog.status.message"), I18n.get("dialog.status.title"),
                JOptionPane.QUESTION_MESSAGE, null, statuses, statuses[0]);
        
        if (selected != null) {
            try {
                if (selected == IssueStatus.ASSIGNED) {
                    String assignee = JOptionPane.showInputDialog(frame, I18n.get("dialog.status.assignee"));
                    if (assignee != null) controller.assignIssue(currentIssueId, assignee);
                } else {
                    String msg = JOptionPane.showInputDialog(frame, I18n.get("dialog.status.comment"));
                    controller.updateStatus(currentIssueId, selected, msg);
                }
                updateIssueDetail(currentIssueId);
            } catch (IllegalArgumentException ex) {
                showMessage(ex.getMessage());
            }
        }
    }

    private void showCreateIssueDialog() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField idField = new JTextField(15);
        JTextField titleField = new JTextField(15);
        JTextArea descField = new JTextArea(4, 15);
        descField.setLineWrap(true);
        JTextField projectField = new JTextField(15);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel(I18n.get("issue.label.id")), gbc);
        gbc.gridx = 1; panel.add(idField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel(I18n.get("issue.label.title")), gbc);
        gbc.gridx = 1; panel.add(titleField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel(I18n.get("issue.project_id")), gbc);
        gbc.gridx = 1; panel.add(projectField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel(I18n.get("issue.label.description")), gbc);
        gbc.gridx = 1; panel.add(new JScrollPane(descField), gbc);

        int option = JOptionPane.showConfirmDialog(frame, panel, I18n.get("issue.create"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            try {
                controller.createIssue(idField.getText(), titleField.getText(), descField.getText(), projectField.getText());
                updateIssueList();
                updateBoardView();
            } catch (IllegalArgumentException ex) {
                showMessage(ex.getMessage());
            }
        }
    }

    private void initStatsPanel() {
        statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JLabel titleLabel = new JLabel(I18n.get("nav.stats"));
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        statsPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        statsPanel.add(new JScrollPane(content) {{ setBorder(null); }}, BorderLayout.CENTER);
    }

    private void updateStats() {
        if (statsPanel == null) return;
        JScrollPane scroll = (JScrollPane) statsPanel.getComponent(1);
        JPanel content = (JPanel) scroll.getViewport().getView();
        content.removeAll();

        Map<String, Long> stats = controller.getStatistics(null);
        
        JLabel subTitle = new JLabel(I18n.get("stats.header"));
        subTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        subTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        content.add(subTitle);

        // Simple Bar Chart visualization
        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            JPanel barRow = new JPanel(new BorderLayout(15, 0));
            barRow.setOpaque(false);
            barRow.setMaximumSize(new Dimension(800, 40));
            barRow.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

            JLabel dateLabel = new JLabel(entry.getKey());
            dateLabel.setPreferredSize(new Dimension(100, 20));
            barRow.add(dateLabel, BorderLayout.WEST);

            long count = entry.getValue();
            JPanel barContainer = new JPanel(null) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0, 122, 255));
                    int width = (int) (count * 50); // Scale factor
                    g2.fillRoundRect(0, 5, Math.min(width, getWidth() - 50), 20, 10, 10);
                    g2.setColor(Color.BLACK);
                    g2.drawString(count + " " + I18n.get("stats.issues_suffix"), Math.min(width, getWidth() - 50) + 10, 20);
                }
            };
            barContainer.setOpaque(false);
            barRow.add(barContainer, BorderLayout.CENTER);

            content.add(barRow);
        }

        content.revalidate();
        content.repaint();
    }

    private void initAdminPanel() {
        adminPanel = new JPanel(new BorderLayout());
        adminPanel.setBackground(Color.WHITE);
        adminPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel(I18n.get("admin.title"));
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        adminPanel.add(titleLabel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty("JTabbedPane.tabType", "card");

        userTable = new JTable();
        projectTable = new JTable();

        JPanel userPanel = new JPanel(new BorderLayout(0, 15));
        userPanel.setOpaque(false);
        userPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        JButton addUserBtn = new JButton(I18n.get("admin.btn.add_user"));
        addUserBtn.addActionListener(e -> showAddUserDialog());
        userPanel.add(addUserBtn, BorderLayout.SOUTH);

        JPanel projectPanel = new JPanel(new BorderLayout(0, 15));
        projectPanel.setOpaque(false);
        projectPanel.add(new JScrollPane(projectTable), BorderLayout.CENTER);
        JButton addProjectBtn = new JButton(I18n.get("admin.btn.add_project"));
        addProjectBtn.addActionListener(e -> showAddProjectDialog());
        projectPanel.add(addProjectBtn, BorderLayout.SOUTH);

        tabbedPane.addTab(I18n.get("admin.tab.users"), userPanel);
        tabbedPane.addTab(I18n.get("admin.tab.projects"), projectPanel);

        adminPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private JTable userTable;
    private JTable projectTable;

    private void updateAdminPanel() {
        if (adminPanel == null) return;
        
        // Update Users Table
        List<User> users = controller.getAllUsers();
        String[] userCols = {I18n.get("admin.label.user_id"), I18n.get("admin.label.name"), I18n.get("admin.label.role")};
        DefaultTableModel userModel = new DefaultTableModel(userCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (User u : users) {
            userModel.addRow(new Object[]{u.getId(), u.getName(), u.getRole()});
        }
        userTable.setModel(userModel);

        // Update Projects Table
        List<Project> projects = controller.getAllProjects();
        String[] projCols = {I18n.get("admin.label.project_id"), I18n.get("admin.label.project_name"), I18n.get("admin.label.description")};
        DefaultTableModel projModel = new DefaultTableModel(projCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Project p : projects) {
            projModel.addRow(new Object[]{p.getId(), p.getName(), p.getDescription()});
        }
        projectTable.setModel(projModel);
    }

    private void showAddUserDialog() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField idField = new JTextField(15);
        JTextField nameField = new JTextField(15);
        JTextField pwField = new JTextField(15);
        Role[] roles = Role.values();
        JComboBox<Role> roleCombo = new JComboBox<>(roles);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel(I18n.get("admin.label.user_id")), gbc);
        gbc.gridx = 1; panel.add(idField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel(I18n.get("admin.label.name")), gbc);
        gbc.gridx = 1; panel.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel(I18n.get("admin.label.password")), gbc);
        gbc.gridx = 1; panel.add(pwField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel(I18n.get("admin.label.role")), gbc);
        gbc.gridx = 1; panel.add(roleCombo, gbc);

        int option = JOptionPane.showConfirmDialog(frame, panel, I18n.get("admin.add_user"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            try {
                controller.addUser(idField.getText(), nameField.getText(), pwField.getText(), (Role)roleCombo.getSelectedItem());
                updateAdminPanel();
                showMessage(I18n.get("admin.msg.user_added"));
            } catch (IllegalArgumentException ex) {
                showMessage(ex.getMessage());
            }
        }
    }

    private void showAddProjectDialog() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField idField = new JTextField(15);
        JTextField nameField = new JTextField(15);
        JTextField descField = new JTextField(15);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel(I18n.get("admin.label.project_id")), gbc);
        gbc.gridx = 1; panel.add(idField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel(I18n.get("admin.label.project_name")), gbc);
        gbc.gridx = 1; panel.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel(I18n.get("admin.label.description")), gbc);
        gbc.gridx = 1; panel.add(descField, gbc);

        int option = JOptionPane.showConfirmDialog(frame, panel, I18n.get("admin.add_project"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            try {
                controller.addProject(idField.getText(), nameField.getText(), descField.getText());
                updateAdminPanel();
                showMessage(I18n.get("admin.msg.project_added"));
            } catch (IllegalArgumentException ex) {
                showMessage(ex.getMessage());
            }
        }
    }

    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }
}
