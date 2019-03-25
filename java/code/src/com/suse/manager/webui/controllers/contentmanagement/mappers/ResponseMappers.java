/**
 * Copyright (c) 2019 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.controllers.contentmanagement.mappers;


import static com.suse.utils.Opt.stream;

import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.contentmgmt.SoftwareProjectSource;

import com.suse.manager.webui.controllers.contentmanagement.response.EnvironmentResponse;
import com.suse.manager.webui.controllers.contentmanagement.response.ProjectHistoryEntryResponse;
import com.suse.manager.webui.controllers.contentmanagement.response.ProjectPropertiesResponse;
import com.suse.manager.webui.controllers.contentmanagement.response.ProjectResponse;
import com.suse.manager.webui.controllers.contentmanagement.response.ProjectResumeResponse;
import com.suse.manager.webui.controllers.contentmanagement.response.ProjectSoftwareSourceResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class to map db entities into view response beans
 */
public class ResponseMappers {

    private ResponseMappers() { }

    /**
     * Map a project db entity to a project properties response view bean
     *
     * @param projectDB the project db entity
     * @return the ProjectPropertiesResponse view bean
     */
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

    /**
     * Map a list of db sources entities to a list of view response beans
     *
     * @param sourcesDB the list of db envs
     * @return the List<EnvironmentResponse> view beans
     */
    public static List<ProjectSoftwareSourceResponse> mapSourcesFromDB(List<SoftwareProjectSource> sourcesDB) {
        return sourcesDB
                .stream()
                .map(sourceDb -> {
                    ProjectSoftwareSourceResponse projectSourceResponse = new ProjectSoftwareSourceResponse();
                    projectSourceResponse.setId(sourceDb.getChannel().getId().toString());
                    projectSourceResponse.setName(sourceDb.getChannel().getName());
                    projectSourceResponse.setLabel(sourceDb.getChannel().getLabel());
                    projectSourceResponse.setType(ProjectSource.Type.SW_CHANNEL.getLabel());
                    projectSourceResponse.setState(sourceDb.getState().name());
                    return projectSourceResponse;
                })
                .collect(Collectors.toList());
    }

    /**
     * Map a list of db envs entities to a list of view response beans
     *
     * @param envsDB the list of db envs
     * @return the List<EnvironmentResponse> view beans
     */
    public static List<EnvironmentResponse> mapEnvironmentsFromDB(List<ContentEnvironment> envsDB) {
        return envsDB
                .stream()
                .map(envDB -> {
                    EnvironmentResponse environmentResponse = new EnvironmentResponse();
                    environmentResponse.setLabel(envDB.getLabel());
                    environmentResponse.setName(envDB.getName());
                    environmentResponse.setVersion(envDB.getVersion());
                    environmentResponse.setDescription(envDB.getDescription());
                    environmentResponse.setProjectLabel(envDB.getContentProject().getLabel());
                    return environmentResponse;
                })
                .collect(Collectors.toList());
    }

    /**
     * Map a list of db envs entities and project db into a full project view response bean
     *
     * @param projectDB the project db entity
     * @param envsDB the list of db envs
     * @return full project view response bean
     */
    public static ProjectResponse mapProjectFromDB(ContentProject projectDB, List<ContentEnvironment> envsDB) {

        ProjectResponse project = new ProjectResponse();

        project.setProperties(mapProjectPropertiesFromDB(projectDB));
        project.setSoftwareSources(mapSourcesFromDB(
                projectDB.getSources()
                        .stream()
                        .flatMap(source -> stream(source.asSoftwareSource()))
                        .collect(Collectors.toList())
        ));
        project.setEnvironments(mapEnvironmentsFromDB(envsDB));

        return project;
    }

    /**
     * Map db info into a list with resume of projects views beans
     *
     * @param envsByProjDB list of projects db with environments
     * @return list with a project resume response bean
     */
    public static List<ProjectResumeResponse> mapProjectListingFromDB(
            Map<ContentProject, List<ContentEnvironment>> envsByProjDB
    ) {
        return envsByProjDB.entrySet().stream()
                .map(e -> {
                    ContentProject project = e.getKey();
                    List<ContentEnvironment> environments = e.getValue();

                    ProjectResumeResponse contentProjectResumeResponse = new ProjectResumeResponse();
                    contentProjectResumeResponse.setProperties(mapProjectPropertiesFromDB(project));
                    contentProjectResumeResponse.setEnvironments(
                            environments.stream()
                                    .map(env -> env.getName())
                                    .collect(Collectors.toList())
                    );
                    return contentProjectResumeResponse;
                })
                .collect(Collectors.toList());
    }

}
