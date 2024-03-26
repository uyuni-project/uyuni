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

import com.redhat.rhn.domain.channel.AppStreamModule;
import com.redhat.rhn.domain.server.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ChannelModulesResponse {

    /**
     * Constructs a ChannelModulesResponse object based on the provided parameters.
     *
     * @param channelIdIn       The ID of the channel.
     * @param channelLabelIn    The label of the channel.
     * @param channelModulesIn  The list of AppStreamModule objects associated with the channel.
     * @param serverIn          The Server object used to check module enablement.
     */
    public ChannelModulesResponse(
            Long channelIdIn,
            String channelLabelIn,
            List<AppStreamModule> channelModulesIn,
            Server serverIn
    ) {
        numberOfAppStreams = channelModulesIn.size();
        channelId = channelIdIn;
        channelLabel = channelLabelIn;
        modulesNames = new TreeSet<>();
        modules = new HashMap<>();
        channelModulesIn.forEach(it -> {
            modulesNames.add(it.getName());
            var module = new AppStreamModuleResponse(it, serverIn);
            if (modules.containsKey(it.getName())) {
                modules.get(it.getName()).add(module);
            }
            else {
                modules.put(it.getName(), new ArrayList<>(List.of(module)));
            }
        });
    }

    private Long channelId;
    private String channelLabel;
    private Set<String> modulesNames;
    private Map<String, List<AppStreamModuleResponse>> modules;

    private Integer numberOfAppStreams;
}
