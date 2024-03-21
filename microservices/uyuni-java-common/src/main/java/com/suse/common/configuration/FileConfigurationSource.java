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

package com.suse.common.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileConfigurationSource extends BaseConfigurationSource {

    // This comparator is needed to make sure we read the child namespace before the base namespace.
    // We sort the item in reverse order based on the length of the file name. If two filenames have the same length,
    // then we need to do a lexicographical comparison to make sure that the filenames themselves are different.
    private static final Comparator<Path> CONFIG_PATH_COMPARATOR = (p1, p2) -> {
        int lenDif = p2.toAbsolutePath().toString().length() - p1.toAbsolutePath().toString().length();
        return lenDif != 0 ? lenDif : p2.compareTo(p1);
    };

    private static final Logger LOGGER = LogManager.getLogger(FileConfigurationSource.class);

    private final List<String> fallbackNamespaces;

    private final String commonFilePrefix;

    private final Properties configValues;

    /**
     * Builds a file configuration source.
     * @param fileSourcesIn List of files and directories to process
     */
    public FileConfigurationSource(List<Path> fileSourcesIn) {
        this(fileSourcesIn, null, Collections.emptyList());
    }

    /**
     * Builds a file configuration source.
     * @param fileSourcesIn List of files and directories to process
     * @param filePrefixIn a common prefix used by all configuration file names
     */
    public FileConfigurationSource(List<Path> fileSourcesIn, String filePrefixIn) {
        this(fileSourcesIn, filePrefixIn, Collections.emptyList());
    }

    /**
     * Builds a file configuration source.
     * @param fileSourcesIn List of files and directories to process
     * @param filePrefixIn a common prefix used by all configuration file names
     * @param fallbackNamespacesIn namespaces to search for, in the given order. These are used when a property,
     *     specified with no namespace, was not found.
     */
    public FileConfigurationSource(List<Path> fileSourcesIn, String filePrefixIn, List<String> fallbackNamespacesIn) {
        configValues = new Properties();

        commonFilePrefix = filePrefixIn;
        fallbackNamespaces = fallbackNamespacesIn;

        fileSourcesIn.stream()
            .flatMap(path -> getConfigurationFiles(path))
            .sorted(CONFIG_PATH_COMPARATOR)
            .map(file -> loadProperties(file))
            .forEach(props -> configValues.putAll(props));
    }

    /**
     * Get the configuration entry for given property
     *
     * @param property string to get the value of
     * @return the value
     */
    @Override
    protected String getRawValue(String property) {
        LOGGER.debug("getString() - called with: {}", property);
        if (property == null) {
            return null;
        }

        String namespace = "";
        String propertyName = property;

        int lastDot = property.lastIndexOf('.');
        if (lastDot > 0) {
            propertyName = property.substring(lastDot + 1);
            namespace = property.substring(0, lastDot);
        }

        LOGGER.debug("getString() - Getting property: {}", propertyName);
        String result = configValues.getProperty(propertyName);
        LOGGER.debug("getString() result: {}", result);

        if (result == null) {
            if (!namespace.isEmpty()) {
                result = configValues.getProperty(namespace + "." + propertyName);
            }
            else {
                // The property was not found, and it has no namespace. Lookup all the provided fallback namespaces
                for (String fallback : fallbackNamespaces) {
                    result = configValues.getProperty(fallback + "." + propertyName);
                    if (result != null) {
                        break;
                    }
                }
            }
        }

        LOGGER.debug("getString() - returning: {}", result);
        if (result == null || result.isEmpty()) {
            return null;
        }

        return result.trim();
    }

    @Override
    public Set<String> getPropertyNames() {
        return configValues.keySet().stream()
            .map(Objects::toString)
            .collect(Collectors.toUnmodifiableSet());
    }

    private static Stream<Path> getConfigurationFiles(Path path) {
        if (Files.isDirectory(path)) {
            try {
                return Files.list(path)
                    .filter(file -> file.getFileName().toString().endsWith(".conf"));
            }
            catch (IOException ex) {
                LOGGER.error("Unable to list file in directory {}", path);
                return Stream.empty();
            }
        }

        return Stream.of(path);
    }

    private Properties loadProperties(Path file) {
        Properties properties;

        try {
            String fileContent = Files.readString(file, StandardCharsets.UTF_8);

            properties = new Properties();
            properties.load(new StringReader(fileContent.replace("\\", "\\\\")));
        }
        catch (Exception ex) {
            LOGGER.error("Could not parse file {}", file, ex);
            return new Properties();
        }

        String namespace = makeNamespace(file);
        LOGGER.debug("Adding namespace: {} for file: {}", namespace, file.toAbsolutePath());
        for (Object key : properties.keySet()) {
            String propertyName = (String) key;
            if (!propertyName.startsWith(namespace)) {
                String qualifiedProperty = namespace + "." + propertyName;
                Object value = properties.remove(propertyName);

                LOGGER.debug("Adding: {}: {}", qualifiedProperty, value);
                properties.put(qualifiedProperty, value);
            }
        }

        return properties;
    }

    private String makeNamespace(Path path) {
        String namespace = path.getFileName().toString();
        // If a common filename prefix was specified
        if (commonFilePrefix != null) {
            // This is really hokey, but it works. Basically, rhn.conf doesn't
            // match the standard rhn_foo.conf convention. So, to create the
            // namespace, we first special case rhn.*
            if (namespace.startsWith(commonFilePrefix + ".")) {
                return "";
            }

            namespace = namespace.replaceFirst(commonFilePrefix + "_", "");
        }

        int lastDotindex = namespace.lastIndexOf('.');
        if (lastDotindex != -1) {
            namespace = namespace.substring(0, namespace.lastIndexOf('.'));
        }
        namespace = namespace.replace("_", ".");

        return namespace;
    }
}
