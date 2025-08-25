/*
 * Copyright (c) 2023--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.notification.types;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.download.DownloadException;
import com.redhat.rhn.common.util.http.HttpClientAdapter;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * Notification data for an update being available for the server.
 */
public class UpdateAvailable implements NotificationData, Serializable {

    private static final LocalizationService LOCALIZATION_SERVICE = LocalizationService.getInstance();

    private static final String VERSION_ELEMENT_START = "<span";
    private static final String VERSION_ELEMENT_ID_ATTRIBUTE = "id=\"current_version\"";
    private static final String VERSION_ELEMENT_END = "</span>";

    private static final String RELEASE_NOTES_URL_UYUNI = "https://www.uyuni-project.org/pages/stable-version.html";
    private static final String RELEASE_NOTES_URL_SUMA =
            "https://www.suse.com/releasenotes/x86_64/multi-linux-manager/";

    private final ManagerVersion managerVersion = new ManagerVersion();

    /**
     * returns true if there is a newer version available.
     *
     * @return boolean
     **/
    public boolean hasUpdateAvailable() {
        try {
            String releaseNotesHtmlContent = getReleaseNotes();
            ManagerVersion latestManagerVersion = new ManagerVersion(extractVersion(releaseNotesHtmlContent));
            return latestManagerVersion.isNewerThan(managerVersion);
        }
        catch (IllegalArgumentException | DownloadException e) {
            throw new RhnRuntimeException(
                    "Failed to extract version from release notes from " + getReleaseNotesUrl(), e
            );
        }
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
    public String getReleaseNotes() throws DownloadException {
        HttpClientAdapter httpClient = new HttpClientAdapter();
        String url = getReleaseNotesUrl();
        HttpGet request = new HttpGet(url);
        try {
            HttpResponse res = httpClient.executeRequest(request);
            int statusCode = res.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new DownloadException(url, res.getStatusLine().getReasonPhrase(), statusCode);
            }
            return IOUtils.toString(
                    res.getEntity().getContent(),
                    StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new DownloadException(url, e.getMessage(), 500);
        }
        finally {
            request.releaseConnection();
        }
    }

    /**
     * Returns the URL to the release notes, considering the product.
     *
     * @return the URL to the release notes
     */
    public String getReleaseNotesUrl() {
        return managerVersion.isUyuni() ? RELEASE_NOTES_URL_UYUNI :
                RELEASE_NOTES_URL_SUMA + managerVersion.getMajor() + "." +
                managerVersion.getMinor() + "/index.html";
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
