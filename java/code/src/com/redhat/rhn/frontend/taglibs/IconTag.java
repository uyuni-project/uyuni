/**
 * Copyright (c) 2013 Red Hat, Inc.
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

package com.redhat.rhn.frontend.taglibs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Tag to easy display the icons
 * <pre>
 * &lt;rhn:icon type="$type" title="$title"/&gt;
 * </pre>
 * @version $Rev$
 */
public class IconTag extends TagSupport {

    private String type;
    private String title;
    private static Map<String, String> icons;

    static {
        icons = new HashMap<String, String>();
        icons.put("action-failed", "fa fa-times-circle-o fa-1-5x text-danger");
        icons.put("action-ok", "fa fa-check-circle-o fa-1-5x text-success");
        icons.put("action-pending", "fa fa-clock-o fa-1-5x");
        icons.put("action-running", "fa fa-exchange fa-1-5x text-info");
        icons.put("errata-bugfix", "fa fa-bug fa-1-5x");
        icons.put("errata-enhance", "fa fa-1-5x spacewalk-icon-enhancement");
        icons.put("errata-security", "fa fa-shield fa-1-5x");
        icons.put("event-type-errata", "fa spacewalk-icon-patches");
        icons.put("event-type-package", "fa spacewalk-icon-packages");
        icons.put("event-type-preferences", "fa fa-cog");
        icons.put("event-type-system", "fa fa-desktop");
        icons.put("header-action", "fa-clock-o");
        icons.put("header-activation-key", "fa fa-key");
        icons.put("header-channel", "fa spacewalk-icon-software-channels");
        icons.put("header-channel-mapping", "fa fa-retweet");
        icons.put("header-configuration", "fa spacewalk-icon-manage-configuration-files");
        icons.put("header-crash", "spacewalk-icon-bug-ex");
        icons.put("header-errata", "fa spacewalk-icon-patches");
        icons.put("header-globe", "fa fa-globe");
        icons.put("header-info", "fa fa-info-circle");
        icons.put("header-list", "fa fa-list");
        icons.put("header-kickstart", "fa fa-rocket");
        icons.put("header-organisation", "fa fa-group");
        icons.put("header-package", "fa spacewalk-icon-packages");
        icons.put("header-preferences", "fa fa-cogs");
        icons.put("header-search", "fa fa-search");
        icons.put("header-signout", "fa fa-sign-out");
        icons.put("header-sitemap", "fa fa-sitemap");
        icons.put("header-snapshot", "fa fa-camera");
        icons.put("header-system", "fa fa-desktop");
        icons.put("header-system-groups", "fa spacewalk-icon-system-groups");
        icons.put("header-system-physical", "fa fa-desktop");
        icons.put("header-system-virt-guest", "fa spacewalk-icon-virtual-guest");
        icons.put("header-system-virt-host", "fa spacewalk-icon-virtual-host");
        icons.put("header-user", "fa fa-user");
        icons.put("header-taskomatic", "fa fa-tachometer");
        icons.put("item-add", "fa fa-plus");
        icons.put("item-clone", "fa fa-files-o");
        icons.put("item-del", "fa fa-trash-o");
        icons.put("item-ssm-add", "fa fa-plus-circle");
        icons.put("item-ssm-del", "fa fa-minus-circle");
        icons.put("monitoring-crit", "fa fa-1-5x spacewalk-icon-health text-danger");
        icons.put("monitoring-ok", "fa fa-1-5x spacewalk-icon-health text-success");
        icons.put("monitoring-pending", "fa fa-1-5x spacewalk-icon-health-pending");
        icons.put("monitoring-status", "fa fa-1-5x spacewalk-icon-monitoring-status");
        icons.put("monitoring-unknown", "fa fa-1-5x spacewalk-icon-health-unknown");
        icons.put("monitoring-warn", "fa fa-1-5x spacewalk-icon-health text-warning");
        icons.put("system-crit", "fa fa-exclamation-circle fa-1-5x text-danger");
        icons.put("system-kickstarting", "fa fa-rocket fa-1-5x");
        icons.put("system-locked", "fa fa-1-5x spacewalk-icon-locked-system");
        icons.put("system-ok", "fa fa-check-circle fa-1-5x text-success");
        icons.put("system-physical", "fa fa-desktop fa-1-5x");
        icons.put("system-unentitled", "fa fa-1-5x spacewalk-icon-Unentitled");
        icons.put("system-unknown", "fa fa-1-5x spacewalk-icon-unknown-system");
        icons.put("system-virt-guest", "fa fa-1-5x spacewalk-icon-virtual-guest");
        icons.put("system-virt-host", "fa fa-1-5x spacewalk-icon-virtual-host");
        icons.put("system-warn", "fa fa-exclamation-triangle fa-1-5x text-warning");
    }

    /**
     * Constructor for Icon tag.
     */
    public IconTag() {
        super();
        type = null;
        title = null;
    }

    /**
     * Set the type of the icon
     * @param typeIn the type of the icon
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * Get the type of the icon
     * @return The type of the icon
     */
    public String getType() {
        return type;
    }

    /**
     * Set the title of the icon
     * @param titleIn the title of the icon
     */
    public void setTitle(String titleIn) {
        title = titleIn;
    }

    /**
     * Get the title of the icon
     * @return The title of the icon
     */
    public String getTitle() {
        return title;
    }

    /**
     * Return just the HTML
     * @return String that contains generated HTML
     */
    public String renderStartTag() {
        if (!icons.containsKey(type)) {
            throw new IllegalArgumentException("Unknown icon type: \"" + type + "\".");
        }

        StringBuilder result = new StringBuilder();
        result.append("<i class=\"" + icons.get(type) + "\"");
        if (title != null) {
            result.append(" title=\"" + title + "\"");
        }
        result.append("></i>");

        return result.toString();
    }

    /** {@inheritDoc}
     * @throws JspException
     */
    public int doStartTag() throws JspException {
        if (!icons.containsKey(type)) {
            throw new IllegalArgumentException("Unknown icon type: \"" + type + "\".");
        }

        JspWriter out = null;
        try {
            out = pageContext.getOut();
            String result = renderStartTag();
            out.print(result);
        }
        catch (IOException ioe) {
            throw new JspException("IO error writing to JSP file:", ioe);
        }
        return SKIP_BODY;
    }

    /**
     * {@inheritDoc}
     */
    public void release() {
        type = null;
        title = null;
        super.release();
    }

}
