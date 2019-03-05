/**
 * Copyright (c) 2017 SUSE LLC
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

package com.suse.manager.webui.menu;

import com.redhat.rhn.common.localization.LocalizationService;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * JSON representation of a Menu Element.
 */
public class MenuItem {

    private String label;
    private String primaryUrl = "#";
    private List<String> urls = new LinkedList<>();
    private List<String> directories = new LinkedList<>();
    private boolean active = false;
    private String target = "";
    private String icon = "";
    private boolean isVisible = true;
    private List<MenuItem> submenu;

    /**
     * Default constructor
     */
    public MenuItem() {
    }

    /**
     * Standard constructor for MenuItem.
     *
     * @param labelIn the label of the menu item
     * @param args arguments for the message
     */
    public MenuItem(String labelIn, Object... args) {
        this.setLabel(labelIn, args);
    }

    /**
     * Gets label.
     *
     * @return the label
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * @param labelKey The label to set.
     * @param args arguments for the message
     */
    public void setLabel(String labelKey, Object... args) {
        String unescapedName = LocalizationService.getInstance().getMessage(labelKey, args);
        this.label = StringEscapeUtils.escapeHtml4(unescapedName);
    }

    /**
     * Gets urls.
     *
     * @return the urls
     */
    public List<String> getUrls() {
        return this.urls;
    }

    /**
     * @param urlsIn The urls to set.
     */
    public void setUrls(String[] urlsIn) {
        this.urls.addAll(Arrays.asList(urlsIn));
    }

    /**
     * Gets primary url
     *
     * @return the primary url
     */
    public String getPrimaryUrl() {
        return this.primaryUrl;
    }

    /**
     * @param primaryUrlIn The primary url
     */
    public void setPrimaryUrl(String primaryUrlIn) {
        this.primaryUrl = primaryUrlIn;
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
     * @param activeIn The active to set.
     */
    public void setActive(boolean activeIn) {
        this.active = activeIn;
    }

    /**
     * Gets target.
     *
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param targetIn The target to set.
     */
    public void setTarget(String targetIn) {
        this.target = targetIn;
    }

    /**
     * Gets icon.
     *
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param iconIn The icon to set.
     */
    public void setIcon(String iconIn) {
        this.icon = iconIn;
    }

    /**
     * Gets submenu.
     *
     * @return the submenu
     */
    public List<MenuItem> getSubmenu() {
        return submenu;
    }

    /**
     * @param submenuIn The submenu to set.
     */
    public void setSubmenu(List<MenuItem> submenuIn) {
        this.submenu = submenuIn;
    }

    /**
     * @return Returns directories.
     */
    public List<String> getDirectories() {
        return directories;
    }

    /**
     * @param directoriesIn The directories to set.
     * @return this MenuItem
     */
    public MenuItem setDirectories(String[] directoriesIn) {
        this.directories.addAll(Arrays.asList(directoriesIn));
        return this;
    }

    /**
     * Gets isVisible.
     *
     * @return the isVisible
     */
    public boolean getIsVisible() {
        return this.isVisible;
    }

    /**
     * @param isVisibleIn the visibility flag
     * @return this MenuItem
     */
    public MenuItem setIsVisible(boolean isVisibleIn) {
        this.isVisible = isVisibleIn;
        return this;
    }

    /**
     * Add a child menu item
     *
     * @param submenuItemIn a MenuItem child
     * @return this MenuItem
     */
    public MenuItem addChild(MenuItem submenuItemIn) {
        if (submenuItemIn.getIsVisible()) {
            if (this.submenu == null) {
                this.submenu = new LinkedList<>();
            }
            this.submenu.add(submenuItemIn);
        }
        return this;
    }

    /**
     * Add a child menu item
     *
     * @param labelIn the label of the MenuItem
     * @return this MenuItem
     */
    public MenuItem addChild(String labelIn) {
        if (this.submenu == null) {
            this.submenu = new LinkedList<>();
        }
        this.submenu.add(new MenuItem(labelIn));
        return this;
    }

    /**
     * Set the primary url
     *
     * @param urlIn the url
     * @return this MenuItem
     */
    public MenuItem withPrimaryUrl(String urlIn) {
        this.primaryUrl = urlIn;
        return this;
    }

    /**
     * Set an alternative url
     *
     * @param urlIn the url
     * @return this MenuItem
     */
    public MenuItem withAltUrl(String urlIn) {
        this.urls.add(urlIn);
        return this;
    }

    /**
     * Set the MenuItem visibility permission
     *
     * @param visibilityIn the visibility flag
     * @return this MenuItem
     */
    public MenuItem withVisibility(boolean visibilityIn) {
        this.isVisible = visibilityIn;
        return this;
    }

    /**
     * Set the icon
     *
     * @param iconIn the icon
     * @return this MenuItem
     */
    public MenuItem withIcon(String iconIn) {
        this.icon = iconIn;
        return this;
    }

    /**
     * Set a directory where this MenuItem relies
     *
     * @param directoryIn the directory
     * @return this MenuItem
     */
    public MenuItem withDir(String directoryIn) {
        this.directories.add(directoryIn);
        return this;
    }

    /**
     * Set the target
     *
     * @param targetIn the target
     * @return this MenuItem
     */
    public MenuItem withTarget(String targetIn) {
        this.target = targetIn;
        return this;
    }
}
