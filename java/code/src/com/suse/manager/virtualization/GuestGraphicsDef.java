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
 * Class representing the graphics device XML definition.
 */
public class GuestGraphicsDef {

    private String type;
    private int port;

    /**
     * @return Returns the type ("spice", "vnc"...)
     */
    public String getType() {
        return type;
    }

    /**
     * @param typeIn The graphics type to set ("spice", "vnc"...)
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * @return The port on which the graphics device is listening
     */
    public int getPort() {
        return port;
    }

    /**
     * @param portIn the port on which the graphics device is listening or -1 for automatic port.
     */
    public void setPort(int portIn) {
        port = portIn;
    }

    /**
     * Parse the libvirt <code>graphics</code> element
     *
     * @param element XML element
     *
     * @return the graphics definition
     */
    public static GuestGraphicsDef parse(Element element) {
        GuestGraphicsDef def = new GuestGraphicsDef();
        def.setType(element.getAttributeValue("type"));
        def.setPort(Integer.parseInt(element.getAttributeValue("port", "-1")));

        return def;
    }
}
