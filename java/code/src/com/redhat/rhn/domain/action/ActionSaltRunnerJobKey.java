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

package com.redhat.rhn.domain.action;

import java.io.Serializable;

public class ActionSaltRunnerJobKey implements Serializable {

    private Action action;
    private String jid;

    /**
     * @return action to get
     */
    public Action getAction() {
        return action;
    }

    /**
     * @param actionIn to set
     */
    public void setAction(Action actionIn) {
        this.action = actionIn;
    }

    /**
     * @return jid to get
     */
    public String getJid() {
        return jid;
    }

    /**
     * @param jidIn to set
     */
    public void setJid(String jidIn) {
        this.jid = jidIn;
    }
}
