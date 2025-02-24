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
import com.redhat.rhn.domain.server.Pillar;

import com.suse.proxy.ProxyConfigUtils;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.utils.Xor;

import com.google.gson.reflect.TypeToken;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Transient;

/**
 * ProxyConfigurationApply - Class representing TYPE_PROXY_CONFIGURATION_APPLY action
 */
public class ProxyConfigurationApplyAction extends Action {

    private static final String APPLY_PROXY_CONFIG = "proxy.apply_proxy_config";
    private final Pillar pillar;

    @Transient
    private final transient Map<String, Object> proxyConfigFiles;

    /**
     * Default constructor
     * @param pillarIn the pillar
     * @param proxyConfigFilesIn the proxy configuration files
     * @param orgIn the organization
     */
    public ProxyConfigurationApplyAction(Pillar pillarIn, Map<String, Object> proxyConfigFilesIn, Org orgIn) {
        this.setActionType(ActionFactory.TYPE_PROXY_CONFIGURATION_APPLY);
        this.setId(ActionFactory.TYPE_PROXY_CONFIGURATION_APPLY.getId().longValue());
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
     * Builds the LocalCall for the apply_proxy_config state apply with the pillar and config files data
     * @return the apply_proxy_config local call
     */
    public LocalCall<Xor<String, Map<String, State.ApplyResult>>> getApplyProxyConfigCall() {
        Map<String, Object> data = new HashMap<>();
        data.putAll(ProxyConfigUtils.applyProxyConfigDataFromPillar(getPillar()));
        data.putAll(getProxyConfigFiles());

        return State.apply(
                Collections.singletonList(APPLY_PROXY_CONFIG),
                Optional.of(data),
                Optional.of(false), Optional.of(false),
                new TypeToken<>() { }
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(pillar, proxyConfigFiles, getOrg());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ProxyConfigurationApplyAction that = (ProxyConfigurationApplyAction) obj;
        return Objects.equals(pillar, that.pillar) &&
                Objects.equals(proxyConfigFiles, that.proxyConfigFiles) &&
                Objects.equals(getOrg(), that.getOrg());
    }

}
