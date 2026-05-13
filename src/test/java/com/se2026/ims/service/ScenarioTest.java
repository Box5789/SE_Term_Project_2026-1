package com.se2026.ims.service;

import com.se2026.ims.controller.IMSController;
import com.se2026.ims.model.*;
import com.se2026.ims.repository.JsonRepository;
import com.se2026.ims.repository.Repository;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ScenarioTest {
    private UserService userService;
    private ProjectService projectService;
    private IssueService issueService;

    private final String DATA_DIR = "src/test/resources/data/";

    @BeforeAll
    public void setup() {
        new File(DATA_DIR).mkdirs();
        
        // 이전 테스트 데이터 삭제
        new File(DATA_DIR + "users.json").delete();
        new File(DATA_DIR + "projects.json").delete();
        new File(DATA_DIR + "issues.json").delete();

        Repository<User> userRepo = new JsonRepository<>(DATA_DIR + "users.json", User.class);
        Repository<Project> projectRepo = new JsonRepository<>(DATA_DIR + "projects.json", Project.class);
        Repository<Issue> issueRepo = new JsonRepository<>(DATA_DIR + "issues.json", Issue.class);

        userService = new UserService(userRepo);
        projectService = new ProjectService(projectRepo);
        issueService = new IssueService(issueRepo);
    }

    @Test
    @Order(1)
    public void testScenario() {
        // 1. Admin이 계정 및 프로젝트 추가
        userService.initializeDemoUsers();
        projectService.initializeDemoProject();
        
        Optional<User> tester1 = userService.getUserById("tester1");
        assertTrue(tester1.isPresent());
        assertEquals(Role.TESTER, tester1.get().getRole());

        Optional<Project> project1 = projectService.getProjectById("project1");
        assertTrue(project1.isPresent());

        // 2. tester1이 이슈 생성 및 코멘트 추가
        String issueId = "issue-1";
        issueService.createIssue(issueId, "Bug in login", "Login page is slow", "project1", "tester1");
        issueService.addComment(issueId, "tester1", "First comment by tester1");
        
        Issue issue = issueService.getIssueById(issueId).orElseThrow();
        assertEquals(IssueStatus.NEW, issue.getStatus());
        assertEquals("tester1", issue.getReporterId());
        assertEquals(1, issue.getComments().size());

        // 3. PL1이 이슈 브라우즈 및 dev1에게 할당
        List<Issue> newIssues = issueService.searchIssues("project1", null, null, IssueStatus.NEW);
        assertFalse(newIssues.isEmpty());
        
        issueService.assignIssue(issue.getId(), "dev1", "pl1");
        
        Issue assignedIssue = issueService.searchIssues("project1", null, "dev1", IssueStatus.ASSIGNED).get(0);
        assertEquals("dev1", assignedIssue.getAssigneeId());
        assertEquals(IssueStatus.ASSIGNED, assignedIssue.getStatus());

        // 4. dev1이 이슈 확인 후 FIXED로 변경
        issueService.fixIssue(issue.getId(), "dev1", "Fixed by optimizing SQL query");
        
        Issue fixedIssue = issueService.searchIssues("project1", null, null, IssueStatus.FIXED).get(0);
        assertEquals("dev1", fixedIssue.getFixerId());
        assertEquals(IssueStatus.FIXED, fixedIssue.getStatus());

        // 5. tester1이 RESOLVED로 변경
        List<Issue> fixedByMe = issueService.searchIssues("project1", "tester1", null, IssueStatus.FIXED);
        assertFalse(fixedByMe.isEmpty());
        
        issueService.updateStatus(issue.getId(), IssueStatus.RESOLVED, "tester1", "Verified the fix");
        
        Issue resolvedIssue = issueService.searchIssues("project1", null, null, IssueStatus.RESOLVED).get(0);
        assertEquals(IssueStatus.RESOLVED, resolvedIssue.getStatus());

        // 6. PL1이 CLOSED로 변경
        issueService.updateStatus(issue.getId(), IssueStatus.CLOSED, "pl1", "Closing issue");
        
        Issue closedIssue = issueService.searchIssues("project1", null, null, IssueStatus.CLOSED).get(0);
        assertEquals(IssueStatus.CLOSED, closedIssue.getStatus());
        
        // 7. 추천 기능 테스트 (기존 이슈 이력 활용)
        // 새로운 이슈 생성
        Issue newIssue = issueService.createIssue("issue-2", "Login page optimization", "Need to improve performance", "project1", "tester2");
        List<String> recommendations = issueService.recommendAssignee(newIssue.getId());
        
        // "Login" 키워드가 겹치므로 dev1이 추천되어야 함
        assertFalse(recommendations.isEmpty());
        assertEquals("dev1", recommendations.get(0));

        // 8. 수정 및 삭제 테스트 추가
        // 8-1. 프로젝트 수정 및 삭제
        projectService.updateProject("project1", "Modified Project Name", "Updated Description");
        Project updatedProject = projectService.getProjectById("project1").orElseThrow();
        assertEquals("Modified Project Name", updatedProject.getName());
        assertEquals("Updated Description", updatedProject.getDescription());

        projectService.addProject("project-to-delete", "Delete Me", "To be deleted");
        assertTrue(projectService.getProjectById("project-to-delete").isPresent());
        projectService.deleteProject("project-to-delete");
        assertFalse(projectService.getProjectById("project-to-delete").isPresent());

        // 8-2. 사용자 수정 및 삭제
        userService.updateUser("dev1", "Updated Dev 1", "newpassword", Role.PL);
        User updatedUser = userService.getUserById("dev1").orElseThrow();
        assertEquals("Updated Dev 1", updatedUser.getName());
        assertEquals("newpassword", updatedUser.getPassword());
        assertEquals(Role.PL, updatedUser.getRole());

        userService.addUser("user-to-delete", "Delete Me", "pw", Role.DEV);
        assertTrue(userService.getUserById("user-to-delete").isPresent());
        userService.deleteUser("user-to-delete");
        assertFalse(userService.getUserById("user-to-delete").isPresent());

        // 8-3. 이슈 수정 및 삭제
        issueService.updateIssue("issue-1", "New Title", "New Description", "project1", Priority.BLOCKER);
        Issue updatedIssue = issueService.getIssueById("issue-1").orElseThrow();
        assertEquals("New Title", updatedIssue.getTitle());
        assertEquals("New Description", updatedIssue.getDescription());
        assertEquals(Priority.BLOCKER, updatedIssue.getPriority());

        issueService.createIssue("issue-to-delete", "Delete Me", "Desc", "project1", "admin");
        assertTrue(issueService.getIssueById("issue-to-delete").isPresent());
        issueService.deleteIssue("issue-to-delete");
        assertFalse(issueService.getIssueById("issue-to-delete").isPresent());
    }

    @Test
    public void testControllerRoleRulesAndPdfScenario() throws IOException {
        IMSController controller = createFreshController();

        assertTrue(controller.login("tester1", "test123"));
        controller.createIssue("auth-1", "Login error", "Login fails on valid password", "project1");
        controller.addComment("auth-1", "Reproduced on Chrome");

        assertThrows(SecurityException.class, () -> controller.assignIssue("auth-1", "dev1"));

        controller.logout();
        assertTrue(controller.login("pl1", "pl123"));
        controller.assignIssue("auth-1", "dev1");
        assertEquals(IssueStatus.ASSIGNED, controller.getIssueById("auth-1").orElseThrow().getStatus());

        controller.logout();
        assertTrue(controller.login("dev2", "dev123"));
        assertThrows(SecurityException.class, () -> controller.updateStatus("auth-1", IssueStatus.FIXED, "Wrong developer"));

        controller.logout();
        assertTrue(controller.login("dev1", "dev123"));
        controller.updateStatus("auth-1", IssueStatus.FIXED, "Fixed login validation");
        assertEquals("dev1", controller.getIssueById("auth-1").orElseThrow().getFixerId());

        controller.logout();
        assertTrue(controller.login("tester2", "test123"));
        assertThrows(SecurityException.class, () -> controller.updateStatus("auth-1", IssueStatus.RESOLVED, "Not my report"));

        controller.logout();
        assertTrue(controller.login("tester1", "test123"));
        controller.updateStatus("auth-1", IssueStatus.RESOLVED, "Verified");

        controller.logout();
        assertTrue(controller.login("pl1", "pl123"));
        controller.updateStatus("auth-1", IssueStatus.CLOSED, "Close");
        assertEquals(IssueStatus.CLOSED, controller.getIssueById("auth-1").orElseThrow().getStatus());

        controller.logout();
        assertTrue(controller.login("tester2", "test123"));
        controller.createIssue("auth-2", "Login performance", "Login page is slow", "project1");

        controller.logout();
        assertTrue(controller.login("pl2", "pl123"));
        List<String> recommendations = controller.getRecommendations("auth-2");
        assertFalse(recommendations.isEmpty());
        assertEquals("dev1", recommendations.get(0));
    }

    @Test
    public void testValidationStatisticsAndPersistence() throws IOException {
        IMSController controller = createFreshController();

        assertTrue(controller.login("tester1", "test123"));
        assertThrows(IllegalArgumentException.class,
                () -> controller.createIssue("bad-1", "", "Description", "project1"));
        assertThrows(IllegalArgumentException.class,
                () -> controller.createIssue("bad-2", "Title", "", "project1"));

        controller.createIssue("stats-1", "Stats title", "Stats description", "project1");
        assertThrows(IllegalStateException.class,
                () -> controller.updateStatus("stats-1", IssueStatus.RESOLVED, "Invalid transition"));

        Map<String, Long> daily = controller.getStatistics("project1");
        Map<String, Long> monthly = controller.getMonthlyStatistics("project1");
        assertEquals(1L, daily.values().stream().mapToLong(Long::longValue).sum());
        assertEquals(1L, monthly.values().stream().mapToLong(Long::longValue).sum());

        Path dir = Files.createTempDirectory("ims-json-readback");
        Repository<Issue> writer = new JsonRepository<>(dir.resolve("issues.json").toString(), Issue.class);
        writer.save(new Issue("persist-1", "Persist title", "Persist description", "project1", "tester1"));

        Repository<Issue> reader = new JsonRepository<>(dir.resolve("issues.json").toString(), Issue.class);
        assertTrue(reader.findById("persist-1").isPresent());
        assertEquals("Persist title", reader.findById("persist-1").orElseThrow().getTitle());
    }

    @Test
    public void testAdminOnlyManagementAndPasswordPreserve() throws IOException {
        IMSController controller = createFreshController();

        assertTrue(controller.login("tester1", "test123"));
        assertThrows(SecurityException.class,
                () -> controller.addUser("blocked", "Blocked", "pw", Role.DEV));
        assertThrows(SecurityException.class,
                () -> controller.addProject("blocked-project", "Blocked", "No"));

        controller.logout();
        assertTrue(controller.login("admin", "admin123"));
        controller.addUser("newdev", "New Dev", "pw123", Role.DEV);
        controller.updateUser("newdev", "Renamed Dev", "", Role.DEV);

        controller.logout();
        assertTrue(controller.login("newdev", "pw123"));
        assertEquals("Renamed Dev", controller.getCurrentUser().getName());
    }

    private IMSController createFreshController() throws IOException {
        Path dir = Files.createTempDirectory("ims-test-data");
        Repository<User> userRepo = new JsonRepository<>(dir.resolve("users.json").toString(), User.class);
        Repository<Project> projectRepo = new JsonRepository<>(dir.resolve("projects.json").toString(), Project.class);
        Repository<Issue> issueRepo = new JsonRepository<>(dir.resolve("issues.json").toString(), Issue.class);
        UserService users = new UserService(userRepo);
        ProjectService projects = new ProjectService(projectRepo);
        IssueService issues = new IssueService(issueRepo);
        users.initializeDemoUsers();
        projects.initializeDemoProject();
        return new IMSController(users, projects, issues);
    }
}
