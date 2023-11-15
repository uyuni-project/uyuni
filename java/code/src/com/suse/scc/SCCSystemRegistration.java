/*
 * Copyright (c) 2023 SUSE LLC
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
import com.suse.scc.model.SCCOrganizationSystemsUpdateResponse;
import com.suse.scc.model.SCCRegisterSystemJson;
import com.suse.scc.model.SCCSystemCredentialsJson;
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

/**
 * Custom class that handles system registry in SCC.
 */
public class SCCSystemRegistration {
    private static final Logger LOG = LogManager.getLogger(SCCSystemRegistration.class);
    private final SCCClient sccClient;
    private final List<SCCRegCacheItem> items;
    private final Credentials primaryCredential;

    /**
     * Constructor
     *
     * @param sccClientIn         the SCC client
     * @param itemsIn             the items to register
     * @param primaryCredentialIn the current primary organization credential
     */
    private SCCSystemRegistration(
            SCCClient sccClientIn,
            List<SCCRegCacheItem> itemsIn,
            Credentials primaryCredentialIn
    ) {
        this.sccClient = sccClientIn;
        this.items = itemsIn;
        this.primaryCredential = primaryCredentialIn;
    }

    /**
     * Registers systems in SCC
     *
     * @param sccClient         the SCC client
     * @param items             the items to register
     * @param primaryCredential the current primary organization credential
     */
    public static void register(
            SCCClient sccClient,
            List<SCCRegCacheItem> items,
            Credentials primaryCredential
    ) {
        SCCSystemRegistration registration = new SCCSystemRegistration(sccClient, items, primaryCredential);
        registration.handle();
    }

    /**
     * Handles the registration of systems
     *
     * @return
     */
    private void handle() {
        // filter cache items that require registration
        Map<SCCSystemId, SCCRegCacheItem> itemsBySccSystemId = new HashMap<>();
        Map<SCCSystemId, SCCRegisterSystemJson> pendingRegistrationSystems = new HashMap<>();

        items.forEach(cacheItem -> {
            /*
                If a system is PAYG, we don't want to send (at least for now) information to SCC
                so that the customer is not charged twice for the same machine. In the future we'll
                send the machine with the flag `is_payg` set to true, but that will be done when the SCC
                team supports it.
             */
            if (cacheItem.getOptServer().filter(s -> s.isForeign()).isEmpty() &&
                    cacheItem.getOptServer().filter(s -> s.isPayg()).isEmpty()) {
                LOG.debug("Forward registration of {}", cacheItem);

                SCCRegisterSystemJson payload = getPayload(cacheItem);
                SCCSystemId sccSystemId = new SCCSystemId(payload.getLogin(), payload.getPassword());

                itemsBySccSystemId.put(sccSystemId, cacheItem);
                pendingRegistrationSystems.put(sccSystemId, payload);
            }
            else {
                applyItemStandardUpdate(cacheItem);
            }
        });


        // register systems
        List<SCCSystemCredentialsJson> registeredSystems =
                batchRegisterSystems(pendingRegistrationSystems.values().stream().collect(Collectors.toList()));

        // update successfully registered cached items
        for (SCCSystemCredentialsJson systemCredentials : registeredSystems) {
            SCCSystemId sccSystemId = new SCCSystemId(systemCredentials.getLogin(), systemCredentials.getPassword());
            SCCRegCacheItem cacheItem = itemsBySccSystemId.get(sccSystemId);

            cacheItem.setSccId(systemCredentials.getId());
            cacheItem.setSccLogin(systemCredentials.getLogin());
            cacheItem.setSccPasswd(systemCredentials.getPassword());

            applyItemStandardUpdate(cacheItem);

            pendingRegistrationSystems.remove(sccSystemId);
        }

        // update failed registered cached items
        for (Map.Entry<SCCSystemId, SCCRegisterSystemJson> entry : pendingRegistrationSystems.entrySet()) {
            SCCRegCacheItem cacheItem = itemsBySccSystemId.get(entry.getKey());
            LOG.error("Error registering system {}", cacheItem.getId());
            cacheItem.setRegistrationErrorTime(new Date());
        }

        // save all items
        items.forEach(cacheItem -> cacheItem.getOptServer().ifPresent(ServerFactory::save));
    }

    private List<SCCSystemCredentialsJson> batchRegisterSystems(
            List<SCCRegisterSystemJson> pendingRegistrationSystems
    ) {
        int batchSize = Config.get().getInt(ConfigDefaults.REG_BATCH_SIZE, 50);

        // split items into batches
        List<List<SCCRegisterSystemJson>> systemsBatches =
                IntStream.range(0, (pendingRegistrationSystems.size() + batchSize - 1) / batchSize)
                        .mapToObj(i -> pendingRegistrationSystems.subList(
                                i * batchSize,
                                Math.min((i + 1) * batchSize, pendingRegistrationSystems.size()
                                )))
                        .collect(Collectors.toList());

        List<SCCSystemCredentialsJson> registeredSystems = new ArrayList<>();

        for (List<SCCRegisterSystemJson> batch : systemsBatches) {
            try {
                SCCOrganizationSystemsUpdateResponse response = sccClient.createUpdateSystems(
                        batch,
                        primaryCredential.getUsername(),
                        primaryCredential.getPassword()
                );
                registeredSystems.addAll(response.getSystems());
            }
            catch (SCCClientException e) {
                LOG.error("SCC error while registering systems", e);
            }
            catch (Exception e) {
                LOG.error("Error registering systems", e);
            }
        }

        return registeredSystems;
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
            hwinfo.put("mem_total", srv.getVirtualInstance().getTotalMemory().toString());
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

    private void applyItemStandardUpdate(SCCRegCacheItem item) {
        item.setSccRegistrationRequired(false);
        item.setRegistrationErrorTime(null);
        item.setCredentials(primaryCredential);
    }
}
