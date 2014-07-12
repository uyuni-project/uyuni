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
package com.redhat.rhn.frontend.xmlrpc.channel;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ChannelTreeNode;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.manager.channel.ChannelManager;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ChannelHandler
 * @version $Rev$
 * @xmlrpc.namespace channel
 * @xmlrpc.doc Provides method to get back a list of Software Channels.
 */
public class ChannelHandler extends BaseHandler {

    /**
     * Lists all visible software channels. For all child channels,
     * 'channel_parent_label' will be the channel label of the parent channel.
     * For all base channels, 'channel_parent_label' will be an empty string.
     * @param loggedInUser The current user
     * @return Returns array of Maps with the following keys:
     * channel_label, channel_parent_label, channel_name, channel_end_of_life,
     * channel_arch
     *
     * @xmlrpc.doc List all visible software channels.
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     *  #array()
     *      #struct("channel")
     *          #prop("string", "label")
     *          #prop("string", "name")
     *          #prop("string", "parent_label")
     *          #prop("string", "end_of_life")
     *          #prop("string", "arch")
     *      #struct_end()
     *  #array_end()
     */
    public List<Map<String, Object>> listSoftwareChannels(User loggedInUser) {

        List<Map<String, Object>> items = ChannelManager.allChannelsTree(loggedInUser);

        // perl just makes stuff so much harder since it
        // transforms data in a map with one line, but it's
        // still looping through the list more than once.
        // To keep backwards compatiblity I need to transform
        // this list of maps into a different list of maps.
        //
        // Just because it is ONE line it doesn't make it efficient.

        List<Map<String, Object>> returnList =
                new ArrayList<Map<String, Object>>(items.size());
        for (Map<String, Object> item : items) {
            // Deprecated stupid code
            // this is some really stupid code, but oh well, c'est la vie
            Map<String, Object> newItem = new HashMap<String, Object>();
            newItem.put("label", item.get("label"));
            newItem.put("parent_label", StringUtils.defaultString(
                    (String) item.get("parent_channel")));
            newItem.put("name", item.get("name"));
            newItem.put("end_of_life",
                    StringUtils.defaultString(
                            (String)item.get("end_of_life")));
            newItem.put("arch", item.get("channel_arch"));

            returnList.add(newItem);
        }

        return returnList;
    }

    /**
     * Lists all software channels that the user's organization is entitled to.
     * @param loggedInUser The current user
     * @return Returns array of channels with info such as channel_label, channel_name,
     * channel_parent_label, packages and systems.
     *
     * @xmlrpc.doc List all software channels that the user's organization is entitled to.
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     *     #array()
     *         $ChannelTreeNodeSerializer
     *     #array_end()
     */
    public Object[] listAllChannels(User loggedInUser) {
        DataResult<ChannelTreeNode> dr = ChannelManager.allChannelTree(loggedInUser, null);
        dr.elaborate();
        return dr.toArray();
    }

    /**
     * Lists all the vendor software channels that the user's organization is
     * entitled to.
     * @param loggedInUser The current user
     * @return Returns array of channels with info such as channel_label, channel_name,
     * channel_parent_label, packages and systems.
     *
     * @xmlrpc.doc Lists all the vendor software channels that the user's organization
     * is entitled to.
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     *     #array()
     *         $ChannelTreeNodeSerializer
     *     #array_end()
     */
    public Object[] listVendorChannels(User loggedInUser) {
        DataResult<ChannelTreeNode> dr = ChannelManager
                .vendorChannelTree(loggedInUser, null);
        dr.elaborate();
        return dr.toArray();
    }

    /**
     * Lists all Red Hat software channels that the user's organization is entitled to.
     * @param loggedInUser The current user
     * @return Returns array of channels with info such as channel_label, channel_name,
     * channel_parent_label, packages and systems.
     * @deprecated being replaced by listVendorChannels(String sessionKey)
     *
     * @xmlrpc.doc List all Red Hat software channels that the user's organization is
     * entitled to.
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     *     #array()
     *         $ChannelTreeNodeSerializer
     *     #array_end()
     */
    @Deprecated
    public Object[] listRedHatChannels(User loggedInUser) {
        return listVendorChannels(loggedInUser);
    }

    /**
     * Lists the most popular software channels based on the popularity
     * count given.
     * @param loggedInUser The current user
     * @param popularityCount channels with at least this many systems subscribed
     * will be returned
     * @return Returns array of channels with info such as channel_label, channel_name,
     * channel_parent_label, packages and systems.
     *
     * @xmlrpc.doc List the most popular software channels.  Channels that have at least
     * the number of systems subscribed as specified by the popularity count will be
     * returned.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("int", "popularityCount")
     * @xmlrpc.returntype
     *     #array()
     *         $ChannelTreeNodeSerializer
     *     #array_end()
     */
    public Object[] listPopularChannels(User loggedInUser, Integer popularityCount) {
        DataResult<ChannelTreeNode> dr = ChannelManager.popularChannelTree(loggedInUser,
                new Long(popularityCount), null);
        dr.elaborate();
        return dr.toArray();
    }

    /**
     * Lists all software channels that belong to the user's organization.
     * @param loggedInUser The current user
     * @return Returns array of channels with info such as channel_label, channel_name,
     * channel_parent_label, packages and systems.
     *
     * @xmlrpc.doc List all software channels that belong to the user's organization.
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     *     #array()
     *         $ChannelTreeNodeSerializer
     *     #array_end()
     */
    public Object[] listMyChannels(User loggedInUser) {
        DataResult<ChannelTreeNode> dr = ChannelManager.myChannelTree(loggedInUser, null);
        dr.elaborate();
        return dr.toArray();
    }

    /**
     * List all software channels that may be shared by the user's organization.
     * @param loggedInUser The current user
     * @return Returns array of channels with info such as channel_label, channel_name,
     * channel_parent_label, packages and systems.
     *
     * @xmlrpc.doc List all software channels that may be shared by the user's
     * organization.
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     *     #array()
     *         $ChannelTreeNodeSerializer
     *     #array_end()
     */
    public Object[] listSharedChannels(User loggedInUser) {
        DataResult<ChannelTreeNode> dr = ChannelManager
                .sharedChannelTree(loggedInUser, null);
        dr.elaborate();
        return dr.toArray();
    }

    /**
     * List all retired software channels.  These are channels that the user's organization
     * is entitled to, but are no longer supported as they have reached their 'end-of-life'
     * date.
     * @param loggedInUser The current user
     * @return Returns array of channels with info such as channel_label, channel_name,
     * channel_parent_label, packages and systems.
     *
     * @xmlrpc.doc List all retired software channels.  These are channels that the user's
     * organization is entitled to, but are no longer supported because they have reached
     * their 'end-of-life' date.
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     *     #array()
     *         $ChannelTreeNodeSerializer
     *     #array_end()
     */
    public Object[] listRetiredChannels(User loggedInUser) {
        DataResult<ChannelTreeNode> dr = ChannelManager
                .retiredChannelTree(loggedInUser, null);
        dr.elaborate();
        return dr.toArray();
    }
}
