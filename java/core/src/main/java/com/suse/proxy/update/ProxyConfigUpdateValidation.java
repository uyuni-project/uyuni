/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.proxy.update;

import static com.suse.proxy.ProxyConfigUtils.MAX_CACHE_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PARENT_FQDN_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PROXY_CERT_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PROXY_KEY_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PROXY_PORT_FIELD;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_BASE_TAG;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_BASE_URL;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE_ADVANCED;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE_SIMPLE;
import static com.suse.proxy.ProxyConfigUtils.ROOT_CA_FIELD;
import static com.suse.proxy.ProxyConfigUtils.SERVER_ID_FIELD;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_FIELD;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_REGISTRY;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_RPM;
import static com.suse.proxy.ProxyConfigUtils.USE_CERTS_MODE_KEEP;
import static com.suse.proxy.ProxyConfigUtils.getSubscribableMgrpxyChannels;
import static com.suse.proxy.ProxyConfigUtils.isMgrpxyInstalled;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_HTTPD;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_SALT_BROKER;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_SQUID;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_SSH;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_TFTPD;
import static com.suse.utils.Predicates.isAbsent;
import static java.lang.String.format;

import com.redhat.rhn.common.UyuniErrorReport;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates data acquired from {@link ProxyConfigUpdateJson} and retrieved from {@link ProxyConfigUpdateAcquisitor}
 * that is required for the rest of the process.
 * Additionally, it verifies whether the target minion satisfies the conditions to apply proxy configuration::
 *   - The minion is not a Manager instance.
 *   - The minion either already has proxy entitlement or can be entitled as such.
 *   - The minion either has mgrpxy installed or can subscribe (or is already subscribed) to a channel
 *   providing it. - only applicable if running an MLM instance
 */
public class ProxyConfigUpdateValidation implements ProxyConfigUpdateContextHandler {
    private static final Logger LOG = LogManager.getLogger(ProxyConfigUpdateValidation.class);
    private static final Pattern FQDN_PATTERN = Pattern.compile("^[A-Za-z0-9-]++(?:\\.[A-Za-z0-9-]++)*+$");
    private static final String NOT_FOUND_ON_CURRENT_PROXY_CONFIGURATION_MESSAGE =
            "%s not found on current proxy configuration";

    private UyuniErrorReport errorReport;

    @Override
    public void handle(ProxyConfigUpdateContext context) {
        ProxyConfigUpdateJson request = context.getRequest();
        this.errorReport = context.getErrorReport();

        if (!registerIfMissing(request.getServerId(), SERVER_ID_FIELD) && isAbsent(context.getProxyFqdn())) {
            errorReport.register("proxyFQDN for the server was not resolved");
            LOG.error("Proxy FQDN for the server {} was not resolved", request.getServerId());
        }

        String parentFqdn = request.getParentFqdn();
        if (!registerIfMissing(parentFqdn, PARENT_FQDN_FIELD) && !FQDN_PATTERN.matcher(parentFqdn).matches()) {
            errorReport.register("parentFQDN is invalid");
        }
        registerIfMissing(request.getProxyPort(), PROXY_PORT_FIELD);
        registerIfMissing(request.getMaxCache(), MAX_CACHE_FIELD);
        validateCertificates(context);

        if (!registerIfMissing(request.getSourceMode(), SOURCE_MODE_FIELD)) {
            validateSourceMode(request);
        }

        MinionServer minion = context.getProxyMinion();
        if (isAbsent(minion)) {
            return;
        }

        if (minion.isMgrServer()) {
            errorReport.register("The system is a Management Server and cannot be converted to a Proxy");
        }

        if (!minion.hasProxyEntitlement() &&
                !context.getSystemEntitlementManager().canEntitleServer(minion, EntitlementManager.PROXY)) {
            errorReport.register("Cannot entitle server ID: {0}", minion.getId());
        }

        if (!ConfigDefaults.get().isUyuni() && !isMgrpxyInstalled(minion)) {
            Set<Channel> subscribableMgrpxyChannels = getSubscribableMgrpxyChannels(minion, context.getUser());
            context.setSubscribableChannelsWithMgrpxy(subscribableMgrpxyChannels);
            if (subscribableMgrpxyChannels == null) {
                errorReport.register("No channel with mgrpxy package found for server ID: {0}", minion.getId());
            }
        }
    }

    private void validateCertificates(ProxyConfigUpdateContext context) {
        ProxyConfigUpdateJson request = context.getRequest();
        if (USE_CERTS_MODE_KEEP.equals(request.getUseCertsMode())) {
            if (isAbsent(context.getProxyConfig())) {
                errorReport.register("No current proxy configuration found to keep certificates");
                return;
            }
            if (isAbsent(context.getRootCA())) {
                errorReport.register(String.format(NOT_FOUND_ON_CURRENT_PROXY_CONFIGURATION_MESSAGE, ROOT_CA_FIELD));
            }
            if (isAbsent(context.getProxyCert())) {
                errorReport.register(String.format(NOT_FOUND_ON_CURRENT_PROXY_CONFIGURATION_MESSAGE, PROXY_CERT_FIELD));
            }
            if (isAbsent(context.getProxyKey())) {
                errorReport.register(String.format(NOT_FOUND_ON_CURRENT_PROXY_CONFIGURATION_MESSAGE, PROXY_KEY_FIELD));
            }
            return;
        }
        registerIfMissing(context.getRootCA(), ROOT_CA_FIELD);
        registerIfMissing(context.getProxyCert(), PROXY_CERT_FIELD);
        registerIfMissing(context.getProxyKey(), PROXY_KEY_FIELD);
    }

    private void validateSourceMode(ProxyConfigUpdateJson request) {
        switch (request.getSourceMode()) {
            case SOURCE_MODE_REGISTRY:
                if (!registerIfMissing(request.getRegistryMode(), REGISTRY_MODE)) {
                    validateSourceRegistryMode(request);
                }
                break;
            case SOURCE_MODE_RPM:
                break;
            default:
                errorReport.register(format(
                        "sourceMode %s is invalid. Must be either 'registry' or 'rpm'",
                        request.getSourceMode()
                ));
        }
    }

    private void validateSourceRegistryMode(ProxyConfigUpdateJson request) {
        switch (request.getRegistryMode()) {
            case REGISTRY_MODE_SIMPLE:
                registerIfMissing(request.getRegistryBaseURL(), REGISTRY_BASE_URL);
                registerIfMissing(request.getRegistryBaseTag(), REGISTRY_BASE_TAG);
                return;
            case REGISTRY_MODE_ADVANCED:
                registerIfMissing(request.getRegistryHttpdURL(), PROXY_HTTPD.getUrlField());
                registerIfMissing(request.getRegistryHttpdTag(), PROXY_HTTPD.getTagField());
                registerIfMissing(request.getRegistrySaltbrokerURL(), PROXY_SALT_BROKER.getUrlField());
                registerIfMissing(request.getRegistrySaltbrokerTag(), PROXY_SALT_BROKER.getTagField());
                registerIfMissing(request.getRegistrySquidURL(), PROXY_SQUID.getUrlField());
                registerIfMissing(request.getRegistrySquidTag(), PROXY_SQUID.getTagField());
                registerIfMissing(request.getRegistrySshURL(), PROXY_SSH.getUrlField());
                registerIfMissing(request.getRegistrySshTag(), PROXY_SSH.getTagField());
                registerIfMissing(request.getRegistryTftpdURL(), PROXY_TFTPD.getUrlField());
                registerIfMissing(request.getRegistryTftpdTag(), PROXY_TFTPD.getTagField());
                return;
            default:
                errorReport.register(format(
                        "sourceRegistryMode %s is invalid. Must be either 'simple' or 'advanced'",
                        request.getRegistryMode()
                ));
        }

    }

    /**
     * Validates and register an error if a given value is not null or empty
     *
     * @param value the value to validate
     * @param field the field name
     * @return true if the value is missing, false otherwise
     */
    public boolean registerIfMissing(Object value, String field) {
        if (isAbsent(value)) {
            errorReport.register(String.format("%s is required", field));
            return true;
        }
        return false;
    }

}
