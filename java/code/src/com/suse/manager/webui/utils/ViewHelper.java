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

import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.frontend.taglibs.helpers.RenderUtils;

import org.apache.commons.lang3.text.WordUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
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
        String rendererClass = "com.redhat.rhn.frontend.nav.DialognavRenderer";
        try {
            Map<String, String> sparkParams = request.params().entrySet().stream().collect(
                    Collectors.toMap(entry -> entry.getKey().substring(1), entry -> entry.getValue()));
            return RenderUtils.INSTANCE.renderNavigationMenu(
                    request.raw(), menuDefinition, rendererClass, 0, 3, sparkParams);
        }
        catch (Exception e) {
            throw new RuntimeException("Error rendering the navigation menu.", e);
        }
    }

    /**
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
     * Render the time in the current user's configured timezone (falling back to
     * the system default in case there is currently no user context).
     *
     * @return user's local time
     */
    public String renderLocalTime() {
        return renderDate(new Date());
    }

    /**
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
}
