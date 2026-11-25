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

package com.redhat.rhn.taskomatic.task.payg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.redhat.rhn.taskomatic.task.payg.beans.PaygProductInfo;

import com.suse.utils.Exceptions.ThrowingConsumer;

import com.google.gson.Gson;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

class PaygExtractRepoDataTest {

    private static final String PYTHON3 = "/usr/bin/python3";

    @TempDir
    private Path temporaryDirectory;

    private Path extractorScript;

    @BeforeAll
    static void checkPythonAvailable() throws InterruptedException {
        Process process = null;

        assumeTrue(Files.isExecutable(Path.of(PYTHON3)), "Python 3 is required to run the tests");

        try {
            process = new ProcessBuilder()
                .command(PYTHON3, "--version")
                .redirectErrorStream(true)
                .start();


            assumeTrue(process.waitFor(10, TimeUnit.SECONDS), "Python 3 availability check timed out");
            assumeTrue(process.exitValue() == 0, "Python 3 is required to run the tests: " + getProcessOutput(process));
        }
        catch (IOException ex) {
            assumeTrue(false, "Python 3 is required to run the tests: " + ex.getMessage());
        }
        finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }

    @BeforeEach
    void copyExtractorScriptToTempDir() throws IOException {
        // Copy the script to a temp directory so it can be imported by the python test harness
        extractorScript = temporaryDirectory.resolve("payg_extract_repo_data.py");
        try (var script = PaygExtractRepoDataTest.class.getResourceAsStream("script/payg_extract_repo_data.py")) {
            Files.copy(Objects.requireNonNull(script), extractorScript);
        }
    }

    @ParameterizedTest
    @MethodSource("installedProductProvider")
    void ensureFiltersOutMlmToolsProduct(String productName, String vendor, boolean included) throws Exception {
        runPythonTest("collect_installed_products_test_harness", List.of(productName, vendor), output -> {
            PaygProductInfo[] products = new Gson().fromJson(output, PaygProductInfo[].class);
            if (!included) {
                assertEquals(0, products.length, "Product " + productName + " must NOT be in the output");
            }
            else {
                assertEquals(1, products.length, "Product " + productName + " must be in the output");
                assertEquals(productName, products[0].getName());
                assertEquals("15.7", products[0].getVersion());
                assertEquals("x86_64", products[0].getArch());
            }
        });
    }

    static Stream<Arguments> installedProductProvider() {
        return Stream.of(
            Arguments.of("sle-manager-tools", "SUSE", false),
            Arguments.of("Multi-Linux-ManagerTools-SLE", "SUSE", false),
            Arguments.of("Multi-Linux-ManagerTools-SLE-Micro", "SUSE", false),
            Arguments.of("Multi-Linux-ManagerTools-SLE-16", "SUSE", false),
            Arguments.of("Multi-Linux-ManagerTools-Future", "SUSE", false),
            Arguments.of("SLES_SAP", "SUSE", true),
            Arguments.of("sle-module-sap-applications", "SUSE", true),
            Arguments.of("sle-module-systems-management", "SUSE", true),
            Arguments.of("SLES_SAP", "OtherVendor", false)
        );
    }

    private void runPythonTest(String testHarness, List<String> extraParams,
                               ThrowingConsumer<String, Exception> consumer) throws Exception {
        // Build the full command line
        Stream<String> command = Stream.concat(
            Stream.of(PYTHON3, "-c", getTestHarness(testHarness), extractorScript.toString()),
            extraParams.stream()
        );

        Process process = new ProcessBuilder()
                .command(command.toList())
                .redirectErrorStream(true)
                .start();

        try {
            assertTrue(process.waitFor(10, TimeUnit.SECONDS), "Python test harness timed out");
            String output = getProcessOutput(process);
            assertEquals(0, process.exitValue(), output);

            consumer.accept(output);
        }
        finally {
            process.destroyForcibly();
        }
    }

    private static String getTestHarness(String testHarness) throws IOException {
        try (var harness = PaygExtractRepoDataTest.class.getResourceAsStream("script/%s.py".formatted(testHarness))) {
            return new String(Objects.requireNonNull(harness).readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (Exception ex) {
            throw new IOException("Unable to retrieve test harness " + testHarness + " to execute the python test", ex);
        }
    }

    private static String getProcessOutput(Process process) throws IOException {
        try (var stdout = process.getInputStream()) {
            return new String(stdout.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (Exception ex) {
            throw new IOException("Unable to collect python process output", ex);
        }
    }
}
