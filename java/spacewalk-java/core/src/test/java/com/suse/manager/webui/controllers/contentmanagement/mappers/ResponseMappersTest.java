/*
 * Copyright (c) 2021 SUSE LLC
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

import static com.redhat.rhn.domain.contentmgmt.ProjectSource.Type.SW_CHANNEL;
import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;

import com.suse.manager.webui.controllers.contentmanagement.response.ProjectPropertiesResponse;
import com.suse.manager.webui.utils.ViewHelper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class ResponseMappersTest extends BaseTestCaseWithUser {

    private ContentManager manager;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        manager = new ContentManager();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
    }

    /**
     * Test if a project's properties are properly mapped
     */
    @Test
    public void testMapProjectPropertiesFromDB() throws Exception {
        ContentProject cp = new ContentProject("myproject", "My Project", "My CLM project", user.getOrg());
        ContentProjectFactory.save(cp);
        manager.createEnvironment(cp.getLabel(), empty(), "dev", "Development", "Development environment", false, user);

        // Initial project creation
        ProjectPropertiesResponse props = ResponseMappers.mapProjectPropertiesFromDB(cp);
        assertEquals("myproject", props.getLabel());
        assertEquals("My Project", props.getName());
        assertEquals("My CLM project", props.getDescription());
        assertNull(props.getLastBuildDate());
        assertTrue(props.getHistoryEntries().isEmpty());

        // First build
        Channel source = ChannelTestUtils.createTestChannel(user);
        source.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha1"));
        ChannelFactory.save(source);
        manager.attachSource(cp.getLabel(), SW_CHANNEL, source.getLabel(), empty(), user);
        manager.buildProject(cp, empty(), false, user);

        props = ResponseMappers.mapProjectPropertiesFromDB(cp);
        Date firstBuildDate = ViewHelper.getDateFromISOString(props.getLastBuildDate());
        assertNotNull(firstBuildDate);
        assertEquals(1, props.getHistoryEntries().size());

        Thread.sleep(1000); // wait before building again

        // Second build
        manager.buildProject(cp, empty(), false, user);
        props = ResponseMappers.mapProjectPropertiesFromDB(cp);
        Date secondBuildDate = ViewHelper.getDateFromISOString(props.getLastBuildDate());
        assertTrue(secondBuildDate.after(firstBuildDate));
        assertEquals(2, props.getHistoryEntries().size());
    }
}
