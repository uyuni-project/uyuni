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

import static java.util.Arrays.asList;

import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;

import com.suse.scc.client.SCCClient;
import com.suse.scc.proxy.SCCProxyRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom class that handles system registry in SCC.
 */
public class SCCSystemRegistration {

    protected final List<SCCSystemRegistrationContextHandler> contextHandlerChain = new ArrayList<>();
    protected final List<SCCSystemRegistrationContextHandler> proxyContextHandlerChain = new ArrayList<>();

    /**
     * Constructor
     */
    public SCCSystemRegistration() {
        this.contextHandlerChain.addAll(asList(
                new SCCSystemRegistrationSystemDataAcquisitor(),
                new SCCSystemRegistrationCreateUpdateSystems(),
                new SCCSystemRegistrationUpdateCachedItems()
        ));
        this.proxyContextHandlerChain.addAll(asList(
                new SCCProxyRegistrationCreateUpdateSystems(),
                new SCCProxyRegistrationUpdateProxyRecords()
        ));
    }

    /**
     * Registers the given items in SCC
     *
     * @param sccClient         the SCC client
     * @param items             the items to register
     * @param primaryCredential the current primary organization credential
     */
    public void register(SCCClient sccClient, List<SCCRegCacheItem> items, SCCCredentials primaryCredential) {
        SCCSystemRegistrationContext context = new SCCSystemRegistrationContext(sccClient, items, primaryCredential);
        for (SCCSystemRegistrationContextHandler handler : contextHandlerChain) {
            handler.handle(context);
        }
    }

    /**
     * Registers the given items in SCC as hub being the SCC proxy for peripherals
     * NOTE: arguments have been shuffled on purpose,
     * to avoid "methods have the same erasure" error with above register method
     *
     * @param sccClient         the SCC client
     * @param proxyRecords      the items to register as hub being the SCC proxy for peripherals
     * @param primaryCredential the current primary organization credential
     */
    public void proxyRegister(SCCClient sccClient, List<SCCProxyRecord> proxyRecords, SCCCredentials primaryCredential) {
        SCCSystemRegistrationContext context =
                new SCCSystemRegistrationContext(sccClient, primaryCredential, proxyRecords);
        for (SCCSystemRegistrationContextHandler handler : proxyContextHandlerChain) {
            handler.handle(context);
        }
    }
}
