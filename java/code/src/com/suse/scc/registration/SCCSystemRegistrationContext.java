/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.scc.registration;

import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;

import com.suse.scc.SCCSystemId;
import com.suse.scc.client.SCCClient;
import com.suse.scc.model.SCCRegisterSystemJson;
import com.suse.scc.model.SCCSystemCredentialsJson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SCCSystemRegistrationContext {

    private final SCCClient sccClient;
    private final List<SCCRegCacheItem> items;
    private final SCCCredentials primaryCredential;

    private final Map<SCCSystemId, SCCRegCacheItem> itemsBySccSystemId;
    private final Map<SCCSystemId, SCCRegisterSystemJson> pendingRegistrationSystems;

    private final List<SCCRegCacheItem> paygSystems;

    private final List<SCCSystemCredentialsJson> registeredSystems;

    /**
     * Constructor
     * @param sccClientIn the scc client
     * @param itemsIn the items
     * @param primaryCredentialIn the primary credential
     */
    public SCCSystemRegistrationContext(
            SCCClient sccClientIn,
            List<SCCRegCacheItem> itemsIn,
            SCCCredentials primaryCredentialIn
    ) {
        this.sccClient = sccClientIn;
        this.items = itemsIn;
        this.primaryCredential = primaryCredentialIn;

        this.itemsBySccSystemId = new HashMap<>();
        this.pendingRegistrationSystems = new HashMap<>();
        this.registeredSystems = new ArrayList<>();
        this.paygSystems = new ArrayList<>();
    }

    public SCCClient getSccClient() {
        return sccClient;
    }

    public List<SCCRegCacheItem> getItems() {
        return items;
    }

    public SCCCredentials getPrimaryCredential() {
        return primaryCredential;
    }

    public Map<SCCSystemId, SCCRegCacheItem> getItemsBySccSystemId() {
        return itemsBySccSystemId;
    }

    public Map<SCCSystemId, SCCRegisterSystemJson> getPendingRegistrationSystems() {
        return pendingRegistrationSystems;
    }

    public List<SCCSystemCredentialsJson> getRegisteredSystems() {
        return registeredSystems;
    }

    /**
     * Set item default values for whenever they are consideres as not requiring registration..
     * @param item the item to set
     */
    public void setItemAsNonRegistrationRequiredItem(SCCRegCacheItem item) {
        item.setSccRegistrationRequired(false);
        item.setRegistrationErrorTime(null);
        item.setCredentials(primaryCredential);
    }

    public List<SCCRegCacheItem> getPaygSystems() {
        return paygSystems;
    }
}
