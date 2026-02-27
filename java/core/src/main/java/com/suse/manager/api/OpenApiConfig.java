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
import com.redhat.rhn.frontend.xmlrpc.api.ApiHandler;

import com.suse.manager.api.docs.UyuniSwaggerReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

public class OpenApiConfig {

    private static final UyuniSwaggerReader READER = new UyuniSwaggerReader();
    private static final Logger LOGGER = LogManager.getLogger(OpenApiConfig.class);

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

    public static OpenAPI processHandlers() {
        getHandlerClasses().forEach((key, value) -> READER.read(value, key));
        return READER.getSpec();
    }

    /**
     * Returns a map of API namespaces to their corresponding handler classes.
     * This is a placeholder for potential dynamic discovery of handler classes.
     * @return Map of API namespaces to handler classes.
     */
    private static Map<String, Class<?>> getHandlerClasses() {
        return Map.of(
            "access", AccessHandler.class,
            "api", ApiHandler.class
        );
    }
}
