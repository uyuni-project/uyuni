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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@NamedQueries({
        @NamedQuery(name = "ActionSaltRunnerJob.findByJid",
            query = "from ActionSaltRunnerJob where jid=:jid")
})
@IdClass(ActionSaltRunnerJobKey.class)
@Table(name = "rhnActionSaltRunnerJob")
public class ActionSaltRunnerJob {

    private Action action;
    private String jid;
    private Long resultCode;
    private String resultMsg;
    private Date pickupTime;
    private Date completionTime;
    private ActionStatus status;

    /**
     * @return action to get
     */
    @Id
    @ManyToOne
    @JoinColumn(name = "action_id")
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
    @Id
    @Column(name = "jid")
    public String getJid() {
        return jid;
    }

    /**
     * @param jidIn to set
     */
    public void setJid(String jidIn) {
        this.jid = jidIn;
    }

    /**
     * @return resultCode to get
     */
    @Column(name = "result_code")
    public Long getResultCode() {
        return resultCode;
    }

    /**
     * @param resultCodeIn to set
     */
    public void setResultCode(Long resultCodeIn) {
        this.resultCode = resultCodeIn;
    }

    /**
     * @return resultMsg to get
     */
    @Column(name = "result_msg")
    public String getResultMsg() {
        return resultMsg;
    }

    /**
     * @param resultMsgIn to set
     */
    public void setResultMsg(String resultMsgIn) {
        this.resultMsg = resultMsgIn;
    }

    /**
     * @return pickupTime to get
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "pickup_time")
    public Date getPickupTime() {
        return pickupTime;
    }

    /**
     * @param pickupTimeIn to set
     */
    public void setPickupTime(Date pickupTimeIn) {
        this.pickupTime = pickupTimeIn;
    }

    /**
     * @return completionTime to get
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "completion_time")
    public Date getCompletionTime() {
        return completionTime;
    }

    /**
     * @param completionTimeIn to set
     */
    public void setCompletionTime(Date completionTimeIn) {
        this.completionTime = completionTimeIn;
    }

    /**
     * @return status to get
     */
    @ManyToOne
    @JoinColumn(name = "status")
    public ActionStatus getStatus() {
        return status;
    }

    /**
     * @param statusIn to set
     */
    public void setStatus(ActionStatus statusIn) {
        this.status = statusIn;
    }
}
