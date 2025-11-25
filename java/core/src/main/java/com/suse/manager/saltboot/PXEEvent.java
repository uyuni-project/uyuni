/*
 * Copyright (c) 2022 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.manager.saltboot;

import com.suse.salt.netapi.datatypes.Event;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PXEEvent {
    private static final Logger LOG = LogManager.getLogger(PXEEvent.class);
    private static final Pattern PATTERN = Pattern.compile("^suse/manager/pxe_update");

    private final String minionId;
    private final Map<String, Object> data;

    /**
     * Creates a new SaltbootEvent
     * @param minionIdIn the id of the minion sending the event
     * @param dataIn data containing more information about this event
     */
    private PXEEvent(String minionIdIn, Map<String, Object> dataIn) {
        this.minionId = minionIdIn;
        this.data = dataIn;
    }

    /**
     * The id of the minion that started
     *
     * @return the minion id
     */
    public String getMinionId() {
        return minionId;
    }

    public String getAction() {
        return (String)data.get("action");
    }

    public String getSaltbootGroup() {
        return String.valueOf(data.get("minion_id_prefix"));
    }

    public String getRoot() {
        return (String)data.get("root");
    }

    /**
     * The device where salt configuration files are stored on the terminal
     *
     * @return Optional of salt device string
     */
    public Optional<String> getSaltDevice() {
        return Optional.ofNullable((String)data.get("salt_device"));
    }

    /**
     * Additional kernel parameters terminal wants to be booted with
     *
     * @return Optional of kernel parameters string
     */
    public Optional<String> getKernelParameters() {
        return Optional.ofNullable((String)data.get("terminal_kernel_parameters"));
    }

    public String getBootImage() {
        return (String)data.get("boot_image");
    }

    /**
     * List of MAC addresses of the terminal excluding localhost interface
     *
     * @return List of string of MAC addresses
     */
    public List<String> getHwAddresses() {
        Map<String, Object> macGrains = (Map<String, Object>)data.get("hwaddr_interfaces");
        return macGrains.entrySet().stream()
                .filter(e -> !e.getKey().equals("lo"))
                .map(e -> (String)e.getValue())
                .collect(Collectors.toList());
    }

    /**
     * Utility method to parse e generic event to a more specific one
     * @param event the generic event to parse
     * @return an option containing the parsed value or non if it could not be parsed
     */
    public static Optional<PXEEvent> parse(Event event) {
        Matcher matcher = PATTERN.matcher(event.getTag());
        if (matcher.matches()) {
            Map<String, Object> data;

            // Validate input data are not empty and correct format
            try {
                data = (Map<String, Object>) event.getData().get("data");
            }
            catch (ClassCastException e) {
                LOG.error("Parsing failed", e);
                return Optional.empty();
            }
            LOG.debug("Data: {}", data);

            if (data.isEmpty()) {
                LOG.error("Empty data");
                return Optional.empty();
            }

            PXEEvent result = new PXEEvent(
                    (String) event.getData().get("id"),
                    data
            );

            try {
                // Validate we have all required data, some may trigger NPE if missing
                if (result.getSaltbootGroup().isEmpty() ||
                    result.getRoot().isEmpty() ||
                    result.getMinionId().isEmpty() ||
                    result.getHwAddresses().isEmpty() ||
                    result.getBootImage().isEmpty()) {
                    LOG.error("Missing required data: {}", data);
                    return Optional.empty();
                }
            }
            catch (NullPointerException e) {
                LOG.error("Parsing failed", e);
                return Optional.empty();
            }
            return Optional.of(result);
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("minionId", minionId)
                .append("data", data)
                .toString();
    }
}
