package com.company.security.resourceapi.entity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Project {
    private Long id;
    private String name;
    private String ownerUsername;
    private String department;


}
