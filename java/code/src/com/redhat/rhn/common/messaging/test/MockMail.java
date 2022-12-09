/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

package com.redhat.rhn.common.messaging.test;

import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.messaging.Mail;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * A Mock implementation of our Mail interface.
 *
 */
public class MockMail implements Mail {

    private int sendCount = 0;
    private int expectedSendCount = 0;
    private String body;
    private String subject;

    /**
     * Create a mail message
     */
    public MockMail() {
    }

    /**
    * Send the actual message
    */
    @Override
    public void send() {
        sendCount++;
    }

    /** Set the recipient of the email message.
     *  This can be a comma or space separated list of recipients
     *
     * @param recipIn recipient email
    */
    @Override
    public void setRecipient(String recipIn) {
        verifyAddress(recipIn);
    }

    /** Set the recipient of the email message.
     *  This can be a comma or space separated list of recipients
     *
     * @param recipIn recipients emails list
    */
    @Override
    public void setRecipients(String[] recipIn) {
        if (recipIn != null) {
            for (String sIn : recipIn) {
                verifyAddress(sIn);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCCRecipients(String[] emailAddrs) {
        if (emailAddrs != null) {
            for (String emailAddrIn : emailAddrs) {
                verifyAddress(emailAddrIn);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBCCRecipients(String[] emailAddrs) {
        if (emailAddrs != null) {
            for (String emailAddrIn : emailAddrs) {
                verifyAddress(emailAddrIn);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFrom(String from) {
        verifyAddress(from);
    }

    private void verifyAddress(String addr) {
        try {
            InternetAddress.parse(addr);
        }
        catch (AddressException e) {
            throw new RuntimeException("Bad address [" + addr + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHeader(String name, String value) {
    }

    /**
     * Set the subject of the email message
     *
     * @param subIn the email subject
     */
    @Override
    public void setSubject(String subIn) {
        subject = subIn;
    }

    /**
     * Set the text of the email message
     *
     * @param bodyIn the email body
     */
    @Override
    public void setBody(String bodyIn) {
        body = bodyIn;
    }

    /**
    * Set the expected number of times send() will be called
    * @param count the expected count
    */
    public void setExpectedSendCount(int count) {
        expectedSendCount = count;
    }

    /**
    * Get the subject so we can verify against it
    * @return the subject
    */
    public String getSubject() {
        return subject;
    }

    /**
    * Get the body so we can verify against it
    * @return the body
    */
    public String getBody() {
        return body;
    }

    /**
     * Verify that the mailer sent enough email.
     */
    public void verify() {
        if (expectedSendCount > sendCount) {
            fail("expectedSendCount: " + expectedSendCount + " actual count: " + sendCount);
        }
    }

}


