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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for SCAP-related operations
 */
public class ScapUtils {

    private static final Pattern RULE = optionPattern("--rule", "[\\w.-]+");
    private static final Pattern SKIP_RULE = optionPattern("--skip-rule", "[\\w.-]+");
    private static final Pattern REFERENCE = optionPattern("--reference", "[\\w.:-]+");
    private static final Pattern CPE = optionPattern("--cpe", "[\\w./-]+");
    private static final Pattern LOCAL_FILES = optionPattern("--local-files", "[\\w./-]+");
    private static final Pattern DATASTREAM_ID = optionPattern("--datastream-id", "[\\w.-]+");
    private static final Pattern XCCDF_ID = optionPattern("--xccdf-id", "[\\w.-]+");
    private static final Pattern BENCHMARK_ID = optionPattern("--benchmark-id", "[\\w.-]+");

    private ScapUtils() {
        // Utility class, no instantiation
    }

    private static Pattern optionPattern(String option, String valuePattern) {
        return Pattern.compile("(?:^|\\s)" + option + "\\s+(" + valuePattern + ")");
    }

    /**
     * Builds the oscap command-line parameters string
     *
     * @param xccdfProfileId XCCDF profile ID (required)
     * @param tailoringFileName Tailoring file name (optional)
     * @param tailoringProfileId Tailoring profile ID (optional)
     * @param advancedArgs Advanced arguments (optional)
     * @param fetchRemoteResources Whether to fetch remote resources
     * @return the formatted parameters for oscap command
     */
    public static String buildOscapParameters(String xccdfProfileId, String tailoringFileName,
                                              String tailoringProfileId, String advancedArgs,
                                              boolean fetchRemoteResources) {
        StringBuilder params = new StringBuilder();
        if (StringUtils.isNotEmpty(xccdfProfileId)) {
            params.append("--profile ").append(xccdfProfileId);
        }
        if (StringUtils.isNotEmpty(tailoringFileName)) {
            params.append(" --tailoring-file ").append(tailoringFileName);
            if (StringUtils.isNotEmpty(tailoringProfileId)) {
                params.append(" --tailoring-profile-id ").append(tailoringProfileId);
            }
        }
        if (StringUtils.isNotEmpty(advancedArgs)) {
            params.append(" ").append(advancedArgs);
        }
        if (fetchRemoteResources) {
            params.append(" --fetch-remote-resources");
        }

        return params.toString().trim();
    }

    /**
     * Extracts the oscap options that the Salt states can forward to the scanned system.
     *
     * The oscap command line is rebuilt on the minion from the pillar, it is not passed through, so
     * an option that is not extracted here never reaches oscap. Only the options selecting which
     * rules are evaluated and where the content comes from are extracted: the ones writing the
     * result files are set by the state itself, since the server collects and parses them.
     *
     * @param parameters the oscap parameters, as entered by the user
     * @return the parsed options, keyed by the pillar entry name
     */
    public static Map<String, Object> parseOscapParameters(String parameters) {
        Map<String, Object> parsed = new HashMap<>();
        if (StringUtils.isEmpty(parameters)) {
            return parsed;
        }

        putRepeatable(parsed, "rule", RULE, parameters);
        putRepeatable(parsed, "skip_rule", SKIP_RULE, parameters);
        putSingle(parsed, "reference", REFERENCE, parameters);
        putSingle(parsed, "cpe", CPE, parameters);
        putSingle(parsed, "local_files", LOCAL_FILES, parameters);
        putSingle(parsed, "datastream_id", DATASTREAM_ID, parameters);
        putSingle(parsed, "xccdf_id", XCCDF_ID, parameters);
        putSingle(parsed, "benchmark_id", BENCHMARK_ID, parameters);

        if (parameters.contains("--fetch-remote-resources")) {
            parsed.put("fetch_remote_resources", true);
        }
        if (parameters.contains("--remediate")) {
            parsed.put("remediate", true);
        }

        return parsed;
    }

    private static void putSingle(Map<String, Object> parsed, String key, Pattern pattern, String parameters) {
        Matcher matcher = pattern.matcher(parameters);
        if (matcher.find()) {
            parsed.put(key, matcher.group(1));
        }
    }

    private static void putRepeatable(Map<String, Object> parsed, String key, Pattern pattern, String parameters) {
        List<String> values = new ArrayList<>();
        Matcher matcher = pattern.matcher(parameters);
        while (matcher.find()) {
            values.add(matcher.group(1));
        }

        if (values.size() == 1) {
            // A single value stays a plain string: a minion whose Salt module does not support
            // lists yet then keeps building the very same command as before.
            parsed.put(key, values.get(0));
        }
        else if (!values.isEmpty()) {
            parsed.put(key, values);
        }
    }
}
