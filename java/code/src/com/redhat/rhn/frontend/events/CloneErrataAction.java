/**
 * Copyright (c) 2009--2015 Red Hat, Inc.
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

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.channel.manage.PublishErrataHelper;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CloneErrataAction
 */
public class CloneErrataAction implements MessageAction {

    private static Logger log = Logger.getLogger(CloneErrataAction.class);

    /**
     * {@inheritDoc}
     */
    public void execute(EventMessage msgIn) {
        CloneErrataEvent msg = (CloneErrataEvent) msgIn;
        cloneErrata(msg.getChannelId(), msg.getErrata(), msg.isRequestRepodataRegen(), msg.getUser());
    }

    private void cloneErrata(Long channelId, Collection<Long> errataToClone, boolean requestRepodataRegen, User user) {
        Channel channel = ChannelFactory.lookupById(channelId);
        if (channel == null) {
            log.error("Failed to clone errata " + errataToClone +
                    " Didn't find channel with id: " + channelId.toString());
            return;
        }
        Collection<Long> list = errataToClone;
        List<Long> cids = new ArrayList<Long>();
        cids.add(channel.getId());
        // let's avoid deadlocks please
        ChannelFactory.lock(channel);

        for (Long eid : list) {
            Errata errata = ErrataFactory.lookupById(eid);
            // we merge custom errata directly (non Redhat and cloned)
            if (errata.getOrg() != null) {
                errata.addChannel(channel);
                ErrataCacheManager.insertCacheForChannelErrata(cids, errata);
                errata.addChannelNotification(channel, new Date());
            }
            else {
                Set<Channel> channelSet = new HashSet<Channel>();
                channelSet.add(channel);

                List<Errata> clones = ErrataManager.lookupPublishedByOriginal(user, errata);
                if (clones.size() == 0) {
                    log.debug("Cloning errata");
                    Errata published = PublishErrataHelper.cloneErrataFast(errata,
                            user.getOrg());
                    published.setChannels(channelSet);
                    ErrataCacheManager.insertCacheForChannelErrata(cids, published);
                    published.addChannelNotification(channel, new Date());
                }
                else {
                    log.debug("Re-publishing clone");
                    ErrataManager.publish(clones.get(0), cids, user);
                }
            }
        }
        // Trigger channel repodata re-generation
        if (list.size() > 0 && requestRepodataRegen) {
            channel.setLastModified(new Date());
            ChannelFactory.save(channel);
            ChannelManager.queueChannelChange(channel.getLabel(),
                    "java::cloneErrata", "Errata cloned");
        }
    }


}
