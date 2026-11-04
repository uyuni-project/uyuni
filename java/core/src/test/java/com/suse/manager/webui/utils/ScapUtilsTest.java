/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.suse.manager.webui.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * Tests for the parsing of the oscap parameters.
 */
public class ScapUtilsTest {

    private static final String PROFILE = "--profile xccdf_org.ssgproject.content_profile_cis";
    private static final String RULE = "xccdf_org.ssgproject.content_rule_package_telnet_removed";
    private static final String OTHER_RULE = "xccdf_org.ssgproject.content_rule_package_tftp_removed";

    @Test
    public void testEmptyParameters() {
        assertTrue(ScapUtils.parseOscapParameters(null).isEmpty());
        assertTrue(ScapUtils.parseOscapParameters("").isEmpty());
    }

    @Test
    public void testSingleSkipRuleIsAString() {
        Map<String, Object> parsed = ScapUtils.parseOscapParameters(PROFILE + " --skip-rule " + RULE);
        assertEquals(RULE, parsed.get("skip_rule"));
    }

    @Test
    public void testRepeatedSkipRulesAreAList() {
        Map<String, Object> parsed = ScapUtils.parseOscapParameters(
                PROFILE + " --skip-rule " + RULE + " --skip-rule " + OTHER_RULE);
        assertEquals(List.of(RULE, OTHER_RULE), parsed.get("skip_rule"));
    }

    @Test
    public void testRepeatedRulesAreAList() {
        Map<String, Object> parsed = ScapUtils.parseOscapParameters("--rule " + RULE + " --rule " + OTHER_RULE);
        assertEquals(List.of(RULE, OTHER_RULE), parsed.get("rule"));
    }

    @Test
    public void testSingleRuleStaysAStringForOlderMinions() {
        Map<String, Object> parsed = ScapUtils.parseOscapParameters("--rule " + RULE);
        assertEquals(RULE, parsed.get("rule"));
    }

    @Test
    public void testSkipRuleIsNotParsedAsARule() {
        Map<String, Object> parsed = ScapUtils.parseOscapParameters(PROFILE + " --skip-rule " + RULE);
        assertFalse(parsed.containsKey("rule"));
    }

    @Test
    public void testContentSelectionOptions() {
        Map<String, Object> parsed = ScapUtils.parseOscapParameters(
                "--reference stigid:RHEL-09-211010 " +
                "--cpe /usr/share/openscap/cpe/openscap-cpe-dict.xml " +
                "--local-files /var/cache/openscap " +
                "--datastream-id scap_org.open-scap_datastream_from_xccdf " +
                "--xccdf-id scap_org.open-scap_cref_xccdf.xml " +
                "--benchmark-id xccdf_org.ssgproject.content_benchmark_RHEL-9");

        assertEquals("stigid:RHEL-09-211010", parsed.get("reference"));
        assertEquals("/usr/share/openscap/cpe/openscap-cpe-dict.xml", parsed.get("cpe"));
        assertEquals("/var/cache/openscap", parsed.get("local_files"));
        assertEquals("scap_org.open-scap_datastream_from_xccdf", parsed.get("datastream_id"));
        assertEquals("scap_org.open-scap_cref_xccdf.xml", parsed.get("xccdf_id"));
        assertEquals("xccdf_org.ssgproject.content_benchmark_RHEL-9", parsed.get("benchmark_id"));
    }

    @Test
    public void testFlags() {
        Map<String, Object> parsed = ScapUtils.parseOscapParameters(
                PROFILE + " --fetch-remote-resources --remediate");
        assertEquals(true, parsed.get("fetch_remote_resources"));
        assertEquals(true, parsed.get("remediate"));

        Map<String, Object> withoutFlags = ScapUtils.parseOscapParameters(PROFILE);
        assertFalse(withoutFlags.containsKey("fetch_remote_resources"));
        assertFalse(withoutFlags.containsKey("remediate"));
    }

    @Test
    public void testUnsupportedOptionsAreIgnored() {
        Map<String, Object> parsed = ScapUtils.parseOscapParameters(
                "--results-arf /tmp/arf.xml --report /tmp/report.html");
        assertTrue(parsed.isEmpty());
    }

    @Test
    public void testValuesAreNotTruncated() {
        Map<String, Object> parsed = ScapUtils.parseOscapParameters(
                "--cpe /tmp/cpe+custom.xml --reference nist:CM-6(a) --local-files /srv/scap+files");

        assertEquals("/tmp/cpe+custom.xml", parsed.get("cpe"));
        assertEquals("nist:CM-6(a)", parsed.get("reference"));
        assertEquals("/srv/scap+files", parsed.get("local_files"));
    }

    @Test
    public void testProfile() {
        Map<String, Object> parsed = ScapUtils.parseOscapParameters(PROFILE);
        assertEquals("xccdf_org.ssgproject.content_profile_cis", parsed.get("profile"));
        assertFalse(parsed.containsKey("tailoring_filename"));
    }

    @Test
    public void testTailoringProfileReplacesTheProfile() {
        Map<String, Object> parsed = ScapUtils.parseOscapParameters(PROFILE +
                " --tailoring-file /srv/tailoring-files/3/tailoring.xml --tailoring-profile-id xccdf_tailored");

        assertEquals("xccdf_tailored", parsed.get("profile"));
        assertEquals("tailoring.xml", parsed.get("tailoring_filename"));
    }

    @Test
    public void testTailoringIdSelectsADataStreamComponent() {
        // "tailoring_id" is the id of the tailoring file transferred from the server, it must not be
        // overwritten by the oscap option of the same name
        Map<String, Object> parsed = ScapUtils.parseOscapParameters(PROFILE + " --tailoring-id component");

        assertEquals("component", parsed.get("tailoring_component_id"));
        assertFalse(parsed.containsKey("tailoring_id"));
    }

    @Test
    public void testRoundTripWithTheParametersBuiltByTheUi() {
        String parameters = ScapUtils.buildOscapParameters("xccdf_org.ssgproject.content_profile_cis",
                "/etc/openscap/tailoring.xml", "xccdf_tailoring_profile", "--skip-rule " + RULE, true);
        Map<String, Object> parsed = ScapUtils.parseOscapParameters(parameters);

        assertEquals("xccdf_tailoring_profile", parsed.get("profile"));
        assertEquals("tailoring.xml", parsed.get("tailoring_filename"));
        assertEquals(RULE, parsed.get("skip_rule"));
        assertEquals(true, parsed.get("fetch_remote_resources"));
    }
}
