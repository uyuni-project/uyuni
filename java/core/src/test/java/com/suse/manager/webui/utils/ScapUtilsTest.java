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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
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
        Map<String, Object> parsed = ScapUtils.parseOscapParameters(PROFILE + " --results-arf /tmp/arf.xml");
        assertFalse(parsed.containsKey("results_arf"));
        assertTrue(parsed.isEmpty());
    }

    @Test
    public void testProfileAndTailoringAreLeftToTheCaller() {
        // Both callers map those to different pillar entries, so they are not parsed here.
        Map<String, Object> parsed = ScapUtils.parseOscapParameters(
                PROFILE + " --tailoring-file /etc/openscap/tailoring.xml --tailoring-id component");
        assertFalse(parsed.containsKey("profile"));
        assertFalse(parsed.containsKey("tailoring_file"));
        assertFalse(parsed.containsKey("tailoring_id"));
    }

    @Test
    public void testRoundTripWithTheParametersBuiltByTheUi() {
        String parameters = ScapUtils.buildOscapParameters("xccdf_org.ssgproject.content_profile_cis",
                "/etc/openscap/tailoring.xml", "xccdf_tailoring_profile", "--skip-rule " + RULE, true);
        Map<String, Object> parsed = ScapUtils.parseOscapParameters(parameters);

        assertEquals(RULE, parsed.get("skip_rule"));
        assertEquals(true, parsed.get("fetch_remote_resources"));
    }
}
