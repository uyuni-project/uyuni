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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.proxy.update;

import static com.suse.proxy.ProxyConfigUtils.EMAIL_FIELD;
import static com.suse.proxy.ProxyConfigUtils.INTERMEDIATE_CAS_FIELD;
import static com.suse.proxy.ProxyConfigUtils.MAX_CACHE_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PARENT_FQDN_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PILLAR_REGISTRY_ENTRY;
import static com.suse.proxy.ProxyConfigUtils.PILLAR_REGISTRY_TAG_ENTRY;
import static com.suse.proxy.ProxyConfigUtils.PILLAR_REGISTRY_URL_ENTRY;
import static com.suse.proxy.ProxyConfigUtils.PROXY_CERT_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PROXY_FQDN_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PROXY_KEY_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PROXY_PILLAR_CATEGORY;
import static com.suse.proxy.ProxyConfigUtils.PROXY_PORT_FIELD;
import static com.suse.proxy.ProxyConfigUtils.ROOT_CA_FIELD;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_REGISTRY;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Pillar;

import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;
import com.suse.proxy.ProxyContainerImagesEnum;
import com.suse.proxy.RegistryUrl;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the saving of the pillars
 */
public class ProxyConfigUpdateSavePillars implements ProxyConfigUpdateContextHandler {

    @Override
    public void handle(ProxyConfigUpdateContext context) {
        ProxyConfigUpdateJson request = context.getRequest();

        MinionServer proxyMinion = context.getProxyMinion();
        Pillar pillar = proxyMinion.getPillarByCategory(PROXY_PILLAR_CATEGORY).orElseGet(() ->
            new Pillar(PROXY_PILLAR_CATEGORY, new HashMap<>(), proxyMinion)
        );

        pillar.getPillar().clear();

        pillar.add(PROXY_FQDN_FIELD, context.getProxyFqdn());
        pillar.add(PARENT_FQDN_FIELD, request.getParentFqdn());
        pillar.add(PROXY_PORT_FIELD, request.getProxyPort());
        pillar.add(MAX_CACHE_FIELD, request.getMaxCache());
        pillar.add(EMAIL_FIELD, request.getEmail());

        pillar.add(ROOT_CA_FIELD, context.getRootCA());
        pillar.add(INTERMEDIATE_CAS_FIELD, context.getIntermediateCAs());
        pillar.add(PROXY_CERT_FIELD, context.getProxyCert());
        pillar.add(PROXY_KEY_FIELD, context.getProxyKey());

        if (SOURCE_MODE_REGISTRY.equals(request.getSourceMode())) {
            Map<String, Map<String, String>> registryEntries = new HashMap<>();
            for (ProxyContainerImagesEnum proxyContainerImage : ProxyContainerImagesEnum.values()) {
                RegistryUrl registryUrl = context.getRegistryUrls().get(proxyContainerImage);
                Map<String, String> registryEntry = new HashMap<>();
                registryEntry.put(PILLAR_REGISTRY_URL_ENTRY, registryUrl.getRegistry());
                registryEntry.put(PILLAR_REGISTRY_TAG_ENTRY, registryUrl.getTag());
                registryEntries.put(proxyContainerImage.getImageName(), registryEntry);
            }
            pillar.add(PILLAR_REGISTRY_ENTRY, registryEntries);
        }
        HibernateFactory.getSession().persist(pillar);
        context.setPillar(pillar);
    }
}
