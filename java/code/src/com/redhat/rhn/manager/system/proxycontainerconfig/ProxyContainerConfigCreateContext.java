/*
 * Copyright (c) 2024 SUSE LLC
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

package com.redhat.rhn.manager.system.proxycontainerconfig;

import com.redhat.rhn.common.client.ClientCertificate;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;

import com.suse.manager.ssl.SSLCertData;
import com.suse.manager.ssl.SSLCertManager;
import com.suse.manager.ssl.SSLCertPair;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Object containing variables required for creating a proxy container configuration
 */
public class ProxyContainerConfigCreateContext {

    private static final Logger LOG = LogManager.getLogger(ProxyContainerConfigCreateContext.class);

    // provided
    private final SaltApi saltApi;
    private final User user;
    private final SystemEntitlementManager systemEntitlementManager;
    private final String serverFqdn;
    private final String proxyFqdn;
    private final Integer proxyPort;
    private final Long maxCache;
    private final String email;
    private final String rootCA;
    private final List<String> intermediateCAs;
    private final SSLCertPair proxyCertKey;
    private final SSLCertPair caPair;
    private final String caPassword;
    private final SSLCertData certData;
    private final SSLCertManager certManager;

    // computed
    private MgrUtilRunner.SshKeygenResult proxySshKey;
    private String serverSshPublicKey;
    private SSLCertPair proxyPair;
    private String rootCaCert;
    private ClientCertificate clientCertificate;
    private String certificate;

    // output
    private final Map<String, Object> configMap = new HashMap<>();
    private final Map<String, Object> sshConfigMap = new HashMap<>();
    private final Map<String, Object> httpConfigMap = new HashMap<>();
    private byte[] configTar;

    ProxyContainerConfigCreateContext(
            SaltApi saltApiIn, User userIn, SystemEntitlementManager systemEntitlementManagerIn, String serverFqdnIn,
            String proxyFqdnIn, Integer proxyPortIn, Long maxCacheIn, String emailIn, String rootCAIn,
            List<String> intermediateCAsIn, SSLCertPair proxyCertKeyIn, SSLCertPair caPairIn, String caPasswordIn,
            SSLCertData certDataIn, SSLCertManager certManagerIn
    ) {
        saltApi = saltApiIn;
        user = userIn;
        systemEntitlementManager = systemEntitlementManagerIn;
        serverFqdn = serverFqdnIn;
        proxyFqdn = proxyFqdnIn;
        proxyPort = proxyPortIn;
        maxCache = maxCacheIn;
        email = emailIn;
        rootCA = rootCAIn;
        intermediateCAs = intermediateCAsIn;
        proxyCertKey = proxyCertKeyIn;
        caPair = caPairIn;
        caPassword = caPasswordIn;
        certData = certDataIn;
        certManager = certManagerIn;
    }

    public SaltApi getSaltApi() {
        return saltApi;
    }

    public User getUser() {
        return user;
    }

    public SystemEntitlementManager getSystemEntitlementManager() {
        return systemEntitlementManager;
    }

    public String getServerFqdn() {
        return serverFqdn;
    }

    public String getProxyFqdn() {
        return proxyFqdn;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public Long getMaxCache() {
        return maxCache;
    }

    public String getEmail() {
        return email;
    }

    public String getRootCA() {
        return rootCA;
    }

    public List<String> getIntermediateCAs() {
        return intermediateCAs;
    }

    public SSLCertPair getProxyCertKey() {
        return proxyCertKey;
    }

    public SSLCertPair getCaPair() {
        return caPair;
    }

    public String getCaPassword() {
        return caPassword;
    }

    public SSLCertData getCertData() {
        return certData;
    }

    public Map<String, Object> getConfigMap() {
        return configMap;
    }

    public Map<String, Object> getSshConfigMap() {
        return sshConfigMap;
    }

    public Map<String, Object> getHttpConfigMap() {
        return httpConfigMap;
    }

    public byte[] getConfigTar() {
        return configTar;
    }

    public void setConfigTar(byte[] configTarIn) {
        configTar = configTarIn;
    }

    public String getRootCaCert() {
        return rootCaCert;
    }

    public void setRootCaCert(String rootCaCertIn) {
        rootCaCert = rootCaCertIn;
    }

    public SSLCertPair getProxyPair() {
        return proxyPair;
    }

    public void setProxyPair(SSLCertPair proxyPairIn) {
        proxyPair = proxyPairIn;
    }

    public String getServerSshPublicKey() {
        return serverSshPublicKey;
    }

    public void setServerSshPublicKey(String serverSshPublicKeyIn) {
        serverSshPublicKey = serverSshPublicKeyIn;
    }

    public MgrUtilRunner.SshKeygenResult getProxySshKey() {
        return proxySshKey;
    }

    public void setProxySshKey(MgrUtilRunner.SshKeygenResult proxySshKeyIn) {
        proxySshKey = proxySshKeyIn;
    }

    public ClientCertificate getClientCertificate() {
        return clientCertificate;
    }

    public void setClientCertificate(ClientCertificate clientCertificateIn) {
        clientCertificate = clientCertificateIn;
    }

    public void setCertificate(String certificateIn) {
        certificate = certificateIn;
    }

    public String getCertificate() {
        return certificate;
    }

    public SSLCertManager getCertManager() {
        return certManager;
    }
}
