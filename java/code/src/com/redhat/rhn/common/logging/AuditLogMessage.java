/**
 * Copyright (c) 2011 Novell
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

public class AuditLogMessage {

    private String uid;
    private String message;
    private String host;
    private Map<String, String> extmap;

    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getUid() {
        return uid;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getHost() {
        return host;
    }
    public void setExtmap(Map<String, String> extmap) {
        this.extmap = extmap;
    }
    public Map<String, String> getExtmap() {
        return extmap;
    }
}
