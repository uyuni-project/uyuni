/*
 * Copyright (c) 2024 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.channel.appstreams;

import com.redhat.rhn.domain.channel.AppStream;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.NoSuchChannelException;
import com.redhat.rhn.manager.appstreams.AppStreamsManager;
import com.redhat.rhn.manager.channel.ChannelManager;

import com.suse.manager.api.ReadOnly;

import java.util.List;

/**
 * Handler for AppStream operations on channels

 * @apidoc.namespace channel.appstreams
 * @apidoc.doc Provides methods to handle appstreams for channels.
 */
public class ChannelAppStreamHandler extends BaseHandler {

    /**
     * List available module streams for a given channel.
     *
     * @param loggedInUser The current user
     * @param channelLabel label of the channel
     * @return list of appstreams
     * @apidoc.doc List available module streams for a given channel.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "channelLabel")
     * @apidoc.returntype
     * #return_array_begin()
     *     $AppStreamSerializer
     * #array_end()
     */
    @ReadOnly
    public List<AppStream> listModuleStreams(User loggedInUser, String channelLabel) {
        Channel channel = ChannelManager.lookupByLabel(loggedInUser.getOrg(), channelLabel);
        if (channel == null) {
            throw new NoSuchChannelException();
        }
        return AppStreamsManager.listChannelAppStreams(channel.getId());
    }

    /**
     * Check if given channel is modular
     *
     * @param loggedInUser The current user
     * @param channelLabel label of the channel
     * @return true if channel is modular false otherwise
     * @apidoc.doc Check if channel is modular.
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "channelLabel")
     * @apidoc.returntype #param_desc("boolean", "result", "true if the channel is modular")
     */
    @ReadOnly
    public boolean isModular(User loggedInUser, String channelLabel) {
        Channel channel = ChannelManager.lookupByLabel(loggedInUser.getOrg(), channelLabel);
        if (channel == null) {
            throw new NoSuchChannelException();
        }
        return channel.isModular();
    }

    /**
     * List of modular channels in users org.
     *
     * @param loggedInUser The current user
     * @return list of modular channels
     * @apidoc.doc List modular channels in users organization.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     * #return_array_begin()
     *     $ChannelSerializer
     * #array_end()
     */
    @ReadOnly
    public List<Channel> listModular(User loggedInUser) {
        return ChannelFactory.listModularChannels(loggedInUser);
    }
}
