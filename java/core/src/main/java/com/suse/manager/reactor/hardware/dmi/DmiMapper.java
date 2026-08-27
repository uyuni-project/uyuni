/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.reactor.hardware.dmi;

import static com.suse.manager.reactor.hardware.HardwareConstants.DMI_ASSET_FORMAT;
import static com.suse.manager.reactor.hardware.HardwareConstants.DMI_KEY_ASSET_TAG;
import static com.suse.manager.reactor.hardware.HardwareConstants.DMI_KEY_MANUFACTURER;
import static com.suse.manager.reactor.hardware.HardwareConstants.DMI_KEY_PRODUCT_NAME;
import static com.suse.manager.reactor.hardware.HardwareConstants.DMI_KEY_RELEASE_DATE;
import static com.suse.manager.reactor.hardware.HardwareConstants.DMI_KEY_SERIAL_NUMBER;
import static com.suse.manager.reactor.hardware.HardwareConstants.DMI_KEY_VENDOR;
import static com.suse.manager.reactor.hardware.HardwareConstants.DMI_KEY_VERSION;
import static com.suse.utils.Predicates.allAbsent;

import com.redhat.rhn.domain.server.Dmi;
import com.redhat.rhn.domain.server.MinionServer;

import com.suse.manager.reactor.utils.ValueMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Maps the DMI information read from the smbios tables to the database.
 */
public class DmiMapper {

    private static final Logger LOG = LogManager.getLogger(DmiMapper.class);

    private final MinionServer server;

    /**
     * Create a DMI mapper.
     *
     * @param serverIn the minion server
     */
    public DmiMapper(MinionServer serverIn) {
        this.server = serverIn;
    }

    /**
     * Store the DMI info as queried from Salt.
     *
     * @param smbiosRecordsBios smbios records of type "BIOS"
     * @param smbiosRecordsSystem smbios records of type "System"
     * @param smbiosRecordsBaseboard smbios records of type "Baseboard"
     * @param smbiosRecordsChassis smbios records of type "Chassis"
     * @return Optional error message if mapping failed
     */
    public Optional<String> mapDmiInfo(
            Map<String, Object> smbiosRecordsBios,
            Map<String, Object> smbiosRecordsSystem,
            Map<String, Object> smbiosRecordsBaseboard,
            Map<String, Object> smbiosRecordsChassis
    ) {
        try {
            ValueMap bios = new ValueMap(smbiosRecordsBios);
            ValueMap system = new ValueMap(smbiosRecordsSystem);
            ValueMap baseboard = new ValueMap(smbiosRecordsBaseboard);
            ValueMap chassis = new ValueMap(smbiosRecordsChassis);

            String biosVendor = bios.getOptionalAsString(DMI_KEY_VENDOR).orElse(null);
            String biosVersion = bios.getOptionalAsString(DMI_KEY_VERSION).orElse(null);
            String biosReleaseDate = bios.getOptionalAsString(DMI_KEY_RELEASE_DATE).orElse(null);

            String productName = system.getOptionalAsString(DMI_KEY_PRODUCT_NAME).orElse(null);
            String systemVersion = system.getOptionalAsString(DMI_KEY_VERSION).orElse(null);
            String systemSerial = system.getOptionalAsString(DMI_KEY_SERIAL_NUMBER).orElse(null);

            String boardManufacturer = baseboard.getOptionalAsString(DMI_KEY_MANUFACTURER).orElse(null);
            String boardName = baseboard.getOptionalAsString(DMI_KEY_PRODUCT_NAME).orElse(null);
            String boardSerial = baseboard.getOptionalAsString(DMI_KEY_SERIAL_NUMBER).orElse(null);

            String chassisSerial = chassis.getOptionalAsString(DMI_KEY_SERIAL_NUMBER).orElse(null);
            String chassisTag = chassis.getOptionalAsString(DMI_KEY_ASSET_TAG).orElse(null);

            Dmi dmi = Optional.ofNullable(server.getDmi()).orElseGet(Dmi::new);

            dmi.setSystem(joinNonBlank(productName, systemVersion));
            dmi.setProduct(productName);
            if (!allAbsent(biosVendor, biosVersion, biosReleaseDate)) {
                dmi.setBios(biosVendor, biosVersion, biosReleaseDate);
            }
            dmi.setVendor(biosVendor);
            dmi.setBoard(joinNonBlank(boardManufacturer, boardName));
            dmi.setAsset(String.format(DMI_ASSET_FORMAT,
                    Objects.toString(chassisSerial, StringUtils.EMPTY),
                    Objects.toString(chassisTag, StringUtils.EMPTY),
                    Objects.toString(boardSerial, StringUtils.EMPTY),
                    Objects.toString(systemSerial, StringUtils.EMPTY)
            ));

            dmi.setServer(server);
            server.setDmi(dmi);

            return Optional.empty();
        }
        catch (Exception e) {
            LOG.error("Failed to map DMI info for minion {} : {}", server.getMinionId(), e);
            return Optional.of("DMI mapping failed: " + e.getMessage());
        }
    }

    /**
     * Join non-blank parts with a single space.
     *
     * @param parts the parts to join
     * @return the joined parts or null if all of them are blank
     */
    private static String joinNonBlank(String... parts) {
        return StringUtils.trimToNull(Stream.of(parts)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.joining(StringUtils.SPACE)));
    }

}
