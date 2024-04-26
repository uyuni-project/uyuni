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
package com.suse.manager.webui.controllers.appstreams.response;

import com.redhat.rhn.domain.channel.AppStream;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.Server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChannelAppStreamsResponse {

    /**
     * Constructs a ChannelAppStreamsResponse object based on the provided parameters.
     *
     * @param channelIn         The channel that the AppStreams belong to.
     * @param appStreamsIn      The set of AppStream objects associated with the channel.
     * @param serverIn          The Server object used to check module enablement.
     */
    public ChannelAppStreamsResponse(
        Channel channelIn,
        List<AppStream> appStreamsIn,
        Server serverIn
    ) {
        channel = new ChannelJson(channelIn);
        appStreams = new HashMap<>();
        appStreamsIn.forEach(it -> {
            var module = new AppStreamModuleResponse(it, serverIn);
            if (appStreams.containsKey(it.getName())) {
                appStreams.get(it.getName()).add(module);
            }
            else {
                appStreams.put(it.getName(), new HashSet<>(List.of(module)));
            }
        });
    }

    private final ChannelJson channel;
    private final Map<String, Set<AppStreamModuleResponse>> appStreams;

    public ChannelJson getChannel() {
        return channel;
    }

    public Map<String, Set<AppStreamModuleResponse>> getAppStreams() {
        return appStreams;
    }

    /**
     * A JSON object representation of an AppStream channel
     */
    public static class ChannelJson {
        /**
         * Instantiate a JSON object
         * @param channelIn the channel
         */
        public ChannelJson(Channel channelIn) {
            id = channelIn.getId();
            label = channelIn.getLabel();
            name = channelIn.getName();
        }

        private final Long id;
        private final String label;
        private final String name;

        public Long getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public String getName() {
            return name;
        }
    }
}
