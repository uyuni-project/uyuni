/**
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

import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.matcher.MatcherRunData;
import com.redhat.rhn.domain.matcher.MatcherRunDataFactory;
import com.redhat.rhn.domain.server.PinnedSubscriptionFactory;

import com.suse.manager.webui.services.impl.MonitoringService;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;


/**
 * Runs the subscription-matcher command.
 */
public class MatcherRunner {

    /** Path where output from subscription-matcher is stored */
    public static final String OUT_DIRECTORY = "/var/lib/spacewalk/subscription-matcher";

    /** Path where logs from subscription-matcher are stored */
    public static final String LOG_DIRECTORY = "/var/log/rhn";

    private static final String MATCHER_CMD = "/usr/bin/subscription-matcher";

    /**
     * Logger for this class
     */
    private static Logger logger = Logger.getLogger(MatcherRunner.class);

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

        Runtime r = Runtime.getRuntime();
        try {
            Process p = r.exec(args.toArray(new String[0]));
            PrintWriter stdin = new PrintWriter(p.getOutputStream());
            boolean isISSMaster = IssFactory.getCurrentMaster() == null;
            boolean isSelfMonitoringEnabled = MonitoringService.isMonitoringEnabled();
            String arch = System.getProperty("os.arch");
            PinnedSubscriptionFactory.getInstance().cleanStalePins();
            String s = new MatcherJsonIO().generateMatcherInput(isISSMaster, arch, isSelfMonitoringEnabled);
            stdin.println(s);
            stdin.flush();
            stdin.close();

            int exitCode = p.waitFor();
            if (exitCode != 0) {
                logger.error("Error while calling the subscription-matcher, exit code " +
                        exitCode);
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
        catch (IOException ioe) {
            logger.error("execute(String[])", ioe);
        }
        catch (InterruptedException e) {
            logger.error("execute(String[])", e);
        }
    }

    /**
     * Returns the matcher input data read from file.
     * @param filename the file name
     * @return the input data or empty in case the file with the input data was not found
     */
    private static String readMatcherFile(String filename) {
        try {
            return FileUtils.readFileToString(new File(MatcherRunner.OUT_DIRECTORY,
                    filename));
        }
        catch (IOException e) {
            throw new IllegalStateException("Matcher ran successfully, but it's not" +
                    " possible to read its input/output file: " + filename);
        }
    }
}
