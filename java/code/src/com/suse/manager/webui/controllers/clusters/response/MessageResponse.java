/**
 * Copyright (c) 2020 SUSE LLC
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

package com.suse.manager.webui.controllers.clusters.response; import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

/**
 * Message view bean.
 */
public class MessageResponse {

    private String severity;
    private String text;
    private List<String> args;

    /**
     * @param severityIn message severity
     * @param textIn message text
     */
    public MessageResponse(String severityIn, String textIn) {
        this.severity = severityIn;
        this.text = textIn;
    }

    /**
     * @param severityIn message severity
     * @param textIn message text
     * @param argsIn message parameters
     */
    public MessageResponse(String severityIn, String textIn, List<String> argsIn) {
        this.severity = severityIn;
        this.text = textIn;
        this.args = argsIn;
    }

    /**
     * Creates a success message.
     * @param textIn the text
     * @param args optional arguments
     * @return a success message
     */
    public static MessageResponse success(String textIn, String... args) {
        return new MessageResponse("success", textIn, Arrays.asList(args));
    }

    /**
     * Creates an info message.
     * @param textIn the text
     * @param args optional arguments
     * @return an info message
     */
    public static MessageResponse info(String textIn, String... args) {
        return new MessageResponse("info", textIn, Arrays.asList(args));
    }

    /**
     * Creates a warning message.
     * @param textIn the text
     * @param args optional arguments
     * @return a warning message
     */
    public static MessageResponse warning(String textIn, String... args) {
        return new MessageResponse("warning", textIn, Arrays.asList(args));
    }

    /**
     * Creates an error message.
     * @param textIn the text
     * @param args optional arguments
     * @return a warning message
     */
    public static MessageResponse error(String textIn, String... args) {
        return new MessageResponse("error", textIn, Arrays.asList(args));
    }

    /**
     * @return severity to get
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * @param severityIn to set
     */
    public void setSeverity(String severityIn) {
        this.severity = severityIn;
    }

    /**
     * @return message to get
     */
    public String getText() {
        return text;
    }

    /**
     * @param textIn to set
     */
    public void setText(String textIn) {
        this.text = textIn;
    }
}
