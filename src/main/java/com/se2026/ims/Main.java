package com.se2026.ims;

import com.se2026.ims.controller.IMSController;
import com.se2026.ims.model.*;
import com.se2026.ims.repository.JsonRepository;
import com.se2026.ims.repository.Repository;
import com.se2026.ims.service.IssueService;
import com.se2026.ims.service.ProjectService;
import com.se2026.ims.service.UserService;
import com.se2026.ims.view.IMSView;
import com.se2026.ims.view.swing.SwingView;

public class Main {
    private static final String DATA_DIR = "src/main/resources/data/";

    public static void main(String[] args) {
        // 1. 모델 초기화 (Repository -> Service)
        Repository<User> userRepo = new JsonRepository<>(DATA_DIR + "users.json", User.class);
        Repository<Project> projectRepo = new JsonRepository<>(DATA_DIR + "projects.json", Project.class);
        Repository<Issue> issueRepo = new JsonRepository<>(DATA_DIR + "issues.json", Issue.class);

        UserService userService = new UserService(userRepo);
        ProjectService projectService = new ProjectService(projectRepo);
        IssueService issueService = new IssueService(issueRepo);

        // 초기 데모 데이터 생성
        userService.initializeDemoUsers();
        projectService.initializeDemoProject();

        // 2. 컨트롤러 초기화
        IMSController controller = new IMSController(userService, projectService, issueService);

        // 3. 뷰 초기화 (다중 UI 지원)
        IMSView view;
        String viewType = args.length > 0 ? args[0].toLowerCase() : "swing";
        
        if (viewType.equals("swing")) {
            view = new SwingView();
        } else {
            // 다른 뷰가 구현되면 여기에 추가 (현재는 다시 Swing으로 대체)
            System.out.println("Unsupported view type: " + viewType + ". Using SwingView instead.");
            view = new SwingView();
        }

        view.setController(controller);
        
        // 4. 실행
        System.out.println("------------------------------------------");
        System.out.println("Starting Issue Management System (IMS)...");
        System.out.println("Current Directory: " + System.getProperty("user.dir"));
        System.out.println("View Type: " + viewType);
        System.out.println("------------------------------------------");
        view.start();
    }
}
