/**
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.manager.ssm;

import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.RhnSetElement;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.channel.ssm.ChannelActionDAO;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.system.SystemManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The current plan for this class is to manage all SSM operations. However, as more is
 * ported from perl to java, there may be a need to break this class into multiple
 * managers to keep it from becoming unwieldly.
 *
 * @author Jason Dobies
 * @version $Revision$
 */
public class SsmManager {

    /** Private constructor to enforce the stateless nature of this class. */
    private SsmManager() {
    }

    /**
     * Given a list of servers and channels that should be subscribed, determines which
     * channels may be subscribed to by which servers. The mapping returned will be from
     * each server to a list of channels that may be subscribed. This list will not
     * be <code>null</code> but may be empty if no subscriptions are determined to be
     * allowed.
     *
     * @param user        user initiating the subscription
     * @param sysMapping     the map of ChannelActionDAO objects
     * @param allChannels channels to attempt to subscribe each server to; may not
     *                    be <code>null</code>
     * @return mapping of each server to a non-null (but potentially empty) list of
     *         channels that are safe to subscribe it to
     */
    public static Map<Long, ChannelActionDAO> verifyChildEntitlements(
            User user, Map<Long, ChannelActionDAO> sysMapping, List<Channel> allChannels) {

        //Load all of the channels in a map for easy lookup
        Map<Long, Channel> idToChan = new HashMap<Long, Channel>();
        for (Channel c : allChannels) {
            idToChan.put(c.getId(), c);
        }

        List<SystemOverview> servers =
                SystemManager.inSet(user, RhnSetDecl.SYSTEMS.getLabel());

        // Keeps a mapping of how many entitlements are left on each channel. This map
        // will be updated as the processing continues, however changes won't be written
        // to the database until the actual subscriptions are made. This way we can keep
        // a more accurate representation of how many entitlements are left rather than
        // always loading the static value from the DB.
        Map<Channel, Long> channelToAvailableEntitlements =
                new HashMap<Channel, Long>(allChannels.size());
        Map<Channel, Long> channelToAvailableFteEntitlements =
                new HashMap<Channel, Long>(allChannels.size());

        for (SystemOverview server : servers) {
            Long sysid = server.getId();

            Set<Long> chanIds = new HashSet<Long>();
            if (sysMapping.get(sysid) != null) {
                chanIds = sysMapping.get(sysid).getSubscribeChannelIds();
            }
            //Use an iterator so i can remove from the set
            Iterator<Long> it = chanIds.iterator();
            while (it.hasNext()) {
                Channel channel = idToChan.get(it.next());
                Long availableEntitlements = channelToAvailableEntitlements.get(channel);
                Long availableFteEntitlements =
                        channelToAvailableFteEntitlements.get(channel);

                if (availableEntitlements == null) {
                    availableEntitlements =
                            ChannelManager.getAvailableEntitlements(user.getOrg(), channel);
                    channelToAvailableEntitlements.put(channel, availableEntitlements);
                }
                if (availableFteEntitlements == null) {
                    availableFteEntitlements =
                            ChannelManager.getAvailableFveEntitlements(
                                    user.getOrg(), channel);
                    // null fte entitlements means not found
                    if (availableFteEntitlements == null) {
                        availableFteEntitlements = 0L;
                    }
                    channelToAvailableFteEntitlements.put(
                            channel, availableFteEntitlements);
                }

                //Most likely a custom channel, null means unlimited entitlements
                if (availableEntitlements == null) {
                    continue;
                }

                // First try to consume an FTE entitlement, then try regular,
                // then remove the system
                if (ChannelManager.isChannelFreeForSubscription(sysid, channel)) {
                    // do nothing, server gets this channel for free
                }
                else if (availableFteEntitlements > 0 &&
                        SystemManager.isServerIdFveEligible(sysid)) {
                    availableFteEntitlements -= 1;
                    channelToAvailableEntitlements.put(channel, availableFteEntitlements);
                }
                else if (availableEntitlements > 0) {
                    // Update our cached count for what will happen when
                    // the subscribe is done
                    availableEntitlements = availableEntitlements - 1;
                    channelToAvailableEntitlements.put(channel, availableEntitlements);
                }
                else {
                    sysMapping.get(sysid).getSubscribeChannelIds().remove(channel.getId());
                    sysMapping.get(sysid).getSubscribeNames().remove(channel.getName());
                    if (sysMapping.get(sysid).isEmtpy()) {
                        sysMapping.remove(sysid);
                    }
                }
            }
        }
        return sysMapping;
    }


    /**
     * Performs channel subscriptions. This method assumes the changes have been validated
     * through:
     * <ul>
     * <li>{@link #linkChannelsToSubscribeForServers(User, List, List)}</li>
     * <li>{@link #linkChannelsToUnsubscribeForServers(List, List)}</li>
     * </ul>
     * <p/>
     * Furthermore, this call assumes the changes have been written to the necessary
     * RhnSets via:
     * <ul>
     * <li>{@link #populateSsmChannelServerSets(User, List)}</li>
     * </ul>
     *
     * @param user user performing the action creations
     * @param sysMapping a collection of ChannelActionDAOs
     */
    public static void performChannelActions(User user,
            Collection<ChannelActionDAO> sysMapping) {

        for (ChannelActionDAO system : sysMapping) {
            for (Long cid : system.getSubscribeChannelIds()) {
                subscribeChannel(system.getId(), cid, user.getId());
            }
            for (Long cid : system.getUnsubscribeChannelIds()) {
                SystemManager.unsubscribeServerFromChannel(system.getId(), cid);
            }
        }
    }


    private static void subscribeChannel(Long sid, Long cid, Long uid) {

        CallableMode m = ModeFactory.getCallableMode("Channel_queries",
                "subscribe_server_to_channel");

        Map in = new HashMap();
        in.put("server_id", sid);
        in.put("user_id", uid);
        in.put("channel_id", cid);
        m.execute(in, new HashMap());
    }

    /**
     * Adds the selected server IDs to the SSM RhnSet.
     *
     * @param user      cannot be <code>null</code>
     * @param serverIds cannot be <code>null</code>
     */
    public static void addServersToSsm(User user, String[] serverIds) {
        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addAll(Arrays.asList(serverIds));
        RhnSetManager.store(set);
    }

    /**
     * Clears the list of servers in the SSM.
     *
     * @param user cannot be <code>null</code>
     */
    public static void clearSsm(User user) {
        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.clear();
        RhnSetManager.store(set);
    }

    /**
     * Returns a list of server-ids of the servers in the SSM selection, for the specified
     * user
     *
     * @param user user whose system-set we care about
     * @return list of server-ids
     */
    public static List<Long> listServerIds(User user) {
        RhnSet ssm = RhnSetDecl.SYSTEMS.lookup(user);
        List<Long> sids = new ArrayList<Long>();
        if (ssm != null) {
            for (RhnSetElement rse : ssm.getElements()) {
                sids.add(rse.getElement());
            }
        }
        return sids;
    }

}
