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

package com.suse.proxy.update;

import com.redhat.rhn.common.UyuniErrorReport;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;
import com.suse.proxy.ProxyContainerImagesEnum;
import com.suse.proxy.RegistryUrl;
import com.suse.proxy.get.ProxyConfigGetFacade;
import com.suse.proxy.get.ProxyConfigGetFacadeImpl;
import com.suse.proxy.model.ProxyConfig;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * This class holds relevant data through the steps of the proxy config update process.
 */
public class ProxyConfigUpdateContext {

    // Provided data
    private final ProxyConfigUpdateJson request;
    private final UyuniErrorReport errorReport = new UyuniErrorReport();
    private final Map<ProxyContainerImagesEnum, RegistryUrl> registryUrls =
            new EnumMap<>(ProxyContainerImagesEnum.class);
    private final SystemManager systemManager;
    private final User user;
    private final ProxyConfigGetFacade proxyConfigGetFacade;

    // Acquired/computed data
    private String proxyFqdn;
    private String rootCA;
    private List<String> intermediateCAs;
    private String proxyCert;
    private String proxyKey;

    private MinionServer proxyMinion;
    private Server parentServer;
    private ProxyConfig proxyConfig;
    private Map<String, Object> proxyConfigFiles;

    private Pillar pillar;
    private Action action;

    /**
     * Constructor
     *
     * @param requestIn       the request
     * @param systemManagerIn the system manager
     * @param userIn          the user
     */
    public ProxyConfigUpdateContext(
            ProxyConfigUpdateJson requestIn,
            SystemManager systemManagerIn,
            User userIn
    ) {
        this(requestIn, systemManagerIn, userIn, new ProxyConfigGetFacadeImpl());
    }

    /**
     * Full params constructor
     *
     * @param requestIn              the request
     * @param systemManagerIn        the system manager
     * @param userIn                 the user
     * @param proxyConfigGetFacadeIn the proxy config get facade
     */
    public ProxyConfigUpdateContext(
            ProxyConfigUpdateJson requestIn,
            SystemManager systemManagerIn,
            User userIn,
            ProxyConfigGetFacade proxyConfigGetFacadeIn
    ) {
        this.request = requestIn;
        this.systemManager = systemManagerIn;
        this.user = userIn;
        this.proxyConfigGetFacade = proxyConfigGetFacadeIn;
    }

    public ProxyConfigUpdateJson getRequest() {
        return request;
    }

    public UyuniErrorReport getErrorReport() {
        return errorReport;
    }

    public String getProxyFqdn() {
        return proxyFqdn;
    }

    public void setProxyFqdn(String proxyFqdnIn) {
        this.proxyFqdn = proxyFqdnIn;
    }

    public Map<ProxyContainerImagesEnum, RegistryUrl> getRegistryUrls() {
        return registryUrls;
    }

    public MinionServer getProxyMinion() {
        return proxyMinion;
    }

    public void setProxyMinion(MinionServer minionServerIn) {
        this.proxyMinion = minionServerIn;
    }

    public Server getParentServer() {
        return parentServer;
    }

    public void setParentServer(Server server) {
        this.parentServer = server;
    }

    public ProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    public void setProxyConfig(ProxyConfig proxyConfigIn) {
        this.proxyConfig = proxyConfigIn;
    }

    public SystemManager getSystemManager() {
        return systemManager;
    }

    public User getUser() {
        return user;
    }

    public Map<String, Object> getProxyConfigFiles() {
        return proxyConfigFiles;
    }

    public void setProxyConfigFiles(Map<String, Object> proxyConfigFilesIn) {
        this.proxyConfigFiles = proxyConfigFilesIn;
    }

    public String getRootCA() {
        return rootCA;
    }

    public void setRootCA(String rootCAIn) {
        rootCA = rootCAIn;
    }

    public List<String> getIntermediateCAs() {
        return intermediateCAs;
    }

    public void setIntermediateCAs(List<String> intermediateCAsIn) {
        intermediateCAs = intermediateCAsIn;
    }

    public String getProxyCert() {
        return proxyCert;
    }

    public void setProxyCert(String proxyCertIn) {
        proxyCert = proxyCertIn;
    }

    public String getProxyKey() {
        return proxyKey;
    }

    public void setProxyKey(String proxyKeyIn) {
        proxyKey = proxyKeyIn;
    }

    public Pillar getPillar() {
        return pillar;
    }

    public void setPillar(Pillar pillarIn) {
        pillar = pillarIn;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action actionIn) {
        action = actionIn;
    }

    public ProxyConfigGetFacade getProxyConfigGetFacade() {
        return proxyConfigGetFacade;
    }
}

