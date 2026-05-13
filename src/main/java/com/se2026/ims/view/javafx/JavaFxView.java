package com.se2026.ims.view.javafx;

import com.se2026.ims.controller.IMSController;
import com.se2026.ims.model.Comment;
import com.se2026.ims.model.Issue;
import com.se2026.ims.model.IssueStatus;
import com.se2026.ims.model.Role;
import com.se2026.ims.model.User;
import com.se2026.ims.view.IMSView;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JavaFxView extends Application implements IMSView {
    private static IMSController sharedController;

    private IMSController controller;
    private Stage stage;
    private TableView<Issue> issueTable;
    private TextArea detailArea;
    private TextArea commentsArea;
    private Label recommendationLabel;
    private String currentIssueId;

    @Override
    public void setController(IMSController controller) {
        this.controller = controller;
        sharedController = controller;
    }

    @Override
    public void start() {
        Application.launch(JavaFxView.class);
    }

    @Override
    public void start(Stage primaryStage) {
        this.controller = sharedController;
        this.stage = primaryStage;
        stage.setTitle("Issue Management System (JavaFX)");
        showLogin();
        stage.show();
    }

    private void showLogin() {
        TextField idField = new TextField();
        idField.setPromptText("ID");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Button loginButton = new Button("Login");

        GridPane form = new GridPane();
        form.setPadding(new Insets(30));
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("IMS Login"), 0, 0, 2, 1);
        form.add(new Label("ID"), 0, 1);
        form.add(idField, 1, 1);
        form.add(new Label("Password"), 0, 2);
        form.add(passwordField, 1, 2);
        form.add(loginButton, 1, 3);

        loginButton.setOnAction(e -> {
            if (controller.login(idField.getText().trim(), passwordField.getText())) {
                showMain();
            } else {
                showMessage("Login failed. Please check your ID and password.");
            }
        });

        passwordField.setOnAction(e -> loginButton.fire());
        stage.setScene(new Scene(form, 420, 220));
    }

    private void showMain() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        User user = controller.getCurrentUser();
        Label userLabel = new Label("Logged in: " + user.getId() + " (" + user.getRole() + ")");
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            controller.logout();
            showLogin();
        });
        HBox header = new HBox(12, userLabel, logoutButton);
        header.setPadding(new Insets(0, 0, 10, 0));
        root.setTop(header);

        issueTable = createIssueTable();
        issueTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, issue) -> {
            if (issue != null) {
                updateIssueDetail(issue.getId());
            }
        });

        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(0, 0, 0, 12));
        detailArea = new TextArea();
        detailArea.setEditable(false);
        detailArea.setPrefRowCount(10);
        commentsArea = new TextArea();
        commentsArea.setEditable(false);
        commentsArea.setPrefRowCount(8);
        recommendationLabel = new Label();
        rightPanel.getChildren().addAll(new Label("Issue Details"), detailArea, recommendationLabel,
                new Label("Comments"), commentsArea, createActionPanel(), createIssueForm());
        VBox.setVgrow(detailArea, Priority.ALWAYS);
        VBox.setVgrow(commentsArea, Priority.ALWAYS);

        SplitPane splitPane = new SplitPane(issueTable, rightPanel);
        splitPane.setDividerPositions(0.58);
        root.setCenter(splitPane);
        root.setBottom(createSearchPanel());

        stage.setScene(new Scene(root, 1180, 760));
        refreshIssues(null, null, null, null);
    }

    private TableView<Issue> createIssueTable() {
        TableView<Issue> table = new TableView<>();
        TableColumn<Issue, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        TableColumn<Issue, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        TableColumn<Issue, String> projectCol = new TableColumn<>("Project");
        projectCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProjectId()));
        TableColumn<Issue, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        TableColumn<Issue, String> assigneeCol = new TableColumn<>("Assignee");
        assigneeCol.setCellValueFactory(data -> new SimpleStringProperty(nullToBlank(data.getValue().getAssigneeId())));
        table.getColumns().addAll(idCol, titleCol, projectCol, statusCol, assigneeCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private HBox createSearchPanel() {
        TextField projectField = new TextField();
        projectField.setPromptText("Project ID");
        TextField reporterField = new TextField();
        reporterField.setPromptText("Reporter ID");
        TextField assigneeField = new TextField();
        assigneeField.setPromptText("Assignee ID");
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().add("All");
        for (IssueStatus status : IssueStatus.values()) {
            statusBox.getItems().add(status.name());
        }
        statusBox.getSelectionModel().selectFirst();
        Button searchButton = new Button("Search");
        Button resetButton = new Button("Reset");

        searchButton.setOnAction(e -> refreshIssues(
                blankToNull(projectField.getText()),
                blankToNull(reporterField.getText()),
                blankToNull(assigneeField.getText()),
                statusBox.getSelectionModel().getSelectedIndex() <= 0
                        ? null
                        : IssueStatus.valueOf(statusBox.getValue())));
        resetButton.setOnAction(e -> {
            projectField.clear();
            reporterField.clear();
            assigneeField.clear();
            statusBox.getSelectionModel().selectFirst();
            refreshIssues(null, null, null, null);
        });

        HBox box = new HBox(8, new Label("Filters:"), projectField, reporterField, assigneeField, statusBox, searchButton, resetButton);
        box.setPadding(new Insets(10, 0, 0, 0));
        return box;
    }

    private VBox createActionPanel() {
        TextField assigneeField = new TextField();
        assigneeField.setPromptText("Assignee ID");
        ComboBox<IssueStatus> statusBox = new ComboBox<>(FXCollections.observableArrayList(IssueStatus.values()));
        statusBox.setPromptText("New status");
        TextField messageField = new TextField();
        messageField.setPromptText("Comment/message");
        Button assignButton = new Button("Assign");
        Button statusButton = new Button("Change Status");
        Button commentButton = new Button("Add Comment");

        assignButton.setOnAction(e -> runAndRefresh(() -> controller.assignIssue(currentIssueId, assigneeField.getText())));
        statusButton.setOnAction(e -> {
            IssueStatus selected = statusBox.getValue();
            if (selected == null) {
                showMessage("Select a status.");
                return;
            }
            runAndRefresh(() -> controller.updateStatus(currentIssueId, selected, messageField.getText()));
        });
        commentButton.setOnAction(e -> runAndRefresh(() -> controller.addComment(currentIssueId, messageField.getText())));

        HBox row1 = new HBox(8, assigneeField, assignButton);
        HBox row2 = new HBox(8, statusBox, messageField, statusButton, commentButton);
        return new VBox(8, new Label("Actions"), row1, row2);
    }

    private GridPane createIssueForm() {
        TextField idField = new TextField();
        idField.setPromptText("Issue ID");
        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        TextField projectField = new TextField("project1");
        projectField.setPromptText("Project ID");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description");
        descriptionField.setPrefRowCount(3);
        Button createButton = new Button("Create Issue");
        createButton.setOnAction(e -> {
            try {
                controller.createIssue(idField.getText(), titleField.getText(), descriptionField.getText(), projectField.getText());
                idField.clear();
                titleField.clear();
                descriptionField.clear();
                refreshIssues(null, null, null, null);
            } catch (RuntimeException ex) {
                showMessage(ex.getMessage());
            }
        });

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(6);
        form.add(new Label("Create Issue"), 0, 0, 2, 1);
        form.add(idField, 0, 1);
        form.add(titleField, 1, 1);
        form.add(projectField, 0, 2);
        form.add(descriptionField, 0, 3, 2, 1);
        form.add(createButton, 1, 4);
        return form;
    }

    private void refreshIssues(String projectId, String reporterId, String assigneeId, IssueStatus status) {
        List<Issue> issues = controller.searchIssues(projectId, reporterId, assigneeId, status);
        issueTable.setItems(FXCollections.observableArrayList(issues));
        if (!issues.isEmpty()) {
            issueTable.getSelectionModel().selectFirst();
        } else {
            currentIssueId = null;
            detailArea.clear();
            commentsArea.clear();
            recommendationLabel.setText("");
        }
    }

    private void updateIssueDetail(String issueId) {
        currentIssueId = issueId;
        Optional<Issue> issueOpt = controller.getIssueById(issueId);
        if (issueOpt.isEmpty()) return;

        Issue issue = issueOpt.get();
        detailArea.setText(
                "Title: " + issue.getTitle() + "\n" +
                "ID: " + issue.getId() + "\n" +
                "Project: " + issue.getProjectId() + "\n" +
                "Status: " + issue.getStatus() + "\n" +
                "Reporter: " + issue.getReporterId() + "\n" +
                "Assignee: " + nullToBlank(issue.getAssigneeId()) + "\n" +
                "Fixer: " + nullToBlank(issue.getFixerId()) + "\n" +
                "Priority: " + issue.getPriority() + "\n" +
                "Reported: " + issue.getReportedDate() + "\n\n" +
                issue.getDescription());
        commentsArea.setText(issue.getComments().stream()
                .map(this::formatComment)
                .collect(Collectors.joining("\n\n")));

        if (controller.getCurrentUser().getRole() == Role.PL) {
            List<String> recommendations = controller.getRecommendations(issueId);
            recommendationLabel.setText(recommendations.isEmpty()
                    ? "Recommended assignees: none"
                    : "Recommended assignees: " + String.join(", ", recommendations));
        } else {
            recommendationLabel.setText("");
        }
    }

    private String formatComment(Comment comment) {
        return "[" + comment.getTimestamp() + "] " + comment.getAuthorId() + "\n" + comment.getContent();
    }

    private void runAndRefresh(Runnable action) {
        if (currentIssueId == null) {
            showMessage("Select an issue first.");
            return;
        }
        String selectedId = currentIssueId;
        try {
            action.run();
            refreshIssues(null, null, null, null);
            updateIssueDetail(selectedId);
        } catch (RuntimeException ex) {
            showMessage(ex.getMessage());
        }
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    @Override
    public void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
