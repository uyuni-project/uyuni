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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.proxy.event;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.NotificationData;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;

import com.suse.salt.netapi.datatypes.Event;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyBackupEvent {
    private static final Logger LOG = LogManager.getLogger(ProxyBackupEvent.class);
    private static final Pattern PATTERN = Pattern.compile("^suse/proxy/backup_finished");

    private final MinionServer minion;
    private final List<String> files;

    /**
     * Creates a new ProxyBackupEvent
     * @param minionIn the id of the minion sending the event
     * @param dataIn data containing more information about this event
     */
    private ProxyBackupEvent(MinionServer minionIn, List<String> dataIn) {
        this.minion = minionIn;
        this.files = dataIn;
    }

    /**
     * The id of the minion that initiated the event
     *
     * @return the minion id
     */
    public MinionServer getMinion() {
        return minion;
    }

    /**
     * Returns files uploaded to the server
     *
     * @return the list of files
     */
    public List<String> getFiles() {
            return this.files;
        }

    /**
     * Utility method to parse e generic event to a more specific one
     * @param event the generic event to parse
     * @return an option containing the parsed value or non if it could not be parsed
     */
    public static Optional<ProxyBackupEvent> parse(Event event) {
        Matcher matcher = PATTERN.matcher(event.getTag());
        if (matcher.matches()) {
            LOG.debug("Processing {}", event.getTag());
            Map<String, List<String>> data;
            final String minionId = (String)event.getData().get("id");

            // First find the minion is present
            Optional<MinionServer> minion = MinionServerFactory.findByMinionId(minionId);
            if (minion.isEmpty()) {
                LOG.error("Proxy minion {} not found!", minion);
                NotificationMessage msg = UserNotificationFactory.createNotificationMessage(new NotificationData() {
                    @Override
                    public String getSummary() {
                        return LocalizationService.getInstance().getMessage("notification.proxybackupfailed");
                    }

                    @Override
                    public String getDetails() {
                        return LocalizationService.getInstance()
                                .getMessage("notification.proxybackupnominion", minionId);
                    }
                });
                UserNotificationFactory.storeNotificationMessageFor(msg, RoleFactory.SAT_ADMIN);
                return Optional.empty();
            }

            try {
                data = (Map<String, List<String>>) event.getData().get("data");
            }
            catch (ClassCastException e) {
                LOG.error("Parsing failed of data for minion {}", minionId, e);
                return Optional.empty();
            }
            // Validate input data are not empty and correct format
            LOG.debug("Data: {}", data);

            if (data.isEmpty()) {
                LOG.error("Empty ProxyBackupEvent data");
                return Optional.empty();
            }

            List<String> files = data.get("files");
            if (files == null) {
                LOG.error("No files section in the event data");
                return Optional.empty();
            }

            ProxyBackupEvent result = new ProxyBackupEvent(
                    minion.get(),
                    files
            );
            return Optional.of(result);
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("minion", minion)
                .append("files", files)
                .toString();
    }
}
