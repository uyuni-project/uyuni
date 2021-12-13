/*
 * Copyright (c) 2019 SUSE LLC
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

import java.util.Optional;

/**
 * Class representing a virtual storage pool device source.
 */
public class PoolSourceDevice {
    private String path;
    private Optional<Boolean> separator;

    /**
     * Device constructor
     *
     * @param pathIn the path or iSCSI Qualified Name
     */
    public PoolSourceDevice(String pathIn) {
        setPath(pathIn);
        setSeparator(Optional.empty());
    }

    /**
     * Device constructor
     *
     * @param pathIn the path or iSCSI Qualified Name
     * @param separatorIn the separator
     */
    public PoolSourceDevice(String pathIn, Optional<Boolean> separatorIn) {
        setPath(pathIn);
        setSeparator(separatorIn);
    }

    /**
     * @return Returns the device path of iSCSI Qualified Name.
     */
    public String getPath() {
        return path;
    }

    /**
     * @param pathIn The device path of iSCSI Qualified Name to set.
     */
    public void setPath(String pathIn) {
        path = pathIn;
    }

    /**
     * @return Returns the separator.
     */
    public Optional<Boolean> isSeparator() {
        return separator;
    }

    /**
     * @param separatorIn The separator to set.
     */
    public void setSeparator(Optional<Boolean> separatorIn) {
        separator = separatorIn;
    }

    /**
     * Extract the data from a libvirt pool XML source device element.
     *
     * @param node the source device XML element
     * @return the created device
     * @throws IllegalArgumentException if the node is missing required attributes or children
     */
    public static PoolSourceDevice parse(Element node) throws IllegalArgumentException {
        PoolSourceDevice result = null;
        if (node != null) {
            String path = node.getAttributeValue("path");
            String separatorStr = node.getAttributeValue("part_separator");
            if (path == null) {
                throw new IllegalArgumentException("Missing mandatory path attribute in device");
            }
            Optional<Boolean> separator = Optional.empty();
            if (separatorStr != null) {
                separator = Optional.of("yes".equals(separatorStr));
            }

            result = new PoolSourceDevice(path, separator);
        }
        return result;
    }
}
