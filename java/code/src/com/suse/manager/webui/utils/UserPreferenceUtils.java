/**
 * Copyright (c) 2020 SUSE LLC
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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.security.acl.Access;
import com.redhat.rhn.common.security.acl.Acl;
import com.redhat.rhn.common.security.acl.AclFactory;
import com.redhat.rhn.domain.user.RhnTimeZone;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.frontend.struts.RequestContext;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

/**
 * Utilities for user preferences.
 */
public class UserPreferenceUtils {

    private final AclFactory aclFactory;

    /**
     * @param aclFactoryIn
     */
    public  UserPreferenceUtils(AclFactory aclFactoryIn) {
        this.aclFactory = aclFactoryIn;
    }

    /**
     * Get the users current locale. If no user is available return the config default
     *
     * @param pageContext the current PageContext
     * @return the users locale
     */
    public String getCurrentLocale(PageContext pageContext) {
        User user = getAuthenticatedUser(pageContext);
        if (isUserAuthenticated(user)) {
            String locale = user.getPreferredLocale();
            if (locale != null) {
                return locale;
            }
        }
        return ConfigDefaults.get().getDefaultLocale();
    }

    /**
     * Get the users current webUI style theme. If no user is available return the config default
     *
     * @param pageContext the current PageContext
     * @return the users webUI style theme
     */
    public String getCurrentWebTheme(PageContext pageContext) {
        User user = getAuthenticatedUser(pageContext);
        if (isUserAuthenticated(user)) {
            String webTheme = user.getWebTheme();
            if (ConfigDefaults.get().getWebThemesList().contains(webTheme) && webTheme != null) {
                return webTheme;
            }
        }
        return ConfigDefaults.get().getDefaultWebTheme();
    }

    /**
     * Extract the current logged-in user
     *
     * @param pageContext the blob where the user is in
     * @return the current User
     */
    public User getAuthenticatedUser(PageContext pageContext) {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        return new RequestContext(request).getCurrentUser();
    }

    /**
     * Get the users configured documentation locale. If no user is available return the config default
     *
     * @param pageContext the current PageContext
     * @return the users documentation locale
     */
    public String getDocsLocale(PageContext pageContext) {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        User user = new RequestContext(request).getCurrentUser();

        if (isUserAuthenticated(user)) {
            String locale = user.getPreferredDocsLocale();
            if (locale != null) {
                return locale;
            }
        }
        return ConfigDefaults.get().getDefaultDocsLocale();
    }

    /**
     * Check if the current User is an authenticated one
     *
     * @param user the current User to evaluate
     * @return true if the User is an authenticated one, false otherwise
     */
    public boolean isUserAuthenticated(User user) {
        Map<String, Object> aclContext = new HashMap<>();
        aclContext.put("user", user);
        Acl acl = aclFactory.getAcl(Access.class.getName());
        return acl.evalAcl(aclContext, "user_authenticated()");
    }

    /**
     * Get the current user timezone.
     * E.g.: CEST
     *
     * If no user is available, fallback and return the client context timezone from where the request comes from.
     * Double fallback on the server timezone if there is no request context
     *
     * @param pageContext the current PageContext
     * @return the user timezone
     */
    public String getUserTimeZone(PageContext pageContext) {
        User user = getAuthenticatedUser(pageContext);
        if (isUserAuthenticated(user)) {
            RhnTimeZone timezone = user.getTimeZone();
            if (timezone != null) {
                return timezone.getOlsonName();
            }
        }

        Context ctx = Context.getCurrentContext();
        Locale locale = ctx != null ? ctx.getLocale() : Locale.getDefault();
        TimeZone timezone = ctx != null ? ctx.getTimezone() : TimeZone.getDefault();
        DateFormat tzFormat = new SimpleDateFormat("z", locale);
        tzFormat.setTimeZone(new GregorianCalendar(timezone, locale).getTimeZone());
        return tzFormat.format(new Date());
    }


    /**
     * Render the user timezone in the extended/verbose format
     * E.g.: Europe/Berlin
     *
     * If no user is available, fallback and return the client context timezone from where the request comes from.
     * Double fallback on the server timezone if there is no request context
     *
     * @param pageContext the current PageContext
     * @return server timezone to be displayed as a string
     */
    public String getExtendedUserTimeZone(PageContext pageContext) {
        User user = getAuthenticatedUser(pageContext);
        if (isUserAuthenticated(user)) {
            RhnTimeZone timezone = user.getTimeZone();
            if (timezone != null) {
                return timezone.getOlsonName();
            }
        }

        Context ctx = Context.getCurrentContext();
        Locale locale = ctx != null ? ctx.getLocale() : Locale.getDefault();
        TimeZone timezone = ctx != null ? ctx.getTimezone() : TimeZone.getDefault();
        return timezone.getID();
    }

    /**
     * TODO
     * Make the parameter configurable from the configuration file
     *
     * @param pageContext the current PageContext
     * @return the String format of the Date
     */
    public String getUserDateFormat(PageContext pageContext) {
        return "YYYY-MM-DD";
    }

    /**
     * TODO
     * Make the parameter configurable from the configuration file
     *
     * @param pageContext the current PageContext
     * @return the String format of the Time
     */
    public String getUserTimeFormat(PageContext pageContext) {
        return "HH:mm:ss";
    }
}
