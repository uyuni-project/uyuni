/*
 * Copyright (c) 2023 SUSE LLC
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
package com.suse.manager.api;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;


public class LoggingInvocationProcessorTest {

    private final Set<String> sensitiveKeywords = LoggingInvocationProcessor.DEFAULT_SENSITIVE_KEYWORDS;
    private final LoggingInvocationProcessor processor = new LoggingInvocationProcessor(
           sensitiveKeywords,
           LoggingInvocationProcessor.DEFAULT_EXPLICIT_OVERRIDE
    );


    private void testRedaction(String param, String value, boolean shouldRedact) {
        MatcherAssert.assertThat(processor.logMessage(
                "system",
                "login",
                Optional.of(Map.of(param, value)),
                "127.0.0.1",
                Optional.empty(),
                Duration.ofMillis(1234)
        ).toString(), shouldRedact ? not(containsString(value)) : containsString(value));
    }


    @Test
    public void testRedactedScenarios() {
        sensitiveKeywords.stream()
            .flatMap(sensitiveKeyword ->
                Stream.of(
                    sensitiveKeyword,
                    sensitiveKeyword.toUpperCase(),
                    sensitiveKeyword.toLowerCase(),
                    sensitiveKeyword + "withSuffix",
                    "withPrefix" + sensitiveKeyword,
                    "withPrefix" + sensitiveKeyword + "withSuffix"
                )
            )
            .forEach(sensitiveKeyword -> {
                testRedaction("nonSensitiveParameterName", "{" + sensitiveKeyword + ": \"hello world\"}", true);
                testRedaction(sensitiveKeyword, "test", true);
                testRedaction(sensitiveKeyword, "{" + sensitiveKeyword + ": \"hello world\"}", true);
            });
    }

    @Test
    public void testNonRedactedScenarios() {
        testRedaction("test", "{" + "test" + ": \"hello world\"}", false);
    }

    @Test
    public void testExplicitOverrides() {
        LoggingInvocationProcessor p = new LoggingInvocationProcessor(
                Set.of("password"),
                Map.of(
                    "test.method", Map.of(
                            "explicitlyRedacted", true,
                            "explicitlyLoggedPassword", false
                    )
                )
        );
        String withExplicitOverride = p.logMessage("test", "method", Optional.of(Map.of(
                "explicitlyRedacted", "shouldBeRedacted",
                "explicitlyLoggedPassword", "passwordInLogMessage"
        )), "127.0.0.1", Optional.empty(), Duration.ZERO).toString();
        MatcherAssert.assertThat(withExplicitOverride, containsString("passwordInLogMessage"));
        MatcherAssert.assertThat(withExplicitOverride, not(containsString("shouldBeRedacted")));


        p = new LoggingInvocationProcessor(
                Set.of("password"),
                Map.of()
        );
        String withoutExplicitOverride = p.logMessage("test", "method", Optional.of(Map.of(
                        "explicitlyRedacted", "shouldBeRedacted",
                        "explicitlyLoggedPassword", "passwordInLogMessage"
        )), "127.0.0.1", Optional.empty(), Duration.ZERO).toString();
        MatcherAssert.assertThat(withoutExplicitOverride, not(containsString("passwordInLogMessage")));
        MatcherAssert.assertThat(withoutExplicitOverride, containsString("shouldBeRedacted"));
    }

}
