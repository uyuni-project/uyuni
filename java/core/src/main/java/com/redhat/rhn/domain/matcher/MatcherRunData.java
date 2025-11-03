/*
 * Copyright (c) 2016--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.matcher;

import static com.redhat.rhn.common.hibernate.HibernateFactory.getByteArrayContents;
import static com.redhat.rhn.common.hibernate.HibernateFactory.stringToByteArray;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Data corresponding to one Subscription Matcher run (contents of I/O files).
 */
@Entity
@Table(name = "suseMatcherRunData")
public class MatcherRunData {

    /** db id */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suse_matcher_run_data_seq")
    @SequenceGenerator(name = "suse_matcher_run_data_seq", sequenceName = "suse_matcher_run_data_id_seq",
            allocationSize = 1)
    private Long id;

    /** input.json contents */
    @Column
    private byte[] inputBinary;

    /** output.json contents */
    @Column
    private byte[] outputBinary;

    /** subscription_report.csv contents */
    @Column
    private byte[] subscriptionReportBinary;

    /** message_report.csv contents */
    @Column
    private byte[] messageReportBinary;

    /** unmatched_product_report.csv contents */
    @Column
    private byte[] unmatchedProductReportBinary;

    /**
     * Gets the contents of the MatcherRunData that corresponds to the given
     * CSV filename.
     *
     * @param filename - name of the corresponding csv file (including the extension)
     * @return string corresponding to the CSV output of the matcher
     */
    public String getCSVContentsByFilename(String filename) {
        switch (filename) {
            case "message_report.csv":
                return getMessageReport();
            case "subscription_report.csv":
                return getSubscriptionReport();
            case "unmatched_product_report.csv":
                return getUnmatchedProductReport();
            default:
                throw new IllegalArgumentException("Illegal csv filename " + filename);
        }
    }

    /**
     * Gets the id.
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param idIn - the id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * Gets the input
     * @return input
     */
    public String getInput() {
        return getByteArrayContents(getInputBinary());
    }

    /**
     * Sets the input
     * @param inputIn the input
     */
    public void setInput(String inputIn) {
        setInputBinary(stringToByteArray(inputIn));
    }

    /**
     * Gets the output
     * @return the output
     */
    public String getOutput() {
        return getByteArrayContents(getOutputBinary());
    }

    /**
     * Sets the output
     * @param outputIn the output
     */
    public void setOutput(String outputIn) {
        setOutputBinary(stringToByteArray(outputIn));
    }

    /**
     * Gets the subscriptionReport
     * @return the subscriptionReport
     */
    public String getSubscriptionReport() {
        return getByteArrayContents(getSubscriptionReportBinary());
    }

    /**
     * Sets the subscriptionReport
     * @param subscriptionReportIn the subscriptionReport
     */
    public void setSubscriptionReport(String subscriptionReportIn) {
        setSubscriptionReportBinary(stringToByteArray(subscriptionReportIn));
    }

    /**
     * Gets the messageReport
     * @return the messageReport
     */
    public String getMessageReport() {
        return getByteArrayContents(getMessageReportBinary());
    }

    /**
     * Sets the messageReport
     * @param messageReportIn the matcherReport
     */
    public void setMessageReport(String messageReportIn) {
        setMessageReportBinary(stringToByteArray(messageReportIn));
    }

    /**
     * Gets the unmatchedProductReport
     * @return the unmatchedProductReport
     */
    public String getUnmatchedProductReport() {
        return getByteArrayContents(getUnmatchedProductReportBinary());
    }

    /**
     * Sets the unmatchedProductReport
     * @param unmatchedProductReportIn the unmatchedProductReport
     */
    public void setUnmatchedProductReport(String unmatchedProductReportIn) {
        setUnmatchedProductReportBinary(stringToByteArray(unmatchedProductReportIn));
    }

    /**
     * Gets the inputBinary.
     * @return inputBinary
     */
    protected byte[] getInputBinary() {
        return inputBinary;
    }

    /**
     * Sets the inputBinary.
     * @param inputIn - the inputBinary
     */
    protected void setInputBinary(byte[] inputIn) {
        inputBinary = inputIn;
    }

    /**
     * Gets the outputBinary.
     * @return outputBinary
     */
    protected byte[] getOutputBinary() {
        return outputBinary;
    }

    /**
     * Sets the outputBinary.
     * @param outputIn - the outputBinary
     */
    protected void setOutputBinary(byte[] outputIn) {
        outputBinary = outputIn;
    }

    /**
     * Gets the subscriptionReportBinary.
     * @return subscriptionReportBinary
     */
    protected byte[] getSubscriptionReportBinary() {
        return subscriptionReportBinary;
    }

    /**
     * Sets the subscriptionReportBinary.
     * @param subscriptionReportIn - the subscriptionReportBinary
     */
    protected void setSubscriptionReportBinary(byte[] subscriptionReportIn) {
        subscriptionReportBinary = subscriptionReportIn;
    }

    /**
     * Gets the messageReportBinary.
     * @return messageReportBinary
     */
    protected byte[] getMessageReportBinary() {
        return messageReportBinary;
    }

    /**
     * Sets the messageReportBinary.
     * @param messageReportIn - the messageReportBinary
     */
    protected void setMessageReportBinary(byte[] messageReportIn) {
        messageReportBinary = messageReportIn;
    }

    /**
     * Gets the unmatchedProductReportBinary.
     * @return unmatchedProductReportBinary
     */
    protected byte[] getUnmatchedProductReportBinary() {
        return unmatchedProductReportBinary;
    }

    /**
     * Sets the unmatchedProductReportBinary.
     * @param unmatchedProductReportIn - the unmatchedProductReportBinary
     */
    protected void setUnmatchedProductReportBinary(byte[] unmatchedProductReportIn) {
        unmatchedProductReportBinary = unmatchedProductReportIn;
    }
}
