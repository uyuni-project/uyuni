/*
 * Copyright (c) 2026 SUSE LLC
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
package com.suse.manager.api;

import com.redhat.rhn.frontend.xmlrpc.access.AccessHandler;
import com.redhat.rhn.frontend.xmlrpc.admin.ssh.AdminSshHandler;
import com.redhat.rhn.frontend.xmlrpc.api.ApiHandler;
import com.redhat.rhn.frontend.xmlrpc.channel.access.ChannelAccessHandler;
import com.redhat.rhn.frontend.xmlrpc.preferences.locale.PreferencesLocaleHandler;
import com.redhat.rhn.frontend.xmlrpc.saltkey.SaltKeyHandler;
import com.redhat.rhn.frontend.xmlrpc.subscriptionmatching.PinnedSubscriptionHandler;

import com.suse.manager.api.docs.UyuniSwaggerReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * Generates the OpenAPI specification for supported XML-RPC handlers.
 */
public final class OpenApiConfig {

    private static final UyuniSwaggerReader READER = new UyuniSwaggerReader();
    private static final Logger LOGGER = LogManager.getLogger(OpenApiConfig.class);

    /**
     * Utility class.
     */
    private OpenApiConfig() {
    }

    /**
     * Writes the generated OpenAPI specification to the configured output directory.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        String outputDirPath = System.getProperty("apidoc.output");

        if (outputDirPath == null || outputDirPath.isEmpty()) {
            LOGGER.error("Missing or empty 'apidoc.output' system property.");
            System.exit(1);
        }

        File outputDir = new File(outputDirPath);
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            LOGGER.error("Failed to create output directory: " + outputDirPath);
            System.exit(1);
        }

        if (!outputDir.isDirectory()) {
            LOGGER.error("The path is not a directory: " + outputDirPath);
            System.exit(1);
        }

        String json = Json.pretty(processHandlers());

        File outFile = new File(outputDir, "openapi.json");
        try (FileWriter writer = new FileWriter(outFile)) {
            writer.write(json);
            LOGGER.info("openapi.json written to: " + outFile.getAbsolutePath());
        }
        catch (IOException e) {
            LOGGER.error("Failed to write openapi.json: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Processes configured handlers and returns the generated OpenAPI specification.
     *
     * @return generated OpenAPI specification
     */
    public static OpenAPI processHandlers() {
        getHandlerClasses().forEach((key, value) -> READER.read(value, key));
        return READER.getSpec();
    }

    /**
     * Returns a map of API namespaces to their corresponding handler classes.
     *
     * @return API namespaces mapped to handler classes
     */
    public static Map<String, Class<?>> getHandlerClasses() {
        Map<String, Class<?>> handlers = new LinkedHashMap<>();
        handlers.put("access", AccessHandler.class);
        handlers.put("admin.ssh", AdminSshHandler.class);
        handlers.put("api", ApiHandler.class);
        handlers.put("channel.access", ChannelAccessHandler.class);
        handlers.put("preferences.locale", PreferencesLocaleHandler.class);
        handlers.put("saltkey", SaltKeyHandler.class);
        handlers.put("subscriptionmatching.pinnedsubscription", PinnedSubscriptionHandler.class);
        return handlers;
    }
}
