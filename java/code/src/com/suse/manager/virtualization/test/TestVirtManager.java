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
package com.suse.manager.virtualization.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.redhat.rhn.domain.server.MinionServer;
import com.suse.manager.virtualization.GuestDefinition;
import com.suse.manager.virtualization.PoolCapabilitiesJson;
import com.suse.manager.virtualization.PoolDefinition;
import com.suse.manager.webui.services.iface.VirtManager;

import java.util.Map;
import java.util.Optional;

public class TestVirtManager implements VirtManager {

    @Override
    public Optional<GuestDefinition> getGuestDefinition(String minionId, String domainName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Map<String, JsonElement>> getCapabilities(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<PoolCapabilitiesJson> getPoolCapabilities(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<PoolDefinition> getPoolDefinition(String minionId, String poolName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, JsonObject> getNetworks(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, JsonObject> getPools(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Map<String, JsonObject>> getVolumes(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateLibvirtEngine(MinionServer minion) {
        throw new UnsupportedOperationException();
    }
}
