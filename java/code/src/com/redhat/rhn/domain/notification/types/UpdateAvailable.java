/*
 * Copyright (c) 2023--2024 SUSE LLC
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
package com.redhat.rhn.domain.notification.types;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.download.DownloadException;
import com.redhat.rhn.common.util.download.DownloadUtils;
import com.redhat.rhn.domain.notification.NotificationMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

/**
 * Notification data for an update being available for the server.
 */
public class UpdateAvailable implements NotificationData, Serializable {

    private static final LocalizationService LOCALIZATION_SERVICE = LocalizationService.getInstance();
    private static final Logger LOG = LogManager.getLogger(UpdateAvailable.class);

    private static final String VERSION_ELEMENT_START = "<span";
    private static final String VERSION_ELEMENT_ID_ATTRIBUTE = "id=\"current_version\"";
    private static final String VERSION_ELEMENT_END = "</span>";

    private final boolean isUyuni = ConfigDefaults.get().isUyuni();
    private final Version version = new Version(
            ConfigDefaults.get().getProductVersion(),
            ConfigDefaults.get().isUyuni()
    );

    /**
     * returns true if there is a newer version available.
     *
     * @return boolean
     **/
    public boolean hasUpdateAvailable() {
        try {
            String releaseNotesHtmlContent = getReleaseNotes();
            Version latestVersion = new Version(extractVersion(releaseNotesHtmlContent), isUyuni);
            return latestVersion.isNewerThan(version);
        }
        catch (IllegalArgumentException | DownloadException e) {
            throw new RhnRuntimeException(
                    "Failed to extract version from release notes from " + getReleaseNotesUrl(), e
            );
        }
    }

    @Override
    public NotificationMessage.NotificationMessageSeverity getSeverity() {
        return NotificationMessage.NotificationMessageSeverity.WARNING;
    }

    @Override
    public NotificationType getType() {
        return NotificationType.UpdateAvailable;
    }

    @Override
    public String getSummary() {
        return LOCALIZATION_SERVICE.getMessage("notification.updateavailable.summary");
    }

    @Override
    public String getDetails() {
        return LOCALIZATION_SERVICE.getMessage("notification.updateavailable.detail", getReleaseNotesUrl());
    }

    /**
     * Downloads the release notes.
     *
     * @return the release notes
     */
    public String getReleaseNotes() {
        return DownloadUtils.downloadUrl(getReleaseNotesUrl());
    }

    /**
     * Returns the URL to the release notes, considering the product.
     *
     * @return the URL to the release notes
     */
    public String getReleaseNotesUrl() {
        return isUyuni ? "https://www.uyuni-project.org/pages/stable-version.html" :
                "https://www.suse.com/releasenotes/x86_64/multi-linux-manager/" +
                        version.getMajor() + "." + version.getMinor() +
                        "/index.html";
    }

    /**
     * Finds and retrieves the content of the first occurrence of a span element that has "current_version" as id
     *
     * @param html the HTML content
     * @return the version as string
     */
    private static String extractVersion(String html) {
        int startIndex = html.indexOf(VERSION_ELEMENT_START);
        if (startIndex == -1) {
            throw new IllegalArgumentException("Start tag " + VERSION_ELEMENT_START + " not found!");
        }

        while (startIndex != -1) {
            // Find the closing '>' of the opening <span> tag
            int tagEndIndex = html.indexOf(">", startIndex);
            if (tagEndIndex == -1) {
                throw new IllegalArgumentException("Malformed HTML: Missing '>' in <span> tag!");
            }

            // Check if this is the span element who has id="current_version"
            String spanTag = html.substring(startIndex, tagEndIndex + 1);
            if (spanTag.contains(VERSION_ELEMENT_ID_ATTRIBUTE)) {
                // Find the closing </span>
                int endIndex = html.indexOf(VERSION_ELEMENT_END, tagEndIndex);
                if (endIndex == -1) {
                    throw new IllegalArgumentException("Matching tag " + VERSION_ELEMENT_END + " not found!");
                }

                return html.substring(tagEndIndex + 1, endIndex).trim();
            }

            // Look for the next span open tag
            startIndex = html.indexOf(VERSION_ELEMENT_START, tagEndIndex);
        }

        return "Span element with id='current_version' not found!";
    }

}
