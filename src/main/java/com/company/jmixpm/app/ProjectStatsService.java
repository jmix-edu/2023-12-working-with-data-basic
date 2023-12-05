package com.company.jmixpm.app;

import com.company.jmixpm.entity.Project;
import com.company.jmixpm.entity.ProjectStats;
import com.company.jmixpm.entity.Task;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ProjectStatsService {

    private final DataManager dataManager;

    public ProjectStatsService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public List<ProjectStats> fetchProjectsStatistics() {
        List<Project> projects = dataManager.load(Project.class)
                .all()
                .fetchPlan("project-with-tasks")
                .list();

        List<ProjectStats> projectStats = projects.stream().map(project -> {
            ProjectStats stat = dataManager.create(ProjectStats.class);
            stat.setId(project.getId());
            stat.setProjectName(project.getName());
            stat.setTasksCount(project.getTasks().size());
            stat.setPlannedEfforts(project.getTasks().stream().map(Task::getEstimatedEfforts).reduce(0, Integer::sum));
            stat.setActualEfforts(getActualEfforts(project.getId()));
            return stat;
        }).collect(Collectors.toList());
        return projectStats;
    }

    private Integer getActualEfforts(UUID projectId) {
        return dataManager.loadValue("select sum(e.timeSpent) from TimeEntry e" +
                " where e.task.project.id = :projectId", Integer.class)
                .parameter("projectId", projectId)
                .one();
    }
}