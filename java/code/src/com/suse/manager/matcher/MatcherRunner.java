/*
 * Copyright (c) 2015 SUSE LLC
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

package com.suse.manager.matcher;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.matcher.MatcherRunData;
import com.redhat.rhn.domain.matcher.MatcherRunDataFactory;
import com.redhat.rhn.domain.server.PinnedSubscriptionFactory;

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.webui.services.impl.MonitoringService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Runs the subscription-matcher command.
 */
public class MatcherRunner {

    private static final Logger LOGGER = LogManager.getLogger(MatcherRunner.class);

    /** Path where output from subscription-matcher is stored */
    public static final String OUT_DIRECTORY = "/var/lib/spacewalk/subscription-matcher";

    /** Path where logs from subscription-matcher are stored */
    public static final String LOG_DIRECTORY = "/var/log/rhn";

    private static final String MATCHER_CMD = "/usr/bin/subscription-matcher";

    private final CloudPaygManager cloudManager;

    /**
     * Default Constructor
     */
    public MatcherRunner() {
        this(GlobalInstanceHolder.PAYG_MANAGER);
    }

    /**
     * Builds an instance with the given cloud manager. For unit testing.
     * @param cloudManagerIn the {@link CloudPaygManager} instance to use
     */
    public MatcherRunner(CloudPaygManager cloudManagerIn) {
        this.cloudManager = cloudManagerIn;
    }

    /**
     * Runs subscription-matcher.
     *
     * @param csvDelimiter delimiter to used for CSV output
     */
    public void run(String csvDelimiter) {
        List<String> args = new LinkedList<>();
        args.add(MATCHER_CMD);
        args.add("--output-directory");
        args.add(OUT_DIRECTORY);
        args.add("--log-directory");
        args.add(LOG_DIRECTORY);
        args.add("--delimiter");
        args.add(csvDelimiter);

        getLogLevel().ifPresent(level -> {
            args.add("--log-level");
            args.add(level);
        });

        Runtime r = Runtime.getRuntime();
        ExecutorService errorReaderService = null;
        ExecutorService inputReaderService = null;
        try {
            boolean isSUMaPayg = cloudManager.isPaygInstance();
            boolean isUyuni = ConfigDefaults.get().isUyuni();

            boolean needsEntitlements = !isUyuni && !isSUMaPayg;
            boolean includeSelf = !isSUMaPayg && !isUyuni && IssFactory.getCurrentMaster() == null;
            boolean isSelfMonitoringEnabled = !isSUMaPayg && !isUyuni && MonitoringService.isMonitoringEnabled();

            PinnedSubscriptionFactory.getInstance().cleanStalePins();
            String arch = System.getProperty("os.arch");
            String s = new MatcherJsonIO()
                .generateMatcherInput(arch, includeSelf, isSelfMonitoringEnabled, needsEntitlements);

            Process p = r.exec(args.toArray(new String[0]));
            try (PrintWriter stdin = new PrintWriter(p.getOutputStream())) {
                stdin.println(s);
                stdin.flush();
            }

            // we need to exhaust the process output not to get stuck
            errorReaderService = exhaustOutputOnBackground(p.getErrorStream());
            inputReaderService = exhaustOutputOnBackground(p.getInputStream());

            int exitCode = p.waitFor();
            if (exitCode != 0) {
                LOGGER.error("Error while calling the subscription-matcher, exit code {}", exitCode);
                return;
            }

            MatcherRunData data = new MatcherRunData();
            data.setInput(readMatcherFile("input.json"));
            data.setOutput(readMatcherFile("output.json"));
            data.setSubscriptionReport(readMatcherFile("subscription_report.csv"));
            data.setMessageReport(readMatcherFile("message_report.csv"));
            data.setUnmatchedProductReport(readMatcherFile("unmatched_product_report.csv"));
            MatcherRunDataFactory.updateData(data);
        }
        catch (IOException e) {
            LOGGER.error("Unable to execute subscription-matcher process", e);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Process was interrupted", e);
        }
        finally {
            if (errorReaderService != null) {
                errorReaderService.shutdown();
            }
            if (inputReaderService != null) {
                inputReaderService.shutdown();
            }
        }
    }

    // reads the given stream until the end
    private static ExecutorService exhaustOutputOnBackground(InputStream stream) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try (InputStreamReader irr = new InputStreamReader(stream)) {
                while (irr.read() != -1) {
                    // no-op
                }
            }
            catch (IOException e) {
                LOGGER.warn("Error reading from stream", e);
            }
        });
        return executorService;
    }

    private static Optional<String> getLogLevel() {
        int debuglevel = Config.get().getInt("debug", 0);

        switch (debuglevel) {
            case 0:
            case 1:
                return empty(); // use matcher default log level
            case 2:
                return of("INFO");
            case 3:
                return of("DEBUG");
            case 4:
                return of("TRACE");
            default:
                return of("ALL");
        }
    }

    /**
     * Returns the matcher input data read from file.
     * @param filename the file name
     * @return the input data or empty in case the file with the input data was not found
     */
    private static String readMatcherFile(String filename) {
        try {
            return Files.readString(Path.of(MatcherRunner.OUT_DIRECTORY, filename));
        }
        catch (IOException e) {
            throw new IllegalStateException("Matcher ran successfully, but it's not" +
                    " possible to read its input/output file: " + filename);
        }
    }
}
