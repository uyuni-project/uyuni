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
import static com.suse.utils.Predicates.isProvided;
import static java.lang.String.format;

import com.suse.manager.ssl.SSLCertGenerationException;
import com.suse.manager.ssl.SSLCertManager;
import com.suse.manager.ssl.SSLCertPair;
import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Retrieves the proxy configuration files
 */
public class ProxyConfigUpdateFileAcquisitor implements ProxyConfigUpdateContextHandler {

    private static final Logger LOG = LogManager.getLogger(ProxyConfigUpdateFileAcquisitor.class);
    private static final Map<String, String[]> EXPECTED_FILE_CONFIGURATIONS = Map.of(
            "server", new String[]{},
            "ca_crt", new String[]{},
            "proxy_fqdn", new String[]{},
            "max_cache_size_mb", new String[]{},
            "server_version", new String[]{},
            "email", new String[]{},
            "httpd", new String[]{"system_id", "server_crt", "server_key"},
            "ssh", new String[]{"server_ssh_key_pub", "server_ssh_push", "server_ssh_push_pub"}
    );

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
                            null, null, null, new SSLCertManager())
            );

            if (isAbsent(context.getProxyConfigFiles())) {
                context.getErrorReport().register("proxy container configuration files were not created");
                LOG.debug("proxy container configuration files were not created");
                return;
            }

            for (Map.Entry<String, String[]> e : EXPECTED_FILE_CONFIGURATIONS.entrySet()) {
                String firstLevelEntry = e.getKey();
                if (!context.getProxyConfigFiles().containsKey(firstLevelEntry)) {
                    String format = format(
                            "proxy container configuration did not generate required entry: %s",
                            firstLevelEntry
                    );
                    context.getErrorReport().register(format);
                    LOG.debug(format);
                    continue;
                }

                String[] secondLevelEntries = e.getValue();
                if (isProvided(secondLevelEntries)) {
                    Map<String, String> secondLevelMap =
                            (Map<String, String>) context.getProxyConfigFiles().get(firstLevelEntry);
                    for (String secondLevelEntry : secondLevelEntries) {
                        if (!secondLevelMap.containsKey(secondLevelEntry)) {
                            String format = format(
                                    "proxy container configuration did not generate required entry: %s > %s",
                                    firstLevelEntry, secondLevelEntry
                            );
                            context.getErrorReport().register(format);
                            LOG.debug(format);
                        }
                    }
                }
            }
        }
        catch (SSLCertGenerationException e) {
            LOG.error("Failed to create proxy container configuration", e);
            context.getErrorReport().register("Failed to create proxy container configuration");
        }

    }

}
