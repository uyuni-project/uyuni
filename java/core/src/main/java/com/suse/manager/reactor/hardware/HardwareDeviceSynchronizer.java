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
package com.suse.manager.reactor.hardware;

import static com.suse.utils.Predicates.isAbsent;

import com.redhat.rhn.domain.server.Device;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Synchronizes hardware devices from udevdb with the minion server.
 */
public class HardwareDeviceSynchronizer {

    private static final Logger LOG = LogManager.getLogger(HardwareDeviceSynchronizer.class);

    private final MinionServer server;

    /**
     * Create a device mapper.
     *
     * @param serverIn the minion server
     */
    public HardwareDeviceSynchronizer(MinionServer serverIn) {
        this.server = serverIn;
    }

    /**
     * Maps hardware devices from udevdb.
     *
     * @param udevdb the udev database entries
     * @return Optional error message if mapping failed
     */
    public Optional<String> mapDevices(List<Map<String, Object>> udevdb) {
        try {
            // Remove any existing devices in case we're refreshing the hw info
            for (Device device : server.getDevices()) {
                ServerFactory.delete(device);
            }
            server.getDevices().clear();

            if (isAbsent(udevdb)) {
                String error = "Devices: Salt module 'udevdb.exportdb' returned an empty list";
                LOG.error("{} for minion: {}", error, server.getMinionId());
                return Optional.of(error);
            }

            udevdb.forEach(dbdev -> {
                Device device = new DeviceMapper().mapDevice(udevdb, dbdev);
                if (device == null) {
                    return;
                }

                device.setServer(server);
                server.getDevices().add(device);
            });

            return Optional.empty();
        }
        catch (Exception e) {
            LOG.error("Failed to map devices for minion {} : {} ", server.getMinionId(), e);
            return Optional.of("Device mapping failed: " + e.getMessage());
        }
    }

}
