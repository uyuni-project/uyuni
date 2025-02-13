/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.domain.action;


import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.Pillar;

import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.proxy.ProxyConfigUtils;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ProxyConfigurationApply - Class representing TYPE_PROXY_CONFIGURATION_APPLY action
 */
public class ProxyConfigurationApplyAction extends Action {

    private final Pillar pillar;
    private final Map<String, Object> proxyConfigFiles;

    /**
     * Default constructor
     * @param pillarIn the pillar
     * @param proxyConfigFilesIn the proxy configuration files
     * @param orgIn the organization
     */
    public ProxyConfigurationApplyAction(Pillar pillarIn, Map<String, Object> proxyConfigFilesIn, Org orgIn) {
        this.setActionType(ActionFactory.TYPE_PROXY_CONFIGURATION_APPLY);
        this.pillar = pillarIn;
        this.proxyConfigFiles = proxyConfigFilesIn;
        this.setOrg(orgIn);
    }

    public Pillar getPillar() {
        return pillar;
    }

    public Map<String, Object> getProxyConfigFiles() {
        return proxyConfigFiles;
    }

    /**
     * Get the apply_proxy_config local call
     * @param minions the minions
     * @return the apply_proxy_config local call
     */
    public Map<LocalCall<?>, List<MinionSummary>> getApplyProxyConfigAction(List<MinionSummary> minions) {
        Map<String, Object> data = new HashMap<>();
        data.putAll(ProxyConfigUtils.applyProxyConfigDataFromPillar(getPillar()));
        data.putAll(getProxyConfigFiles());

        return Map.of(
                State.apply(Collections.singletonList(SaltServerActionService.APPLY_PROXY_CONFIG), Optional.of(data)),
                minions
        );
    }
}
