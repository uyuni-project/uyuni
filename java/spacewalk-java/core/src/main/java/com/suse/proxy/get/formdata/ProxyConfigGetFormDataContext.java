/*
 * Copyright (c) 2025 SUSE LLC
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

package com.suse.proxy.get.formdata;

import com.redhat.rhn.common.UyuniErrorReport;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;

import com.suse.proxy.model.ProxyConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class holds relevant data through the steps for acquiring the form data for the proxy config.
 */
public class ProxyConfigGetFormDataContext {

    private final UyuniErrorReport errorReport = new UyuniErrorReport();
    private final SystemEntitlementManager systemEntitlementManager;
    private final User user;
    private final Server server;
    private final ProxyConfig proxyConfig;

    private final Map<String, Object> proxyConfigAsMap = new HashMap<>();
    private final List<String> electableParentsFqdn = new ArrayList<>();
    private String registryUrlExample;
    private String registryTagExample;
    private boolean hasCertificates = false;
    private Set<Channel> subscribableChannels = new HashSet<>();

    /**
     * Constructor
     *
     * @param userIn        the user
     * @param serverIn      the server
     * @param proxyConfigIn the current proxy configuration
     * @param systemEntitlementManagerIn the system entitlement manager
     */
    public ProxyConfigGetFormDataContext(
            User userIn,
            Server serverIn,
            ProxyConfig proxyConfigIn,
            SystemEntitlementManager systemEntitlementManagerIn
    ) {
        this.user = userIn;
        this.server = serverIn;
        this.proxyConfig =  proxyConfigIn;
        this.systemEntitlementManager = systemEntitlementManagerIn;
    }

    public User getUser() {
        return user;
    }

    public Server getServer() {
        return server;
    }

    public Map<String, Object> getProxyConfigAsMap() {
        return proxyConfigAsMap;
    }

    public List<String> getElectableParentsFqdn() {
        return electableParentsFqdn;
    }

    public ProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    public String getRegistryUrlExample() {
        return registryUrlExample;
    }

    public void setRegistryUrlExample(String registryUrlExampleIn) {
        registryUrlExample = registryUrlExampleIn;
    }

    public String getRegistryTagExample() {
        return registryTagExample;
    }

    public void setRegistryTagExample(String registryTagExampleIn) {
        registryTagExample = registryTagExampleIn;
    }

    /**
     * Returns true if the proxy configuration has certificates.
     *
     * @return true if the proxy configuration has certificates
     */
    public boolean hasCertificates() {
        return hasCertificates;
    }

    public void setHasCertificates(boolean hasCertificatesIn) {
        hasCertificates = hasCertificatesIn;
    }

    public UyuniErrorReport getErrorReport() {
        return errorReport;
    }

    public SystemEntitlementManager getSystemEntitlementManager() {
        return systemEntitlementManager;
    }

    public Set<Channel> getSubscribableChannels() {
        return subscribableChannels;
    }

    public void setSubscribableChannels(Set<Channel> subscribableChannelsIn) {
        subscribableChannels = subscribableChannelsIn;
    }
}
