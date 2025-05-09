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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;

import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.model.SCCUpdateSystemJson;
import com.suse.scc.model.SCCVirtualizationHostJson;
import com.suse.scc.proxy.SCCProxyFactory;
import com.suse.scc.proxy.SCCProxyRecord;
import com.suse.scc.registration.SCCSystemRegistration;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SCCSystemRegistrationManager {

    private static final Logger LOG = LogManager.getLogger(SCCSystemRegistrationManager.class);
    private final SCCClient sccClient;
    private final SCCProxyFactory sccProxyFactory;
    private final SCCSystemRegistration sccSystemRegistration;

    /**
     * Constructor
     *
     * @param sccClientIn the scc client
     * @param sccProxyFactoryIn the scc proxy factory
     */
    public SCCSystemRegistrationManager(SCCClient sccClientIn, SCCProxyFactory sccProxyFactoryIn) {
        this(sccClientIn, sccProxyFactoryIn, new SCCSystemRegistration());
    }

    /**
     * Constructor
     *
     * @param sccClientIn             the scc client
     * @param sccProxyFactoryIn       the scc proxy factory
     * @param sccSystemRegistrationIn the scc system registration
     */
    public SCCSystemRegistrationManager(SCCClient sccClientIn,
                                        SCCProxyFactory sccProxyFactoryIn,
                                        SCCSystemRegistration sccSystemRegistrationIn) {
        this.sccClient = sccClientIn;
        this.sccProxyFactory = sccProxyFactoryIn;
        this.sccSystemRegistration = sccSystemRegistrationIn;
    }

    /**
     * Update last_seen field in SCC for all registered clients
     * @param primaryCredential the primary scc credential
     */
    public void updateLastSeen(SCCCredentials primaryCredential) {
        List<Map<String, Object>> candidates = SCCCachingFactory.listUpdateLastSeenCandidates(primaryCredential);

        List<SCCUpdateSystemJson> sysList = candidates.stream()
                .map(c -> new SCCUpdateSystemJson(
                        c.get("scc_login").toString(),
                        c.get("scc_passwd").toString(),
                        (Date) c.get("checkin")))
                .toList();

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
                    SCCCredentials itemCredentials = cacheItem.getOptCredentials().get();
                    try {
                        LOG.debug("de-register system {}", cacheItem);
                        sccClient.deleteSystem(sccId, itemCredentials.getUsername(), itemCredentials.getPassword());
                        SCCCachingFactory.deleteRegCacheItem(cacheItem);
                    }
                    catch (SCCClientException e) {
                        handleErrorDeregister(cacheItem, e, forceDBDeletion);
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

    private void handleErrorDeregister(SCCRegCacheItem cacheItem, SCCClientException e, boolean forceDBDeletion) {
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

    /**
     * De-register a system from SCC
     *
     * @param proxyRecords the proxy records identifying the system to de-register
     * @param forceDBDeletion force delete the proxy record when set to true
     */
    public void proxyDeregister(List<SCCProxyRecord> proxyRecords, boolean forceDBDeletion) {
        proxyRecords.forEach(proxyRecord -> proxyRecord.getOptSccId().ifPresentOrElse(
                sccId -> {
                    try {
                        LOG.debug("de-register system {}", proxyRecord);
                        sccClient.deleteSystem(sccId, proxyRecord.getSccLogin(), proxyRecord.getSccPasswd());
                        sccProxyFactory.remove(proxyRecord);
                    }
                    catch (SCCClientException e) {
                        handleErrorProxyDeregister(proxyRecord, e, forceDBDeletion);
                    }
                    catch (Exception e) {
                        handleErrorProxyDeregister(proxyRecord, e);
                    }
                },
                () -> {
                    LOG.debug("delete not registered cache item {}", proxyRecord);
                    sccProxyFactory.remove(proxyRecord);
                }
        ));
    }

    private void handleErrorProxyDeregister(SCCProxyRecord proxyRecord, SCCClientException e, boolean forceDBDeletion) {
        boolean doRemove = forceDBDeletion;
        if (e.getHttpStatusCode() == HttpStatus.SC_NOT_FOUND) {
            LOG.info("System {} not found in SCC", proxyRecord.getSccId());
            doRemove = true;
        }
        else {
            handleErrorProxyDeregister(proxyRecord, e);
        }

        if (doRemove) {
            sccProxyFactory.remove(proxyRecord);
        }
    }

    private void handleErrorProxyDeregister(SCCProxyRecord proxyRecord, Exception e) {
        LOG.error("Error while deregistering system {}", proxyRecord.getSccId(), e);
        proxyRecord.setSccRegistrationErrorTime(new Date());
        sccProxyFactory.save(proxyRecord);
    }

    /**
     * Register systems in SCC
     *
     * @param proxyRecords the proxy records identifying the system to register
     * @param primaryCredential the current primary organization credential
     */
    public void proxyRegister(List<SCCProxyRecord> proxyRecords, SCCCredentials primaryCredential) {
        sccSystemRegistration.proxyRegister(sccClient, proxyRecords, primaryCredential);
    }

    /**
     * Register systems in SCC
     *
     * @param items the items to register
     * @param primaryCredential the current primary organization credential
     */
    public void register(List<SCCRegCacheItem> items, SCCCredentials primaryCredential) {
        sccSystemRegistration.register(sccClient, items, primaryCredential);
    }

    /**
     * Insert or Update virtualization host data at SCC
     * @param virtHosts the virtual host data
     * @param primaryCredential primary credential
     */
    public void virtualInfo(List<SCCVirtualizationHostJson> virtHosts, SCCCredentials primaryCredential) {
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
