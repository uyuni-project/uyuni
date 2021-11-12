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

import org.jdom.Element;

/**
 * Represents the libvirt domain vcpu definition
 */
public class GuestVcpuDef {

    private int max;
    private int current;

    /**
     * @return maximum number of Virtual CPUs
     */
    public int getMax() {
        return max;
    }

    /**
     * Set the maximum number of virtual CPUs.
     *
     * If setting a value smaller than the amount of current VCPUs, the
     * latter will be adjusted.
     *
     * @param maxIn maximum number of Virtual CPUs
     */
    public void setMax(int maxIn) {
        this.max = maxIn;
        this.current = Math.min(this.max, this.current);
    }

    /**
     * @return current number of Virtual CPUs
     */
    public int getCurrent() {
        return current;
    }

    /**
     * Set the current number of virtual CPUs.
     *
     * Don't forget to change the maximum before raising the current amount of CPUs
     * or it will be capped.
     *
     * @param currentIn current number of Virtual CPUs
     */
    public void setCurrent(int currentIn) {
        this.current = Math.min(currentIn, this.max);
    }

    /**
     * Parse libvirt domain definition XML &lt;vcpu&gt; element
     *
     * @param element XML element
     * @return the cpu definition
     */
    public static GuestVcpuDef parse(Element element) {
        GuestVcpuDef def = new GuestVcpuDef();

        def.setMax(Integer.parseUnsignedInt(element.getTextTrim()));
        def.setCurrent(def.max);
        String value = element.getAttributeValue("current");
        if (value != null) {
            def.setCurrent(Integer.parseUnsignedInt(value));
        }

        return def;
    }

}
