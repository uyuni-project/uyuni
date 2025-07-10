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


import static com.suse.utils.Predicates.isAbsent;
import static java.lang.String.format;

import com.redhat.rhn.common.UyuniErrorReport;

import com.suse.manager.ssl.SSLCertGenerationException;
import com.suse.manager.ssl.SSLCertManager;
import com.suse.manager.ssl.SSLCertPair;
import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Retrieves the proxy configuration files
 */
public class ProxyConfigUpdateFileAcquisitor implements ProxyConfigUpdateContextHandler {

    public static final Map<String, Object> EXPECTED_FILE_CONFIGURATIONS = Map.of(
            "server", String.class,
            "ca_crt", String.class,
            "proxy_fqdn", String.class,
            "max_cache_size_mb", Long.class,
            "server_version", String.class,
            "email", String.class,
            "httpd", Map.of(
                    "system_id", String.class,
                    "server_crt", String.class,
                    "server_key", String.class
            ),
            "ssh", Map.of(
                    "server_ssh_key_pub", String.class,
                    "server_ssh_push", String.class,
                    "server_ssh_push_pub", String.class
            ),
            "replace_fqdns", List.of(String.class)
    );
    private static final Logger LOG = LogManager.getLogger(ProxyConfigUpdateFileAcquisitor.class);
    private static final String ERROR_MESSAGE_MISSING_ENTRY =
            "proxy container configuration did not generate required entry: %s";
    private static final String ERROR_MESSAGE_UNEXPECTED_VALUE =
            "proxy container configuration generated an unexpected value for entry: %s";

    @Override
    public void handle(ProxyConfigUpdateContext context) {
        ProxyConfigUpdateJson request = context.getRequest();

        try {
            context.setProxyConfigFiles(
                    context.getSystemManager().createProxyContainerConfigFiles(
                            context.getUser(),
                            context.getProxyFqdn(),
                            request.getProxyPort(),
                            request.getParentFqdn(),
                            Long.valueOf(request.getMaxCache()) * 1024L,
                            request.getEmail(),
                            context.getRootCA(),
                            context.getIntermediateCAs(),
                            new SSLCertPair(context.getProxyCert(), context.getProxyKey()),
                            null, null, null, new SSLCertManager(),
                            request.getProxySshPub(), request.getProxySshPriv(), request.getParentSshPub())
            );

        }
        catch (SSLCertGenerationException e) {
            LOG.error("Failed to create proxy container configuration", e);
            context.getErrorReport().register("Failed to create proxy container configuration");
            return;
        }

        Map<String, Object> proxyConfigFiles = context.getProxyConfigFiles();
        if (isAbsent(proxyConfigFiles)) {
            context.getErrorReport().register("proxy container configuration files were not created");
            LOG.error("proxy container configuration files were not created");
            return;
        }
        validateProxyConfigFiles(proxyConfigFiles, EXPECTED_FILE_CONFIGURATIONS, "", context.getErrorReport());
    }


    /**
     * Recursively validates the structure of the proxy configuration files
     *
     * @param map               the map to validate
     * @param expectedStructure the expected structure of the map
     * @param parentKey         the parent key of the map
     * @param uyuniErrorReport    the error report to register errors
     */
    public void validateProxyConfigFiles(
            Map<String, Object> map,
            Map<String, Object> expectedStructure,
            String parentKey,
            UyuniErrorReport uyuniErrorReport
    ) {
        for (Map.Entry<String, Object> entry : expectedStructure.entrySet()) {
            String key = entry.getKey();
            Object expectedValue = entry.getValue();
            String fullKey = parentKey.isEmpty() ? key : parentKey + "." + key;

            if (!map.containsKey(key)) {
                logAndRegisterError(uyuniErrorReport, ERROR_MESSAGE_MISSING_ENTRY, fullKey);
                continue;
            }

            Object actualValue = map.get(key);
            if (expectedValue instanceof Class) {
                if (!expectedValue.equals(actualValue.getClass())) {
                    logAndRegisterError(uyuniErrorReport, ERROR_MESSAGE_UNEXPECTED_VALUE, fullKey);
                }
            }
            else if (expectedValue instanceof Map) {
                if (!(actualValue instanceof Map)) {
                    logAndRegisterError(uyuniErrorReport, ERROR_MESSAGE_UNEXPECTED_VALUE, fullKey);
                }
                else {
                    validateProxyConfigFiles(
                            (Map<String, Object>) actualValue,
                            (Map<String, Object>) expectedValue,
                            fullKey,
                            uyuniErrorReport
                    );
                }
            }
        }
    }


    private void logAndRegisterError(UyuniErrorReport uyuniErrorReport, String message, Object... args) {
        String formattedMessage = format(message, args);
        uyuniErrorReport.register(formattedMessage);
        LOG.error(formattedMessage);
    }

}
