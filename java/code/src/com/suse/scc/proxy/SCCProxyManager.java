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

import com.suse.scc.model.SCCRegisterSystemJson;
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
     * @param systemsList a list of {@Link SCCRegisterSystemJson} to be created
     * @param peripheralFqdnIn the fqdn of the peripheral
     * @return list of corresponding generated records
     */
    public List<SCCProxyRecord> createSystems(List<SCCRegisterSystemJson> systemsList, String peripheralFqdnIn) {

        List<SCCProxyRecord> systemsRecords = new ArrayList<>();

        for (SCCRegisterSystemJson system : systemsList) {
            String sccLogin = system.getLogin();
            String sccPasswd = system.getPassword();
            String sccCreationJson = Json.GSON.toJson(system);

            SCCProxyRecord sccProxyRecord = new SCCProxyRecord(peripheralFqdnIn, sccLogin, sccPasswd, sccCreationJson);
            sccProxyFactory.save(sccProxyRecord);
            systemsRecords.add(sccProxyRecord);
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
}
