/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
package com.redhat.rhn.manager.entitlement;

import com.redhat.rhn.domain.entitlement.AnsibleControlNodeEntitlement;
import com.redhat.rhn.domain.entitlement.BootstrapEntitlement;
import com.redhat.rhn.domain.entitlement.ContainerBuildHostEntitlement;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.entitlement.ForeignEntitlement;
import com.redhat.rhn.domain.entitlement.ManagementEntitlement;
import com.redhat.rhn.domain.entitlement.MonitoringEntitlement;
import com.redhat.rhn.domain.entitlement.OSImageBuildHostEntitlement;
import com.redhat.rhn.domain.entitlement.PeripheralServerEntitlement;
import com.redhat.rhn.domain.entitlement.SaltEntitlement;
import com.redhat.rhn.domain.entitlement.VirtualizationEntitlement;
import com.redhat.rhn.manager.BaseManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * EntitlementManager
 */
public class EntitlementManager extends BaseManager {

    private static final Logger LOG = LogManager.getLogger(EntitlementManager.class);

    //  ENTITLEMENTS
    public static final Entitlement MANAGEMENT = new ManagementEntitlement();
    public static final Entitlement VIRTUALIZATION = new VirtualizationEntitlement();
    public static final Entitlement BOOTSTRAP = new BootstrapEntitlement();
    public static final Entitlement SALT = new SaltEntitlement();
    public static final Entitlement FOREIGN = new ForeignEntitlement();
    public static final Entitlement CONTAINER_BUILD_HOST =
            new ContainerBuildHostEntitlement();
    public static final Entitlement OSIMAGE_BUILD_HOST =
            new OSImageBuildHostEntitlement();
    public static final Entitlement MONITORING = new MonitoringEntitlement();
    public static final Entitlement ANSIBLE_CONTROL_NODE = new AnsibleControlNodeEntitlement();
    public static final Entitlement PERIPHERAL_SERVER = new PeripheralServerEntitlement();

    public static final String UNENTITLED = "unentitled";
    public static final String ENTERPRISE_ENTITLED = "enterprise_entitled";
    public static final String VIRTUALIZATION_ENTITLED = "virtualization_host";
    public static final String BOOTSTRAP_ENTITLED = "bootstrap_entitled";
    public static final String SALT_ENTITLED = "salt_entitled";
    public static final String FOREIGN_ENTITLED = "foreign_entitled";
    public static final String CONTAINER_BUILD_HOST_ENTITLED = "container_build_host";
    public static final String OSIMAGE_BUILD_HOST_ENTITLED = "osimage_build_host";
    public static final String MONITORING_ENTITLED = "monitoring_entitled";
    public static final String ANSIBLE_CONTROL_NODE_ENTITLED = "ansible_control_node";
    public static final String PERIPHERAL_SERVER_ENTITLED = "peripheral_server";

    private static final Set<Entitlement> ADDON_ENTITLEMENTS;
    private static final Set<Entitlement> BASE_ENTITLEMENTS;
    static {
        ADDON_ENTITLEMENTS = new LinkedHashSet<>();
        ADDON_ENTITLEMENTS.add(VIRTUALIZATION);
        ADDON_ENTITLEMENTS.add(CONTAINER_BUILD_HOST);
        ADDON_ENTITLEMENTS.add(OSIMAGE_BUILD_HOST);
        ADDON_ENTITLEMENTS.add(MONITORING);
        ADDON_ENTITLEMENTS.add(ANSIBLE_CONTROL_NODE);
        ADDON_ENTITLEMENTS.add(PERIPHERAL_SERVER);

        BASE_ENTITLEMENTS = new LinkedHashSet<>();
        BASE_ENTITLEMENTS.add(MANAGEMENT);
        BASE_ENTITLEMENTS.add(SALT);
        BASE_ENTITLEMENTS.add(FOREIGN);
        BASE_ENTITLEMENTS.add(BOOTSTRAP);
    }

    /**
     * Returns the entitlement whose name matches the given <code>name</code>
     * @param name Name of Entitlement.
     * @return the entitlement whose name matches the given name.
     */
    public static Entitlement getByName(String name) {
        if (ENTERPRISE_ENTITLED.equals(name)) {
            return MANAGEMENT;
        }
        else if (VIRTUALIZATION_ENTITLED.equals(name)) {
            return VIRTUALIZATION;
        }
        else if (BOOTSTRAP_ENTITLED.equals(name)) {
            return BOOTSTRAP;
        }
        else if (SALT_ENTITLED.equals(name)) {
            return SALT;
        }
        else if (FOREIGN_ENTITLED.equals(name)) {
            return FOREIGN;
        }
        else if (CONTAINER_BUILD_HOST_ENTITLED.equals(name)) {
            return CONTAINER_BUILD_HOST;
        }
        else if (OSIMAGE_BUILD_HOST_ENTITLED.equals(name)) {
            return OSIMAGE_BUILD_HOST;
        }
        else if (MONITORING_ENTITLED.equals(name)) {
            return MONITORING;
        }
        else if (ANSIBLE_CONTROL_NODE_ENTITLED.equals(name)) {
            return ANSIBLE_CONTROL_NODE;
        }
        else if (PERIPHERAL_SERVER_ENTITLED.equals(name)) {
            return PERIPHERAL_SERVER;
        }
        LOG.debug("Unknown entitlement: {}", name);
        return null;
    }

    /**
     * Returns the static set of addon entitlements.
     * @return Unmodifiable set.
     */
    public static Set<Entitlement> getAddonEntitlements() {
        return Collections.unmodifiableSet(ADDON_ENTITLEMENTS);
    }

    /**
     * Returns the static set of base entitlements.
     * @return Unmodifiable set.
     */
    public static Set<Entitlement>  getBaseEntitlements() {
        return Collections.unmodifiableSet(BASE_ENTITLEMENTS);
    }
}
