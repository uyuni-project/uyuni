/*
 * Copyright (c) 2021 SUSE LLC
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
package com.redhat.rhn.domain.action.virtualization;

import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.virtualization.NetworkDefinition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Action creating a virtual network
 */
public class VirtualizationNetworkCreateAction extends BaseVirtualizationNetworkAction {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .create();

    private NetworkDefinition definition;

    /**
     * @return network definition
     */
    public NetworkDefinition getDefinition() {
        return definition;
    }

    /**
     * Return the serialized definition, to be used only by hibernate.
     *
     * @return serialized definition
     */
    public String getDefinitionAsString() {
        return GSON.toJson(definition);
    }

    /**
     * @param definitionIn network definition
     */
    public void setDefinition(NetworkDefinition definitionIn) {
        definition = definitionIn;
    }

    /**
     * Set the network definition from its serialized version.
     * To be used only by hibernate.
     *
     * @param definitionIn the serialized definition
     */
    public void setDefinitionAsString(String definitionIn) {
        definition = GSON.fromJson(definitionIn, new TypeToken<NetworkDefinition>() { }.getType());
    }

    @Override
    public String getWebSocketActionId() {
        return String.format("new-%s", getId());
    }
}
