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
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRegCacheItem;
import com.redhat.rhn.domain.server.Server;

import com.suse.scc.model.SCCMinProductJson;
import com.suse.scc.model.SCCRegisterSystemJson;
import com.suse.utils.Opt;

import org.apache.commons.lang3.RandomStringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

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
        SCCCachingFactory.initNewSystemsToForward();
        List<SCCRegCacheItem> forwardRegistration = SCCCachingFactory.findSystemsToForwardRegistration();
        List<SCCRegCacheItem> deregister = SCCCachingFactory.listDeregisterItems();

        for (SCCRegCacheItem rci : forwardRegistration) {
            SCCRegisterSystemJson payload = getPayload(rci);
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

        String passwd = rci.getOptSccPasswd().orElseGet(() -> {
            String pw = RandomStringUtils.randomAlphanumeric(64);
            rci.setSccPasswd(pw);
            SCCCachingFactory.saveRegCacheItem(rci);
            return pw;
        });

        return new SCCRegisterSystemJson(srv.getId().toString(), passwd, srv.getHostname(),
                hwinfo, products);
    }
}
