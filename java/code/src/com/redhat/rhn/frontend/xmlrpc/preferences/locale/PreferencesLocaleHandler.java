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
package com.redhat.rhn.frontend.xmlrpc.preferences.locale;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.user.RhnTimeZone;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidLocaleCodeException;
import com.redhat.rhn.frontend.xmlrpc.InvalidTimeZoneException;
import com.redhat.rhn.frontend.xmlrpc.user.XmlRpcUserHelper;
import com.redhat.rhn.manager.user.UserManager;

import com.suse.manager.api.ReadOnly;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.util.List;

/**
 * PreferencesLocaleHandler XMLRPC handler for the "preferences.locale"
 * namespace.
 * @apidoc.namespace preferences.locale
 * @apidoc.doc Provides methods to access and modify user locale information
 */
public class PreferencesLocaleHandler extends BaseHandler {

    /**
     * Set the TimeZone for the given user.
     * @param loggedInUser The current user
     * in user.
     * @param login The login of the user whose timezone will be changed.
     * @param tzid TimeZone id
     * @return Returns 1 if successful (exception otherwise)
     *
     * @apidoc.doc Set a user's timezone.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.param #param_desc("int", "tzid" "Timezone ID. (from listTimeZones)")
     * @apidoc.returntype #return_int_success()
     */
    public int setTimeZone(User loggedInUser, String login, Integer tzid) {
        List tzs = UserManager.lookupAllTimeZones();
        Object o = CollectionUtils.find(tzs, new TzPredicate(tzid));
        if (o == null) {
            throw new InvalidTimeZoneException(tzid);
        }

        User target = XmlRpcUserHelper.getInstance()
                                      .lookupTargetUser(loggedInUser, login);

        target.setTimeZone((RhnTimeZone)o);
        UserManager.storeUser(target);

        return 1;
    }

    /**
     * Set the language the user will display in the user interface.
     * @param loggedInUser The current user
     * in user.
     * @param login The login of the user whose language will be changed.
     * @param locale Locale code to be used as the users language.
     * @return Returns 1 if successful (exception otherwise)
     *
     * @apidoc.doc Set a user's locale.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "login", "User's login name.")
     * @apidoc.param #param_desc("string", "locale", "Locale to set. (from listLocales)")
     * @apidoc.returntype #return_int_success()
     */
    public int setLocale(User loggedInUser, String login, String locale) {
        LocalizationService ls = LocalizationService.getInstance();
        List locales = ls.getConfiguredLocales();
        Object o = CollectionUtils.find(locales, new LocalePredicate(locale));
        if (o == null) {
            throw new InvalidLocaleCodeException(locale);
        }

        User target = XmlRpcUserHelper.getInstance()
                                      .lookupTargetUser(loggedInUser, login);
        target.setPreferredLocale(locale);
        UserManager.storeUser(target);
        return 1;
    }

    /**
     * Returns a list of all understood TimeZones. This should be used
     * as the input into setTimeZone.
     * @return list of all understood TimeZones. This should be used
     * as the input into setTimeZone.
     *
     * @apidoc.doc Returns a list of all understood timezones. Results can be
     * used as input to setTimeZone.
     * @apidoc.returntype
     * #return_array_begin()
     *   $RhnTimeZoneSerializer
     * #array_end()
     */
    @ReadOnly
    public Object[] listTimeZones() {
        return UserManager.lookupAllTimeZones().toArray();
    }

    /**
     * Returns a list of all understood Locales. This should be used
     * as the input into setLocale.
     * @return list of all understood Locales. This should be used
     * as the input into setLocale.
     *
     * @apidoc.doc Returns a list of all understood locales. Can be
     * used as input to setLocale.
     * @apidoc.returntype
     * #array_single("string", "Locale code.")
     */
    @ReadOnly
    public Object[] listLocales() {
        LocalizationService ls = LocalizationService.getInstance();
        return ls.getConfiguredLocales().toArray();
    }

    /**
     * TzPredicate - used to find a valid id in the list.
     */
    public static class TzPredicate implements Predicate {

        private int id = -1;

        /**
         * constructor
         * @param tzid timezone id to be found
         */
        public TzPredicate(int tzid) {
            id = tzid;
        }

        /** {@inheritDoc} */
        public boolean evaluate(Object object) {
            RhnTimeZone tz = (RhnTimeZone) object;
            return (tz.getTimeZoneId() == id);
        }
    }

    /**
     * LocalePredicate - used to find a valid id in the list.
     */
    public static class LocalePredicate implements Predicate {

        private String locale = "en_US";

        /**
         * constructor
         * @param l locale to be found.
         */
        public LocalePredicate(String l) {
            locale = l;
        }

        /** {@inheritDoc} */
        public boolean evaluate(Object object) {
            return (object.equals(locale));
        }
    }
}
