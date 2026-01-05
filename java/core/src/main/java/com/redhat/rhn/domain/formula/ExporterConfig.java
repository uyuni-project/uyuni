/*
 * Copyright (c) 2021 SUSE LLC
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

package com.redhat.rhn.domain.formula;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExporterConfig {

    private static final String PORT_ARG_REGEX = "--(?:telemetry\\.address|web\\.listen-address)=[\"']?:([0-9]*)[\"']?";
    private static final String PORT_ADDRESS_REGEX = ":([0-9]*)$";

    private final Boolean enabled;
    private final String name;
    private final String endpointName;
    private final String address;
    private final String args;
    private final String proxyModule;

    /**
     * Instantiates new exporter configuration object
     * @param enabledIn flag enabling the exporter
     * @param exporterNameIn exporter name
     * @param endpointNameIn endpoint name
     * @param addressIn the address of the endpoint where metrics are exposed
     * @param argsIn the string with command line arguments
     * @param proxyModuleIn module name for exporter exporter
     */
    public ExporterConfig(String exporterNameIn, Boolean enabledIn, String endpointNameIn,
                          String addressIn, String argsIn, String proxyModuleIn) {
        this.enabled = enabledIn;
        this.name = exporterNameIn;
        this.endpointName = endpointNameIn;
        this.address = addressIn;
        this.args = argsIn;
        this.proxyModule = proxyModuleIn;
    }

    /**
     * Instantiate new exporter using name and configuration map
     * @param exporterName exporter name
     * @param exporterConfigMap map with configuration values
     */
    public ExporterConfig(String exporterName, Map<String, Object> exporterConfigMap) {
        this(
                exporterName,
                Optional.ofNullable(exporterConfigMap.getOrDefault("enabled", false))
                        .filter(Boolean.class::isInstance).map(Boolean.class::cast).orElse(false),
                Optional.ofNullable(exporterConfigMap.get("name"))
                        .filter(String.class::isInstance).map(String.class::cast).orElse(null),
                Optional.ofNullable(exporterConfigMap.get("address"))
                        .filter(String.class::isInstance).map(String.class::cast).orElse(null),
                Optional.ofNullable(exporterConfigMap.get("args"))
                        .filter(String.class::isInstance).map(String.class::cast).orElse(null),
                Optional.ofNullable(exporterConfigMap.get("proxy_module"))
                        .filter(String.class::isInstance).map(String.class::cast).orElse(null)
        );
    }

    public String getName() {
        return name;
    }

    public Optional<String> getEndpointName() {
        return Optional.ofNullable(endpointName);
    }

    public Optional<String> getProxyModule() {
        return Optional.ofNullable(proxyModule);
    }

    public String getEndpointNameOrFallback() {
        return getEndpointName().orElseGet(this::getName);
    }

    public String getProxyModuleOrFallback() {
        return getProxyModule().orElseGet(this::getEndpointNameOrFallback);
    }

    public Boolean isEnabled() {
        return enabled;
    }

    /**
     * Get port number at which metrics are exposed
     * @return port number
     */
    public Optional<Integer> getPort() {
        Optional<Integer> port = getPatternMatchGroupAsInteger(PORT_ARG_REGEX, args);
        if (port.isEmpty()) {
            port = getPatternMatchGroupAsInteger(PORT_ADDRESS_REGEX, address);
        }
        return port;
    }

    private Optional<Integer> getPatternMatchGroupAsInteger(String regex, String input) {
        Optional<Integer> intGroup = Optional.empty();
        Optional<String> optInput = Optional.ofNullable(input);
        if (optInput.isPresent()) {
            Pattern intPattern = Pattern.compile(regex);
            Matcher intMatcher = intPattern.matcher(optInput.get());
            if (intMatcher.find()) {
                intGroup = Optional.of(Integer.valueOf(intMatcher.group(1)));
            }
        }
        return intGroup;
    }
}
