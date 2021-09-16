/**
 * Copyright (c) 2015 SUSE LLC
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

package com.suse.manager.webui.utils;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.frontend.taglibs.helpers.RenderUtils;

import com.suse.manager.clusters.ClusterManager;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import org.apache.commons.lang3.text.WordUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

import spark.Request;

/**
 * Utility class for Jade views.
 */
public enum ViewHelper {
    /**
     * Singleton instance
     */
    INSTANCE;

    private static final RenderUtils RENDER_UTILS = GlobalInstanceHolder.RENDER_UTILS;
    private static final ClusterManager CLUSTER_MANAGER = GlobalInstanceHolder.CLUSTER_MANAGER;

    ViewHelper() { }

    /**
     * Singleton implementation
     * @return an instance of this class
     */
    public static ViewHelper getInstance() {
        return INSTANCE;
    }

    /**
     * Capitalizes a string.
     *
     * @param s the string
     * @return the capitalized string
     */
    public String capitalize(String s) {
        return WordUtils.capitalize(s);
    }

    /**
     * Generate the navigation menu defined by the given menu definition.
     *
     * @param request the request object
     * @param menuDefinition the menu definition
     * @return the navigation menu markup as string
     */
    public String renderNavigationMenu(Request request, String menuDefinition) {
        return renderNavigationMenuWithParams(request, menuDefinition, null);
    }

    /**
     * Generate the navigation menu defined by the given menu definition.
     *
     * @param request the request object
     * @param menuDefinition the menu definition
     * @param additionalParams parameters to add to the menu links
     * @return the navigation menu markup as string
     */
    public String renderNavigationMenuWithParams(Request request, String menuDefinition,
            Map<String, String[]> additionalParams) {
        String rendererClass = "com.redhat.rhn.frontend.nav.DialognavRenderer";
        try {
            Map<String, String> sparkParams = request.params().entrySet().stream().collect(
                    Collectors.toMap(entry -> entry.getKey().substring(1), entry -> entry.getValue()));
            return RENDER_UTILS.renderNavigationMenu(
                    request.raw(), menuDefinition, rendererClass, 0, 3, sparkParams, additionalParams);
        }
        catch (Exception e) {
            throw new RuntimeException("Error rendering the navigation menu.", e);
        }
    }

    /**
     * TODO -> DROP
     * This is a buggy method, it does not return the "user" timezone but the timezone of the "Context" from where
     * the request comes from, AKA the client/browser timezone. Uyuni/SUSE Manager user has a dedicated preference
     * parameter to define it. See layout_head.jsp
     *
     *
     * Render the current user's configured timezone for being displayed (falling back to
     * the system default in case there is currently no user context).
     *
     * @return timezone to be displayed
     */
    public String renderTimezone() {
        Context ctx = Context.getCurrentContext();
        Locale locale = ctx != null ? ctx.getLocale() : Locale.getDefault();
        TimeZone timezone = ctx != null ? ctx.getTimezone() : TimeZone.getDefault();
        DateFormat tzFormat = new SimpleDateFormat("z", locale);
        tzFormat.setTimeZone(new GregorianCalendar(timezone, locale).getTimeZone());
        return tzFormat.format(new Date());
    }

    /**
     * TODO -> DROP
     * This is a buggy method, it does not use the "user" timezone but the timezone of the "Context" from where
     * the request comes from, AKA the client/browser timezone. Uyuni/SUSE Manager user has a dedicated preference
     * parameter to define it. See layout_head.jsp
     *
     *
     * Render the time in the current user's configured timezone (falling back to
     * the system default in case there is currently no user context).
     *
     * @return user's local time
     */
    public String renderLocalTime() {
        return renderDate(new Date());
    }

    /**
     * TODO -> DROP
     * This is a buggy method, it does not use the "user" timezone but the timezone of the "Context" from where
     * the request comes from, AKA the client/browser timezone. Uyuni/SUSE Manager user has a dedicated preference
     * parameter to define it. See layout_head.jsp
     *
     *
     * Render a given time in the current user's configured timezone
     * @param date the date
     * @return user's local time
     */
    public String renderDate(Date date) {
        Context ctx = Context.getCurrentContext();
        Locale locale = ctx != null ? ctx.getLocale() : Locale.getDefault();
        TimeZone timezone = ctx != null ? ctx.getTimezone() : TimeZone.getDefault();
        DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX", locale);
        isoFormat.setTimeZone(new GregorianCalendar(timezone, locale).getTimeZone());
        return isoFormat.format(date);
    }

    /**
     * Render a given time in the current user's configured timezone
     * @param instant the instant
     * @return user's local time
     */
    public String renderDate(Instant instant) {
        return renderDate(new Date(instant.toEpochMilli()));
    }

    /**
     * Render a date time configured on the server default timezone
     *
     * @return server default time as a string
     */
    public String getServerTime() {
        Locale locale = Locale.getDefault();
        TimeZone timezone = TimeZone.getDefault();
        DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX", locale);
        isoFormat.setTimeZone(new GregorianCalendar(timezone, locale).getTimeZone());
        return isoFormat.format(new Date());
    }

    /**
     * Render the server timezone
     * E.g.: CEST
     *
     * @return server timezone to be displayed as a string
     */
    public String getServerTimeZone() {
        Locale locale = Locale.getDefault();
        TimeZone timezone = TimeZone.getDefault();
        DateFormat tzFormat = new SimpleDateFormat("z", locale);
        tzFormat.setTimeZone(new GregorianCalendar(timezone, locale).getTimeZone());
        return tzFormat.format(new Date());
    }

    /**
     * Render the server timezone in the extended/verbose format
     * E.g.: Europe/Berlin
     *
     * @return server timezone to be displayed as a string
     */
    public String getExtendedServerTimeZone() {
        Locale locale = Locale.getDefault();
        TimeZone timezone = TimeZone.getDefault();
        return timezone.getID();
    }

    /**
     * Checks if a value of a formula is equal to the given argument.
     * @param server the server to check
     * @param formulaName the name of the formula to check
     * @param valueName the name of the value to check for equality
     * @param valueToCheck the value to check
     * @return true if the value is equal to the given argument
     */
    public boolean formulaValueEquals(Server server, String formulaName, String valueName, String valueToCheck) {
        if (server == null) {
            return false;
        }
        if (!server.asMinionServer().isPresent()) {
            return false;
        }
        List<String> enabledFormulas = FormulaFactory
                .getFormulasByMinionId(MinionServerFactory.getMinionId(server.getId()));
        if (!enabledFormulas.contains(formulaName)) {
            return false;
        }

        Map<String, Object> systemData = FormulaFactory.
                getFormulaValuesByNameAndMinionId(formulaName, server.asMinionServer().get().getMinionId())
                .orElseGet(Collections::emptyMap);
        Map<String, Object> groupData = FormulaFactory
                .getGroupFormulaValuesByNameAndServerId(formulaName, server.getId())
                .orElseGet(Collections::emptyMap);
        return Objects.toString(systemData.get(valueName), "")
                .equals(valueToCheck) ||
                Objects.toString(groupData.get(valueName), "")
                        .equals(valueToCheck);
    }

    /**
     * @param server the server to check
     * @return true if the server has either ssh-push or ssh-push-tunnel contact methods
     */
    public boolean hasSshPushContactMethod(Server server) {
        return ContactMethodUtil.isSSHPushContactMethod(server.getContactMethod());
    }

    /**
     * @param groupId server group id
     * @return true if the group is owned by a cluster
     */
    public boolean isClusterGroup(String groupId) {
        return CLUSTER_MANAGER.isClusterGroup(Long.parseLong(groupId));
    }
}
