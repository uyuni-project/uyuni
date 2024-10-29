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
package com.redhat.rhn.domain.notification.types.test;


import static com.redhat.rhn.common.conf.ConfigDefaults.PRODUCT_NAME;
import static com.redhat.rhn.common.conf.ConfigDefaults.PRODUCT_VERSION_MGR;
import static com.redhat.rhn.common.conf.ConfigDefaults.PRODUCT_VERSION_UYUNI;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.util.download.DownloadException;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.types.NotificationType;
import com.redhat.rhn.domain.notification.types.UpdateAvailable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class UpdateAvailableTest {
    private static final String FAILED_TO_EXTRACT_VERSION_MESSAGE_PREFIX =
            "Failed to extract version from release notes from";
    private static final String RELEASE_NOTES_HTML_ELEMENT =
            "<html>Release Notes for v<span id=\"current_version\">%s</span>.</html>";
    private static final String SUMA = "SUSE Manager";
    private static final String UYUNI = "Uyuni";

    /**
     * Set up the test environment.
     * Used to avoid property product_name overriding web.product_name.
     */
    @BeforeEach
    public void setUp() {
        Config.get().remove("product_name");
    }

    /**
     * Test hasUpdateAvailable when a new version of SUMA is available.
     */
    @Test
    public void testSuccessSumaWhenUpdateAvailable() {
        // Set current product version
        Config.get().setString(PRODUCT_NAME, SUMA);
        Config.get().setString(PRODUCT_VERSION_MGR, "5.0.1");

        // "Mock" the release notes to contain a newer version
        UpdateAvailable updateAvailable = new UpdateAvailable() {
            @Override
            public String getReleaseNotes() {
                return String.format(RELEASE_NOTES_HTML_ELEMENT, "5.3.2");
            }
        };

        assertTrue(updateAvailable.hasUpdateAvailable());
        assertEquals(NotificationType.UpdateAvailable, updateAvailable.getType());
        assertEquals(NotificationMessage.NotificationMessageSeverity.warning, updateAvailable.getSeverity());
        assertEquals("Updates are available.", updateAvailable.getSummary());
    }

    /**
     * Test hasUpdateAvailable when no new version of SUMA is available.
     */
    @Test
    public void testSuccessSumaWhenNoUpdateAvailable() {
        final String currentVersion = "5.0.1";

        // Set current product version
        Config.get().setString(PRODUCT_NAME, SUMA);
        Config.get().setString(PRODUCT_VERSION_MGR, currentVersion);

        // "Mock" the release notes to contain the same version
        UpdateAvailable updateAvailable = new UpdateAvailable() {
            @Override
            public String getReleaseNotes() {
                return String.format(RELEASE_NOTES_HTML_ELEMENT, currentVersion);
            }
        };

        assertFalse(updateAvailable.hasUpdateAvailable());
    }

    /**
     * Test hasUpdateAvailable when a new version of Uyuni is available.
     */
    @Test
    public void testSuccessUyuniWhenUpdateAvailable() {
        // Set current product version
        Config.get().setString(PRODUCT_NAME, UYUNI);
        Config.get().setString(PRODUCT_VERSION_UYUNI, "2024.07");

        // "Mock" the release notes to contain a newer version
        UpdateAvailable updateAvailable = new UpdateAvailable() {
            @Override
            public String getReleaseNotes() {
                return String.format(RELEASE_NOTES_HTML_ELEMENT, "2024.08");
            }
        };

        assertTrue(updateAvailable.hasUpdateAvailable());
        assertEquals(NotificationType.UpdateAvailable, updateAvailable.getType());
        assertEquals(NotificationMessage.NotificationMessageSeverity.warning, updateAvailable.getSeverity());
        assertEquals("Updates are available.", updateAvailable.getSummary());
    }

    /**
     * Test the success of the method hasUpdateAvailable when no new version of Uyuni is available.
     */
    @Test
    public void testSuccessUyuniWhenNoUpdateAvailable() {
        final String currentVersion = "2024.07";

        // Set current product version
        Config.get().setString(PRODUCT_NAME, UYUNI);
        Config.get().setString(PRODUCT_VERSION_UYUNI, currentVersion);

        // "Mock" the release notes to the same version
        UpdateAvailable updateAvailable = new UpdateAvailable() {
            @Override
            public String getReleaseNotes() {
                return String.format(RELEASE_NOTES_HTML_ELEMENT, currentVersion);
            }
        };

        assertFalse(updateAvailable.hasUpdateAvailable());
    }

    /**
     * Test the failure of the method hasUpdateAvailable when the retrieved released notes belong to another product.
     */
    @Test
    public void testFailureUyuniWhenWrongReleaseNotesRetrieved() {
        // Set current product version
        Config.get().setString(PRODUCT_NAME, UYUNI);
        Config.get().setString(PRODUCT_VERSION_UYUNI, "2024.07");

        // "Mock" the release notes to retrieve a SUMA version
        UpdateAvailable updateAvailable = new UpdateAvailable() {
            @Override
            public String getReleaseNotes() {
                return String.format(RELEASE_NOTES_HTML_ELEMENT, "5.0.1");
            }
        };

        RhnRuntimeException thrown = assertThrows(RhnRuntimeException.class, updateAvailable::hasUpdateAvailable);

        assertTrue(thrown.getMessage().contains(FAILED_TO_EXTRACT_VERSION_MESSAGE_PREFIX));
    }

    /**
     * Test the failure of the method hasUpdateAvailable when no version was extracted.
     */
    @Test
    public void testFailureUyuniWhenNoVersionDetected() {
        UpdateAvailable updateAvailable = new UpdateAvailable() {
            @Override
            public String getReleaseNotes() {
                return "";
            }
        };

        RhnRuntimeException thrown = assertThrows(RhnRuntimeException.class, updateAvailable::hasUpdateAvailable);

        assertTrue(thrown.getMessage().contains(FAILED_TO_EXTRACT_VERSION_MESSAGE_PREFIX));
    }

    /**
     * Test the failure of the method hasUpdateAvailable when no version can be extracted from html
     * @param releaseNotesHtml the html content retrieved
     */
    @ParameterizedTest
    @MethodSource("invalidHtmlData")
    public void testFailureWhenInvalidHtml(String releaseNotesHtml) {
        UpdateAvailable updateAvailable = new UpdateAvailable() {
            @Override
            public String getReleaseNotes() {
                return releaseNotesHtml;
            }
        };

        RhnRuntimeException thrown = assertThrows(RhnRuntimeException.class, updateAvailable::hasUpdateAvailable);

        assertTrue(thrown.getMessage().contains(FAILED_TO_EXTRACT_VERSION_MESSAGE_PREFIX));

    }

    static Stream<Arguments> invalidHtmlData() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("<html>Release Notes for v<div id=\"current_version\">2024-09</div>"),
                Arguments.of("<html>Release Notes for v<span id=\"current_version\">"),
                Arguments.of("<html>Release Notes for v<span id=\"current_version\" 2024-10</span>"),
                Arguments.of("<html>Release Notes for v<span 2024-11</span"),
                Arguments.of("<html>Release Notes for v<span id=\"otherid\">2024-08</span>")
        );
    }


    /**
     * Test the failure of the method hasUpdateAvailable when no version was extracted.
     */
    @Test
    public void testFailureUyuniWhenDownloadFails() {
        UpdateAvailable updateAvailable = new UpdateAvailable() {
            @Override
            public String getReleaseNotes() {
                throw new DownloadException("dummy", new RuntimeException("dummy"));
            }
        };

        RhnRuntimeException thrown = assertThrows(RhnRuntimeException.class, updateAvailable::hasUpdateAvailable);

        assertTrue(thrown.getMessage().contains(FAILED_TO_EXTRACT_VERSION_MESSAGE_PREFIX));
    }

    /**
     * Test method hasUpdateAvailable is able to extract versions event when the element has more attributes than
     * expected
     * @param releaseNotesHtml the html content retrieved
     */
    @ParameterizedTest
    @MethodSource("validHtmlData")
    public void testSuccessWhenValidHtml(String releaseNotesHtml) {
        // Set current product version
        Config.get().setString(PRODUCT_NAME, UYUNI);
        Config.get().setString(PRODUCT_VERSION_UYUNI, "2024.07");

        UpdateAvailable updateAvailable = new UpdateAvailable() {
            @Override
            public String getReleaseNotes() {
                return releaseNotesHtml;
            }
        };

        assertDoesNotThrow(updateAvailable::hasUpdateAvailable);
    }

    static Stream<Arguments> validHtmlData() {
        return Stream.of(
                Arguments.of("<span id=\"current_version\">2025.01</span>"),
                Arguments.of("...<span id=\"current_version\">2025.02</span>...not html"),
                Arguments.of("<div><span class=\"some classes\" id=\"current_version\" " +
                        "style=\"display:none\">2025.03</span></div>"),
                Arguments.of("<span id=\"current_version\">2025.04</span><span id=\"current_version\">2025.05</span>")
        );
    }

}
