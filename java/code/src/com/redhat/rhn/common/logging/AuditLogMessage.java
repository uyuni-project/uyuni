/**
 * Copyright (c) 2011--2012 Novell
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

package com.redhat.rhn.common.logging;

import java.util.Map;

/**
 * Class representation of a single log message.
 */
public class AuditLogMessage {

    private String uid;
    private String message;
    private String host;
    private Map<String, String> extmap;

    /**
     * Set the uid.
     * @param uidIn uid
     */
    public void setUid(String uidIn) {
        this.uid = uidIn;
    }

    /**
     * Return the uid.
     * @return uid
     */
    public String getUid() {
        return uid;
    }

    /**
     * Set the message.
     * @param messageIn message
     */
    public void setMessage(String messageIn) {
        this.message = messageIn;
    }

    /**
     * Return the message.
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the host.
     * @param hostIn host
     */
    public void setHost(String hostIn) {
        this.host = hostIn;
    }

    /**
     * Return the host.
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * Set the extmap.
     * @param extmapIn extmap
     */
    public void setExtmap(Map<String, String> extmapIn) {
        this.extmap = extmapIn;
    }

    /**
     * Return the extmap.
     * @return extmap
     */
    public Map<String, String> getExtmap() {
        return extmap;
    }
}
