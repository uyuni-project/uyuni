/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.proxy.get;

import static com.redhat.rhn.common.ErrorReportingStrategies.logReportingStrategy;
import static com.suse.proxy.ProxyConfigUtils.PROXY_PILLAR_CATEGORY;
import static com.suse.utils.Predicates.isAbsent;
import static java.util.Arrays.asList;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;

import com.suse.proxy.ProxyConfigUtils;
import com.suse.proxy.get.formdata.ProxyConfigGetFormDataAcquisitor;
import com.suse.proxy.get.formdata.ProxyConfigGetFormDataContext;
import com.suse.proxy.get.formdata.ProxyConfigGetFormDataContextHandler;
import com.suse.proxy.get.formdata.ProxyConfigGetFormDataPreConditions;
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
    public static final String MGRPXY = "mgrpxy";
    private final List<ProxyConfigGetFormDataContextHandler> getFormDataContextHandlerChain = new ArrayList<>();

    /**
     * Constructor
     */
    public ProxyConfigGetFacadeImpl() {
        this.getFormDataContextHandlerChain.addAll(asList(
                new ProxyConfigGetFormDataPreConditions(),
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
     * @param user                       the user
     * @param server                     the server
     * @param systemEntitlementManager   the systemEntitlementManager
     * @return the form data
     */
    @Override
    public Map<String, Object> getFormData(
            User user,
            Server server,
            SystemEntitlementManager systemEntitlementManager
    ) {
        ProxyConfigGetFormDataContext context =
                new ProxyConfigGetFormDataContext(user, server, this.getProxyConfig(server), systemEntitlementManager);

        for (ProxyConfigGetFormDataContextHandler handler : getFormDataContextHandlerChain) {
            handler.handle(context);
            context.getErrorReport().report(logReportingStrategy(this));
            if (context.getErrorReport().hasErrors()) {
                break;
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("currentConfig", Json.GSON.toJson(context.getProxyConfigAsMap()));
        data.put("parents", Json.GSON.toJson(context.getElectableParentsFqdn()));
        data.put("validationErrors", Json.GSON.toJson(context.getErrorReport().getErrorMessages()));
        data.put("registryUrlExample", context.getRegistryUrlExample());
        data.put("registryTagExample", context.getRegistryTagExample());
        data.put("hasCertificates", context.hasCertificates());

        return data;
    }

}
