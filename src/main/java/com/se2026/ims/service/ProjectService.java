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
        requireText(id, "Project ID cannot be empty");
        requireText(name, "Project name cannot be empty");
        Project project = new Project(id.trim(), name.trim(), description == null ? "" : description.trim());
        projectRepository.save(project);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Optional<Project> getProjectById(String id) {
        return projectRepository.findById(id);
    }

    public void updateProject(String id, String name, String description) {
        projectRepository.findById(id).ifPresent(project -> {
            requireText(name, "Project name cannot be empty");
            project.setName(name.trim());
            project.setDescription(description == null ? "" : description.trim());
            projectRepository.update(project);
        });
    }

    public void deleteProject(String id) {
        projectRepository.delete(id);
    }

    public void initializeDemoProject() {
        if (projectRepository.findAll().isEmpty()) {
            addProject("project1", "SE Term Project", "Software Engineering Term Project 2026-1");
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
