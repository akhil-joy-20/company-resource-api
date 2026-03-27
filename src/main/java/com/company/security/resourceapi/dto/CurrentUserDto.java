package com.company.security.resourceapi.dto;

import java.util.Set;

public class CurrentUserDto {

    private final String userId;
    private final String username;
    private final String email;
    private final String department;
    private final Set<String> roles;

    public CurrentUserDto(String userId, String username, String email,
                          String department, Set<String> roles) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.department = department;
        this.roles = roles;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Set<String> getRoles() { return roles; }
    public String getDepartment() { return department; }
    public boolean hasPermission(String permission) {
        return roles.contains(permission);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

}
