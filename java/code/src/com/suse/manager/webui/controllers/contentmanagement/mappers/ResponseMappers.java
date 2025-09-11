/*
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

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProjectHistoryEntry;
import com.redhat.rhn.domain.contentmgmt.EnvironmentTarget;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget;
import com.redhat.rhn.domain.contentmgmt.SoftwareProjectSource;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApi;
import com.redhat.rhn.domain.contentmgmt.validation.ContentProjectValidator;
import com.redhat.rhn.domain.contentmgmt.validation.ContentValidationMessage;

import com.suse.manager.webui.controllers.contentmanagement.response.EnvironmentResponse;
import com.suse.manager.webui.controllers.contentmanagement.response.FilterResponse;
import com.suse.manager.webui.controllers.contentmanagement.response.ProjectFilterResponse;
import com.suse.manager.webui.controllers.contentmanagement.response.ProjectHistoryEntryResponse;
import com.suse.manager.webui.controllers.contentmanagement.response.ProjectMessageResponse;
import com.suse.manager.webui.controllers.contentmanagement.response.ProjectPropertiesResponse;
import com.suse.manager.webui.controllers.contentmanagement.response.ProjectResponse;
import com.suse.manager.webui.controllers.contentmanagement.response.ProjectResumeResponse;
import com.suse.manager.webui.controllers.contentmanagement.response.ProjectSoftwareSourceResponse;
import com.suse.manager.webui.utils.ViewHelper;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Utility class to map db entities into view response beans
 */
public class ResponseMappers {

    // The modulemd API to use with the content project validator
    private static final ModulemdApi MODULEMD_API = new ModulemdApi();

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
        Optional<Date> lastBuildDate = projectDB.getHistoryEntries().stream()
                .map(ContentProjectHistoryEntry::getCreated)
                .max(Comparator.naturalOrder());
        if (lastBuildDate.isPresent()) {
            properties.setLastBuildDate(ViewHelper.formatDateTimeToISO(lastBuildDate.get()));
        }
        else {
            properties.setLastBuildDate(null);
        }

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
     * @param swSourcesWithUnsyncedPatches
     * @param sourceTagetChannelIds
     * @return the List&lt;EnvironmentResponse&gt; view beans
     */
    public static List<ProjectSoftwareSourceResponse> mapSourcesFromDB(List<SoftwareProjectSource> sourcesDB,
            Set<SoftwareProjectSource> swSourcesWithUnsyncedPatches, Map<Long, Long> sourceTagetChannelIds) {
        return sourcesDB
                .stream()
                .map(sourceDb -> {
                    ProjectSoftwareSourceResponse projectSourceResponse = new ProjectSoftwareSourceResponse();
                    projectSourceResponse.setChannelId(sourceDb.getChannel().getId());
                    projectSourceResponse.setName(sourceDb.getChannel().getName());
                    projectSourceResponse.setLabel(sourceDb.getChannel().getLabel());
                    projectSourceResponse.setType(ProjectSource.Type.SW_CHANNEL.getLabel());
                    projectSourceResponse.setState(sourceDb.getState().name());
                    projectSourceResponse.setHasUnsyncedPatches(swSourcesWithUnsyncedPatches.contains(sourceDb));
                    projectSourceResponse.setTargetChannelId(sourceTagetChannelIds
                            .getOrDefault(sourceDb.getChannel().getId(), null));
                    return projectSourceResponse;
                })
                .collect(Collectors.toList());
    }

    /**
     * Map a list of db envs entities to a list of view response beans
     *
     * @param envsDB the list of db envs
     * @return the List&lt;EnvironmentResponse&gt; view beans
     */
    public static List<EnvironmentResponse> mapEnvironmentsFromDB(List<ContentEnvironment> envsDB) {
        return envsDB
                .stream()
                .map(envDB -> {
                    EnvironmentResponse environmentResponse = new EnvironmentResponse();
                    environmentResponse.setId(envDB.getId());
                    environmentResponse.setLabel(envDB.getLabel());
                    environmentResponse.setName(envDB.getName());
                    environmentResponse.setVersion(envDB.getVersion());
                    environmentResponse.setDescription(envDB.getDescription());
                    environmentResponse.setProjectLabel(envDB.getContentProject().getLabel());
                    environmentResponse.setStatus(
                            envDB.computeStatus()
                                    .map(EnvironmentTarget.Status::getLabel)
                                    .orElse(null)
                    );
                    environmentResponse.setBuiltTime(envDB.computeBuiltTime().orElse(null));
                    environmentResponse.setHasProfiles(envDB.getTargets().stream()
                            .map(EnvironmentTarget::asSoftwareTarget)
                            .flatMap(Optional::stream)
                            .map(SoftwareEnvironmentTarget::getChannel)
                            .anyMatch(Channel::containsDistributions)
                    );
                    return environmentResponse;
                })
                .collect(Collectors.toList());
    }

    /**
     * Map a list of db envs entities and project db into a full project view response bean
     *
     * @param projectDB the project db entity
     * @param envsDB the list of db envs
     * @param swSourcesWithUnsyncedPatches set of {@link SoftwareProjectSource}s with patches out of sync
     * @param sourceTagetChannelIds mapping source to target channel ids
     * @return full project view response bean
     */
    public static ProjectResponse mapProjectFromDB(ContentProject projectDB, List<ContentEnvironment> envsDB,
            Set<SoftwareProjectSource> swSourcesWithUnsyncedPatches, Map<Long, Long> sourceTagetChannelIds) {

        ProjectResponse project = new ProjectResponse();

        project.setProperties(mapProjectPropertiesFromDB(projectDB));
        project.setSoftwareSources(mapSourcesFromDB(
                projectDB.getSources()
                        .stream()
                        .flatMap(source -> stream(source.asSoftwareSource()))
                        .collect(Collectors.toList()),
                swSourcesWithUnsyncedPatches, sourceTagetChannelIds
        ));
        project.setFilters(mapProjectFilterFromDB(projectDB.getProjectFilters()));
        project.setEnvironments(mapEnvironmentsFromDB(envsDB));

        ContentProjectValidator projectValidator = new ContentProjectValidator(projectDB, MODULEMD_API);
        project.setProjectMessages(mapProjectMessagesFromDB(projectValidator.validate()));

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
                                    .map(ContentEnvironment::getName)
                                    .collect(Collectors.toList())
                    );
                    return contentProjectResumeResponse;
                })
                .collect(Collectors.toList());
    }

    /**
     * Map db info into a list with filters views beans with projects
     *
     * @param filtersWithProjects list of filters db with projects
     * @return list with a filter resume response bean
     */
    public static List<FilterResponse> mapFilterListingFromDB(
            Map<ContentFilter, List<ContentProject>> filtersWithProjects) {
        return filtersWithProjects.entrySet().stream()
                .map(e -> {
                    ContentFilter filter = e.getKey();
                    List<ContentProject> projects = e.getValue();

                    FilterResponse contentFilterResponse = new FilterResponse();
                    contentFilterResponse.setId(filter.getId());
                    contentFilterResponse.setName(filter.getName());
                    contentFilterResponse.setEntityType(filter.getEntityType().getLabel());
                    contentFilterResponse.setMatcher(filter.getCriteria().getMatcher().getLabel());
                    contentFilterResponse.setCriteriaKey(filter.getCriteria().getField());
                    contentFilterResponse.setCriteriaValue(filter.getCriteria().getValue());
                    contentFilterResponse.setRule(filter.getRule().getLabel());
                    contentFilterResponse.setProjects(
                            projects.stream()
                                    .map(p -> new ImmutablePair<>(p.getLabel(), p.getName()))
                                    .collect(Collectors.toList())
                    );
                    return contentFilterResponse;
                })
                .sorted(Comparator.comparing(FilterResponse::getName))
                .collect(Collectors.toList());
    }


    /**
     * Map db info into a list with resume of filters views beans
     *
     * @param projectFilters list of projectFilters
     * @return list with a filter resume response bean
     */
    public static List<ProjectFilterResponse> mapProjectFilterFromDB(List<ContentProjectFilter> projectFilters) {
        return projectFilters
                .stream()
                .map(projectFilter -> {
                    ContentFilter filter = projectFilter.getFilter();
                    ProjectFilterResponse contentProjectFilterResponse = new ProjectFilterResponse();
                    contentProjectFilterResponse.setId(filter.getId());
                    contentProjectFilterResponse.setName(filter.getName());
                    contentProjectFilterResponse.setEntityType(filter.getEntityType().getLabel());
                    contentProjectFilterResponse.setMatcher(filter.getCriteria().getMatcher().getLabel());
                    contentProjectFilterResponse.setCriteriaKey(filter.getCriteria().getField());
                    contentProjectFilterResponse.setCriteriaValue(filter.getCriteria().getValue());
                    contentProjectFilterResponse.setRule(filter.getRule().getLabel());
                    contentProjectFilterResponse.setState(projectFilter.getState().toString());
                    return contentProjectFilterResponse;
                })
                .collect(Collectors.toList());
    }

    /**
     * Map validation messages into a list with resume of message views beans
     *
     * @param messages list of validation messages
     * @return list with a filter resume response bean
     */
    public static List<ProjectMessageResponse> mapProjectMessagesFromDB(List<ContentValidationMessage> messages) {
        return messages.stream()
                .map(m -> new ProjectMessageResponse(m.getMessage(), m.getType(), m.getEntity()))
                .collect(Collectors.toList());
    }
}
