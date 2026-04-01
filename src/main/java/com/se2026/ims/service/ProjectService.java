package com.se2026.ims.service;

import com.se2026.ims.model.Project;
import com.se2026.ims.repository.Repository;

import java.util.List;
import java.util.Optional;

public class ProjectService {
    private final Repository<Project> projectRepository;

    public ProjectService(Repository<Project> projectRepository) {
        this.projectRepository = projectRepository;
    }

    public void addProject(String id, String name, String description) {
        Project project = new Project(id, name, description);
        projectRepository.save(project);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Optional<Project> getProjectById(String id) {
        return projectRepository.findById(id);
    }

    public void initializeDemoProject() {
        if (projectRepository.findAll().isEmpty()) {
            addProject("project1", "SE Term Project", "Software Engineering Term Project 2026-1");
        }
    }
}
