package com.se2026.ims.service;

import com.se2026.ims.model.*;
import com.se2026.ims.repository.JsonRepository;
import com.se2026.ims.repository.Repository;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;
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
    }
}
