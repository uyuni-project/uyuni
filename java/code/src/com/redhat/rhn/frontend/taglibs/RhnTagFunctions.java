/**
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.StringUtil;


/**
 * RhnTagFunctions - class to encapsulate the set of static methods that
 * a JSP can interact with.  See rhn-taglib.tld for list of {@literal <function>}
 * definitions
 * @version $Rev$
 */
public class RhnTagFunctions {

    // Pure util class.  No need for construction.
    private RhnTagFunctions() {
    }

    /**
     * Joins an array of Strings into a comma-separated String
     *
     * @param array The array of Strings to join
     * @return String containing a comma-separated list
     */
    public static String arrayToString(String[] array) {
        return StringUtils.join(array, ',');
    }

    /**
     * Checks to see if an array of Strings contains any element that .equals()
     * the given String
     *
     * @param array The array of Strings to check
     * @param string The string to look for in the array
     * @return Boolean telling you if the String was found or not
     */
    public static Boolean arrayContains(String[] array, String string) {
        if (array == null) {
            return false;
        }
        for (String s : array) {
            if (s.equals(string)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fetch a value from our Configuration system.  Makes a
     * call into com.redhat.rhn.common.config.Config.get()
     *
     * @param confIn configuration value you are searching for
     * @return String containing the config value.  If not found
     * returns NULL
     */
    public static String getConfig(String confIn) {
        return Config.get().getString(confIn);
    }

    /**
     * Provides the i18n'ed string for the given messageId.
     * This is exclusively used by the el expressions in
     * various jsp pages. For this to work you'd have to
     * add
     * <p>
     * {@literal <%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>}
     *  to the top of your jsp page
     * <p>
     *  The you can say
     * <p>
     * ${rhn:localize("rhn.foo.bar")} to get the localized value
     * @param messageId the message id to localize on
     * @return the i18ned string...
     */
    public static String localizedValue(String messageId) {
        return localizedValue(messageId, null);
    }

    /**
     * This method is a variation of the the localizedValue(key) method
     * This is exclusively used by the el expressions in
     * various jsp pages. For this to work you'd have to
     * add
     * <p>
     * {@literal <%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>}
     *  to the top of your jsp page
     * <p>
     *  The you can say
     * <p>
     * ${rhn:localizeWithParams("rhn.foo.bar","param0|param1")} to get the localized value
     * The method parameter 'param' takes a list of key parameters  seperated by a '|'.
     * It is rather unfortunate for it to take a '|' instead of taking a String[]
     * as params.. This is because we donot know a way of creating arrays
     * as el expressions...
     *
     * @param messageId the message id to localize on
     * @param params a list of values separated by '|', or null...
     * @return the i18ned string...
     */
    public static String localizedValue(String messageId, String params) {
        LocalizationService service = LocalizationService.getInstance();

        if (params != null && params.trim().length() > 0) {
            Object [] args = params.split("\\|");
            for (int i = 0; i < args.length; i++) {
                args[i] = ((String)args[i]).trim();
            }
            return service.getMessage(messageId, args);
        }
        return service.getMessage(messageId);
    }

    /**
     * This method is translates the message together with all the parameters
     *
     * @param messageId the message id to localize on
     * @param params a list of values separated by '|', or null...
     * @return the i18ned string
     */
    public static String localizedValueAndParams(String messageId, String params) {
        LocalizationService service = LocalizationService.getInstance();

        if (params != null && params.trim().length() > 0) {
            Object [] args = params.split("\\|");
            for (int i = 0; i < args.length; i++) {
                args[i] = service.getMessage(((String)args[i]).trim());
            }
            return service.getMessage(messageId, args);
        }
        return service.getMessage(messageId);
    }

    /**
     * This method translates the message together with the 2nd parameter
     * @param messageId massage id
     * @param param1 regular param
     * @param param2 param to localize
     * @return the i18ned string
     */
    public static String localizedValueAnd2ndParam(String messageId, String param1,
            String param2) {
        LocalizationService service = LocalizationService.getInstance();
        Object [] args = new String[2];
        args[0] = param1.trim();
        args[1] = StringUtils.isEmpty(param2) ? null : service.getMessage(param2.trim());
        return service.getMessage(messageId, args);
    }

    /**
     * Encode a urlEncode a param
     *
     * @param param to be url encoded
     * @return url encoded param
     */
    public static String urlEncode(String param) {
        return StringUtil.urlEncode(param);
    }

    /**
     * Escape the characters in a String using JavaScript String rules.
     *
     * @param param to be escaped
     * @return escaped string
     */
    public static String escapeJS(String param) {
        return StringEscapeUtils.escapeJavaScript(param);
    }
}
