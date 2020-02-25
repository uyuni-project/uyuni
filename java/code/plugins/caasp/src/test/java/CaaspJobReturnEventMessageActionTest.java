/**
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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.caasp.CaaspPackageProfileUpdate;
import com.suse.manager.reactor.messaging.JobReturnEventMessage;
import com.suse.manager.reactor.messaging.JobReturnEventMessageAction;
import com.suse.manager.reactor.messaging.test.JobReturnEventMessageActionTest;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.event.JobReturnEvent;
import org.apache.commons.codec.digest.DigestUtils;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.Collections;
import java.util.Optional;

import static junit.framework.Assert.assertTrue;


public class CaaspJobReturnEventMessageActionTest extends JMockBaseTestCaseWithUser {

    private SaltService saltServiceMock;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
        Config.get().setString("server.secret_key",
                DigestUtils.sha256Hex(TestUtils.randomString()));
        saltServiceMock = context().mock(SaltService.class);
        SystemEntitler.INSTANCE.setSaltService(saltServiceMock);
    }

    /**
     * Test the processing of packages.profileupdate job return event in the case where the system has installed CaaSP
     * and it should be locked via Salt formula
     *
     * @throws Exception in case of an error
     */
    public void testPackagesProfileUpdateWithCaaSPSystemLocked() throws Exception {
        // Prepare test objects: minion server, products and action
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setMinionId("minionsles12-suma3pg.vagrant.local");
        SUSEProductTestUtils.createVendorSUSEProducts();
        Action action = ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_PACKAGES_REFRESH_LIST);
        action.addServerAction(ActionFactoryTest.createServerAction(minion, action));
        HibernateFactory.getSession().flush();
        // Setup an event message from file contents
        this.getClass().getResource("/packages.profileupdate.caasp.json");
        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                JobReturnEventMessageActionTest
                        .getJobReturnEventFromClasspath("/packages.profileupdate.caasp.json", action.getId(),
                                Collections.emptyMap()));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        assertTrue(minion.getInstalledProducts().stream().anyMatch(
                p -> p.getName().equalsIgnoreCase(CaaspPackageProfileUpdate.CAASP_PRODUCT_IDENTIFIER)));
        assertTrue(action.getServerActions().stream()
                .filter(serverAction -> serverAction.getServer().equals(minion))
                .findAny().get().getStatus().equals(ActionFactory.STATUS_COMPLETED));
    }

}
