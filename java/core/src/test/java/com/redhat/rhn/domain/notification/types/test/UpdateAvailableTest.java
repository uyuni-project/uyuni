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
package com.redhat.rhn.domain.notification.types.test;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.util.download.DownloadException;
import com.redhat.rhn.domain.notification.types.NotificationSeverity;
import com.redhat.rhn.domain.notification.types.NotificationType;
import com.redhat.rhn.domain.notification.types.UpdateAvailable;
import com.redhat.rhn.testing.MockObjectTestCase;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.util.stream.Stream;

@SuppressWarnings("java:S1192")
public class UpdateAvailableTest extends MockObjectTestCase {
    private static final String FAILED_TO_EXTRACT_VERSION_MESSAGE_PREFIX =
            "Failed to extract version from release notes from";
    private static final String RELEASE_NOTES_HTML_ELEMENT =
            "<html>Release Notes for v<span id=\"current_version\">%s</span>.</html>";

    private final ConfigDefaults configDefaults = ConfigDefaults.get();

    @BeforeEach
    public void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @AfterEach
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        setConfigDefaultsInstance(configDefaults);
    }

    /**
     * Test hasUpdateAvailable when a new semantic version is available.
     */
    @Test
    public void testSuccessHasUpdateAvailableWhenNewSemanticVersion() {
        // Set product version to version 5.0.1
        mockConfigDefaults(false, "5.0.1");

        // "Mock" the release notes to contain a newer version
        UpdateAvailable updateAvailable = new UpdateAvailable() {
            @Override
            public String getReleaseNotes() {
                return String.format(RELEASE_NOTES_HTML_ELEMENT, "5.3.2");
            }

            @Override
            public NotificationType getType() {
                return NotificationType.UPDATE_AVAILABLE;
            }
        };

        assertTrue(updateAvailable.hasUpdateAvailable());
        assertEquals(NotificationType.UPDATE_AVAILABLE, updateAvailable.getType());
        assertEquals(NotificationSeverity.WARNING, updateAvailable.getSeverity());
        assertEquals("Updates are available.", updateAvailable.getSummary());
    }

    /**
     * Test hasUpdateAvailable when no new semantic version is available.
     */
    @Test
    public void testSuccessHasUpdateAvailableWhenNoNewSemanticVersion() {
        final String currentVersion = "5.0.1";

        // Set current product version
        mockConfigDefaults(false, currentVersion);

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
     * Test hasUpdateAvailable when a new data driven version is available.
     */
    @Test
    public void testSuccessHasUpdateAvailableWhenNewDataDrivenVersion() {
        // Set current product version
        mockConfigDefaults(true, "2024.07");

        // "Mock" the release notes to contain a newer version
        UpdateAvailable updateAvailable = new UpdateAvailable() {
            @Override
            public String getReleaseNotes() {
                return String.format(RELEASE_NOTES_HTML_ELEMENT, "2024.08");
            }

            @Override
            public NotificationType getType() {
                return NotificationType.UPDATE_AVAILABLE;
            }
        };

        assertTrue(updateAvailable.hasUpdateAvailable());
        assertEquals(NotificationType.UPDATE_AVAILABLE, updateAvailable.getType());
        assertEquals(NotificationSeverity.WARNING, updateAvailable.getSeverity());
        assertEquals("Updates are available.", updateAvailable.getSummary());
    }

    /**
     * Test hasUpdateAvailable when no new data driven version is available.
     */
    @Test
    public void testSuccessHasUpdateAvailableWhenNoNewDataDrivenVersion() {
        final String currentVersion = "2024.07";

        // Set current product version
        mockConfigDefaults(true, "2024.07");

        // "Mock" the release notes to the same version
        UpdateAvailable updateAvailable = new UpdateAvailable() {
            @Override
            public String getReleaseNotes() {
                return String.format(RELEASE_NOTES_HTML_ELEMENT, currentVersion);
            }
        };

        assertFalse(updateAvailable.hasUpdateAvailable());
    }

    @Test
    public void testReleaseNotesUrlDataDrivenVersion() {
        mockConfigDefaults(true, "2024.07");

        UpdateAvailable updateAvailable = new UpdateAvailable();
        assertEquals("https://www.uyuni-project.org/pages/stable-version.html",
                updateAvailable.getReleaseNotesUrl());
    }

    @Test
    public void testReleaseNotesUrlSemanticVersion() {
        mockConfigDefaults(false, "5.1.1");

        UpdateAvailable updateAvailable = new UpdateAvailable();
        assertEquals("https://www.suse.com/releasenotes/x86_64/multi-linux-manager/5.1/index.html",
                updateAvailable.getReleaseNotesUrl());
    }

    /**
     * Test the failure of the method hasUpdateAvailable when the retrieved released notes belong to another product.
     */
    @Test
    public void testFailureHasUpdateAvailableWhenDifferentProductReleaseNotes() {
        // Set current product version
        mockConfigDefaults(true, "2024.07");

        // "Mock" the release notes to retrieve a semantic version
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
    public void testFailureHasUpdateAvailableWhenNoVersionDetected() {
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
    public void testFailureHasUpdateAvailableWhenDownloadFails() {
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
        mockConfigDefaults(true, "2024.07");

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


    @SuppressWarnings({"java:S1171", "java:S3599"})
    private void mockConfigDefaults(boolean expectedIsUyuni, String expectedProductVersion) {
        ConfigDefaults mockConfigDefaults = context.mock(ConfigDefaults.class);

        context.checking(new Expectations() {{
            allowing(mockConfigDefaults).getProductName();
            will(returnValue(expectedIsUyuni ? "Uyuni" : "SUSE Manager"));
            allowing(mockConfigDefaults).isUyuni();
            will(returnValue(expectedIsUyuni));
            allowing(mockConfigDefaults).getProductVersion();
            will(returnValue(expectedProductVersion));
            allowing(mockConfigDefaults).getDefaultLocale();
            will(returnValue("en_US"));
        }});

        try {
            setConfigDefaultsInstance(mockConfigDefaults);
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Overrides the ConfigDefaults instance
     * @param configDefaultsIn the ConfigDefaults instance
     * @throws NoSuchFieldException if a field with the specified name is not found.
     * @throws IllegalAccessException if the field is not accessible.
     */
    @SuppressWarnings("java:S3011")
    private static void setConfigDefaultsInstance(ConfigDefaults configDefaultsIn)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = ConfigDefaults.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, configDefaultsIn);
    }

}
