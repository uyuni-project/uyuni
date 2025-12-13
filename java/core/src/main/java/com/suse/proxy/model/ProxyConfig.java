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

package com.suse.proxy.model;

import java.util.List;

/**
 * Class representing the proxy configuration
 */
public class ProxyConfig {

    private Long serverId;
    private String proxyFqdn;
    private String parentFqdn;
    private Integer proxyPort;
    private Integer maxCache;
    private String email;

    private String rootCA;
    private List<String> intermediateCAs;
    private String proxyCert;
    private String proxyKey;

    private String proxySshPub;
    private String proxySshPriv;
    private String parentSshPub;

    private ProxyConfigImage httpdImage;
    private ProxyConfigImage saltBrokerImage;
    private ProxyConfigImage squidImage;
    private ProxyConfigImage sshImage;
    private ProxyConfigImage tftpdImage;

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverIdIn) {
        serverId = serverIdIn;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPortIn) {
        proxyPort = proxyPortIn;
    }

    public String getProxyFqdn() {
        return proxyFqdn;
    }

    public void setProxyFqdn(String proxyFqdnIn) {
        proxyFqdn = proxyFqdnIn;
    }

    public String getParentFqdn() {
        return parentFqdn;
    }

    public void setParentFqdn(String parentFqdnIn) {
        parentFqdn = parentFqdnIn;
    }

    public Integer getMaxCache() {
        return maxCache;
    }

    public void setMaxCache(Integer maxCacheIn) {
        maxCache = maxCacheIn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String emailIn) {
        email = emailIn;
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

    public String getProxySshPub() {
        return proxySshPub;
    }

    public void setProxySshPub(String proxySshPubIn) {
        proxySshPub = proxySshPubIn;
    }

    public String getProxySshPriv() {
        return proxySshPriv;
    }

    public void setProxySshPriv(String proxySshPrivIn) {
        proxySshPriv = proxySshPrivIn;
    }

    public String getParentSshPub() {
        return parentSshPub;
    }

    public void setParentSshPub(String parentSshPubIn) {
        parentSshPub = parentSshPubIn;
    }

    public ProxyConfigImage getHttpdImage() {
        return httpdImage;
    }

    public void setHttpdImage(ProxyConfigImage httpdImageIn) {
        httpdImage = httpdImageIn;
    }

    public ProxyConfigImage getSaltBrokerImage() {
        return saltBrokerImage;
    }

    public void setSaltBrokerImage(ProxyConfigImage saltBrokerImageIn) {
        saltBrokerImage = saltBrokerImageIn;
    }

    public ProxyConfigImage getSquidImage() {
        return squidImage;
    }

    public void setSquidImage(ProxyConfigImage squidImageIn) {
        squidImage = squidImageIn;
    }

    public ProxyConfigImage getSshImage() {
        return sshImage;
    }

    public void setSshImage(ProxyConfigImage sshImageIn) {
        sshImage = sshImageIn;
    }

    public ProxyConfigImage getTftpdImage() {
        return tftpdImage;
    }

    public void setTftpdImage(ProxyConfigImage tftpdImageIn) {
        tftpdImage = tftpdImageIn;
    }
}
