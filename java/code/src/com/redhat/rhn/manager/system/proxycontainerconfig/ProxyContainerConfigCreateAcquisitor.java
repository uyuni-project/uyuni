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

import static com.suse.utils.Predicates.allProvided;
import static com.suse.utils.Predicates.isAbsent;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.ProxyInfo;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFQDN;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.SystemManagerUtils;
import com.redhat.rhn.manager.system.SystemsExistException;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;

import com.suse.manager.ssl.SSLCertPair;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Acquires and validates all necessary data for the Proxy container configuration creation files.
 */
public class ProxyContainerConfigCreateAcquisitor implements ProxyContainerConfigCreateContextHandler {
    private static final Logger LOG = LogManager.getLogger(SystemManagerUtils.class);

    private SaltApi saltApi;

    @Override
    public void handle(ProxyContainerConfigCreateContext context) {
        this.saltApi = context.getSaltApi();

        // Generate SSH keys for proxy
        MgrUtilRunner.SshKeygenResult proxySshKey = saltApi.generateSSHKey(null, null)
                .orElseThrow(raiseAndLog("Could not generate proxy salt-ssh SSH keys."));

        if (!(proxySshKey.getReturnCode() == 0 || proxySshKey.getReturnCode() == -1)) {
            throw raiseAndLog("Generating proxy salt-ssh SSH keys failed: " + proxySshKey.getStderr()).get();
        }

        context.setProxySshKey(proxySshKey);

        //config.yaml
        SSLCertPair proxyPair = context.getProxyCertKey();
        String rootCaCert = context.getRootCA();
        if (proxyPair == null || !proxyPair.isComplete()) {
            proxyPair = context.getCertManager().generateCertificate(
                    context.getCaPair(),
                    context.getCaPassword(),
                    context.getCertData()
            );
            rootCaCert = context.getCaPair().getCertificate();
        }

        context.setProxyPair(proxyPair);
        context.setRootCaCert(rootCaCert);

        //httpd.yaml
        Set<String> fqdns = new HashSet<>();
        fqdns.add(context.getProxyFqdn());

        SSLCertPair proxyCertKey = context.getProxyCertKey();
        if (proxyCertKey != null && proxyCertKey.getCertificate() != null) {
            // Get the cnames from the certificate using openssl
            fqdns.addAll(context.getCertManager().getNamesFromSslCert(proxyCertKey.getCertificate()));
        }
        else {
            fqdns.addAll(context.getCertData().getAllCnames());
        }

        Server proxySystem = getOrCreateProxySystem(
                context.getSystemEntitlementManager(),
                context.getUser(), context.getProxyFqdn(), fqdns, context.getProxyPort(), proxySshKey.getPublicKey()
        );
        try {
            context.setClientCertificate(SystemManager.createClientCertificate(proxySystem));
        }
        catch (InstantiationException e) {
            throw raiseAndLog("Failed creating client certificate: " + e.getMessage()).get();
        }

        // Check the SSL files using mgr-ssl-cert-setup
        try {
            context.setCertificate(saltApi.checkSSLCert(rootCaCert, proxyPair,
                    context.getIntermediateCAs() != null ? context.getIntermediateCAs() : List.of()));
        }
        catch (IllegalArgumentException err) {
            throw new RhnRuntimeException("Certificate check failure: " + err.getMessage());
        }

        //ssh.yaml
        context.setServerSshPublicKey(getServerSshPublicKey(context.getUser(), context.getServerFqdn()));
    }

    /**
     * Retrieves or create a proxy system
     *
     * @param systemEntitlementManager the system entitlement manager
     * @param creator                  the user creating the proxy system
     * @param proxyName                the FQDN of the proxy system
     * @param port                     the SSH port of the proxy system
     * @param sshPublicKey             the SSH public key of the proxy system
     * @return the proxy system
     */
    private Server getOrCreateProxySystem(
            SystemEntitlementManager systemEntitlementManager,
            User creator, String proxyName, Set<String> fqdns, Integer port, String sshPublicKey
    ) {
        Optional<Server> existing = findByAnyFqdn(fqdns);
        if (existing.isPresent()) {
            Server server = existing.get();
            if (!(server.hasEntitlement(EntitlementManager.FOREIGN) ||
                    server.hasEntitlement(EntitlementManager.SALT))) {
                throw new SystemsExistException(List.of(server.getId()));
            }
            // The SSH key is going to change remove it from the known hosts
            SystemManagerUtils.removeSaltSSHKnownHosts(saltApi, server);
            ProxyInfo info = server.getProxyInfo();
            if (info == null) {
                info = new ProxyInfo();
                info.setServer(server);
                server.setProxyInfo(info);
            }
            info.setSshPort(port);
            info.setSshPublicKey(sshPublicKey.getBytes());
            return server;
        }
        Server server = ServerFactory.createServer();
        server.setName(proxyName);
        server.setHostname(proxyName);
        server.getFqdns().addAll(fqdns.stream()
                .filter(fqdn -> !fqdn.contains("*"))
                .map(fqdn -> new ServerFQDN(server, fqdn)).collect(Collectors.toList()));
        server.setOrg(creator.getOrg());
        server.setCreator(creator);

        String uniqueId = SystemManagerUtils.createUniqueId(List.of(proxyName));
        server.setDigitalServerId(uniqueId);
        server.setMachineId(uniqueId);
        server.setOs("(unknown)");
        server.setRelease("(unknown)");
        server.setSecret(RandomStringUtils.random(64, 0, 0, true, true,
                null, new SecureRandom()));
        server.setAutoUpdate("N");
        server.setContactMethod(ServerFactory.findContactMethodByLabel("default"));
        server.setLastBoot(System.currentTimeMillis() / 1000);
        server.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        server.updateServerInfo();
        ServerFactory.save(server);

        ProxyInfo info = new ProxyInfo();
        info.setServer(server);
        info.setSshPort(port);
        info.setSshPublicKey(sshPublicKey.getBytes());
        server.setProxyInfo(info);

        systemEntitlementManager.setBaseEntitlement(server, EntitlementManager.FOREIGN);
        return server;
    }

    /**
     * Retrieves the specified server's SSH public key
     *
     * @param user       the current user
     * @param serverFqdn the FQDN of the server
     * @return the server's SSH public key
     */
    private String getServerSshPublicKey(User user, String serverFqdn) {
        String localManagerFqdn = Config.get().getString(ConfigDefaults.SERVER_HOSTNAME);
        if (isAbsent(localManagerFqdn)) {
            throw raiseAndLog("Could not determine the local SUSE Manager FQDN.").get();
        }

        if (localManagerFqdn.equals(serverFqdn)) {
            MgrUtilRunner.SshKeygenResult serverSshKey =
                    saltApi.generateSSHKey(SaltSSHService.SSH_KEY_PATH, SaltSSHService.SUMA_SSH_PUB_KEY)
                            .orElseThrow(raiseAndLog("Could not generate salt-ssh public key."));

            if (!(serverSshKey.getReturnCode() == 0 || serverSshKey.getReturnCode() == -1)) {
                throw raiseAndLog("Generating salt-ssh public key failed: " + serverSshKey.getStderr()).get();
            }

            return serverSshKey.getPublicKey();
        }

        Server serverServer = ServerFactory.lookupProxiesByOrg(user).stream()
                .filter(proxy -> serverFqdn.equals(proxy.getName())).findFirst()
                .orElseThrow(raiseAndLog("Could not find specified server named " + serverFqdn +
                        " in the organization."));

        if (!allProvided(serverServer.getProxyInfo(), serverServer.getProxyInfo().getSshPublicKey())) {
            throw raiseAndLog("Could not find the public SSH key for the server.").get();
        }

        return new String(serverServer.getProxyInfo().getSshPublicKey());
    }

    /**
     * Raises and logs an exception
     *
     * @param message exception message
     * @return exception supplier
     */
    private Supplier<RhnRuntimeException> raiseAndLog(String message) {
        LOG.error(message);
        return () -> new RhnRuntimeException(message);
    }

    private Optional<Server> findByAnyFqdn(Set<String> fqdns) {
        for (String fqdn : fqdns) {
            Optional<Server> server = ServerFactory.findByFqdn(fqdn);
            if (server.isPresent()) {
                return server;
            }
        }
        return Optional.empty();
    }
}
