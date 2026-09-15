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
 * SPDX-License-Identifier: GPL-2.0-only
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

import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.model.SCCOrganizationSystemsUpdateResponse;
import com.suse.scc.model.SCCRegisterSystemItem;
import com.suse.scc.model.SCCSystemCredentialsJson;
import com.suse.scc.model.SCCUpdateSystemItem;
import com.suse.scc.model.SCCVirtualizationHostJson;
import com.suse.scc.proxy.SCCProxyFactory;
import com.suse.scc.proxy.SCCProxyRecord;
import com.suse.scc.proxy.SccProxyStatus;
import com.suse.scc.registration.SCCSystemRegistration;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SCCSystemRegistrationManager {

    private static final Logger LOG = LogManager.getLogger(SCCSystemRegistrationManager.class);
    private final SCCClient sccClient;
    private final SCCProxyFactory sccProxyFactory;
    private final SCCSystemRegistration sccSystemRegistration;

    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .create();

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
     * @return the scc proxy factory
     */
    public SCCProxyFactory getSccProxyFactory() {
        return sccProxyFactory;
    }

    /**
     * Update last_seen field in SCC for all registered clients
     * @param sccPrimaryCredentials the primary scc credential
     * @param updateLastSeenItems the list of  {@link SCCUpdateSystemItem} systems to update last_seen field
     */
    public void updateLastSeen(List<SCCUpdateSystemItem> updateLastSeenItems, SCCCredentials sccPrimaryCredentials) {

        ArrayList<List<SCCUpdateSystemItem>> batches = new ArrayList<>(
                IntStream.range(0, updateLastSeenItems.size()).boxed().collect(
                        Collectors.groupingBy(e -> e / Config.get().getInt(ConfigDefaults.REG_BATCH_SIZE, 200),
                                Collectors.mapping(updateLastSeenItems::get, Collectors.toList())
                                )).values());
        for (List<SCCUpdateSystemItem> batch: batches) {
            try {
                sccClient.updateBulkLastSeen(batch,
                        sccPrimaryCredentials.getUsername(),
                        sccPrimaryCredentials.getPassword());
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
     * Update last_seen field in SCC for all registered clients
     * @param sccPrimaryCredentials the primary scc credential
     * @param proxyUpdateLastSeen the list of  {@link SCCProxyRecord} systems to update last_seen field
     */
    public void proxyUpdateLastSeen(List<SCCProxyRecord> proxyUpdateLastSeen, SCCCredentials sccPrimaryCredentials) {
        ArrayList<List<SCCProxyRecord>> batches = new ArrayList<>(
                IntStream.range(0, proxyUpdateLastSeen.size()).boxed().collect(
                        Collectors.groupingBy(e -> e / Config.get().getInt(ConfigDefaults.REG_BATCH_SIZE, 200),
                                Collectors.mapping(proxyUpdateLastSeen::get, Collectors.toList())
                        )).values());
        for (List<SCCProxyRecord> proxyRecordBatch: batches) {
            try {
                List<SCCUpdateSystemItem> batch = proxyRecordBatch.stream()
                        .map(r -> new SCCUpdateSystemItem(r.getSccLogin(), r.getSccPasswd(), r.getLastSeenAt()))
                        .toList();

                sccClient.updateBulkLastSeen(batch,
                        sccPrimaryCredentials.getUsername(),
                        sccPrimaryCredentials.getPassword());

                proxyRecordBatch.forEach(r -> {
                    r.setLastSeenAt(null);
                    sccProxyFactory.save(r);
                });
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
     * De-register a system from SCC as a proxy
     *
     * @param proxyRecords the proxy records identifying the system to de-register
     * @param sccPrimaryCredentials the current primary organization credential
     * @param forceDBDeletion force delete the proxy record when set to true
     */
    public void proxyDeregister(List<SCCProxyRecord> proxyRecords, SCCCredentials sccPrimaryCredentials,
                                boolean forceDBDeletion) {
        proxyRecords.forEach(proxyRecord -> proxyRecord.getOptSccId().ifPresentOrElse(
                sccId -> {
                    try {
                        LOG.debug("de-register system {}", proxyRecord);
                        sccClient.deleteSystem(sccId,
                                sccPrimaryCredentials.getUsername(),
                                sccPrimaryCredentials.getPassword());
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
     * Force de-register a system from SCC as a proxy
     *
     * @param proxyRecords          the proxy records identifying the system to de-register
     * @param sccPrimaryCredentials the current primary organization credential
     */
    public void proxyForceDeregister(List<SCCProxyRecord> proxyRecords, SCCCredentials sccPrimaryCredentials) {
        proxyRecords.forEach(proxyRecord -> proxyRecord.getOptSccId().ifPresent(
                sccId -> {
                    try {
                        LOG.debug("force de-register system {}", proxyRecord);
                        sccClient.deleteSystem(sccId,
                                sccPrimaryCredentials.getUsername(),
                                sccPrimaryCredentials.getPassword());
                    }
                    catch (SCCClientException e) {
                        if (e.getHttpStatusCode() == HttpStatus.SC_NOT_FOUND) {
                            LOG.info("System {} not found in SCC", proxyRecord.getSccId());
                        }
                        else {
                            LOG.error("Error while force deregistering system {}", proxyRecord.getSccId(), e);
                        }
                    }
                    catch (Exception e) {
                        LOG.error("Error while force deregistering system {}", proxyRecord.getSccId(), e);
                    }
                }
        ));
    }

    /**
     * Register systems in SCC
     *
     * @param items the items to register
     * @param sccPrimaryCredentials the current primary organization credential
     */
    public void register(List<SCCRegCacheItem> items, SCCCredentials sccPrimaryCredentials) {
        sccSystemRegistration.register(sccClient, items, sccPrimaryCredentials);
    }

    /**
     * Register systems in SCC as a proxy
     *
     * @param proxyRecords the proxy records identifying the system to register
     * @param sccPrimaryCredentials the current primary organization credential
     */
    public void proxyRegister(List<SCCProxyRecord> proxyRecords, SCCCredentials sccPrimaryCredentials) {
        ArrayList<List<SCCProxyRecord>> batches = new ArrayList<>(
                IntStream.range(0, proxyRecords.size()).boxed().collect(
                        Collectors.groupingBy(e -> e / Config.get().getInt(ConfigDefaults.REG_BATCH_SIZE, 200),
                                Collectors.mapping(proxyRecords::get, Collectors.toList())
                        )).values());

        for (List<SCCProxyRecord> recordBatch: batches) {
            try {
                List<SCCRegisterSystemItem> batchCreationJson = recordBatch.stream()
                        .map(r -> Json.GSON.fromJson(r.getSccCreationJson(), SCCRegisterSystemItem.class))
                        .toList();

                SCCOrganizationSystemsUpdateResponse response = sccClient.createUpdateSystems(
                        batchCreationJson,
                        sccPrimaryCredentials.getUsername(),
                        sccPrimaryCredentials.getPassword());

                for (SCCProxyRecord proxyRecord : recordBatch) {
                    Optional<SCCSystemCredentialsJson> maybeSystemCredential = response.getSystems().stream()
                        .filter(e -> e.getLogin().equals(proxyRecord.getSccLogin()))
                        .findAny();

                    if (maybeSystemCredential.isPresent()) {
                        SCCSystemCredentialsJson systemCredential = maybeSystemCredential.get();

                        proxyRecord.setSccId(systemCredential.getId());
                        proxyRecord.setSccRegistrationErrorTime(null);
                        proxyRecord.setStatus(SccProxyStatus.SCC_CREATED);
                    }
                    else {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Error registering system {}", proxyRecord);
                        }

                        proxyRecord.setSccId(null);
                        proxyRecord.setSccRegistrationErrorTime(new Date());
                        proxyRecord.setStatus(SccProxyStatus.SCC_CREATION_PENDING);
                    }
                    sccProxyFactory.save(proxyRecord);
                }
            }
            catch (SCCClientException e) {
                LOG.error("SCC error while updating virtualization hosts", e);
                recordBatch.forEach(r -> r.setSccRegistrationErrorTime(new Date()));
            }
            catch (Exception e) {
                LOG.error("Error updating virtualization hosts", e);
            }
        }
    }

    /**
     * Insert or Update virtualization host data at SCC
     * @param virtHosts the virtual host data
     * @param sccPrimaryCredentials primary credential
     */
    public void virtualInfo(List<SCCVirtualizationHostJson> virtHosts, SCCCredentials sccPrimaryCredentials) {
        ArrayList<List<SCCVirtualizationHostJson>> batches = new ArrayList<>(
                IntStream.range(0, virtHosts.size()).boxed().collect(
                        Collectors.groupingBy(e -> e / Config.get().getInt(ConfigDefaults.REG_BATCH_SIZE, 200),
                                Collectors.mapping(virtHosts::get, Collectors.toList())
                        )).values());
        for (List<SCCVirtualizationHostJson> batch: batches) {
            try {
                sccClient.setVirtualizationHost(batch,
                        sccPrimaryCredentials.getUsername(),
                        sccPrimaryCredentials.getPassword());
            }
            catch (SCCClientException e) {
                LOG.error("SCC error while updating virtualization hosts", e);
            }
            catch (Exception e) {
                LOG.error("Error updating virtualization hosts", e);
            }
        }
    }

    /**
     * Insert or Update virtualization host data at SCC
     * @param proxyVirtHosts the virtual host data
     * @param sccPrimaryCredentials primary credential
     */
    public void proxyVirtualInfo(List<SCCProxyRecord> proxyVirtHosts, SCCCredentials sccPrimaryCredentials) {
        ArrayList<List<SCCProxyRecord>> batches = new ArrayList<>(
                IntStream.range(0, proxyVirtHosts.size()).boxed().collect(
                        Collectors.groupingBy(e -> e / Config.get().getInt(ConfigDefaults.REG_BATCH_SIZE, 200),
                                Collectors.mapping(proxyVirtHosts::get, Collectors.toList())
                        )).values());

        for (List<SCCProxyRecord> recordBatch: batches) {
            try {
                List<SCCVirtualizationHostJson> batch = recordBatch.stream()
                        .map(r -> gson.fromJson(r.getSccCreationJson(), SCCVirtualizationHostJson.class))
                        .toList();
                sccClient.setVirtualizationHost(batch,
                        sccPrimaryCredentials.getUsername(),
                        sccPrimaryCredentials.getPassword());
                recordBatch.forEach(sccProxyFactory::remove);
            }
            catch (SCCClientException e) {
                LOG.error("SCC error while updating virtualization hosts", e);
                recordBatch.forEach(r -> r.setSccRegistrationErrorTime(new Date()));
            }
            catch (Exception e) {
                LOG.error("Error updating virtualization hosts", e);
            }
        }
    }
}
