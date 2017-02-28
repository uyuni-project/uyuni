/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.EventDatabaseMessage;
import org.hibernate.Transaction;

/**
 * An event to signal that a set of states is dirty and needs
 * to be applied to a particular server
 */
public class ImageBuildEventMessage implements EventDatabaseMessage {

    private final long serverId;
    private final Long userId;
    private final String tag;
    private final long imageProfileId;
    private final Transaction txn;

    /**
     * Constructor for creating a {@link ImageBuildEventMessage} for a given server.
     *
     * @param serverIdIn     the server
     * @param userIdIn       the user
     * @param tagIn          the tag
     * @param imageProfileIdIn the image profile
     */
    public ImageBuildEventMessage(long serverIdIn, Long userIdIn,
                                  String tagIn, long imageProfileIdIn) {
        serverId = serverIdIn;
        userId = userIdIn;
        tag = tagIn;
        imageProfileId = imageProfileIdIn;
        txn = HibernateFactory.getSession().getTransaction();
    }

    /**
     * Return the server id.
     *
     * @return the server id
     */
    public Long getServerId() {
        return serverId;
    }

    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public String toText() {
        return toString();
    }

    /**
     * Gets image profile.
     *
     * @return the image profile id
     */
    public long getImageProfileId() {
        return imageProfileId;
    }

    /**
     * Gets tag.
     *
     * @return the tag
     */
    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return "ImageProfileEventMessage[serverId: " + serverId + ", tag: " +
                tag + "]";
    }

    @Override
    public Transaction getTransaction() {
        return txn;
    }
}
