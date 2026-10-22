/*
 * Copyright (c) 2022 SUSE LLC
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.matcher.MatcherJsonIO;
import com.suse.manager.webui.services.subscriptionmatching.SubscriptionMatchProcessor;
import com.suse.matcher.json.OutputJson;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith(JUnit5Mockery.class)
class SubscriptionWarningTest {

    @RegisterExtension
    protected final JUnit5Mockery context = new JUnit5Mockery();

    @BeforeEach
    void setup() {
        context.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @ParameterizedTest
    @MethodSource("warningOutcomeProvider")
    void canDetectCorrectlyIfWarningIsNeeded(Instant referenceTime, boolean expectedOutcome) {
        MatcherJsonIO matcherJsonIO = context.mock(MatcherJsonIO.class);
        OutputJson outputJson = parseOutputJson("three-subscriptions.json");

        context.checking(new Expectations() {{
            allowing(matcherJsonIO).getLastMatcherOutput();
            will(returnValue(Optional.of(outputJson)));
        }});

        var subscriptionWarning = new SubscriptionWarning();
        assertEquals(
            expectedOutcome,
            subscriptionWarning.expiresSoon(referenceTime, matcherJsonIO, new SubscriptionMatchProcessor())
        );
    }

    static Stream<Arguments> warningOutcomeProvider() {
        return Stream.of(
            // Nothing expired, no warning
            Arguments.of(Instant.parse("2026-03-05T00:00:00.000Z"), false),
            // 90 days before first expiration, still no warning
            Arguments.of(Instant.parse("2026-10-03T00:00:00.000Z"), false),
            // 92 days before second expiration, first one is already expiring soon
            Arguments.of(Instant.parse("2026-10-12T00:00:00.000Z"), true),
            // 30 days before second expiration, both should be soon expiring
            Arguments.of(Instant.parse("2026-12-16T00:00:00.000Z"), true),
            // 5 days after the second expiration, both are expired recently
            Arguments.of(Instant.parse("2027-01-20T00:00:00.000Z"), true),
            // 35 days after the first expiration, only one should be considered expired recently
            Arguments.of(Instant.parse("2027-02-05T00:00:00.000Z"), true),
            // 30 days after the second expiration, nothing should be considered expired recently, no warning
            Arguments.of(Instant.parse("2027-02-15T00:00:00.000Z"), false)
        );
    }

    @Test
    void returnsFalseWhenNoRunIsPresent() {
        MatcherJsonIO matcherJsonIO = context.mock(MatcherJsonIO.class);

        context.checking(new Expectations() {{
            allowing(matcherJsonIO).getLastMatcherOutput();
            will(returnValue(Optional.empty()));
        }});

        var subscriptionWarning = new SubscriptionWarning();
        assertFalse(subscriptionWarning.expiresSoon(Instant.now(), matcherJsonIO, new SubscriptionMatchProcessor()));
    }

    @Test
    void returnsFalseWhenNoSubscriptionArePresent() {
        var matcherJsonIO = context.mock(MatcherJsonIO.class);
        var outputJson = context.mock(OutputJson.class);

        context.checking(new Expectations() {{
            allowing(outputJson).getSubscriptions();
            will(returnValue(List.of()));

            allowing(outputJson).getMatches();
            will(returnValue(List.of()));

            allowing(matcherJsonIO).getLastMatcherOutput();
            will(returnValue(Optional.of(outputJson)));
        }});

        var subscriptionWarning = new SubscriptionWarning();
        assertFalse(subscriptionWarning.expiresSoon(Instant.now(), matcherJsonIO, new SubscriptionMatchProcessor()));
    }

    @Test
    void ignoreNonEligibleSubscriptions() {
        MatcherJsonIO matcherJsonIO = context.mock(MatcherJsonIO.class);
        OutputJson outputJson = parseOutputJson("unrelated-subscriptions.json");

        context.checking(new Expectations() {{
            allowing(matcherJsonIO).getLastMatcherOutput();
            will(returnValue(Optional.of(outputJson)));
        }});

        var subscriptionWarning = new SubscriptionWarning();
        // There are two expired subscription at this date, Rancher Prime and NeuVector but they should be
        // excluded because they are not relevant to be shown in the MLM UI
        Instant time = Instant.parse("2026-10-15T00:00:00.000Z");
        assertFalse(subscriptionWarning.expiresSoon(time, matcherJsonIO, new SubscriptionMatchProcessor()));
    }

    private static OutputJson parseOutputJson(String filename) {
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

        try (InputStream stream = Files.newInputStream(Path.of(TestUtils.findTestData(filename).toURI()));
             Reader reader = new InputStreamReader(stream)) {
            return gson.fromJson(new JsonReader(reader), OutputJson.class);
        }
        catch (Exception ex) {
            return fail("Unable to read file " + filename, ex);
        }
    }
}
