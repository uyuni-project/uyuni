/**
 * Copyright (c) 2019 SUSE LLC
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

package com.redhat.rhn.frontend.events;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.EventDatabaseMessage;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget;
import com.redhat.rhn.domain.user.User;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.Transaction;

/**
 * Message bearing data for {@link AlignSoftwareTargetAction}
 */
public class AlignSoftwareTargetMsg implements EventDatabaseMessage {

    private final Channel source;
    private final SoftwareEnvironmentTarget target;
    private final User user;
    private final Transaction txn;

    /**
     * Standard constructor
     * @param src the source Channel
     * @param tgt the {@link SoftwareEnvironmentTarget} in which the source channel will be aligned
     * @param userIn the User
     */
    public AlignSoftwareTargetMsg(Channel src, SoftwareEnvironmentTarget tgt, User userIn) {
        this.source = src;
        this.target = tgt;
        this.user = userIn;
        this.txn = HibernateFactory.getSession().getTransaction();
    }

    /**
     * Gets the source Channel.
     *
     * @return source
     */
    public Channel getSource() {
        return source;
    }

    /**
     * Gets the {@link SoftwareEnvironmentTarget}
     *
     * @return target
     */
    public SoftwareEnvironmentTarget getTarget() {
        return target;
    }

    /**
     * Gets the user.
     *
     * @return user
     */
    public User getUser() {
        return user;
    }

    @Override
    public Transaction getTransaction() {
        return txn;
    }

    @Override
    public String toText() {
        return toString();
    }

    @Override
    public Long getUserId() {
        return user.getId();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("source", source)
                .append("target", target)
                .append("user", user)
                .toString();
    }
}
