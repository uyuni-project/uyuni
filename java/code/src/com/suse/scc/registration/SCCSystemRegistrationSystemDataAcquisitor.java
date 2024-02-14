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

package com.suse.scc.registration;

import static java.util.Optional.ofNullable;

import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.manager.content.ContentSyncManager;

import com.suse.scc.SCCSystemId;
import com.suse.scc.model.SCCHwInfoJson;
import com.suse.scc.model.SCCMinProductJson;
import com.suse.scc.model.SCCRegisterSystemJson;
import com.suse.utils.Opt;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This object goal is to filter the cached items that require registration and build their own payload entry for the
 * SCC rest call.
 */
public class SCCSystemRegistrationSystemDataAcquisitor implements SCCSystemRegistrationContextHandler {
    private static final Logger LOG = LogManager.getLogger(SCCSystemRegistrationSystemDataAcquisitor.class);

    @Override
    public void handle(SCCSystemRegistrationContext context) {
        context.getItems().forEach(cacheItem -> {
            if (isSccRegistrationRequired(cacheItem)) {
                LOG.debug("Forward registration of {}", cacheItem);
                getPayload(cacheItem).ifPresent(payload -> {
                    SCCSystemId sccSystemId = new SCCSystemId(payload.getLogin(), payload.getPassword());
                    context.getItemsBySccSystemId().put(sccSystemId, cacheItem);
                    context.getPendingRegistrationSystems().put(sccSystemId, payload);
                });
            }
            else {
                context.getPaygSystems().add(cacheItem);
            }
        });
    }

    /**
     * If a system is PAYG, we don't want to send (at least for now) information to SCC
     * so that the customer is not charged twice for the same machine. In the future we'll
     * send the machine with the flag `is_payg` set to true, but that will be done when the SCC
     * team supports it.
     */
    private boolean isSccRegistrationRequired(SCCRegCacheItem cacheItem) {
        return cacheItem.getOptServer().filter(Server::isForeign).isEmpty() &&
                cacheItem.getOptServer().filter(Server::isPayg).isEmpty();
    }

    private Optional<SCCRegisterSystemJson> getPayload(SCCRegCacheItem rci) {
        return rci.getOptServer().map(srv -> {
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

            SCCHwInfoJson hwInfo = new SCCHwInfoJson();

            Optional<CPU> cpu = ofNullable(srv.getCpu());
            cpu.flatMap(c -> ofNullable(c.getNrCPU())).ifPresent(c -> hwInfo.setCpus(c.intValue()));
            cpu.flatMap(c -> ofNullable(c.getNrsocket())).ifPresent(c -> hwInfo.setSockets(c.intValue()));
            hwInfo.setArch(srv.getServerArch().getLabel().split("-")[0]);
            if (srv.isVirtualGuest()) {
                VirtualInstance virtualInstance = srv.getVirtualInstance();

                hwInfo.setHypervisor(virtualInstance.getType().getHypervisor().orElse(""));
                hwInfo.setCloudProvider(virtualInstance.getType().getCloudProvider().orElse(""));
                ofNullable(virtualInstance.getUuid())
                        .ifPresent(u -> hwInfo.setUuid(u.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                                "$1-$2-$3-$4-$5")));

                ofNullable(virtualInstance.getTotalMemory())
                        .ifPresent(totalMemory -> hwInfo.setMemTotal(totalMemory.intValue()));
            }
            else {
                hwInfo.setMemTotal((int) srv.getRam());
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

            return new SCCRegisterSystemJson(login, passwd, srv.getHostname(), hwInfo, products,
                    srv.getServerInfo().getCheckin());
        });
    }

}
