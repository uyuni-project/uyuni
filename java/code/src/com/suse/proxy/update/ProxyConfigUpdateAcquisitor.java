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

import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE_ADVANCED;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE_SIMPLE;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_REGISTRY;
import static com.suse.proxy.ProxyConfigUtils.USE_CERTS_MODE_KEEP;
import static com.suse.utils.Predicates.isAbsent;
import static com.suse.utils.Predicates.isProvided;
import static java.util.Optional.ofNullable;

import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerFQDN;
import com.redhat.rhn.domain.server.ServerFactory;

import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;
import com.suse.proxy.ProxyContainerImagesEnum;
import com.suse.proxy.RegistryUrl;
import com.suse.proxy.model.ProxyConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URISyntaxException;

/**
 * Acquires information required for updating the proxy configuration
 * Might these be just used for validation purposes or actually updating the proxy configuration
 */
public class ProxyConfigUpdateAcquisitor implements ProxyConfigUpdateContextHandler {
    private static final Logger LOG = LogManager.getLogger(ProxyConfigUpdateAcquisitor.class);

    @Override
    public void handle(ProxyConfigUpdateContext context) {
        acquireProxyMinion(context);
        acquireCertificates(context);
        acquireParentServer(context);
        buildRegistryUrls(context);
    }

    /**
     * Acquires the proxy minion and its configuration if it already exists
     *
     * @param context the context
     */
    private void acquireProxyMinion(ProxyConfigUpdateContext context) {
        Long serverId = context.getRequest().getServerId();
        if (isProvided(serverId)) {
            MinionServerFactory.lookupById(serverId).ifPresent(minionServer -> {
                if (minionServer.hasProxyEntitlement()) {
                    context.setProxyMinion(minionServer);
                    context.setProxyFqdn(ofNullable(minionServer.findPrimaryFqdn())
                            .map(ServerFQDN::getName)
                            .orElse(minionServer.getName()));
                    context.setProxyConfig(context.getProxyConfigGetFacade().getProxyConfig(minionServer));
                }
            });
        }
    }

    /**
     * Acquires the certificates from the request or the current proxy configuration
     * In case the request specifies to keep the current certificates, the current proxy configuration is used.
     * Two main reasons for this:
     * 1) We want to keep the exact same certificates we have stored;
     * 2) WebUi is getting truncated certificates data, that's also what we'll get back.
     * @param context the context
     */
    private void acquireCertificates(ProxyConfigUpdateContext context) {
        ProxyConfigUpdateJson request = context.getRequest();
        if (isAbsent(request.getUseCertsMode())) {
            return;
        }
        boolean keepCerts = USE_CERTS_MODE_KEEP.equals(request.getUseCertsMode());
        ProxyConfig proxyConfig = context.getProxyConfig();
        if (keepCerts && isAbsent(proxyConfig)) {
            return;
        }

        context.setRootCA(keepCerts ? proxyConfig.getRootCA() : request.getRootCA());
        context.setIntermediateCAs(keepCerts ? proxyConfig.getIntermediateCAs() : request.getIntermediateCAs());
        context.setProxyCert(keepCerts ? proxyConfig.getProxyCert() : request.getProxyCert());
        context.setProxyKey(keepCerts ? proxyConfig.getProxyKey() : request.getProxyKey());
    }

    /**
     * Acquires the parent server if provided
     *
     * @param context the context
     */
    private void acquireParentServer(ProxyConfigUpdateContext context) {
        String parentFqdn = context.getRequest().getParentFqdn();
        if (isProvided(parentFqdn)) {
            ServerFactory.findByFqdn(parentFqdn).ifPresent(server -> {
                if (server.isMgrServer() || server.isProxy()) {
                    context.setParentServer(server);
                }
            });
        }
    }

    /**
     * Builds the registry URLs for the proxy container images
     *
     * @param context the context
     */
    private void buildRegistryUrls(ProxyConfigUpdateContext context) {
        ProxyConfigUpdateJson request = context.getRequest();
        if (!SOURCE_MODE_REGISTRY.equals(request.getSourceMode())) {
            return;
        }

        try {
            if (REGISTRY_MODE_SIMPLE.equals(request.getRegistryMode())) {
                String registryBaseURL = request.getRegistryBaseURL();
                String registryBaseTag = request.getRegistryBaseTag();
                String separator = registryBaseURL.endsWith("/") ? "" : "/";

                for (ProxyContainerImagesEnum proxyImage : ProxyContainerImagesEnum.values()) {
                    context.getRegistryUrls().put(
                            proxyImage,
                            new RegistryUrl(registryBaseURL + separator + proxyImage.getImageName(), registryBaseTag)
                    );
                }
            }
            else if (REGISTRY_MODE_ADVANCED.equals(request.getRegistryMode())) {
                context.getRegistryUrls().put(
                        ProxyContainerImagesEnum.PROXY_HTTPD,
                        new RegistryUrl(request.getRegistryHttpdURL(), request.getRegistryHttpdTag())
                );
                context.getRegistryUrls().put(
                        ProxyContainerImagesEnum.PROXY_SALT_BROKER,
                        new RegistryUrl(request.getRegistrySaltbrokerURL(), request.getRegistrySaltbrokerTag())
                );
                context.getRegistryUrls().put(
                        ProxyContainerImagesEnum.PROXY_SQUID,
                        new RegistryUrl(request.getRegistrySquidURL(), request.getRegistrySquidTag())
                );
                context.getRegistryUrls().put(
                        ProxyContainerImagesEnum.PROXY_SSH,
                        new RegistryUrl(request.getRegistrySshURL(), request.getRegistrySshTag())
                );
                context.getRegistryUrls().put(
                        ProxyContainerImagesEnum.PROXY_TFTPD,
                        new RegistryUrl(request.getRegistryTftpdURL(), request.getRegistryTftpdTag())
                );
            }
        }
        catch (URISyntaxException e) {
            LOG.error("Invalid creating Registry URL {}", context);
            context.getErrorReport().register("Invalid Registry URL");
        }
    }

}
