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

package com.suse.manager.webui.utils.gson;

import java.util.List;

/**
 * JSON representation of a Menu Element.
 */
public class JSONMenuElement {
    private String label;
    private String url;
    private boolean active;
    private List<JSONMenuElement> submenu;

    /**
     * Empty constructor
     */
    public JSONMenuElement() {
    }

    /**
     * All-arg constructor for JSONMenuElement.
     *
     * @param labelIn the label of the link
     * @param urlIn the href value of the link
     * @param activeIn if it is the current page
     * @param submenuIn the submenu level list
     */
    public JSONMenuElement (String labelIn, String urlIn, boolean activeIn, List<JSONMenuElement> submenuIn) {
        this.label = labelIn;
        this.url = urlIn;
        this.active = activeIn;
        this.submenu = submenuIn;
    }

    /**
     * Gets label.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url The url to set.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets active.
     *
     * @return the active
     */
    public boolean getActive() {
        return active;
    }

    /**
     * @param active The active to set.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Gets submenu.
     *
     * @return the submenu
     */
    public List<JSONMenuElement> getSubmenu() {
        return submenu;
    }

    /**
     * @param submenu The submenu to set.
     */
    public void setSubmenu(List<JSONMenuElement> submenu) {
        this.submenu = submenu;
    }
}
