/*
 * Copyright (c) 2025 SUSE LLC
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

package com.suse.proxy.get;

import static com.suse.proxy.ProxyConfigUtils.PROXY_PILLAR_CATEGORY;
import static com.suse.utils.Predicates.isAbsent;
import static java.util.Arrays.asList;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

import com.suse.proxy.ProxyConfigUtils;
import com.suse.proxy.get.formdata.ProxyConfigGetFormDataAcquisitor;
import com.suse.proxy.get.formdata.ProxyConfigGetFormDataContext;
import com.suse.proxy.get.formdata.ProxyConfigGetFormDataContextHandler;
import com.suse.proxy.get.formdata.ProxyConfigGetFormDataProxyInitializer;
import com.suse.proxy.get.formdata.ProxyConfigGetFormDefaults;
import com.suse.proxy.model.ProxyConfig;
import com.suse.utils.Json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class responsible for getting proxy configurations
 */
public class ProxyConfigGetFacadeImpl implements ProxyConfigGetFacade {
    private final List<ProxyConfigGetFormDataContextHandler> getFormDataContextHandlerChain = new ArrayList<>();

    /**
     * Constructor
     */
    public ProxyConfigGetFacadeImpl() {
        this.getFormDataContextHandlerChain.addAll(asList(
                new ProxyConfigGetFormDataProxyInitializer(),
                new ProxyConfigGetFormDefaults(),
                new ProxyConfigGetFormDataAcquisitor()
        ));
    }

    /**
     * Get the proxy configuration
     *
     * @param server the server
     * @return the proxy configuration
     */
    @Override
    public ProxyConfig getProxyConfig(Server server) {
        if (isAbsent(server)) {
            return null;
        }
        return server.asMinionServer()
                .flatMap(minionServer -> minionServer
                        .getPillarByCategory(PROXY_PILLAR_CATEGORY)
                        .map(ProxyConfigUtils::proxyConfigFromPillar))
                .orElse(null);
    }

    /**
     * Get the data to be rendered in the form
     *
     * @param user   the user
     * @param server the server
     * @return the form data
     */
    @Override
    public Map<String, Object> getFormData(User user, Server server) {
        ProxyConfigGetFormDataContext context =
                new ProxyConfigGetFormDataContext(user, server, this.getProxyConfig(server));

        // only sort of validation required ad this point
        if (server != null) {
            for (ProxyConfigGetFormDataContextHandler handler : getFormDataContextHandlerChain) {
                handler.handle(context);
            }
        }
        else {
            context.setInitFailMessage("Server not found");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("currentConfig", Json.GSON.toJson(context.getProxyConfigAsMap()));
        data.put("parents", Json.GSON.toJson(context.getElectableParentsFqdn()));
        data.put("initFailMessage", context.getInitFailMessage());
        data.put("registryUrlExample", context.getRegistryUrlExample());
        data.put("registryTagExample", context.getRegistryTagExample());

        return data;
    }

}
