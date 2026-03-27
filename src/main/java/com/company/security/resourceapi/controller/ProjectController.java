package com.company.security.resourceapi.controller;

import com.company.security.resourceapi.entity.Project;
import com.company.security.resourceapi.service.ProjectService;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // Get One RBAC + ownership handled in service
    @PreAuthorize("hasAnyRole('APP_ADMIN','APP_MANAGER','APP_USER')")
    @GetMapping("/{id}")
    public Project getProject(@PathVariable Long id) {
        return projectService.getProject(id);
    }

    //Get all
    @PreAuthorize("hasAnyRole('APP_ADMIN','APP_MANAGER','APP_USER')")
    @GetMapping
    public List<Project> getAllProjects() {
        return projectService.getAllProjects();
    }

    // CREATE — Permission + Department restriction
    @PreAuthorize("hasRole('PROJECT_WRITE')") //permission based if permission from DB can load it as grantedauthority in secconfig and use hasAuthority (Here for simplicity we add permissions as  KC roles)
    @PostMapping
    public Project createProject(@RequestBody Project project) {
        return projectService.createProject(project);
    }

    // UPDATE — Permission + Ownership/Admin
    @PreAuthorize("hasRole('PROJECT_WRITE')")
    @PutMapping("/{id}")
    public Project updateProject(@PathVariable Long id,
                                 @RequestBody Project project) {
        return projectService.updateProject(id, project);
    }

    // DELETE — ADMIN only
    @PreAuthorize("hasRole('APP_ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return "Project deleted";
    }

    //Attribute based inside service
    @PreAuthorize("hasRole('PROJECT_READ')")
    @GetMapping("/department")
    public List<Project> getDepartmentProjects() {
        return projectService.getDepartmentProjects();
    }
    //we can also use Attribute based using pre-auth but don't it is better to keep pre-auth simple
    //@PreAuthorize("hasRole('PROJECT_READ') and @currentUserService.getCurrentUser().department == 'IT'")

    // Role hierarchy validation endpoint
    @PreAuthorize("hasRole('APP_USER')")
    @GetMapping("/role-hierarchy")
    public String rolehierarchy() {
        return "Role Hierarchy check";
    }

    ///

    // POST-FILTER
    @PreAuthorize("hasRole('PROJECT_READ')")
    @PostFilter("filterObject.department == @currentUserService.getCurrentUser().department")
    @GetMapping("/filtered")
    public List<Project> getAllProjectsFiltered() {
        return projectService.getAllProjectsFiltered();
    }

    // PRE-FILTER
    @PreAuthorize("hasRole('PROJECT_READ')")
    @PreFilter("filterObject > 0") //---> [1, 101, 3, 0, -5] -- removes 0 and -5 ,  check db for other ids return projects if found
    @PostMapping("/bulk-details")
    public List<Project> getProjectsByIds(@RequestBody List<Long> ids) {
        return projectService.getProjectsByIds(ids);
    }

    // POST-AUTHORIZE
    @PreAuthorize("hasRole('PROJECT_READ')")
    @PostAuthorize("returnObject.ownerUsername == authentication.token.claims['preferred_username'] or hasRole('APP_ADMIN')")
    //authentication.principal → is a Jwt object
    @GetMapping("/secure/{id}")
    public Project getProjectSecure(@PathVariable Long id) {
        return projectService.getProjectSecure(id);
    }

    // MANAGER-ONLY APPROVAL
    @PreAuthorize("hasRole('APP_MANAGER')")
    @PostMapping("/approve/{id}")
    public String approveProject(@PathVariable Long id) {
        return projectService.approveProject(id);
    }

    // NOTE: logout/refresh -> Postman → Keycloak direct

}
