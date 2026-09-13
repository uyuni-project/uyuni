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
package com.suse.manager.reactor.messaging;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.action.ActionBuilder;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionTypeEnum;
import com.redhat.rhn.domain.action.dup.DistUpgradeAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeActionDetails;
import com.redhat.rhn.domain.action.dup.DistUpgradeChannelTask;
import com.redhat.rhn.domain.action.server.ServerActionFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactoryTest;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductTestUtils;
import com.redhat.rhn.domain.product.SUSEProductUpgrade;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.TestSystemQuery;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.utils.salt.custom.SumaUtil.PublicCloudInstanceFlavor;
import com.suse.salt.netapi.calls.modules.Zypper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Tests for {@link RegistrationUtils}.
 */
class RegistrationUtilsTest extends BaseTestCaseWithUser {

    private static final String MINION_ID = "test-minion";

    // A handler must be registered for ApplyStatesEventMessage, otherwise
    // MessageQueue.publish() silently drops the message instead of queuing it, which
    // would make MessageQueue.getMessageCount() useless for observing whether
    // RegistrationUtils#scheduleSLES16VerificationIfNeeded actually published anything.
    // Actual dispatch of a queued message happens asynchronously on a background thread
    // and, for an EventDatabaseMessage such as ApplyStatesEventMessage, is gated on this
    // test's own DB transaction becoming inactive - which only happens once this test
    // method has returned - so delivery itself is never observed here, only enqueuing.
    private final MessageAction countingMessageAction = msg -> {
        // no-op: we only care that MessageQueue.publish() enqueued a message
    };

    @BeforeEach
    void registerMessageAction() {
        // Stop any dispatcher thread left running by a previous test so that nothing
        // else can race with us and pop messages off the queue while we are inspecting
        // MessageQueue.getMessageCount() below.
        MessageQueue.stopMessaging();
        MessageQueue.registerAction(countingMessageAction, ApplyStatesEventMessage.class);
    }

    @AfterEach
    void deregisterMessageAction() {
        MessageQueue.deRegisterAction(countingMessageAction, ApplyStatesEventMessage.class);
        MessageQueue.stopMessaging();
    }

    /**
     * A PAYG instance is always allowed, regardless of the products installed:
     * {@link RegistrationUtils#isAllowedOnPayg} must short-circuit before ever
     * looking up products, so a {@link SystemQuery} that throws on any call is used.
     */
    @Test
    void testIsAllowedOnPaygTrueForPaygInstance() {
        SystemQuery systemQuery = new TestSystemQuery() {
            @Override
            public Optional<List<Zypper.ProductInfo>> getProducts(String minionId) {
                throw new AssertionError("must not be called for a PAYG instance");
            }
        };

        boolean allowed = RegistrationUtils.isAllowedOnPayg(systemQuery, MINION_ID, emptySet(),
                new ValueMap(Map.of("os", "suse")), PublicCloudInstanceFlavor.PAYG);

        assertTrue(allowed);
    }

    /**
     * A non-PAYG instance running a free product is allowed.
     */
    @Test
    void testIsAllowedOnPaygTrueForFreeProduct() {
        ChannelFamily channelFamily = ErrataTestUtils.createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        product.setFree(true);

        SystemQuery systemQuery = productSystemQuery(product);

        boolean allowed = RegistrationUtils.isAllowedOnPayg(systemQuery, MINION_ID, emptySet(),
                new ValueMap(Map.of("os", "suse")), PublicCloudInstanceFlavor.BYOS);

        assertTrue(allowed);
    }

    /**
     * A non-PAYG instance running a non-free product whose channel family is not one of
     * the proxy/tools families must not be allowed.
     */
    @Test
    void testIsAllowedOnPaygFalseForNonFreeProductOutsideAllowedFamilies() {
        ChannelFamily channelFamily = ErrataTestUtils.createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        product.setFree(false);

        SystemQuery systemQuery = productSystemQuery(product);

        boolean allowed = RegistrationUtils.isAllowedOnPayg(systemQuery, MINION_ID, emptySet(),
                new ValueMap(Map.of("os", "suse")), PublicCloudInstanceFlavor.BYOS);

        assertFalse(allowed);
    }

    private SystemQuery productSystemQuery(SUSEProduct product) {
        Zypper.ProductInfo productInfo = new Zypper.ProductInfo(
                product.getName(),
                product.getArch().getLabel(), "descr", "eol", "epoch", "flavor",
                true, true, "productline", Optional.of("registerrelease"),
                product.getRelease(), "repo", "shortname", "summary", "vendor",
                product.getVersion());

        return new TestSystemQuery() {
            @Override
            public Optional<List<Zypper.ProductInfo>> getProducts(String minionId) {
                return Optional.of(List.of(productInfo));
            }
        };
    }

    /**
     * No pending actions at all: nothing to verify, so nothing must be scheduled.
     */
    @Test
    void testScheduleSLES16VerificationNoPendingActions() {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        int messageCountBefore = MessageQueue.getMessageCount();
        RegistrationUtils.scheduleSLES16VerificationIfNeeded(minion);

        assertEquals(messageCountBefore, MessageQueue.getMessageCount());
    }

    /**
     * A pending action of an unrelated type (not a DistUpgradeAction) must be ignored.
     */
    @Test
    void testScheduleSLES16VerificationIgnoresUnrelatedPendingAction() {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        ActionManager.scheduleApplyStates(user, List.of(minion.getId()),
                List.of(ApplyStatesEventMessage.PACKAGES), new Date());
        TestUtils.flushSession();

        int messageCountBefore = MessageQueue.getMessageCount();
        RegistrationUtils.scheduleSLES16VerificationIfNeeded(minion);

        assertEquals(messageCountBefore, MessageQueue.getMessageCount());
    }

    /**
     * A pending DistUpgradeAction that is not a cross-major SLES 15 -> 16 migration
     * (e.g. a regular service pack migration) must not trigger verification.
     */
    @Test
    void testScheduleSLES16VerificationIgnoresNonMajorMigration() {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        SUSEProduct sles15sp6 = createTestSlesProduct("15.6");
        SUSEProduct sles15sp7 = createTestSlesProduct("15.7");
        createPendingDistUpgradeAction(minion, sles15sp6, sles15sp7);

        int messageCountBefore = MessageQueue.getMessageCount();
        RegistrationUtils.scheduleSLES16VerificationIfNeeded(minion);

        assertEquals(messageCountBefore, MessageQueue.getMessageCount());
    }

    /**
     * A pending SLES 15 -> 16 migration with no verify action running yet must schedule
     * the verification, observed as a new message being published on the queue.
     */
    @Test
    void testScheduleSLES16VerificationSchedulesForPendingMigration() {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        SUSEProduct sles15 = createTestSlesProduct("15.7");
        SUSEProduct sles16 = createTestSlesProduct("16.0");
        createPendingDistUpgradeAction(minion, sles15, sles16);

        int messageCountBefore = MessageQueue.getMessageCount();
        RegistrationUtils.scheduleSLES16VerificationIfNeeded(minion);

        assertEquals(messageCountBefore + 1, MessageQueue.getMessageCount());
    }

    /**
     * If a SLES 16 verify action is already pending for the minion, verification must
     * not be scheduled a second time.
     */
    @Test
    void testScheduleSLES16VerificationSkipsWhenVerifyAlreadyPending() {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        SUSEProduct sles15 = createTestSlesProduct("15.7");
        SUSEProduct sles16 = createTestSlesProduct("16.0");
        createPendingDistUpgradeAction(minion, sles15, sles16);

        ActionManager.scheduleApplyStates(user, List.of(minion.getId()),
                List.of(ApplyStatesEventMessage.DISTUPGRADE_SLES16_VERIFY), new Date());
        TestUtils.flushSession();

        int messageCountBefore = MessageQueue.getMessageCount();
        RegistrationUtils.scheduleSLES16VerificationIfNeeded(minion);

        assertEquals(messageCountBefore, MessageQueue.getMessageCount());
    }

    private SUSEProduct createTestSlesProduct(String version) {
        return SUSEProductTestUtils.createTestSUSEProduct(user, "sles", version, "x86_64",
                "sles-family-" + TestUtils.randomString(), true);
    }

    private void createPendingDistUpgradeAction(MinionServer minion, SUSEProduct fromProduct, SUSEProduct toProduct) {
        DistUpgradeActionDetails details = new DistUpgradeActionDetails();
        details.setServer(minion);
        details.setDryRun(false);
        details.addProductUpgrade(new SUSEProductUpgrade(fromProduct, toProduct));

        Channel sles156Channel = ChannelFactoryTest.createTestChannel(user);
        sles156Channel.setLabel("sles-15.6-channel");
        Channel sles157Channel = ChannelFactoryTest.createTestChannel(user);
        sles157Channel.setLabel("sles-15.7-channel");
        minion.addChannel(sles157Channel);
        ServerFactory.save(minion);

        DistUpgradeChannelTask subscribeTask = new DistUpgradeChannelTask();
        subscribeTask.setChannel(sles157Channel);
        subscribeTask.setTask(DistUpgradeChannelTask.SUBSCRIBE);
        details.addChannelTask(subscribeTask);

        DistUpgradeChannelTask unsubscribeTask = new DistUpgradeChannelTask();
        unsubscribeTask.setChannel(sles156Channel);
        unsubscribeTask.setTask(DistUpgradeChannelTask.UNSUBSCRIBE);
        details.addChannelTask(unsubscribeTask);

        Map<Long, DistUpgradeActionDetails> detailsMap = new HashMap<>();
        detailsMap.put(minion.getId(), details);

        // Schedule the main action
        DistUpgradeAction action = (DistUpgradeAction) new ActionBuilder()
                .ofType(ActionTypeEnum.TYPE_DIST_UPGRADE)
                .withSchedulerUser(user)
                .withName("Distupgrade")
                .withEarliest(new Date())
                .build();
        ActionFactory.save(action);

        detailsMap.values().stream()
                .map(DistUpgradeActionDetails::getServer)
                .forEach(server -> ServerActionFactory.createAddServerAction(server, action));
        action.setDetailsMap(detailsMap);
        ActionFactory.save(action);

        TestUtils.flushSession();
    }
}
