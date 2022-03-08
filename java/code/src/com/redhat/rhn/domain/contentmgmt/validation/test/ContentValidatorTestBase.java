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

package com.redhat.rhn.domain.contentmgmt.validation.test;

import static com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType.MODULE;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType.PACKAGE;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.Rule.ALLOW;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.Rule.DENY;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.CONTAINS;
import static com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher.EQUALS;
import static com.redhat.rhn.domain.contentmgmt.ProjectSource.Type.SW_CHANNEL;
import static java.util.Optional.empty;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.contentmgmt.validation.ContentValidationMessage;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.manager.contentmgmt.test.MockModulemdApi;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.TestUtils;

import java.util.List;

public abstract class ContentValidatorTestBase extends BaseTestCaseWithUser {

    private final LocalizationService loc;

    private ContentProject project;

    private ContentManager manager;

    protected ContentValidatorTestBase() {
        loc = LocalizationService.getInstance();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        manager = new ContentManager(new MockModulemdApi());
        project = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());

        ContentProjectFactory.save(project);
    }

    /**
     * Test the text, the type and the entity of a single message in the validation messages list
     *
     * @param text the expected text
     * @param type the expected type
     * @param entity the expected entity
     * @param messages the list of validation messages
     */
    final void assertSingleMessage(String text, String type, String entity, List<ContentValidationMessage> messages) {
        assertEquals(1, messages.size());
        ContentValidationMessage message = messages.get(0);
        assertEquals(text, message.getMessage());
        assertEquals(type, message.getType());
        assertEquals(entity, message.getEntity());
    }

    /**
     * Attach a (non-modular) source to the content project
     */
    final void attachSource() throws Exception {
        Channel channel = ChannelTestUtils.createBaseChannel(user);
        manager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);
    }

    /**
     * Attach a filter to the content project
     */
    final void attachFilter() {
        FilterCriteria criteria = new FilterCriteria(CONTAINS, "name", "aaa");
        ContentFilter filter = manager.createFilter(TestUtils.randomString(), DENY, PACKAGE, criteria, user);
        manager.attachFilter("cplabel", filter.getId(), user);
    }

    /**
     * Attach a modular source to the content project
     */
    final void attachModularSource() throws Exception {
        Channel channel = MockModulemdApi.createModularTestChannel(user);
        manager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);
    }

    /**
     * Attach a modular filter to the content project
     */
    final void attachModularFilter() {
        attachModularFilter("postgresql:10");
    }

    /**
     * Attach a modular filter with a specific value to the content project
     *
     * @param value the filter value
     */
    final void attachModularFilter(String value) {
        FilterCriteria criteria = new FilterCriteria(EQUALS, "module_stream", value);
        ContentFilter filter = manager.createFilter(TestUtils.randomString(), ALLOW, MODULE, criteria, user);
        manager.attachFilter("cplabel", filter.getId(), user);
    }

    /**
     * @return localization service
     */
    public LocalizationService getLoc() {
        return loc;
    }

    /**
     * @return content project
     */
    public ContentProject getProject() {
        return project;
    }
}
