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
package com.suse.scc;

import static java.util.Optional.ofNullable;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
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
import com.suse.scc.model.SCCUpdateSystemJson;
import com.suse.scc.model.SCCVirtualizationHostJson;
import com.suse.utils.Opt;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SCCSystemRegistrationManager {

    private static final Logger LOG = LogManager.getLogger(SCCSystemRegistrationManager.class);
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
     * Update last_seen field in SCC for all registered clients
     * @param primaryCredential the primary scc credential
     */
    public void updateLastSeen(Credentials primaryCredential) {
        List<Map<String, Object>> candidates = SCCCachingFactory.listUpdateLastSeenCandidates(primaryCredential);

        List<SCCUpdateSystemJson> sysList = candidates.stream()
                .map(c -> new SCCUpdateSystemJson(
                        c.get("scc_login").toString(),
                        c.get("scc_passwd").toString(),
                        (Date) c.get("checkin")))
                .collect(Collectors.toList());

        ArrayList<List<SCCUpdateSystemJson>> batches = new ArrayList<>(
                IntStream.range(0, sysList.size()).boxed().collect(
                        Collectors.groupingBy(e -> e / Config.get().getInt(ConfigDefaults.REG_BATCH_SIZE, 200),
                                Collectors.mapping(e->sysList.get(e), Collectors.toList())
                                )).values());
        for (List<SCCUpdateSystemJson> batch: batches) {
            try {
                sccClient.updateBulkLastSeen(batch, primaryCredential.getUsername(), primaryCredential.getPassword());
            }
            catch (SCCClientException e) {
                LOG.error("SCC error while updating systems", e);
            }
            catch (Exception e) {
                LOG.error("Error updating systems", e);
            }
        }
    }

    /**
     * De-register a system from SCC
     *
     * @param items the cache item identify the system to de-register
     * @param forceDBDeletion force delete the cache item when set to true
     */
    public void deregister(List<SCCRegCacheItem> items, boolean forceDBDeletion) {
        items.forEach(cacheItem -> cacheItem.getOptSccId().ifPresentOrElse(
                sccId -> {
                    Credentials itemCredentials = cacheItem.getOptCredentials().get();
                    try {
                        LOG.debug("de-register system {}", cacheItem);
                        sccClient.deleteSystem(sccId, itemCredentials.getUsername(), itemCredentials.getPassword());
                        SCCCachingFactory.deleteRegCacheItem(cacheItem);
                    }
                    catch (SCCClientException e) {
                        if (e.getHttpStatusCode() == 404) {
                            LOG.info("System {} not found in SCC", cacheItem.getId());
                        }
                        else {
                            LOG.error("SCC error while deregistering system {}", cacheItem.getId(), e);
                        }
                        if (forceDBDeletion || e.getHttpStatusCode() == 404) {
                            SCCCachingFactory.deleteRegCacheItem(cacheItem);
                        }
                        cacheItem.setRegistrationErrorTime(new Date());
                    }
                    catch (Exception e) {
                        LOG.error("Error deregistering system {}", cacheItem.getId(), e);
                        cacheItem.setRegistrationErrorTime(new Date());
                    }
                },
                () -> {
                    LOG.debug("delete not registered cache item {}", cacheItem);
                    SCCCachingFactory.deleteRegCacheItem(cacheItem);
                }
        ));
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
                if (cacheItem.getOptServer().filter(s -> s.isForeign()).isEmpty()) {
                    LOG.debug("Forward registration of {}", cacheItem);
                    SCCSystemCredentialsJson systemCredentials = sccClient.createSystem(
                            getPayload(cacheItem),
                            itemCredentials.getUsername(),
                            itemCredentials.getPassword());
                    cacheItem.setSccId(systemCredentials.getId());
                    cacheItem.setSccLogin(systemCredentials.getLogin());
                    cacheItem.setSccPasswd(systemCredentials.getPassword());
                }
                // Foreign systems will not be send to SCC
                // but we need the entry in case it is a hypervisor and we need to send
                // virtualization host data to SCC
                cacheItem.setSccRegistrationRequired(false);
                cacheItem.setRegistrationErrorTime(null);
                cacheItem.setCredentials(itemCredentials);
            }
            catch (Exception e) {
                LOG.error("Error registering system {}", cacheItem.getId(), e);
                cacheItem.setRegistrationErrorTime(new Date());
            }
            cacheItem.getOptServer().ifPresent(ServerFactory::save);
        });
    }

    private SCCRegisterSystemJson getPayload(SCCRegCacheItem rci) {
        Server srv = rci.getOptServer().get();
        List<SCCMinProductJson> products = Opt.fold(srv.getInstalledProductSet(),
                (Supplier<List<SUSEProduct>>) LinkedList::new,
                s -> {
                    List<SUSEProduct> prd = new LinkedList<>();
                    prd.add(s.getBaseProduct());
                    prd.addAll(s.getAddonProducts());
                    return prd;
                }
        ).stream()
                .map(SCCMinProductJson::new)
                .collect(Collectors.toList());

        Map<String, String> hwinfo = new HashMap<>();
        Optional<CPU> cpu = ofNullable(srv.getCpu());
        cpu.flatMap(c -> ofNullable(c.getNrCPU())).ifPresent(c -> hwinfo.put("cpus", c.toString()));
        cpu.flatMap(c -> ofNullable(c.getNrsocket())).ifPresent(c -> hwinfo.put("sockets", c.toString()));
        hwinfo.put("arch", srv.getServerArch().getLabel().split("-")[0]);
        if (srv.isVirtualGuest()) {
            hwinfo.put("hypervisor", srv.getVirtualInstance().getType().getHypervisor().orElse(""));
            hwinfo.put("cloud_provider", srv.getVirtualInstance().getType().getCloudProvider().orElse(""));
            ofNullable(srv.getVirtualInstance().getUuid())
                    .ifPresent(u -> hwinfo.put("uuid", u.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                    "$1-$2-$3-$4-$5")));
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
            String pw = RandomStringUtils.random(64, 0, 0, true, true, null, new SecureRandom());
            rci.setSccPasswd(pw);
            SCCCachingFactory.saveRegCacheItem(rci);
            return pw;
        });

        return new SCCRegisterSystemJson(login, passwd, srv.getHostname(), hwinfo, products,
                srv.getServerInfo().getCheckin());
    }

    /**
     * Insert or Update virtualization host data at SCC
     * @param virtHosts the virtual host data
     * @param primaryCredential primary credential
     */
    public void virtualInfo(List<SCCVirtualizationHostJson> virtHosts, Credentials primaryCredential) {
        ArrayList<List<SCCVirtualizationHostJson>> batches = new ArrayList<>(
                IntStream.range(0, virtHosts.size()).boxed().collect(
                        Collectors.groupingBy(e -> e / Config.get().getInt(ConfigDefaults.REG_BATCH_SIZE, 200),
                                Collectors.mapping(e -> virtHosts.get(e), Collectors.toList())
                        )).values());
        for (List<SCCVirtualizationHostJson> batch: batches) {
            try {
                sccClient.setVirtualizationHost(batch, primaryCredential.getUsername(),
                        primaryCredential.getPassword());
            }
            catch (SCCClientException e) {
                LOG.error("SCC error while updating virtualization hosts", e);
            }
            catch (Exception e) {
                LOG.error("Error updating virtualization hosts", e);
            }
        }
    }
}
