/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.user;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.user.RhnTimeZone;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.manager.user.UserManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * User locale override stuff
 *
 * @version $Rev $
 */
public class BaseUserSetupAction extends RhnAction {

    /**
     * Builds LangDisplayBean for none locale
     * @param locale the default locale
     * @return LangDisplayBean
     */
    public LangDisplayBean buildDefaultLocale(String locale) {
        LocalizationService ls =
            LocalizationService.getInstance();
        LangDisplayBean ldb = new LangDisplayBean();
        ldb.setLanguageCode("default");
        ldb.setLocalizedName(ls.getMessage("preferences.jsp.lang.default",
                ls.getMessage("preferences.jsp.lang." + locale)));
        return ldb;
    }

    /**
     * Sets user's locale to the provided request context
     * @param ctx RequestContext
     * @param user User
     */
    public void setCurrentLocale(RequestContext ctx, User user) {
        String userLocale = user.getPreferredLocale();
        ctx.getRequest().setAttribute("currentLocale", Objects.requireNonNullElse(userLocale, "default"));
    }

    /**
     * Sets the locale to be used for the documentation to the provided request context
     * @param ctx RequestContext
     * @param user User
     */
    public void setDocsLocale(RequestContext ctx, User user) {
        String docsLocale = user.getPreferredDocsLocale();
        ctx.getRequest().setAttribute("currentDocsLocale", Objects.requireNonNullElse(docsLocale, "default"));
    }

    /**
     * Builds Map of configured locales and locale image uris
     * @param locales list of locales
     * @return Map of configured locales and locale image uris
     */
    public Map<String, LangDisplayBean> buildImageMap(List<String> locales) {
        Map<String, LangDisplayBean> retval = new LinkedHashMap();
        LocalizationService ls = LocalizationService.getInstance();
        for (String locale : locales) {
            LangDisplayBean ldb = new LangDisplayBean();
            ldb.setLanguageCode(locale);
            ldb.setLocalizedName(ls.getMessage("preferences.jsp.lang." + locale));
            retval.put(locale, ldb);
        }
        return retval;
    }

    /**
     * Lists available time zones
     * @return List of available time zones
     */
    public List getTimeZones() {
        List dataList = UserManager.lookupAllTimeZones();
        List displayList = new ArrayList();
        for (Object oIn : dataList) {
            String display = LocalizationService.getInstance()
                    .getMessage(((RhnTimeZone) oIn).getOlsonName());
            String value = String.valueOf(((RhnTimeZone) oIn).getTimeZoneId());
            displayList.add(createDisplayMap(display, value));
        }
        return displayList;
    }

    private Map createDisplayMap(String display, String value) {
        Map selection = new HashMap();
        selection.put("display", display);
        selection.put("value", value);
        return selection;
    }

    public List getWebThemes() {
        return ConfigDefaults.get().getWebThemesList();
    }
}
