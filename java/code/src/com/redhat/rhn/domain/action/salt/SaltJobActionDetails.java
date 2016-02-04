/**
 * Copyright (c) 2016 SUSE
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
package com.redhat.rhn.domain.action.salt;

import com.redhat.rhn.domain.action.ActionChild;

/**
 * SaltJobActionDetails - Class representation of the table rhnActionSaltJob.
 */
public class SaltJobActionDetails extends ActionChild {

    private Long actionId;
    private String jid;
    private String data;

    /**
     * Get the action ID.
     * @return
     */
    public Long getActionId() {
        return actionId;
    }

    /**
     * Set the action ID.
     * @param actionId
     */
    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    /**
     * Get the data.
     * @return data
     */
    public String getData() {
        return data;
    }

    /**
     * Set the data.
     * @param data the data of this job
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Return the jid.
     * @return jid
     */
    public String getJid() {
        return jid;
    }

    /**
     * Set the jid.
     * @param jidIn jid
     */
    public void setJid(String jidIn) {
        jid = jidIn;
    }
}
