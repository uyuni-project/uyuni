/**
 * Copyright (c) 2016 SUSE LLC
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

package com.redhat.rhn.domain.matcher;

/**
 * Data corresponding to one Subscription Matcher run (contents of I/O files).
 */
public class MatcherRunData {

    /** db id */
    private Long id;
    /** input.json contents */
    private String input;
    /** output.json contents */
    private String output;
    /** subscription_report.csv contents */
    private String subscriptionReport;
    /** message_report.csv contents */
    private String messageReport;
    /** unmatched_system_report.csv contents */
    private String unmatchedSystemReport;

    /**
     * Gets the id.
     *
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param idIn - the id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * Gets the input.
     *
     * @return input
     */
    public String getInput() {
        return input;
    }

    /**
     * Sets the input.
     *
     * @param inputIn - the input
     */
    public void setInput(String inputIn) {
        input = inputIn;
    }

    /**
     * Gets the output.
     *
     * @return output
     */
    public String getOutput() {
        return output;
    }

    /**
     * Sets the output.
     *
     * @param outputIn - the output
     */
    public void setOutput(String outputIn) {
        output = outputIn;
    }

    /**
     * Gets the subscriptionReport.
     *
     * @return subscriptionReport
     */
    public String getSubscriptionReport() {
        return subscriptionReport;
    }

    /**
     * Sets the subscriptionReport.
     *
     * @param subscriptionReportIn - the subscriptionReport
     */
    public void setSubscriptionReport(String subscriptionReportIn) {
        subscriptionReport = subscriptionReportIn;
    }

    /**
     * Gets the messageReport.
     *
     * @return messageReport
     */
    public String getMessageReport() {
        return messageReport;
    }

    /**
     * Sets the messageReport.
     *
     * @param messageReportIn - the messageReport
     */
    public void setMessageReport(String messageReportIn) {
        messageReport = messageReportIn;
    }

    /**
     * Gets the unmatchedSystemReport.
     *
     * @return unmatchedSystemReport
     */
    public String getUnmatchedSystemReport(){
        return unmatchedSystemReport;
    }

    /**
     * Sets the unmatchedSystemReport.
     *
     * @param unmatchedSystemReportIn - the unmatchedSystemReport
     */
   public void setUnmatchedSystemReport(String unmatchedSystemReportIn) {
        unmatchedSystemReport = unmatchedSystemReportIn;
    }
}
