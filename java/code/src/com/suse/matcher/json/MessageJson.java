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
package com.suse.matcher.json;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON representation of a user message generated during the match (error, warning, etc.).
 */
public class MessageJson {

    /** A label identifying the message type. */
    private String type;

    /** Arbitrary data connected to this message. */
    private Map<String, String> data = new LinkedHashMap<>();

    /**
     * Standard constructor.
     *
     * @param typeIn the type
     * @param dataIn the data
     */
    public MessageJson(String typeIn, Map<String, String> dataIn) {
        type = typeIn;
        data = dataIn;
    }

    /**
     * Gets the a label identifying the message type.
     *
     * @return the a label identifying the message type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the a label identifying the message type.
     *
     * @param typeIn the new a label identifying the message type
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * Gets the data connected to this message.
     *
     * @return the data connected to this message
     */
    public Map<String, String> getData() {
        return data;
    }

    /**
     * Sets the data connected to this message.
     *
     * @param dataIn the new data connected to this message
     */
    public void setData(Map<String, String> dataIn) {
        data = dataIn;
    }
}
