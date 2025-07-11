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

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

import com.suse.proxy.model.ProxyConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class holds relevant data through the steps for acquiding the form data for the proxy config.
 */
public class ProxyConfigGetFormDataContext {

    private final User user;
    private final Server server;
    private final ProxyConfig proxyConfig;

    private final Map<String, Object> proxyConfigAsMap = new HashMap<>();
    private final List<String> electableParentsFqdn = new ArrayList<>();
    private String initFailMessage;
    private String registryUrlExample;
    private String registryTagExample;

    /**
     * Constructor
     *
     * @param userIn        the user
     * @param serverIn      the server
     * @param proxyConfigIn the current proxy configuration
     */
    public ProxyConfigGetFormDataContext(User userIn, Server serverIn, ProxyConfig proxyConfigIn) {
        this.user = userIn;
        this.server = serverIn;
        this.proxyConfig =  proxyConfigIn;
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

    public void setInitFailMessage(String initFailMessageIn) {
        this.initFailMessage = initFailMessageIn;
    }

    public ProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    public String getInitFailMessage() {
        return initFailMessage;
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
}
