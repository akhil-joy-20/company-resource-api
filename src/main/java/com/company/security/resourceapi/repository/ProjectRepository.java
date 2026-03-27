package com.company.security.resourceapi.repository;

import com.company.security.resourceapi.entity.Project;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ProjectRepository {

    private final Map<Long, Project> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(100);

    public Project save(Project project) {
        if (project.getId() == null) {
            project.setId(idGenerator.incrementAndGet());
        }
        store.put(project.getId(), project);
        return project;
    }

    public Optional<Project> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Project> findAll() {
        return new ArrayList<>(store.values());
    }

    public void deleteById(Long id) {
        store.remove(id);
    }

    public List<Project> findAllById(List<Long> ids) {
        return ids.stream()
                .map(store::get)
                .filter(Objects::nonNull)
                .toList();
    }
}
