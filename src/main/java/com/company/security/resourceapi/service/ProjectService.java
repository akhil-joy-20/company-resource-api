package com.company.security.resourceapi.service;

import com.company.security.resourceapi.dto.CurrentUserDto;
import com.company.security.resourceapi.entity.Project;
import com.company.security.resourceapi.repository.ProjectRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final CurrentUserService currentUserService;
    private final ProjectRepository projectRepository;


    public ProjectService(CurrentUserService currentUserService,ProjectRepository projectRepository) {
        this.currentUserService = currentUserService;
        this.projectRepository = projectRepository;

        //demo data
        seedDemoProjects();
    }

    private void seedDemoProjects() {

        projectRepository.save(new Project(null, "HR System Revamp", "user", "HR"));
        projectRepository.save(new Project(null, "Employee Onboarding Portal", "sarah", "HR"));
        projectRepository.save(new Project(null, "Payroll Compliance Tool", "user", "HR"));

        projectRepository.save(new Project(null, "Finance Portal", "manager", "IT"));
        projectRepository.save(new Project(null, "Budget Forecast Engine", "finance_admin", "Finance"));
        projectRepository.save(new Project(null, "Audit Automation Tool", "john", "Finance"));

        projectRepository.save(new Project(null, "Infra Upgrade", "admin", "IT"));
        projectRepository.save(new Project(null, "Internal Migration", "developer1", "IT"));
        projectRepository.save(new Project(null, "API Gateway Modernization", "john", "IT"));
        projectRepository.save(new Project(null, "SRE Monitoring Dashboard", "sre_lead", "IT"));

        projectRepository.save(new Project(null, "Company Strategy 2026", "ceo_admin", "Management"));
        projectRepository.save(new Project(null, "Vendor Assessment", "manager", "Management"));

        projectRepository.save(new Project(null, "Brand Refresh Initiative", "marketing_lead", "Marketing"));
        projectRepository.save(new Project(null, "Social Media Analytics", "user", "Marketing"));

        projectRepository.save(new Project(null, "Security Compliance Audit", "security_admin", "Security"));
        projectRepository.save(new Project(null, "Zero Trust Rollout", "admin", "IT"));
        projectRepository.save(new Project(null, "GDPR Readiness Program", "john", "Legal"));

    }


    public Project getProject(Long id) {

        CurrentUserDto user = currentUserService.getCurrentUser();

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (user.hasRole("APP_ADMIN")) return project;

        if (user.hasRole("APP_MANAGER") &&
                project.getDepartment().equals(user.getDepartment()))
            return project;

        if (project.getOwnerUsername().equals(user.getUsername()))
            return project;

        throw new AccessDeniedException("You cannot access this project");
    }

    public List<Project> getAllProjects() {

        CurrentUserDto user = currentUserService.getCurrentUser();
        List<Project> all = projectRepository.findAll();

        if (user.hasRole("APP_ADMIN")) return all;

        if (user.hasRole("APP_MANAGER"))
            return all.stream()
                    .filter(p -> p.getDepartment().equals(user.getDepartment()))
                    .toList();

        return all.stream()
                .filter(p -> p.getOwnerUsername().equals(user.getUsername()))
                .toList();
    }

    public Project createProject(Project project) {

        CurrentUserDto user = currentUserService.getCurrentUser();

        if (!user.hasPermission("PROJECT_WRITE"))
            throw new AccessDeniedException("Missing PROJECT_WRITE");

        if (!"IT".equals(user.getDepartment()))
            throw new AccessDeniedException("Only IT can create projects");

        project.setOwnerUsername(user.getUsername());
        project.setDepartment(user.getDepartment());

        return projectRepository.save(project);
    }

    public Project updateProject(Long id, Project updated) {

        CurrentUserDto user = currentUserService.getCurrentUser();

        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!user.hasPermission("PROJECT_WRITE"))
            throw new AccessDeniedException("Missing PROJECT_WRITE");

        if (!existing.getOwnerUsername().equals(user.getUsername())
                && !user.hasRole("APP_ADMIN"))
            throw new AccessDeniedException("Not allowed to update");

        existing.setName(updated.getName());
        existing.setDepartment(updated.getDepartment());

        return projectRepository.save(existing);
    }

    public void deleteProject(Long id) {

        CurrentUserDto user = currentUserService.getCurrentUser();

        if (!user.hasRole("APP_ADMIN"))
            throw new AccessDeniedException("Only admin can delete");

        projectRepository.deleteById(id);
    }

    public List<Project> getDepartmentProjects() {

        CurrentUserDto user = currentUserService.getCurrentUser();

        return projectRepository.findAll().stream()
                .filter(p -> p.getDepartment().equals(user.getDepartment()))
                .toList();
    }

    ///

    public List<Project> getAllProjectsFiltered() {
        return projectRepository.findAll();
    }

    public List<Project> getProjectsByIds(List<Long> ids) {
        return projectRepository.findAllById(ids);
    }

    public Project getProjectSecure(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    public String approveProject(Long id) {

        CurrentUserDto user = currentUserService.getCurrentUser();

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        return "Project " + project.getName()
                + " approved by manager: " + user.getUsername();
    }

}
