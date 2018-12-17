package com.suse.manager.reactor.messaging.test;

import static org.hamcrest.Matchers.equalTo;

import com.redhat.rhn.testing.TestUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.salt.netapi.calls.LocalCall;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides utility functions to use in salt-related unit tests.
 */
public class SaltTestUtils {

    private static final Gson GSON = new GsonBuilder().create();

    /**
     * Compute a salt call response from a data file (often a JSON one, but not mandatory).
     *
     * Here is an example of how to use this function:
     *
     * <pre>
     *   Map<String, String> placeholders = new HashMap<>();
     *       placeholders.put("b99a8176-4f40-498d-8e61-2f6ade654fe2", uuid);
     *
     *   context().checking(new Expectations() {{
     *       oneOf(saltServiceMock).callSync(
     *               with(any(LocalCall.class)),
     *               with(host.asMinionServer().get().getMinionId()));
     *       will(returnValue(SaltTestUtils.getSaltResponse(
     *               "/com/suse/manager/reactor/messaging/test/some-result.json", placeholders,
     *               new TypeToken<Map<String, JsonElement>>() { }.getType())));
     *   }});
     * </pre>
     *
     * @param filename the path to the data file
     * @param placeholders a map used to replace substrings of the data file by other data
     * @param type type of the result. If <code>null</code> the resulting type will be an <code>Optional&lt;String&gt;</code>
     *
     * @return the mocked up salt response
     *
     * @throws Exception if anything bad happens.
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getSaltResponse(String filename, Map<String, String> placeholders, Type type) throws Exception {
        Path path = new File(TestUtils.findTestData(filename).getPath()).toPath();
        String content = Files.lines(path).collect(Collectors.joining("\n"));

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                content = StringUtils.replace(content, entry.getKey(), entry.getValue());
            }
        }

        T target = (T)content;
        if (type != null) {
            target = GSON.fromJson(content, type);
        }
        return Optional.of(target);
    }

    /**
     * Function to use in expectations to match a salt call.
     *
     * Example usage matching <code>virt.network_info</code> calls:
     *
     * <pre>
     *   context().checking(new Expectations() {{
     *       oneOf(saltServiceMock).callSync(
     *               with(SaltTestUtils.functionEquals("virt", "network_info")),
     *               with(host.asMinionServer().get().getMinionId()));
     *       will(returnValue(SaltTestUtils.getSaltResponse(
     *               "/com/suse/manager/webui/controllers/test/virt.net.info.json",
     *               null, new TypeToken<Map<String, JsonElement>>() { }.getType())));
     *   }});
     * </pre>
     *
     * @param module salt module to match
     * @param func salt function to match
     *
     * @return the matcher for the expectations.
     */
    public static Matcher<LocalCall<Object>> functionEquals(String module, String func) {
        return Matchers.allOf(
                Matchers.hasProperty("moduleName", equalTo(module)),
                Matchers.hasProperty("functionName", equalTo(func)));
    }
}
