/**
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
package com.suse.scc;

import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.content.ContentSyncManager;

import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.model.SCCMinProductJson;
import com.suse.scc.model.SCCRegisterSystemJson;
import com.suse.scc.model.SCCSystemCredentialsJson;
import com.suse.utils.Opt;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public class SCCSystemRegistrationManager {

    private final Logger LOG = Logger.getLogger(SCCSystemRegistrationManager.class);
    private final SCCClient sccClient;

    /**
     * Constructor
     *
     * @param sccClientIn
     */
    public SCCSystemRegistrationManager(SCCClient sccClientIn) {
        this.sccClient = sccClientIn;
    }

    /**
     * De-register a system from SCC
     *
     * @param items the cache item identify the system to de-register
     * @param forceDBDeletion force delete the cache item when set to true
     */
    public void deregister(List<SCCRegCacheItem> items, boolean forceDBDeletion) {
        items.forEach(cacheItem -> {
            cacheItem.getOptSccId().ifPresentOrElse(
                    sccId -> {
                        Credentials itemCredentials = cacheItem.getOptCredentials().get();
                        try {
                            LOG.debug("de-register system " + cacheItem);
                            sccClient.deleteSystem(sccId, itemCredentials.getUsername(), itemCredentials.getPassword());
                            SCCCachingFactory.deleteRegCacheItem(cacheItem);
                        }
                        catch (SCCClientException e) {
                            LOG.error("Error deregistering system " + cacheItem.getId(), e);
                            if (forceDBDeletion || e.getHttpStatusCode() == 404) {
                                SCCCachingFactory.deleteRegCacheItem(cacheItem);
                            }
                        }
                    },
                    () -> {
                        LOG.debug("delete not registered cache item " + cacheItem);
                        SCCCachingFactory.deleteRegCacheItem(cacheItem);
                    }
            );
        });
    }

    /**
     * Register a system at SCC
     *
     * @param items the items to register
     * @param primaryCredential the current primary organization credential
     */
    public void register(List<SCCRegCacheItem> items, Credentials primaryCredential) {
        items.forEach(cacheItem -> {
            try {
                Credentials itemCredentials = cacheItem.getOptCredentials().orElse(primaryCredential);
                LOG.debug("Forward registration of " + cacheItem);
                SCCSystemCredentialsJson systemCredentials = sccClient.createSystem(
                        getPayload(cacheItem),
                        itemCredentials.getUsername(),
                        itemCredentials.getPassword());
                cacheItem.setSccId(systemCredentials.getId());
                cacheItem.setSccLogin(systemCredentials.getLogin());
                cacheItem.setSccPasswd(systemCredentials.getPassword());
                cacheItem.setSccRegistrationRequired(false);
                cacheItem.setRegistrationErrorTime(null);
                cacheItem.setCredentials(itemCredentials);
            }
            catch (SCCClientException e) {
                LOG.error("Error registering system " + cacheItem.getId(), e);
                cacheItem.setRegistrationErrorTime(new Date());
            }
            cacheItem.getOptServer().ifPresent(ServerFactory::save);
        });
    }

    private SCCRegisterSystemJson getPayload(SCCRegCacheItem rci) {
        Server srv = rci.getOptServer().get();
        List<SCCMinProductJson> products = Opt.fold(srv.getInstalledProductSet(),
                () -> {
                    return new LinkedList<SUSEProduct>();
                },
                s -> {
                    List<SUSEProduct> prd = new LinkedList<>();
                    prd.add(s.getBaseProduct());
                    prd.addAll(s.getAddonProducts());
                    return prd;
                }
        ).stream()
                .map(p -> new SCCMinProductJson(p))
                .collect(Collectors.toList());

        Map<String, String> hwinfo = new HashMap<>();
        Optional<CPU> cpu = ofNullable(srv.getCpu());
        cpu.flatMap(c -> ofNullable(c.getNrCPU())).ifPresent(c -> hwinfo.put("cpus", c.toString()));
        cpu.flatMap(c -> ofNullable(c.getNrsocket())).ifPresent(c -> hwinfo.put("sockets", c.toString()));
        hwinfo.put("arch", srv.getServerArch().getLabel().split("-")[0]);
        if (srv.isVirtualGuest()) {
            hwinfo.put("hypervisor", srv.getVirtualInstance().getType().getHypervisor().orElse(""));
            hwinfo.put("cloud_provider", srv.getVirtualInstance().getType().getCloudProvider().orElse(""));
            ofNullable(srv.getVirtualInstance().getUuid()).ifPresent(u -> {
                hwinfo.put("uuid", u.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                        "$1-$2-$3-$4-$5"));
            });
        }
        else {
            // null == physical instance
            hwinfo.put("hypervisor", null);
        }

        String login = rci.getOptSccLogin().orElseGet(() -> {
            String l = String.format("%s-%s", ContentSyncManager.getUUID(), srv.getId().toString());
            rci.setSccLogin(l);
            SCCCachingFactory.saveRegCacheItem(rci);
            return l;
        });
        String passwd = rci.getOptSccPasswd().orElseGet(() -> {
            String pw = RandomStringUtils.randomAlphanumeric(64);
            rci.setSccPasswd(pw);
            SCCCachingFactory.saveRegCacheItem(rci);
            return pw;
        });

        return new SCCRegisterSystemJson(login, passwd, srv.getHostname(), hwinfo, products);
    }
}
