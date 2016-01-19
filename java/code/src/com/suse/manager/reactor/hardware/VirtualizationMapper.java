package com.suse.manager.reactor.hardware;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceType;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Created by matei on 1/19/16.
 */
public class VirtualizationMapper extends AbstractHardwareMapper<VirtualInstance> {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(VirtualizationMapper.class);

    /**
     * The constructor.
     *
     * @param saltService a {@link SaltService} instance
     */
    public VirtualizationMapper(SaltService saltService) {
        super(saltService);
    }

    @Override
    public VirtualInstance map(MinionServer server, ValueMap grains) {
        String virtType = grains.getValueAsString("virtual");
        String virtSubtype = grains.getValueAsString("virtual_subtype");

        if (StringUtils.isNotBlank(virtType) && !"physical".equals(virtType)) {
            String virtUuid = grains.getValueAsString("uuid");
            if (StringUtils.isNotBlank(virtUuid)) {

                // TODO clarify   # Check to see if this uuid has already been registered to a
                //       # host and is confirmed.

                VirtualInstance virtualInstance = new VirtualInstance();
                virtualInstance.setUuid(StringUtils.remove(virtUuid, '-'));
                virtualInstance.setConfirmed(1L);
                virtualInstance.setGuestSystem(server);
                virtualInstance.setState(VirtualInstanceFactory.getInstance().getStoppedState());
                virtualInstance.setName(null);
                virtualInstance.setHostSystem(null);

                String virtTypeLabel = null;
                switch(virtType) {
                    case "xen":
                        if ("Xen PV DomU".equals(virtSubtype)) {
                            virtTypeLabel = "para_virtualized";
                        } else {
                            virtTypeLabel = "fully_virtualized";
                        }
                        break;
                    case "qemu":
                    case "kvm":
                        virtTypeLabel = "qemu";
                        break;
                    case "VMware":
                        virtTypeLabel = "qemu";
                        break;
                    case "HyperV":
                        virtTypeLabel = "hyperv";
                        break;
                    case "VirtualBox":
                        virtTypeLabel = "virtualbox";
                        break;
                    // TODO detect Hitachi LPAR (virtage)
                    default:
                        LOG.warn(String.
                                format("Unsupported virtual instance type '%s' for minion '%s'",
                                        virtType, server.getMinionId()));
                        // TODO do what with other virt types ?
//                    case "Parallels":
//                    case "oracle":
//                    case "bochs":
//                    case "chroot":
//                    case "uml":
//                    case "systemd-nspawn":
//                    case "VirtualPC":
//                    case "LXC":
//                    case "bhyve":
//                    case "openvzhn":
//                    case "openvzve":
//                    case "gce": // Google
//                    case "OpenStack":
                }
                VirtualInstanceType type = VirtualInstanceFactory.getInstance()
                        .getVirtualInstanceType(virtTypeLabel);

                if (type == null) { // fallback
                    type = VirtualInstanceFactory.getInstance().getParaVirtType();
                    LOG.warn(String.format("Can't find virtual instance type for string '%s'. " +
                            "Defaulting to '%s' for minion '%s'", virtType, type.getLabel(), server.getMinionId()));
                }

                virtualInstance.setType(type);
                virtualInstance.setState(VirtualInstanceFactory.getInstance().getUnknownState());

                return virtualInstance;
            }
        }
        return null;

    }
}
