package com.suse.manager.reactor.messaging.test;

import static java.util.Arrays.asList;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.distupgrade.test.DistUpgradeManagerTest;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.reactor.messaging.ImageDeployedEventMessage;
import com.suse.manager.reactor.messaging.ImageDeployedEventMessageAction;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.salt.ImageDeployedEvent;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Grains;
import com.suse.salt.netapi.calls.modules.Zypper;
import com.suse.salt.netapi.parser.JsonParser;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Test for {@link com.suse.manager.webui.utils.salt.ImageDeployedEvent}
 */
public class ImageDeployedEventMessageActionTest extends JMockBaseTestCaseWithUser {

    // Fixed test parameters
    private MinionServer testMinion;
    private Channel baseChannelX8664;
    private Map<String, Object> grains;

    // Mocks
    private SaltService saltMock;
    private TaskomaticApi taskomaticMock;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);

        saltMock = mock(SaltService.class);
        taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);

        // setup a minion
        testMinion = MinionServerFactoryTest.createTestMinionServer(user);
        testMinion.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        grains = getGrains();

        // setup channels & product
        ChannelFamily channelFamily = ErrataTestUtils.createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        baseChannelX8664 = setupBaseAndRequiredChannels(channelFamily, product);

        context().checking(new Expectations() {{
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));

            allowing(saltMock).getGrains(testMinion.getMinionId());
            will(returnValue(grains));

            List<Zypper.ProductInfo> pil = new ArrayList<>();
            Zypper.ProductInfo pi = new Zypper.ProductInfo(
                    product.getName(),
                    product.getArch().getLabel(), "descr", "eol", "epoch", "flavor",
                    true, true, "productline", Optional.of("registerrelease"),
                    "test", "repo", "shortname", "summary", "vendor",
                    product.getVersion());
            pil.add(pi);
            allowing(saltMock).callSync(
                    with(any(LocalCall.class)),
                    with(any(String.class)));
            will(returnValue(Optional.of(pil)));
        }});
    }

    /**
     * Happy path scenario: machine_id grain is present.
     * In this case we test that at the end of the Action, the minion has correct channels
     * (based on its product) assigned.
     */
    public void testChannelsAssigned() {
        grains.put("machine_id", testMinion.getMachineId());

        ImageDeployedEvent event = new ImageDeployedEvent(new ValueMap(grains));
        ImageDeployedEventMessageAction action = new ImageDeployedEventMessageAction(saltMock);
        EventMessage message = new ImageDeployedEventMessage(event);
        action.execute(message);

        assertEquals(baseChannelX8664, testMinion.getBaseChannel());
    }

    /**
     * Happy path scenario: machine_id grain is present.
     * In this case we test that at the end of the Action, the minion has correct channels
     * (based on its product) assigned. Old channel assignments will be overridden.
     */
    public void testBaseChannelChanged() throws Exception {
        grains.put("machine_id", testMinion.getMachineId());

        Channel oldBase = ChannelTestUtils.createBaseChannel(user);
        oldBase.setChannelArch(ChannelFactory.lookupArchByName("x86_64"));
        SystemManager.subscribeServerToChannel(user, testMinion, oldBase);
        Channel oldChild = ChannelTestUtils.createChildChannel(user, oldBase);
        oldChild.setChannelArch(ChannelFactory.lookupArchByName("x86_64"));
        SystemManager.subscribeServerToChannel(user, testMinion, oldChild);
        System.out.println(testMinion.getChannels());

        ImageDeployedEvent event = new ImageDeployedEvent(new ValueMap(grains));
        ImageDeployedEventMessageAction action = new ImageDeployedEventMessageAction(saltMock);
        EventMessage message = new ImageDeployedEventMessage(event);
        action.execute(message);

        assertTrue(testMinion.getChannels().contains(baseChannelX8664));
        assertFalse(testMinion.getChannels().removeAll(asList(oldBase, oldChild)));
    }

    /**
     * machine_id grain is missing -> no channels should be assigned
     */
    public void testMachineIdMissing() {
        ImageDeployedEvent event = new ImageDeployedEvent(new ValueMap(grains));
        ImageDeployedEventMessageAction action = new ImageDeployedEventMessageAction(saltMock);
        EventMessage message = new ImageDeployedEventMessage(event);
        action.execute(message);

        assertNull(testMinion.getBaseChannel());
    }

    private Channel setupBaseAndRequiredChannels(ChannelFamily channelFamily,
            SUSEProduct product)
        throws Exception {
        ChannelProduct channelProduct = ErrataTestUtils.createTestChannelProduct();
        ChannelArch channelArch = ChannelFactory.findArchByLabel("channel-x86_64");
        Channel baseChannelX8664 = DistUpgradeManagerTest
                .createTestBaseChannel(channelFamily, channelProduct, channelArch);
        SUSEProductTestUtils.createTestSUSEProductChannel(baseChannelX8664, product, true);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user, "channel-x86_64");
        Channel channel3 = ChannelFactoryTest.createTestChannel(user, "channel-x86_64");
        channel2.setParentChannel(baseChannelX8664);
        channel3.setParentChannel(baseChannelX8664);
        SUSEProductTestUtils.createTestSUSEProductChannel(channel2, product, true);
        SUSEProductTestUtils.createTestSUSEProductChannel(channel3, product, true);
        return baseChannelX8664;
    }

    private Map<String, Object> getGrains() throws ClassNotFoundException, IOException {
        String grainLines = Files.lines(new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/test/dummy_grains.json").getPath()
        ).toPath()).collect(Collectors.joining("\n"));
        return new JsonParser<>(Grains.items(false).getReturnType()).parse(grainLines);
    }
}