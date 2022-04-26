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

package com.redhat.rhn.domain.contentmgmt.test;

import static com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType.MODULE;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.Rule.ALLOW;
import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.contentmgmt.ModuleFilter;
import com.redhat.rhn.domain.contentmgmt.modulemd.Module;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ModuleFilterTest extends BaseTestCaseWithUser {

    private ContentManager contentManager;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        contentManager = new ContentManager();
        UserTestUtils.addUserRole(user, ORG_ADMIN);
    }

    @Test
    public void testGetModule() {
        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "module_stream", "mymodule");
        ModuleFilter filter =
                (ModuleFilter) contentManager.createFilter("mymodule-filter-1", ALLOW, MODULE, criteria, user)
                        .asModuleFilter().get();

        Module module = filter.getModule();
        assertEquals("mymodule", module.getName());
        assertNull(module.getStream());

        criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "module_stream", "mymodule:mystream:foo");
        filter = (ModuleFilter) contentManager.createFilter("mymodule-filter-2", ALLOW, MODULE, criteria, user)
                .asModuleFilter().get();

        // The field value is interpreted as module_name : stream_name
        // Additional colons must be included in the stream name
        module = filter.getModule();
        assertEquals("mymodule", module.getName());
        assertEquals("mystream:foo", module.getStream());
    }

}
