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
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.domain.server.Server;

import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCWebClient;
import com.suse.scc.model.SCCMinProductJson;
import com.suse.scc.model.SCCRegisterSystemJson;
import com.suse.scc.model.SCCSystemCredentialsJson;
import com.suse.utils.Opt;

import org.apache.commons.lang3.RandomStringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class ForwardRegistrationTask extends RhnJavaJob {

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        if (!ConfigDefaults.get().isForwardRegistrationEnabled()) {
            log.debug("Forwarding registrations disabled");
            return;
        }
        try {
            if (Config.get().getString(ContentSyncManager.RESOURCE_PATH) == null) {
                URI url = new URI(Config.get().getString(ConfigDefaults.SCC_URL));
                //TODO: find a better place to put getUUID
                String uuid = ContentSyncManager.getUUID();
                SCCCachingFactory.initNewSystemsToForward();
                List<SCCRegCacheItem> forwardRegistration = SCCCachingFactory.findSystemsToForwardRegistration();
                List<SCCRegCacheItem> deregister = SCCCachingFactory.listDeregisterItems();
                List<Credentials> credentials = CredentialsFactory.lookupSCCCredentials();
                credentials.stream().filter(c -> c.isPrimarySCCCredential()).findFirst().ifPresent(credential -> {
                    SCCConfig config = new SCCConfig(url, credential.getUsername(), credential.getPassword(), uuid);
                    SCCWebClient sccClient = new SCCWebClient(config);
                    deregister.forEach(cacheItem -> {
                        try {
                            sccClient.deleteSystem(cacheItem.getId());
                        }
                        catch (SCCClientException e) {
                            log.error("Error deregistering system " + cacheItem.getId(), e);
                        }
                    });
                    forwardRegistration.forEach(cacheItem -> {
                        cacheItem.setCredentials(credential);
                        try {
                            SCCSystemCredentialsJson systemCredentials = sccClient.createSystem(getPayload(cacheItem));
                            cacheItem.setSccId(systemCredentials.getId());
                            cacheItem.setSccLogin(systemCredentials.getLogin());
                            cacheItem.setSccPasswd(systemCredentials.getPassword());
                            cacheItem.setRegistrationErrorTime(null);
                        }
                        catch (SCCClientException e) {
                            log.error("Error registering system " + cacheItem.getId(), e);
                            cacheItem.setRegistrationErrorTime(new Date());
                        }
                        cacheItem.getOptServer().ifPresent(ServerFactory::save);
                    });
                });
            }
        }
        catch (URISyntaxException e) {
           log.error(e);
        }
    }

    public SCCRegisterSystemJson getPayload(SCCRegCacheItem rci) {
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
        Optional.ofNullable(srv.getCpu().getNrCPU()).ifPresent(c -> hwinfo.put("cpus", c.toString()));
        Optional.ofNullable(srv.getCpu().getNrsocket()).ifPresent(c -> hwinfo.put("sockets", c.toString()));
        hwinfo.put("arch", srv.getServerArch().getLabel().split("-")[0]);
        if (srv.isVirtualGuest()) {
            String cloud = "";
            String hv = srv.getVirtualInstance().getType().getLabel();
            switch(hv) {
            case "fully_virtualized":
            case "para_virtualized":
                hv = "XEN";
                break;
            case "qemu":
                hv = "KVM";
                break;
            case "vmware":
            case "hyperv":
                break;
            case "nutanix":
                hv = "Nutanix";
                break;
            case "azure":
                cloud = "Microsoft";
                break;
            case "aws":
                cloud = "Amazon";
                break;
            case "gce":
                cloud = "Google";
                break;
            default:
                break;
            }
            hwinfo.put("hypervisor", hv);
            hwinfo.put("cloud_provider", cloud);
            Optional.ofNullable(srv.getVirtualInstance().getUuid()).ifPresent(uuid -> {
                hwinfo.put("uuid", uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
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
