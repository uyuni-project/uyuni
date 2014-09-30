/**
 * Copyright (c) 2014 SUSE
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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;

/**
 * ScheduleRepoSyncAction
 */
public class ScheduleRepoSyncAction implements MessageAction {

    /** The logger. */
    protected static Logger logger = Logger.getLogger(ScheduleRepoSyncAction.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(EventMessage msg) {
        ScheduleRepoSyncEvent event = (ScheduleRepoSyncEvent) msg;
        if (logger.isDebugEnabled()) {
            logger.debug("Scheduling repo sync for: " + event.getChannelLabel());
        }
        scheduleRepoSync(event.getChannelLabel());
    }

    /**
     * Schedule an immediate reposync via the Taskomatic API.
     *
     * @param channelLabel label of the channel to sync
     */
    private void scheduleRepoSync(String channelLabel) {
        List<String> labels = new ArrayList<String>();
        labels.add(channelLabel);
        @SuppressWarnings("unchecked")
        List<Long> channelIds = ChannelFactory.getChannelIds(labels);
        if (!channelIds.isEmpty()) {
            this.rpcInvoke("tasko.scheduleSingleSatRepoSync", channelIds.get(0));
        }
    }

    /**
     * Invoke an XMLRPC method from the client.
     *
     * @param name
     * @param args
     * @return
     */
    private Object rpcInvoke(String name, Object...args) {
        try {
            return new XmlRpcClient(ConfigDefaults.get()
                    .getTaskoServerUrl(), false)
                    .invoke(name, args);
        }
        catch (MalformedURLException e) {
            throw new TaskomaticApiException(e);
        }
        catch (XmlRpcException e) {
            throw new TaskomaticApiException(e);
        }
        catch (XmlRpcFault e) {
            throw new TaskomaticApiException(e);
        }
    }
}
