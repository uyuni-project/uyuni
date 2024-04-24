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

import com.redhat.rhn.common.conf.ConfigDefaults;

import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible for organizing the contents for the proxy container configuration files .
 */
public class ProxyContainerConfigCreateGenerateFileMaps implements ProxyContainerConfigCreateContextHandler {
    @Override
    public void handle(ProxyContainerConfigCreateContext context) {
        // config.yaml
        context.getConfigMap().put("server", context.getServerFqdn());
        context.getConfigMap().put("max_cache_size_mb", context.getMaxCache());
        context.getConfigMap().put("email", context.getEmail());
        context.getConfigMap().put("server_version", ConfigDefaults.get().getProductVersion());
        context.getConfigMap().put("proxy_fqdn", context.getProxyFqdn());
        context.getConfigMap().put("ca_crt", context.getRootCaCert());

        // httpd.yaml
        Map<String, Object> httpdConfig = new HashMap<>();
        httpdConfig.put("system_id", context.getClientCertificate().asXml());
        httpdConfig.put("server_crt", context.getCertificate());
        httpdConfig.put("server_key", context.getProxyPair().getKey());
        context.getHttpConfigMap().put("httpd", httpdConfig);

        // ssh.yaml
        Map<String, Object> sshConfig = new HashMap<>();
        sshConfig.put("server_ssh_key_pub", context.getServerSshPublicKey());
        sshConfig.put("server_ssh_push", context.getProxySshKey().getKey());
        sshConfig.put("server_ssh_push_pub", context.getProxySshKey().getPublicKey());
        context.getSshConfigMap().put("ssh", sshConfig);
    }
}
