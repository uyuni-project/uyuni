/*
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.contentmgmt.test;

import static java.util.Optional.empty;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.frontend.xmlrpc.ContentValidationFaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidArgsException;
import com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.manager.contentmgmt.test.MockModulemdApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentManagementHandlerTest extends BaseHandlerTestCase {

    private ContentManager manager = new ContentManager(new MockModulemdApi());
    private ContentManagementHandler handler = new ContentManagementHandler(manager);

    public void testCreateModuleFilter() {
        Map<String, Object> filterCriteria = new HashMap<>();
        filterCriteria.put("matcher", "equals");
        filterCriteria.put("field", "module_stream");
        filterCriteria.put("value", "postgresql:10");
        handler.createFilter(admin, "my-filter", "allow", "module", filterCriteria);

        List<ContentFilter> filters = ContentProjectFactory.listFilters(admin);
        assertEquals(1, filters.size());

        ContentFilter filter = filters.get(0);
        assertEquals("my-filter", filter.getName());
        assertEquals("allow", filter.getRule().getLabel());
        assertEquals("module", filter.getEntityType().getLabel());
        assertEquals("equals", filter.getCriteria().getMatcher().getLabel());
        assertEquals("module_stream", filter.getCriteria().getField());
        assertEquals("postgresql:10", filter.getCriteria().getValue());
    }

    /**
     * Filter rule is not applicable for module filters. The "rule" argument should be ignored.
     */
    public void testModuleFilterRule() {
        Map<String, Object> filterCriteria = new HashMap<>();
        filterCriteria.put("matcher", "equals");
        filterCriteria.put("field", "module_stream");
        filterCriteria.put("value", "postgresql:10");
        try {
            handler.createFilter(admin, "my-allow-filter", "deny", "module", filterCriteria);
            fail("'deny' filters must not be supported for appstream filters.");
        }
        catch (InvalidArgsException e) {
            List<ContentFilter> filters = ContentProjectFactory.listFilters(admin);
            assertEquals(0, filters.size());
        }
    }

    /**
     * Test if the project is built successfully with valid module filters
     */
    public void testBuildWithModuleFilters() throws Exception {
        Channel channel = MockModulemdApi.createModularTestChannel(admin);

        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", admin.getOrg());
        ContentProjectFactory.save(cp);
        ContentEnvironment env = manager.createEnvironment(cp.getLabel(), empty(), "fst", "first env",
                "desc", false, admin);
        manager.attachSource("cplabel", ProjectSource.Type.SW_CHANNEL, channel.getLabel(), empty(), admin);

        FilterCriteria criteria = new FilterCriteria(Matcher.EQUALS, "module_stream", "postgresql:10");
        ContentFilter filter = manager.createFilter(
                "my-filter", ContentFilter.Rule.ALLOW, EntityType.MODULE, criteria, admin);
        manager.attachFilter("cplabel", filter.getId(), admin);

        handler.buildProject(admin, "cplabel");
        assertEquals(Long.valueOf(1), env.getVersion());
    }

    /**
     * Test if the project is validated correctly before build
     */
    public void testBuildWithUnmatchingModuleFilters() throws Exception {
        Channel channel = MockModulemdApi.createModularTestChannel(admin);

        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", admin.getOrg());
        ContentProjectFactory.save(cp);
        manager.createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false, admin);
        manager.attachSource("cplabel", ProjectSource.Type.SW_CHANNEL, channel.getLabel(), empty(), admin);

        FilterCriteria criteria1 = new FilterCriteria(Matcher.EQUALS, "module_stream", "firstmodule:notexists");
        FilterCriteria criteria2 = new FilterCriteria(Matcher.EQUALS, "module_stream", "secondmodule:notexists");
        ContentFilter filter1 = manager.createFilter(
                "my-filter-1", ContentFilter.Rule.ALLOW, EntityType.MODULE, criteria1, admin);
        ContentFilter filter2 = manager.createFilter(
                "my-filter-2", ContentFilter.Rule.ALLOW, EntityType.MODULE, criteria2, admin);
        manager.attachFilter("cplabel", filter1.getId(), admin);
        manager.attachFilter("cplabel", filter2.getId(), admin);

        try {
            handler.buildProject(admin, "cplabel");
            fail("Project validation should fail.");
        }
        catch (ContentValidationFaultException e) {
            // Multiple messages should be written in separate lines
            assertEquals(2, e.getMessage().lines().count());
            assertTrue(e.getMessage().lines().anyMatch("Module 'firstmodule:notexists' not found."::equals));
            assertTrue(e.getMessage().lines().anyMatch("Module 'secondmodule:notexists' not found."::equals));
        }
    }

    /**
     * Test if the project is validated correctly before promote
     */
    public void testPromoteWithUnmatchingModuleFilters() throws Exception {
        Channel channel = MockModulemdApi.createModularTestChannel(admin);

        // Create and build the project
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", admin.getOrg());
        ContentProjectFactory.save(cp);
        manager.createEnvironment(cp.getLabel(), empty(), "first", "first env", "desc", false, admin);
        manager.attachSource("cplabel", ProjectSource.Type.SW_CHANNEL, channel.getLabel(), empty(), admin);
        manager.buildProject(cp, empty(), false, admin);

        // Add an invalid module filter
        FilterCriteria criteria = new FilterCriteria(Matcher.EQUALS, "module_stream", "mymodule:notexists");
        ContentFilter filter = manager.createFilter(
                "my-filter", ContentFilter.Rule.ALLOW, EntityType.MODULE, criteria, admin);
        manager.attachFilter("cplabel", filter.getId(), admin);

        // Create second environment
        manager.createEnvironment(cp.getLabel(), empty(), "second", "second env", "desc", false, admin);

        try {
            handler.promoteProject(admin, "cplabel", "second");
            fail("Project validation should fail.");
        }
        catch (ContentValidationFaultException e) {
            assertEquals("Module 'mymodule:notexists' not found.", e.getMessage());
        }
    }
}
