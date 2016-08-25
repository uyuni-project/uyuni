/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.reactor.hardware;

import com.google.gson.JsonSyntaxException;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceType;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltGrains;
import com.suse.salt.netapi.calls.modules.Smbios.RecordType;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Detect if minion is running in a VM and which VM type.
 */
public class VirtualizationMapper extends AbstractHardwareMapper<VirtualInstance> {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(VirtualizationMapper.class);

    /**
     * The constructor.
     *
     * @param saltServiceInvoker a {@link SaltServiceInvoker} instance
     */
    public VirtualizationMapper(SaltServiceInvoker saltServiceInvoker) {
        super(saltServiceInvoker);
    }

    @Override
    public void doMap(MinionServer server, ValueMap grains) {
        String virtType = grains.getValueAsString("virtual");
        String virtSubtype = grains.getValueAsString("virtual_subtype");
        String virtUuid = grains.getValueAsString("uuid");
        String cpuarch = grains.getValueAsString(SaltGrains.CPUARCH.getValue());

        if (virtType == null) {
            setError("Grain 'virtual' has no value");
        }

        VirtualInstanceType type = null;

        if (StringUtils.isNotBlank(virtType) && !"physical".equals(virtType)) {
            if (StringUtils.isNotBlank(virtUuid)) {

                virtUuid = StringUtils.remove(virtUuid, '-');

                String virtTypeLabel = null;
                switch (virtType) {
                    case "xen":
                        if ("Xen PV DomU".equals(virtSubtype)) {
                            virtTypeLabel = "para_virtualized";
                        }
                        else {
                            virtTypeLabel = "fully_virtualized";
                        }
                        break;
                    case "qemu":
                    case "kvm":
                        virtTypeLabel = "qemu";
                        break;
                    case "VMware":
                        virtTypeLabel = "vmware";
                        break;
                    case "HyperV":
                        virtTypeLabel = "hyperv";
                        break;
                    case "VirtualBox":
                        virtTypeLabel = "virtualbox";
                        break;
                    default:
                        LOG.warn(String.
                                format("Unsupported virtual instance " +
                                        "type '%s' for minion '%s'",
                                virtType, server.getMinionId()));
                        // TODO what to do with other virt types ?
                }
                type = VirtualInstanceFactory.getInstance()
                        .getVirtualInstanceType(virtTypeLabel);

                if (type == null) { // fallback
                    type = VirtualInstanceFactory.getInstance().getParaVirtType();
                    LOG.warn(String.format(
                            "Can't find virtual instance type for string '%s'. " +
                            "Defaulting to '%s' for minion '%s'",
                            virtType, type.getLabel(), server.getMinionId()));
                }

            }
        }
        else if (CpuArchUtil.isDmiCapable(cpuarch)) {
            // there's no DMI on S390 and PPC64
            try {
                ValueMap dmiSystem = saltInvoker.getDmiRecords(server.getMinionId(),
                        RecordType.SYSTEM).map(ValueMap::new).orElseGet(ValueMap::new);
                String manufacturer = dmiSystem.getValueAsString("manufacturer");
                String productName = dmiSystem.getValueAsString("product_name");
                if ("HITACHI".equalsIgnoreCase(manufacturer) &&
                        productName.endsWith(" HVM LPAR")) {
                    if (StringUtils.isEmpty(virtUuid)) {
                        virtUuid = "flex-guest";
                    }
                    type = VirtualInstanceFactory.getInstance()
                            .getVirtualInstanceType("virtage");
                }
            }
            catch (JsonSyntaxException e) {
                LOG.warn("Could not retrieve DMI info from minion '" +
                        server.getMinionId() + "'. JSON syntax error.");
            }
        }

        if (type != null) {
            VirtualInstance virtualInstance = server.getVirtualInstance();
            if (virtualInstance == null) {
                virtualInstance = new VirtualInstance();
                virtualInstance.setUuid(virtUuid);
                virtualInstance.setConfirmed(1L);
                virtualInstance.setHostSystem(null);
                virtualInstance.setName(null);
                virtualInstance.setType(type);
                virtualInstance.setState(VirtualInstanceFactory
                        .getInstance().getUnknownState());
                // add the virtualInstance to the server.
                // do it at the end to avoid hibernate flushing
                // an incomplete virtualInstance
                virtualInstance.setGuestSystem(server);

            }
            else if (virtualInstance.getConfirmed() != 1L) {
                virtualInstance.setConfirmed(1L);
            }
        }
    }
}
