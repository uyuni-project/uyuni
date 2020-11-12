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

package com.redhat.rhn.manager.contentmgmt.test;

import static com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType.MODULE;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType.PACKAGE;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.Rule.ALLOW;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.Rule.DENY;
import static com.redhat.rhn.domain.contentmgmt.ProjectSource.Type.SW_CHANNEL;
import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.contentmgmt.modulemd.ConflictingStreamsException;
import com.redhat.rhn.domain.contentmgmt.modulemd.Module;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModuleNotFoundException;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulePackagesResponse;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.manager.contentmgmt.DependencyResolutionException;
import com.redhat.rhn.manager.contentmgmt.DependencyResolutionResult;
import com.redhat.rhn.manager.contentmgmt.DependencyResolver;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DependencyResolverTest extends BaseTestCaseWithUser {

    private Channel modularChannel;
    private ContentManager contentManager;
    private DependencyResolver resolver;
    private MockModulemdApi api;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        user.addPermanentRole(ORG_ADMIN);

        modularChannel = MockModulemdApi.createModularTestChannel(user);
        api = new MockModulemdApi();
        contentManager = new ContentManager(api);

        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        contentManager.attachSource("cplabel", SW_CHANNEL, modularChannel.getLabel(), Optional.empty(), user);

        resolver = new DependencyResolver(cp, api);
    }

    /**
     * Test the resolver with no filters
     * Should resolve an empty list.
     */
    @Test
    public void testNoFilters() throws DependencyResolutionException {
        assertEquals(0, resolver.resolveFilters(emptyList()).getFilters().size());
    }

    /**
     * Test package filter resolution
     * Since no dependency resolution is implemented for package filters, the resulting list must be unchanged.
     */
    @Test
    public void testNoModuleFilters() throws DependencyResolutionException {
        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.CONTAINS, "name", "mypkg");
        ContentFilter filter = contentManager.createFilter("mypkg-filter", DENY, PACKAGE, criteria, user);

        List<ContentFilter> result = resolver.resolveFilters(singletonList(filter)).getFilters();

        assertEquals(1, result.size());
        assertEquals(result.get(0), filter);
    }

    /**
     * Test the resolver with both Module and Package filters
     * Package filters should be left untouched, where the module filter should be transformed into new package filters.
     */
    @Test
    public void testMixedFilters() throws DependencyResolutionException {
        FilterCriteria criteria1 = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "module_stream", "postgresql:10");
        ContentFilter filter1 = contentManager.createFilter("mymodule-filter", ALLOW, MODULE, criteria1, user);
        FilterCriteria criteria2 = new FilterCriteria(FilterCriteria.Matcher.CONTAINS, "name", "mypkg");
        ContentFilter filter2 = contentManager.createFilter("mypkg-filter", DENY, PACKAGE, criteria2, user);

        DependencyResolutionResult result = resolver.resolveFilters(Arrays.asList(filter1, filter2));

        // Resolved modules must be present
        List<Module> modules = result.getModules();
        assertEquals(singletonList(new Module("postgresql", "10")), modules);

        List<ContentFilter> filters = result.getFilters();
        assertNotEmpty(filters);
        // The original module filter must be absent
        assertTrue(filters.stream().noneMatch(filter1::equals));
        // The package filter must be unmodified
        assertTrue(filters.stream().anyMatch(f -> filter2.equals(f) && f.getId() != null));
    }

    /**
     * Test the resolver with a non-matching filter
     * Should throw a DependencyResolutionException
     */
    @Test
    public void testNonMatchingModuleFilter() {
        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "module_stream", "postgresql:foo");
        ContentFilter filter = contentManager.createFilter("mymodule-filter", ALLOW, MODULE, criteria, user);

        try {
            resolver.resolveFilters(singletonList(filter));
            fail("Should throw DependencyResolutionException.");
        }
        catch (DependencyResolutionException e) {
            assertTrue(e.getCause() instanceof ModuleNotFoundException);
            ModuleNotFoundException cause = (ModuleNotFoundException) e.getCause();
            assertEquals(1, cause.getModules().size());
            assertEquals("postgresql", cause.getModules().get(0).getName());
            assertEquals("foo", cause.getModules().get(0).getStream());
        }
    }

    /**
     * Test the resolver with a matching filter
     *
     * Expected result from the sample data in MockModulemdApi:
     * 5 ALLOW nevra filters for modular packages of the selected modules (postgresql:10, perl:5.24)
     * 3 DENY name filters for postgresql:10 provided apis (postgresql, postgresql-server, perl)
     * 7 DENY nevra filters for all modular packages
     *
     * @see DependencyResolver#resolveModularDependencies
     */
    @Test
    public void testResolveModuleFilters() throws Exception {
        FilterCriteria criteria1 = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "module_stream", "postgresql:10");
        ContentFilter filter1 = contentManager.createFilter("postgresql-filter", ALLOW, MODULE, criteria1, user);
        FilterCriteria criteria2 = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "module_stream", "perl:5.24");
        ContentFilter filter2 = contentManager.createFilter("perl-filter", ALLOW, MODULE, criteria2, user);

        DependencyResolutionResult result = resolver.resolveFilters(Arrays.asList(filter1, filter2));

        // Resolved modules must be present
        List<Module> modules = result.getModules();
        assertEquals(Arrays.asList(new Module("postgresql", "10"), new Module("perl", "5.24")), modules);

        // The original module filters must be absent
        List<ContentFilter> filters = result.getFilters();
        assertEquals(15, filters.size());
        assertTrue(filters.stream().noneMatch(filter1::equals));
        assertTrue(filters.stream().noneMatch(filter2::equals));

        // All filters should be transient
        assertTrue(filters.stream().allMatch(f -> f.getId() == null));

        ModulePackagesResponse moduleData = api.getPackagesForModules(singletonList(modularChannel),
                Arrays.asList(new Module("postgresql", "10"), new Module("perl", "5.24")));

        // ALLOW filters for selected modular packages
        // This overrides deny-all filters for modular packages
        moduleData.getRpmPackages()
                .forEach(p -> assertTrue(filters.stream().anyMatch(f -> isAllowNevraEquals(f, p))));

        // DENY filters for postgresql:10 provided apis (postgresql, postgresql-server)
        // For the enabled module, all other packages with the same name should be filtered out from different sources
        moduleData.getRpmApis().forEach(a -> assertTrue(filters.stream().anyMatch(f -> isDenyNameMatches(f, a))));

        // DENY filters for all modular packages
        // Deny-all rule for all modular packages (are overridden by ALLOW filters for the selected modules)
        api.getAllPackages(singletonList(modularChannel))
                .forEach(p -> assertTrue(filters.stream().anyMatch(f -> isDenyNevraEquals(f, p))));
    }

    /**
     * Test the resolver with a module that has updates available
     *
     * In the sample data, perl 5.24 has 5.24.0 and 5.24.1 (update) packages.
     */
    @Test
    public void testResolveModuleFiltersVersionUpdates() throws DependencyResolutionException {
        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "module_stream", "perl:5.24");
        ContentFilter filter = contentManager.createFilter("perl-filter", ALLOW, MODULE, criteria, user);

        List<ContentFilter> result = resolver.resolveFilters(singletonList(filter)).getFilters();

        assertEquals(10, result.size());
        // There should be one and only one "perl" api filter
        assertEquals(1, result.stream().filter(f -> isDenyNameMatches(f, "perl")).count());
        // There should be allow filters for both versions
        assertTrue(result.stream().anyMatch(f -> isAllowNevraEquals(f, "perl-0:5.24.0-xxx.x86_64")));
        assertTrue(result.stream().anyMatch(f -> isAllowNevraEquals(f, "perl-0:5.24.1-yyy.x86_64")));
    }

    /**
     * Test scenario: perl-5.26 is defined as perl:5.26 stream in the module metadata, but the actual package is a
     * regular package outside the module space. We should be able to allow serving this package from outside of the
     * module space.
     */
    @Test
    public void testModuleFiltersForeignPackagesSelected() throws Exception {
        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "module_stream", "perl:5.26");
        ContentFilter filter = contentManager.createFilter("perl-5.24-filter", ALLOW, MODULE, criteria, user);

        List<ContentFilter> result = resolver.resolveFilters(singletonList(filter)).getFilters();
        assertNotEmpty(result);
        // Since "perl" is not served as a modular package, there shouldn't be any deny filters for name "perl"
        assertTrue(result.stream().noneMatch(f -> isDenyNameMatches(f, "perl")));
        // No override should exist for perl-5.26
        assertTrue(result.stream()
                .noneMatch(f -> ALLOW.equals(f.getRule()) && f.getCriteria().getValue().startsWith("perl-5.26")));
        // As a result, "perl-5.26" will be served from any source if available, whether the module is selected or not.
        // This matches the DNF behavior.
    }

    /**
     * Test scenario: perl-5.26 is defined as perl:5.26 stream in the module metadata, but the actual package is a
     * regular package outside the module space. We should be able to filter this package out in case another stream
     * for the same module (e.g. perl-5.24) is selected to prevent conflicts.
     */
    @Test
    public void testModuleFiltersForeignPackagesConflicting() throws Exception {
        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "module_stream", "perl:5.24");
        ContentFilter filter = contentManager.createFilter("perl-5.24-filter", ALLOW, MODULE, criteria, user);

        List<ContentFilter> result = resolver.resolveFilters(singletonList(filter)).getFilters();
        assertNotEmpty(result);
        // Filter out every "perl" in all sources (including perl-5.26)
        assertTrue(result.stream().anyMatch(f -> isDenyNameMatches(f, "perl")));
        // Override perl-5.24 to be in the target
        assertTrue(result.stream()
                .anyMatch(f -> ALLOW.equals(f.getRule()) && f.getCriteria().getValue().startsWith("perl-0:5.24")));
        // No override can exist for perl-5.26 because the package is not defined in the module metadata
        assertTrue(result.stream()
                .noneMatch(f -> ALLOW.equals(f.getRule()) && f.getCriteria().getValue().startsWith("perl-5.26")));
        // As a result, "perl" will be exclusively served from the "perl:5.24" module.
    }

    /**
     * Test resolver with conflicting module filters
     */
    @Test
    public void testConflictingStreams() {
        FilterCriteria criteria1 = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "module_stream", "perl:5.24");
        ContentFilter filter1 = contentManager.createFilter("perl-5.24-filter", ALLOW, MODULE, criteria1, user);
        FilterCriteria criteria2 = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "module_stream", "perl:5.26");
        ContentFilter filter2 = contentManager.createFilter("perl-5.26-filter", ALLOW, MODULE, criteria2, user);

        try {
            resolver.resolveFilters(Arrays.asList(filter1, filter2));
            fail("Should throw DependencyResolutionException.");
        }
        catch (DependencyResolutionException e) {
            assertTrue(e.getCause() instanceof ConflictingStreamsException);
        }
    }

    private boolean isAllowNevraEquals(ContentFilter f, String value) {
        return isFilterOfType(f, ALLOW, FilterCriteria.Matcher.EQUALS, "nevra", value);
    }

    private boolean isDenyNevraEquals(ContentFilter f, String value) {
        return isFilterOfType(f, DENY, FilterCriteria.Matcher.EQUALS, "nevra", value);
    }

    private boolean isDenyNameMatches(ContentFilter f, String value) {
        return isFilterOfType(f, DENY, FilterCriteria.Matcher.MATCHES, "name", value);
    }

    private boolean isFilterOfType(ContentFilter f, ContentFilter.Rule rule, FilterCriteria.Matcher matcher,
            String field, String value) {
        return rule.equals(f.getRule()) && matcher.equals(f.getCriteria().getMatcher()) && field
                .equals(f.getCriteria().getField()) && value.equals(f.getCriteria().getValue());
    }
}
