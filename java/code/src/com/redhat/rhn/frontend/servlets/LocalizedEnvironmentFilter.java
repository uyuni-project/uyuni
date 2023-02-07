/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

package com.redhat.rhn.frontend.servlets;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.session.WebSession;
import com.redhat.rhn.domain.user.RhnTimeZone;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.user.UserManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.core.Config;

/**
 * Filter for centralizing locale and charset detection
 * and processing per-request
 *
 */
public class LocalizedEnvironmentFilter implements Filter {

    private static final Logger LOG = LogManager.getLogger(LocalizedEnvironmentFilter.class);


    private static final ThreadLocal<Long> CURRENT_SESSION_ID = new ThreadLocal<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        initializeContext(httpRequest);
        chain.doFilter(request, response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig config) {
    }

    private void initializeContext(HttpServletRequest request) {
        Context current = Context.getCurrentContext();
        RequestContext requestCtx = new RequestContext(request);
        User user = requestCtx.getCurrentUser();
        setTimeZone(current, user, request);
        resolveLocale(current, user, request);
        WebSession webSession = requestCtx.getWebSessionIfExists();
        if (webSession != null) {
            CURRENT_SESSION_ID.set(webSession.getId());
        }
    }

    private void setTimeZone(Context ctx, User user, HttpServletRequest request) {
        RhnTimeZone tz = null;
        if (user != null) {
            tz = user.getTimeZone();
            LOG.debug("Set timezone from the user.");
        }
        if (tz == null) {
            // Use the Appserver timezone if the User doesn't have one.
            String olsonName = TimeZone.getDefault().getID();
            if (LOG.isDebugEnabled()) {
                LOG.debug("olson name [{}]", olsonName);
            }
            tz = UserManager.getTimeZone(olsonName);
            // if we're still null set it to a default
            if (tz == null) {
                tz = new RhnTimeZone();
                tz.setOlsonName(olsonName);

                LOG.debug("timezone still null");
            }
        }
        ctx.setTimezone(tz.getTimeZone());
        // Set the timezone on the request so that fmt:formatDate
        // can find it
        Config.set(request, Config.FMT_TIME_ZONE, tz.getTimeZone());
    }

    private void resolveLocale(Context ctx, User user,
            HttpServletRequest request) {
        RhnHttpServletRequest rhnRequest = (RhnHttpServletRequest) request;
        ctx.setOriginalLocale(rhnRequest.getBrowserLocale());
        if (user != null && user.getPreferredLocale() != null) {
            Locale userLocale = null;
            String preferredLocale = user.getPreferredLocale();
            String[] localeParts = preferredLocale.split("_");
            switch (localeParts.length) {
            case 3:
                userLocale = new Locale(localeParts[0], localeParts[1],
                        localeParts[2]);
                break;
            case 2:
                userLocale = new Locale(localeParts[0], localeParts[1]);
                break;
            default:
                userLocale = new Locale(preferredLocale);
                break;

            }
            ctx.setLocale(userLocale);
        }
        else {
            ctx.setLocale(LocalizationService.DEFAULT_LOCALE);
        }
        rhnRequest.configureLocale();
    }

    /**
     * @return the id of the currently logged in user.
     */
    public static Long getCurrentSessionId() {
        return CURRENT_SESSION_ID.get();
    }
}
