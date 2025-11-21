/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.domain.action.config.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.config.ConfigAction;
import com.redhat.rhn.domain.action.config.ConfigDiffAction;
import com.redhat.rhn.domain.action.config.ConfigRevisionAction;
import com.redhat.rhn.domain.action.config.ConfigRevisionActionResult;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.config.ConfigurationFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * ConfigRevisionActionTest
 */
public class ConfigRevisionActionTest extends RhnBaseTestCase {

    @Test
    public void testBeanMethods() {
        ConfigRevisionAction cra = new ConfigRevisionAction();
        Date now = new Date();
        Long three = 3L;
        ConfigAction parent = new ConfigDiffAction();
        Server server = ServerFactory.createServer();
        ConfigRevision revision = ConfigurationFactory.newConfigRevision();
        ConfigRevisionActionResult result = new ConfigRevisionActionResult();

        cra.setCreated(now);
        assertEquals(now, cra.getCreated());

        cra.setModified(now);
        assertEquals(now, cra.getModified());

        cra.setFailureId(three);
        assertEquals(three, cra.getFailureId());

        cra.setParentAction(parent);
        assertEquals(parent, cra.getParentAction());

        cra.setServer(server);
        assertEquals(server, cra.getServer());

        cra.setConfigRevision(revision);
        assertEquals(revision, cra.getConfigRevision());

        cra.setConfigRevisionActionResult(result);
        assertEquals(result, cra.getConfigRevisionActionResult());
    }

    /**
     * Test fetching a ConfigRevisionAction
     * @throws Exception something bad happened
     */
    @Test
    public void testLookupConfigRevision() throws Exception {
        User user = UserTestUtils.createUser(this);
        Action a = ActionFactoryTest.createAction(user, ActionFactory.TYPE_CONFIGFILES_DEPLOY);

        assertNotNull(a);
        assertInstanceOf(ConfigAction.class, a);
        assertNotNull(a.getActionType());

        ConfigAction a2 = (ConfigAction) ActionFactoryTest.createAction(user,
                          ActionFactory.TYPE_CONFIGFILES_DEPLOY);
        ActionFactory.save(a2);
        ConfigRevisionAction cra = (ConfigRevisionAction)
            a2.getConfigRevisionActions().toArray()[0];
        assertNotNull(cra.getId());
    }

    public static void createTestRevision(User user, Action parent) {
        ConfigRevisionAction cra = new ConfigRevisionAction();
        cra.setServer(ServerFactoryTest.createTestServer(user));

        cra.setConfigRevision(ConfigTestUtils.createConfigRevision(user.getOrg()));
        cra.setCreated(new Date());
        cra.setModified(new Date());
        ((ConfigAction) parent).addConfigRevisionAction(cra);
    }

}
