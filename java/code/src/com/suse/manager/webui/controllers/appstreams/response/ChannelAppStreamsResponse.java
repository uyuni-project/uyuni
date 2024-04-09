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
import com.redhat.rhn.domain.server.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ChannelAppStreamsResponse {

    /**
     * Constructs a ChannelAppStreamsResponse object based on the provided parameters.
     *
     * @param channelIdIn       The ID of the channel.
     * @param channelLabelIn    The label of the channel.
     * @param appStreamsIn      The list of AppStream objects associated with the channel.
     * @param serverIn          The Server object used to check module enablement.
     */
    public ChannelAppStreamsResponse(
        Long channelIdIn,
        String channelLabelIn,
        List<AppStream> appStreamsIn,
        Server serverIn
    ) {
        numberOfAppStreams = appStreamsIn.size();
        channelId = channelIdIn;
        channelLabel = channelLabelIn;
        modulesNames = new TreeSet<>();
        appStreams = new HashMap<>();
        appStreamsIn.forEach(it -> {
            modulesNames.add(it.getName());
            var module = new AppStreamModuleResponse(it, serverIn);
            if (appStreams.containsKey(it.getName())) {
                appStreams.get(it.getName()).add(module);
            }
            else {
                appStreams.put(it.getName(), new ArrayList<>(List.of(module)));
            }
        });
    }

    private Long channelId;
    private String channelLabel;
    private Set<String> modulesNames;
    private Map<String, List<AppStreamModuleResponse>> appStreams;

    private Integer numberOfAppStreams;

    public Set<String> getModulesNames() {
        return modulesNames;
    }
}
