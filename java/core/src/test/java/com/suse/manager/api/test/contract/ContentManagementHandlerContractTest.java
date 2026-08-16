/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.api.test.contract;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.contentmgmt.PackageFilter;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.contentmgmt.SoftwareProjectSource;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ContentManagementHandlerContractTest extends BaseOpenApiTest {

    private static final String PROJECT_LABEL = "test-project";
    private static final String ENV_LABEL = "test-env";
    private static final String SOURCE_TYPE = "software";
    private static final String SOURCE_LABEL = "test-channel";
    private static final Integer FILTER_ID = 10;

    @Override
    protected String getApiNamespace() {
        return "contentmanagement";
    }

    @Override
    protected Class<ContentManagementHandler> getHandlerClass() {
        return ContentManagementHandler.class;
    }

    private ContentManagementHandler handler() {
        return (ContentManagementHandler) handlerMock;
    }

    private Org org() {
        Org org = new Org();
        org.setId(1L);
        return org;
    }

    private ContentProject project() {
        ContentProject project = new ContentProject(PROJECT_LABEL, "Test Project", "a test project", org());
        project.setId(1L);
        return project;
    }

    private ContentEnvironment environment() {
        ContentEnvironment environment =
                new ContentEnvironment(ENV_LABEL, "Test Environment", "a test environment", project());
        environment.setId(2L);
        environment.setVersion(1L);
        return environment;
    }

    private ProjectSource source() {
        Channel channel = new Channel();
        channel.setLabel(SOURCE_LABEL);

        SoftwareProjectSource projectSource = new SoftwareProjectSource(project(), channel);
        projectSource.setState(ProjectSource.State.BUILT);
        return projectSource;
    }

    private ContentFilter filter() {
        PackageFilter contentFilter = new PackageFilter();
        contentFilter.setId((long) FILTER_ID);
        contentFilter.setName("test-filter");
        contentFilter.setOrg(org());
        contentFilter.setRule(ContentFilter.Rule.DENY);
        contentFilter.setCriteria(new FilterCriteria(FilterCriteria.Matcher.CONTAINS, "name", "kernel"));
        return contentFilter;
    }

    /**
     * Mirrors the output of {@code ContentProjectFilterSerializer}, whose nested filter cannot be
     * built here without a persisted project filter association.
     *
     * @return the serialized form of an assigned filter
     */
    private Map<String, Object> assignedFilter() {
        Map<String, Object> criteria = new LinkedHashMap<>();
        criteria.put("matcher", "contains");
        criteria.put("field", "name");
        criteria.put("value", "kernel");

        Map<String, Object> contentFilter = new LinkedHashMap<>();
        contentFilter.put("id", FILTER_ID);
        contentFilter.put("name", "test-filter");
        contentFilter.put("orgId", 1);
        contentFilter.put("entityType", "package");
        contentFilter.put("rule", "deny");
        contentFilter.put("criteria", criteria);

        Map<String, Object> assigned = new LinkedHashMap<>();
        assigned.put("state", "ATTACHED");
        assigned.put("filter", contentFilter);
        return assigned;
    }

    private Map<String, Object> environmentDifference() {
        Map<String, Object> diff = new LinkedHashMap<>();
        diff.put("id", 5);
        diff.put("type", "package");
        diff.put("action", "ADD");
        diff.put("name", "kernel");
        diff.put("description", "kernel-1.0");
        return diff;
    }

    private Map<String, Object> criteriaBody() {
        Map<String, Object> criteria = new LinkedHashMap<>();
        criteria.put("matcher", "contains");
        criteria.put("field", "name");
        criteria.put("value", "kernel");
        return criteria;
    }

    @Test
    public void testListProjects() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listProjects(with(mockUser));
            will(returnValue(List.of(project())));
        }});

        validateApiContract("/contentmanagement/listProjects", "GET")
                .onHandlerMethod("listProjects", User.class);
    }

    @Test
    public void testLookupProject() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).lookupProject(with(mockUser), with(PROJECT_LABEL));
            will(returnValue(project()));
        }});

        validateApiContract("/contentmanagement/lookupProject", "GET")
                .withParams(Map.of("projectLabel", new String[] {PROJECT_LABEL}))
                .onHandlerMethod("lookupProject", User.class, String.class);
    }

    @Test
    public void testCreateProject() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).createProject(with(mockUser), with(PROJECT_LABEL), with("Test Project"),
                    with("a test project"));
            will(returnValue(project()));
        }});

        validateApiContract("/contentmanagement/createProject", "POST")
                .withBody(Map.of("projectLabel", PROJECT_LABEL, "name", "Test Project",
                        "description", "a test project"))
                .onHandlerMethod("createProject", User.class, String.class, String.class, String.class);
    }

    @Test
    public void testUpdateProject() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).updateProject(with(mockUser), with(PROJECT_LABEL), with(any(Map.class)));
            will(returnValue(project()));
        }});

        validateApiContract("/contentmanagement/updateProject", "POST")
                .withBody(Map.of("projectLabel", PROJECT_LABEL,
                        "props", Map.of("name", "New Name", "description", "New description")))
                .onHandlerMethod("updateProject", User.class, String.class, Map.class);
    }

    @Test
    public void testRemoveProject() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).removeProject(with(mockUser), with(PROJECT_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/contentmanagement/removeProject", "POST")
                .withBody(Map.of("projectLabel", PROJECT_LABEL))
                .onHandlerMethod("removeProject", User.class, String.class);
    }

    @Test
    public void testListProjectEnvironments() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listProjectEnvironments(with(mockUser), with(PROJECT_LABEL));
            will(returnValue(List.of(environment())));
        }});

        validateApiContract("/contentmanagement/listProjectEnvironments", "GET")
                .withParams(Map.of("projectLabel", new String[] {PROJECT_LABEL}))
                .onHandlerMethod("listProjectEnvironments", User.class, String.class);
    }

    @Test
    public void testLookupEnvironment() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).lookupEnvironment(with(mockUser), with(PROJECT_LABEL), with(ENV_LABEL));
            will(returnValue(environment()));
        }});

        validateApiContract("/contentmanagement/lookupEnvironment", "GET")
                .withParams(Map.of("projectLabel", new String[] {PROJECT_LABEL},
                        "envLabel", new String[] {ENV_LABEL}))
                .onHandlerMethod("lookupEnvironment", User.class, String.class, String.class);
    }

    @Test
    public void testListEnvironmentDifference() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listEnvironmentDifference(with(mockUser), with(PROJECT_LABEL), with(ENV_LABEL));
            will(returnValue(List.of(environmentDifference())));
        }});

        validateApiContract("/contentmanagement/listEnvironmentDifference", "GET")
                .withParams(Map.of("projectLabel", new String[] {PROJECT_LABEL},
                        "envLabel", new String[] {ENV_LABEL}))
                .onHandlerMethod("listEnvironmentDifference", User.class, String.class, String.class);
    }

    @Test
    public void testCreateEnvironment() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).createEnvironment(with(mockUser), with(PROJECT_LABEL), with(""),
                    with(ENV_LABEL), with("Test Environment"), with("a test environment"));
            will(returnValue(environment()));
        }});

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("projectLabel", PROJECT_LABEL);
        body.put("predecessorLabel", "");
        body.put("envLabel", ENV_LABEL);
        body.put("name", "Test Environment");
        body.put("description", "a test environment");

        validateApiContract("/contentmanagement/createEnvironment", "POST")
                .withBody(body)
                .onHandlerMethod("createEnvironment", User.class, String.class, String.class, String.class,
                        String.class, String.class);
    }

    @Test
    public void testUpdateEnvironment() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).updateEnvironment(with(mockUser), with(PROJECT_LABEL), with(ENV_LABEL),
                    with(any(Map.class)));
            will(returnValue(environment()));
        }});

        validateApiContract("/contentmanagement/updateEnvironment", "POST")
                .withBody(Map.of("projectLabel", PROJECT_LABEL, "envLabel", ENV_LABEL,
                        "props", Map.of("name", "New Name", "description", "New description")))
                .onHandlerMethod("updateEnvironment", User.class, String.class, String.class, Map.class);
    }

    @Test
    public void testRemoveEnvironment() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).removeEnvironment(with(mockUser), with(PROJECT_LABEL), with(ENV_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/contentmanagement/removeEnvironment", "POST")
                .withBody(Map.of("projectLabel", PROJECT_LABEL, "envLabel", ENV_LABEL))
                .onHandlerMethod("removeEnvironment", User.class, String.class, String.class);
    }

    @Test
    public void testListProjectSources() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listProjectSources(with(mockUser), with(PROJECT_LABEL));
            will(returnValue(List.of(source())));
        }});

        validateApiContract("/contentmanagement/listProjectSources", "GET")
                .withParams(Map.of("projectLabel", new String[] {PROJECT_LABEL}))
                .onHandlerMethod("listProjectSources", User.class, String.class);
    }

    @Test
    public void testLookupSource() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).lookupSource(with(mockUser), with(PROJECT_LABEL), with(SOURCE_TYPE),
                    with(SOURCE_LABEL));
            will(returnValue(source()));
        }});

        validateApiContract("/contentmanagement/lookupSource", "GET")
                .withParams(Map.of("projectLabel", new String[] {PROJECT_LABEL},
                        "sourceType", new String[] {SOURCE_TYPE},
                        "sourceLabel", new String[] {SOURCE_LABEL}))
                .onHandlerMethod("lookupSource", User.class, String.class, String.class, String.class);
    }

    @Test
    public void testAttachSource() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).attachSource(with(mockUser), with(PROJECT_LABEL), with(SOURCE_TYPE),
                    with(SOURCE_LABEL), with(0));
            will(returnValue(source()));
        }});

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("projectLabel", PROJECT_LABEL);
        body.put("sourceType", SOURCE_TYPE);
        body.put("sourceLabel", SOURCE_LABEL);
        body.put("sourcePosition", 0);

        validateApiContract("/contentmanagement/attachSource", "POST")
                .withBody(body)
                .onHandlerMethod("attachSource", User.class, String.class, String.class, String.class,
                        int.class);
    }

    @Test
    public void testDetachSource() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).detachSource(with(mockUser), with(PROJECT_LABEL), with(SOURCE_TYPE),
                    with(SOURCE_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/contentmanagement/detachSource", "POST")
                .withBody(Map.of("projectLabel", PROJECT_LABEL, "sourceType", SOURCE_TYPE,
                        "sourceLabel", SOURCE_LABEL))
                .onHandlerMethod("detachSource", User.class, String.class, String.class, String.class);
    }

    @Test
    public void testListFilters() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listFilters(with(mockUser));
            will(returnValue(List.of(filter())));
        }});

        validateApiContract("/contentmanagement/listFilters", "GET")
                .onHandlerMethod("listFilters", User.class);
    }

    @Test
    public void testLookupFilter() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).lookupFilter(with(mockUser), with(FILTER_ID));
            will(returnValue(filter()));
        }});

        validateApiContract("/contentmanagement/lookupFilter", "GET")
                .withParams(Map.of("filterId", new String[] {FILTER_ID.toString()}))
                .onHandlerMethod("lookupFilter", User.class, Integer.class);
    }

    @Test
    public void testListFilterCriteria() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listFilterCriteria(with(mockUser));
            will(returnValue(List.of(Map.of("type", "package", "matcher", "contains", "field", "name"))));
        }});

        validateApiContract("/contentmanagement/listFilterCriteria", "GET")
                .onHandlerMethod("listFilterCriteria", User.class);
    }

    @Test
    public void testCreateFilter() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).createFilter(with(mockUser), with("test-filter"), with("deny"),
                    with("package"), with(any(Map.class)));
            will(returnValue(filter()));
        }});

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "test-filter");
        body.put("rule", "deny");
        body.put("entityType", "package");
        body.put("criteria", criteriaBody());

        validateApiContract("/contentmanagement/createFilter", "POST")
                .withBody(body)
                .onHandlerMethod("createFilter", User.class, String.class, String.class, String.class,
                        Map.class);
    }

    @Test
    public void testCreateAppStreamFilters() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).createAppStreamFilters(with(mockUser), with("prefix"), with(SOURCE_LABEL),
                    with(PROJECT_LABEL));
            will(returnValue(List.of(filter())));
        }});

        validateApiContract("/contentmanagement/createAppStreamFilters", "POST")
                .withBody(Map.of("prefix", "prefix", "channelLabel", SOURCE_LABEL,
                        "projectLabel", PROJECT_LABEL))
                .onHandlerMethod("createAppStreamFilters", User.class, String.class, String.class,
                        String.class);
    }

    @Test
    public void testUpdateFilter() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).updateFilter(with(mockUser), with(FILTER_ID), with("new-name"), with("allow"),
                    with(any(Map.class)));
            will(returnValue(filter()));
        }});

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("filterId", FILTER_ID);
        body.put("name", "new-name");
        body.put("rule", "allow");
        body.put("criteria", criteriaBody());

        validateApiContract("/contentmanagement/updateFilter", "POST")
                .withBody(body)
                .onHandlerMethod("updateFilter", User.class, Integer.class, String.class, String.class,
                        Map.class);
    }

    @Test
    public void testRemoveFilter() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).removeFilter(with(mockUser), with(FILTER_ID));
            will(returnValue(1));
        }});

        validateApiContract("/contentmanagement/removeFilter", "POST")
                .withBody(Map.of("filterId", FILTER_ID))
                .onHandlerMethod("removeFilter", User.class, Integer.class);
    }

    @Test
    public void testListProjectFilters() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listProjectFilters(with(mockUser), with(PROJECT_LABEL));
            will(returnValue(List.of(assignedFilter())));
        }});

        validateApiContract("/contentmanagement/listProjectFilters", "GET")
                .withParams(Map.of("projectLabel", new String[] {PROJECT_LABEL}))
                .onHandlerMethod("listProjectFilters", User.class, String.class);
    }

    @Test
    public void testAttachFilter() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).attachFilter(with(mockUser), with(PROJECT_LABEL), with(FILTER_ID));
            will(returnValue(filter()));
        }});

        validateApiContract("/contentmanagement/attachFilter", "POST")
                .withBody(Map.of("projectLabel", PROJECT_LABEL, "filterId", FILTER_ID))
                .onHandlerMethod("attachFilter", User.class, String.class, Integer.class);
    }

    @Test
    public void testDetachFilter() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).detachFilter(with(mockUser), with(PROJECT_LABEL), with(FILTER_ID));
            will(returnValue(1));
        }});

        validateApiContract("/contentmanagement/detachFilter", "POST")
                .withBody(Map.of("projectLabel", PROJECT_LABEL, "filterId", FILTER_ID))
                .onHandlerMethod("detachFilter", User.class, String.class, Integer.class);
    }

    @Test
    public void testBuildProject() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).buildProject(with(mockUser), with(PROJECT_LABEL), with("a build"));
            will(returnValue(1));
        }});

        validateApiContract("/contentmanagement/buildProject", "POST")
                .withBody(Map.of("projectLabel", PROJECT_LABEL, "message", "a build"))
                .onHandlerMethod("buildProject", User.class, String.class, String.class);
    }

    @Test
    public void testPromoteProject() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).promoteProject(with(mockUser), with(PROJECT_LABEL), with(ENV_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/contentmanagement/promoteProject", "POST")
                .withBody(Map.of("projectLabel", PROJECT_LABEL, "envLabel", ENV_LABEL))
                .onHandlerMethod("promoteProject", User.class, String.class, String.class);
    }

    @Test
    public void testGenerateProjectDifference() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).generateProjectDifference(with(mockUser), with(PROJECT_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/contentmanagement/generateProjectDifference", "POST")
                .withBody(Map.of("projectLabel", PROJECT_LABEL))
                .onHandlerMethod("generateProjectDifference", User.class, String.class);
    }

    @Test
    public void testGenerateEnvironmentDifference() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).generateEnvironmentDifference(with(mockUser), with(PROJECT_LABEL),
                    with(ENV_LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/contentmanagement/generateEnvironmentDifference", "POST")
                .withBody(Map.of("projectLabel", PROJECT_LABEL, "environmentLabel", ENV_LABEL))
                .onHandlerMethod("generateEnvironmentDifference", User.class, String.class, String.class);
    }
}
