/*
 * Copyright (c) 2018--2021 SUSE LLC
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
package com.suse.manager.reactor.messaging.test;

import static org.hamcrest.Matchers.equalTo;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.salt.netapi.calls.Call;
import com.suse.salt.netapi.parser.JsonParser;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.results.Return;
import com.suse.salt.netapi.utils.ClientUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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
     * <pre>{@code
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
     * }</pre>
     *
     * @param filename the path to the data file
     * @param placeholders a map used to replace substrings of the data file by other data
     * @param type type of the result. If <code>null</code> the resulting type will be an
     *             <code>Optional&lt;String&gt;</code>
     * @param <T> type of the result
     *
     * @return the mocked up salt response
     *
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getSaltResponse(String filename, Map<String, String> placeholders,
                                                  TypeToken<T> type) {
        try {
            String path = new File(TestUtils.findTestData(filename).getPath()).getAbsolutePath();
            String content = FileUtils.readStringFromFile(path);

            if (placeholders != null) {
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    content = StringUtils.replace(content, entry.getKey(), entry.getValue());
                }
            }

            T target = (T) content;
            if (type != null) {
                target = GSON.fromJson(content, type.getType());
            }
            return Optional.of(target);
        }
        catch (IOException | ClassNotFoundException e) {
           throw new RhnRuntimeException(e);
        }
    }

    /**
     * Compute a salt call response from a data file for use with a SaltClient mock.
     *
     * Here is an example of how to use this function:
     *
     * <pre>{@code
     *   context().checking(new Expectations() {{
     *       oneOf(saltClient).call(with(SaltTestUtils.functionEquals("mgrutil", "ssh_keygen")),
     *                              with(any(Client.class)), with(any(Optional.class)), with(any(Map.class)),
     *                              with(any(TypeToken.class)), with(any(AuthMethod.class)));
     *       will(returnValue(SaltTestUtils.getCompletionStage(
     *                              "/com/suse/manager/webui/services/impl/test/service/ssh_keygen.json",
     *                              new TypeToken<MgrUtilRunner.SshKeygenResult>() { }.getType())));
     *   }});
     * }</pre>
     *
     * @param filename the path to the data file
     * @param type type of the result.
     * @param <T> type of the result
     *
     * @return the mocked up salt response
     */
    @SuppressWarnings("unchecked")
    public static <T> CompletionStage<Return<List<T>>> getCompletionStage(String filename, Type type) {
        try {
            String path = new File(TestUtils.findTestData(filename).getPath()).getAbsolutePath();
            String content = FileUtils.readStringFromFile(path);

            Type resultType = ClientUtils.parameterizedType(null, Result.class, type);
            Type listType = ClientUtils.parameterizedType(null, List.class, resultType);
            Type returnType = ClientUtils.parameterizedType(null, Return.class, listType);
            TypeToken<Return<List<T>>> token = (TypeToken<Return<List<T>>>) TypeToken.get(returnType);
            Return<List<T>> ret = new JsonParser<>(token).parse(content);
            return CompletableFuture.completedStage(ret);
        }
        catch (IOException | ClassNotFoundException e) {
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * Function to use in expectations to match a salt runner call.
     *
     * Example usage matching <code>mgrutil.ssh_keygen</code> calls:
     *
     * <pre>{@code
     *   context().checking(new Expectations() {{
     *       oneOf(saltClient).call(with(SaltTestUtils.functionEquals("mgrutil", "ssh_keygen")),
     *                              with(any(Client.class)), with(any(Optional.class)), with(any(Map.class)),
     *                              with(any(TypeToken.class)), with(any(AuthMethod.class)));
     *   }});
     * }</pre>
     *
     * @param module salt module to match
     * @param func salt function to match
     * @param <T> the type to match
     *
     * @return the matcher for the expectations.
     */
    public static <T> Matcher<Call<T>> functionEquals(String module, String func) {
        return Matchers.allOf(
                Matchers.hasProperty("moduleName", equalTo(module)),
                Matchers.hasProperty("functionName", equalTo(func)));
    }

    private SaltTestUtils() {
    }
}
