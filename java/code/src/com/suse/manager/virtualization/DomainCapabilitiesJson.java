/**
 * Copyright (c) 2018 SUSE LLC
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
package com.suse.manager.virtualization;

import java.util.List;
import java.util.Map;

/**
 * Class representing the salt output for domain capabilities
 */
public class DomainCapabilitiesJson {

    private String arch;
    private String domain;
    private Map<String, Map<String, List<String>>> devices;

    /**
     * @return Returns the arch.
     */
    public String getArch() {
        return arch;
    }

    /**
     * @param archIn The arch to set.
     */
    public void setArch(String archIn) {
        arch = archIn;
    }

    /**
     * @return Returns the domain.
     */
    public String getDomain() {
        return domain;
    }

    /**
     * @param domainIn The domain to set.
     */
    public void setDomain(String domainIn) {
        domain = domainIn;
    }

    /**
     * @return Returns the devices.
     */
    public Map<String, Map<String, List<String>>> getDevices() {
        return devices;
    }

    /**
     * @param devicesIn The devices to set.
     */
    public void setDevices(Map<String, Map<String, List<String>>> devicesIn) {
        devices = devicesIn;
    }
}
