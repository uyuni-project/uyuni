package com.suse.manager.webui.controllers.contentmanagement.mappers;


import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentProject;

import com.suse.manager.webui.controllers.contentmanagement.response.EnvironmentResponse;
import com.suse.manager.webui.controllers.contentmanagement.response.ProjectHistoryEntryResponse;
import com.suse.manager.webui.controllers.contentmanagement.response.ProjectPropertiesResponse;
import com.suse.manager.webui.controllers.contentmanagement.response.ProjectResponse;
import com.suse.manager.webui.controllers.contentmanagement.response.ProjectResumeResponse;
import com.suse.manager.webui.controllers.contentmanagement.response.ProjectSourcesResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResponseMappers {


    public static ProjectPropertiesResponse mapProjectPropertiesFromDB(ContentProject projectDB) {
        ProjectPropertiesResponse properties = new ProjectPropertiesResponse();

        properties.setLabel(projectDB.getLabel());
        properties.setName(projectDB.getName());
        properties.setDescription(projectDB.getDescription());

        List<ProjectHistoryEntryResponse> historyEntries = projectDB.getHistoryEntries().stream()
                .map(entry -> {
                    ProjectHistoryEntryResponse historyEntryResponse = new ProjectHistoryEntryResponse();
                    historyEntryResponse.setMessage(entry.getMessage());
                    historyEntryResponse.setVersion(entry.getVersion());
                    return historyEntryResponse;
                })
                .collect(Collectors.toList());

        properties.setHistoryEntries(historyEntries);
        return properties;
    }

    public static List<EnvironmentResponse> mapEnvironmentsFromDB(List<ContentEnvironment> envsDB) {
        return envsDB
                .stream()
                .map(envDB -> {
                    EnvironmentResponse environmentResponse = new EnvironmentResponse();
                    environmentResponse.setLabel(envDB.getLabel());
                    environmentResponse.setName(envDB.getName());
                    environmentResponse.setDescription(envDB.getDescription());
                    environmentResponse.setProjectLabel(envDB.getContentProject().getLabel());
                    return environmentResponse;
                })
                .collect(Collectors.toList());
    }

    public static ProjectResponse mapProjectFromDB(ContentProject projectDB, List<ContentEnvironment> envsDB) {

        ProjectResponse project = new ProjectResponse();
        List<ProjectSourcesResponse> sources = new ArrayList<>();

        project.setProperties(mapProjectPropertiesFromDB(projectDB));
        project.setSources(sources);
        project.setEnvironments(mapEnvironmentsFromDB(envsDB));

        return project;
    }

    public static List<ProjectResumeResponse> mapProjectListingFromDB(Map<ContentProject, List<ContentEnvironment>> envsByProjDB) {
        List<ProjectResumeResponse> projectsResume = new ArrayList<ProjectResumeResponse>();
        envsByProjDB.forEach((project, environments) -> {
            ProjectResumeResponse contentProjectResumeResponse = new ProjectResumeResponse();
            contentProjectResumeResponse.setProperties(mapProjectPropertiesFromDB(project));
            contentProjectResumeResponse.setEnvironments(
                    environments.stream()
                            .map(env -> env.getName())
                            .collect(Collectors.toList())
            );
            projectsResume.add(contentProjectResumeResponse);
        });
        return projectsResume;
    }

}
