/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.scc.proxy;

import com.suse.scc.model.SCCRegisterSystemItem;
import com.suse.scc.model.SCCVirtualizationHostJson;
import com.suse.utils.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Business logic to manage SCC proxy in hub
 */
public class SCCProxyManager {

    private final SCCProxyFactory sccProxyFactory;

    /**
     * Default constructor
     */
    public SCCProxyManager() {
        this(new SCCProxyFactory());
    }

    /**
     * Builds an instance with the given dependencies
     *
     * @param sccProxyFactoryIn the sccProxyFactory factory
     */
    public SCCProxyManager(SCCProxyFactory sccProxyFactoryIn) {
        this.sccProxyFactory = sccProxyFactoryIn;
    }

    /**
     * Creates systems from the lists and stores data to be later registered to SCC
     *
     * @param systemsList a list of {@link SCCRegisterSystemItem} to be created
     * @param peripheralFqdnIn the fqdn of the peripheral
     * @return list of corresponding generated records
     */
    public List<SCCProxyRecord> createOrUpdateSystems(List<SCCRegisterSystemItem> systemsList,
                                                      String peripheralFqdnIn) {

        List<SCCProxyRecord> systemsRecords = new ArrayList<>();

        for (SCCRegisterSystemItem system : systemsList) {
            String sccLogin = system.getLogin();
            String sccPasswd = system.getPassword();

            if (system.isOnlyLastSeenAt()) {
                //update last seen at: record MUST be present
                Optional<SCCProxyRecord> maybeSccProxyRecord = sccProxyFactory
                        .lookupBySccLoginAndPassword(sccLogin, sccPasswd);

                maybeSccProxyRecord.ifPresent(sccProxyRecord -> {
                    sccProxyRecord.setLastSeenAt(system.getLastSeenAt());
                    sccProxyFactory.save(sccProxyRecord);
                    systemsRecords.add(sccProxyRecord);
                });
            }
            else {
                //full creation or update
                SCCProxyRecord sccProxyRecord = sccProxyFactory
                        .lookupBySccLoginAndPassword(sccLogin, sccPasswd)
                        .orElse(new SCCProxyRecord(peripheralFqdnIn, sccLogin, sccPasswd));
                String sccCreationJson = Json.GSON.toJson(system);
                sccProxyRecord.setSccCreationJson(sccCreationJson);
                sccProxyRecord.setStatus(SccProxyStatus.SCC_CREATION_PENDING);
                sccProxyFactory.save(sccProxyRecord);
                systemsRecords.add(sccProxyRecord);
            }
        }

        return systemsRecords;
    }

    /**
     * Marks a system for deletion
     *
     * @param systemProxyId the proxy id of the system
     * @return false if the id was not found
     */
    public boolean deleteSystem(long systemProxyId) {
        Optional<SCCProxyRecord> proxyRecord = sccProxyFactory.lookupByProxyId(systemProxyId);

        if (proxyRecord.isPresent()) {
            SCCProxyRecord sccProxyRecord = proxyRecord.get();
            sccProxyRecord.setStatus(SccProxyStatus.SCC_REMOVAL_PENDING);
            sccProxyFactory.save(sccProxyRecord);
            return true;
        }

        return false; //not found
    }

    /**
     * Store virtual host systems to forward it later to SCC
     *
     * @param virtHostsList the list of virtual hosts
     * @param peripheralFqdn the peripheral FQDN
     */
    public void setVirtualizationHosts(List<SCCVirtualizationHostJson> virtHostsList, String peripheralFqdn) {
        for (SCCVirtualizationHostJson host : virtHostsList) {
            String sccLogin = host.getIdentifier();
            String sccCreationJson = Json.GSON.toJson(host);

            Optional<SCCProxyRecord> optProxyRecord = sccProxyFactory.lookupBySccLoginAndStatus(
                    sccLogin, SccProxyStatus.SCC_VIRTHOST_PENDING);
            SCCProxyRecord proxyRecord = optProxyRecord
                    .map(r -> {
                        r.setPeripheralFqdn(peripheralFqdn);
                        r.setSccCreationJson(sccCreationJson);
                        r.setSccRegistrationErrorTime(null);
                        return r;
                    })
                    .orElse(new SCCProxyRecord(peripheralFqdn, sccLogin, null, sccCreationJson,
                            SccProxyStatus.SCC_VIRTHOST_PENDING));
            sccProxyFactory.save(proxyRecord);
        }
    }
}
