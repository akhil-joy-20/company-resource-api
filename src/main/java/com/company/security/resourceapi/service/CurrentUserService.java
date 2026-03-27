package com.company.security.resourceapi.service;

import com.company.security.resourceapi.dto.CurrentUserDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CurrentUserService {

    public CurrentUserDto getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new IllegalStateException("No JWT authentication found");
        }

        Jwt jwt = jwtAuth.getToken();

        String userId = jwt.getClaimAsString("sub");
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        String department = jwt.getClaimAsString("department"); //attribute based auth

        Set<String> roles = jwtAuth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.replace("ROLE_", ""))
                .collect(Collectors.toSet());

        return new CurrentUserDto(userId, username, email, department, roles);
    }

}
