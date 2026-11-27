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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.frontend.xmlrpc.contentmgmt;

import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.frontend.xmlrpc.BaseHandlerTestCase;
import com.redhat.rhn.frontend.xmlrpc.ContentValidationFaultException;
import com.redhat.rhn.frontend.xmlrpc.EntityNotExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidArgsException;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchChannelException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.manager.contentmgmt.MockModulemdApi;
import com.redhat.rhn.manager.errata.syncwithpackages.dto.SyncedErratumWithPackages;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentManagementHandlerTest extends BaseHandlerTestCase {

    private ContentManager manager = new ContentManager(new MockModulemdApi());
    private ContentManagementHandler handler = new ContentManagementHandler(manager);

    private static final String ADVISORY = "CLM-2026:1234";
    private static final String DEV_HOTFIX = "cp-dev-hotfix";
    private static final String TEST_HOTFIX = "cp-test-hotfix";

    @RegisterExtension
    protected final Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }};

    @Test
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
    @Test
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
    @Test
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
    @Test
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
    @Test
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

    /**
     * Tests the result of the content manager is returned when cloning an erratum into one hotfix channel.
     */
    @Test
    void testCloneErrataIntoHotfixChannel() {
        ContentManager mockedManager = context.mock(ContentManager.class);
        SyncedErratumWithPackages synced = new SyncedErratumWithPackages(null, List.of());
        context.checking(new Expectations() {{
            oneOf(mockedManager).cloneIntoHotfixChannel(admin, ADVISORY, DEV_HOTFIX);
            will(returnValue(synced));
        }});

        assertSame(synced,
                new ContentManagementHandler(mockedManager).cloneErrataIntoHotfixChannel(admin, ADVISORY, DEV_HOTFIX));
    }

    /**
     * Tests the result of the content manager is returned when cloning an erratum into several hotfix channels.
     */
    @Test
    void testCloneErrataIntoHotfixChannels() {
        ContentManager mockedManager = context.mock(ContentManager.class);
        SyncedErratumWithPackages synced = new SyncedErratumWithPackages(null, List.of());
        Map<String, SyncedErratumWithPackages> result = Map.of(DEV_HOTFIX, synced, TEST_HOTFIX, synced);
        List<String> labels = List.of(DEV_HOTFIX, TEST_HOTFIX);
        context.checking(new Expectations() {{
            oneOf(mockedManager).cloneIntoHotfixChannels(admin, ADVISORY, labels);
            will(returnValue(result));
        }});

        assertEquals(result,
                new ContentManagementHandler(mockedManager).cloneErrataIntoHotfixChannels(admin, ADVISORY, labels));
    }

    /**
     * Tests the errors of the content manager reach the caller unchanged when cloning into one hotfix channel, except
     * an unknown advisory, which becomes a fault.
     */
    @Test
    void testCloneErrataIntoHotfixChannelErrors() {
        ContentManager mockedManager = context.mock(ContentManager.class);
        ContentManagementHandler mockedHandler = new ContentManagementHandler(mockedManager);
        List<RuntimeException> errors = List.of(
                new InvalidChannelException("not a hotfix channel"),
                new NoSuchChannelException(DEV_HOTFIX),
                new PermissionCheckFailureException("not a channel admin"));

        for (RuntimeException error : errors) {
            context.checking(new Expectations() {{
                oneOf(mockedManager).cloneIntoHotfixChannel(admin, ADVISORY, DEV_HOTFIX);
                will(throwException(error));
            }});

            assertSame(error, assertThrows(error.getClass(),
                    () -> mockedHandler.cloneErrataIntoHotfixChannel(admin, ADVISORY, DEV_HOTFIX)));
        }

        context.checking(new Expectations() {{
            oneOf(mockedManager).cloneIntoHotfixChannel(admin, ADVISORY, DEV_HOTFIX);
            will(throwException(new EntityNotExistsException(ADVISORY)));
        }});
        assertThrows(EntityNotExistsFaultException.class,
                () -> mockedHandler.cloneErrataIntoHotfixChannel(admin, ADVISORY, DEV_HOTFIX));
    }

    /**
     * Tests the errors of the content manager reach the caller unchanged when cloning into several hotfix
     * channels, except an unknown advisory, which becomes a fault.
     */
    @Test
    void testCloneErrataIntoHotfixChannelsErrors() {
        ContentManager mockedManager = context.mock(ContentManager.class);
        ContentManagementHandler mockedHandler = new ContentManagementHandler(mockedManager);
        List<String> labels = List.of(DEV_HOTFIX, TEST_HOTFIX);
        List<RuntimeException> errors = List.of(
                new InvalidParameterException("hotfixLabels must not be empty"),
                new InvalidChannelException("not a hotfix channel"),
                new NoSuchChannelException(TEST_HOTFIX),
                new PermissionCheckFailureException("not a channel admin"));

        for (RuntimeException error : errors) {
            context.checking(new Expectations() {{
                oneOf(mockedManager).cloneIntoHotfixChannels(admin, ADVISORY, labels);
                will(throwException(error));
            }});

            assertSame(error, assertThrows(error.getClass(),
                    () -> mockedHandler.cloneErrataIntoHotfixChannels(admin, ADVISORY, labels)));
        }

        context.checking(new Expectations() {{
            oneOf(mockedManager).cloneIntoHotfixChannels(admin, ADVISORY, labels);
            will(throwException(new EntityNotExistsException(ADVISORY)));
        }});
        assertThrows(EntityNotExistsFaultException.class,
                () -> mockedHandler.cloneErrataIntoHotfixChannels(admin, ADVISORY, labels));
    }

    /**
     * Tests the result of the content manager is returned when listing the compatible hotfix channels.
     */
    @Test
    void testListCompatibleHotfixChannelLabelsForAdvisory() {
        ContentManager mockedManager = context.mock(ContentManager.class);
        Map<String, List<String>> result = Map.of("cp", List.of(DEV_HOTFIX, TEST_HOTFIX));
        context.checking(new Expectations() {{
            oneOf(mockedManager).listCompatibleHotfixChannelLabelsByProject(ADVISORY, admin);
            will(returnValue(result));
        }});

        assertEquals(result, new ContentManagementHandler(mockedManager)
                .listCompatibleHotfixChannelLabelsForAdvisory(admin, ADVISORY));
    }

    /**
     * Tests an unknown advisory is reported as a fault when listing the compatible hotfix channels.
     */
    @Test
    void testListCompatibleHotfixChannelLabelsForUnknownAdvisory() {
        ContentManager mockedManager = context.mock(ContentManager.class);
        context.checking(new Expectations() {{
            oneOf(mockedManager).listCompatibleHotfixChannelLabelsByProject(ADVISORY, admin);
            will(throwException(new EntityNotExistsException(ADVISORY)));
        }});

        assertThrows(EntityNotExistsFaultException.class, () -> new ContentManagementHandler(mockedManager)
                .listCompatibleHotfixChannelLabelsForAdvisory(admin, ADVISORY));
    }
}
