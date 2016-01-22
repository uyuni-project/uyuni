/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.reactor;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.impl.SaltAPIService;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Event handler to create system records for salt minions.
 */
public class RegisterMinionAction extends AbstractDatabaseAction {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(RegisterMinionAction.class);

    // Reference to the SaltService instance
    private final SaltService SALT_SERVICE;


    //HACK: set installed product depending on the grains
    // to get access to suse channels
    private final HashMap<String, Long> productIdMap = new HashMap<>();

    /**
     * Default constructor.
     */
    public RegisterMinionAction() {
        this(SaltAPIService.INSTANCE);
    }

    /**
     * Constructor taking a {@link SaltService} instance.
     *
     * @param saltService the salt service to use
     */
    public RegisterMinionAction(SaltService saltService) {
        SALT_SERVICE = saltService;
        productIdMap.put("SLES12x86_64", 1117L);
        productIdMap.put("SLES12.1x86_64", 1322L);
        productIdMap.put("SLES11x86_64", 824L);
        productIdMap.put("SLES11.1x86_64", 769L);
        productIdMap.put("SLES11.2x86_64", 690L);
        productIdMap.put("SLES11.3x86_64", 814L);
        productIdMap.put("SLES11.4x86_64", 1300L);
    }

    /**
     * {@inheritDoc}
     */
    public void doExecute(EventMessage msg) {
        RegisterMinionEvent event = (RegisterMinionEvent) msg;
        String minionId = event.getMinionId();

        // Match minions via their machine id
        String machineId = SALT_SERVICE.getMachineId(minionId);
        if (machineId == null) {
            LOG.info("Cannot find machine id for minion: " + minionId);
            return;
        }
        Optional<MinionServer> optMinion = MinionServerFactory.findByMachineId(machineId);
        if (optMinion.isPresent()) {
            MinionServer registeredMinion = optMinion.get();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Minion already registered, updating profile: " +
                        minionId + " [" + machineId + "]");
            }
            if (!minionId.equals(registeredMinion.getName())) {
                registeredMinion.setName(minionId);
                registeredMinion.setMinionId(minionId);
                ServerFactory.save(registeredMinion);
            }
            return;
        }
        try {
            // Create the server
            MinionServer server = new MinionServer();
            server.setMachineId(machineId);
            server.setMinionId(minionId);
            server.setName(minionId);
            server.setDigitalServerId(machineId);

            // All registered minions initially belong to the default organization
            server.setOrg(OrgFactory.getSatelliteOrg());

            SALT_SERVICE.syncGrains(minionId);
            SALT_SERVICE.syncModules(minionId);

            ValueMap grains = new ValueMap(SALT_SERVICE.getGrains(minionId));

            String osfullname = grains.getValueAsString("osfullname");
            String osrelease = grains.getValueAsString("osrelease");
            String kernelrelease = grains.getValueAsString("kernelrelease");
            String osarch = grains.getValueAsString("osarch");

            server.setOs(osfullname);
            server.setRelease(osrelease);
            server.setRunningKernel(kernelrelease);
            server.setSecret(RandomStringUtils.randomAlphanumeric(64));
            server.setAutoUpdate("N");
            server.setLastBoot(System.currentTimeMillis() / 1000);
            server.setCreated(new Date());
            server.setModified(server.getCreated());
            server.setContactMethod(ServerFactory.findContactMethodByLabel("default"));
            server.setServerArch(
                    ServerFactory.lookupServerArchByLabel(osarch + "-redhat-linux"));

            server.updateServerInfo();

            mapHardwareGrains(server, grains);

            //HACK: set installed product depending on the grains
            // to get access to suse channels
            String key = osfullname + osrelease + osarch;
            Optional.ofNullable(productIdMap.get(key)).ifPresent(productId -> {
                SUSEProduct product =  SUSEProductFactory.lookupByProductId(productId);
                if (product != null) {
                    // Insert into suseInstalledProduct
                    InstalledProduct prd = new InstalledProduct();
                    prd.setName(product.getName());
                    prd.setVersion(product.getVersion());
                    prd.setRelease(product.getRelease());
                    prd.setArch(product.getArch());
                    prd.setBaseproduct(true);

                    Set<InstalledProduct> products = new HashSet<>();
                    products.add(prd);

                    // Insert into suseServerInstalledProduct
                    server.setInstalledProducts(products);
                }
            });

            ServerFactory.save(server);

            triggerGetHardwareInfo(server, grains);
            triggerGetNetworkInfo(server, grains);

            // Assign the SaltStack base entitlement by default
            server.setBaseEntitlement(
                    EntitlementManager.getByName(EntitlementManager.SALTSTACK_ENTITLED));

            Map<String, String> data = new HashMap<>();
            data.put("minionId", minionId);
            data.put("machineId", machineId);
            SALT_SERVICE.sendEvent("susemanager/minion/registered", data);
            LOG.info("Finished minion registration: " + minionId);

            // Trigger certification deployment
            MessageQueue.publish(
                    new StateDirtyEvent(server.getId(), null, StateDirtyEvent.CERTIFICATE));
            // Trigger an update of the package profile
            MessageQueue.publish(new UpdatePackageProfileEventMessage(server.getId()));
        }
        catch (Throwable t) {
            LOG.error("Error registering minion for event: " + event, t);
        }
    }

    private void mapHardwareGrains(MinionServer server, ValueMap grains) {
        // for efficiency do this here
        server.setRam(grains.getValueAsLong("mem_total").orElse(0L));
    }

    private void triggerGetNetworkInfo(MinionServer server, ValueMap grains) {
        MessageQueue.publish(
               new GetNetworkInfoEventMessage(server.getId(), grains));
    }

    private void triggerGetHardwareInfo(MinionServer server, ValueMap grains) {
        MessageQueue.publish(new GetHardwareInfoEventMessage(server.getId()));
    }

}
