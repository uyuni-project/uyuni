/*
 * Copyright (c) 2014 Red Hat, Inc.
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
package com.redhat.rhn.domain.common;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * SatConfigFactory
 */
public class SatConfigFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(SatConfigFactory.class);

    public static final String EXT_AUTH_DEFAULT_ORGID = "extauth_default_orgid";
    public static final String EXT_AUTH_USE_ORGUNIT = "extauth_use_orgunit";
    public static final String EXT_AUTH_KEEP_ROLES = "extauth_keep_temproles";
    public static final String SYSTEM_CHECKIN_THRESHOLD = "system_checkin_threshold";

    public static final String PSW_CHECK_LENGHT_MIN = "password_check_lenght_min";
    public static final String PSW_CHECK_LENGHT_MAX = "password_check_lenght_max";
    public static final String PSW_CHECK_LOWER_CHAR_FLAG = "password_check_lower_char_flag";
    public static final String PSW_CHECK_UPPER_CHAR_FLAG = "password_check_upper_char_flag";
    public static final String PSW_CHECK_DIGIT_FLAG = "password_check_digit_flag";
    public static final String PSW_CHECK_CONSECUTIVE_CHAR_FLAG = "password_check_consecutive_char_flag";
    public static final String PSW_CHECK_SPECIAL_CHAR_FLAG = "password_check_special_char_flag";
    public static final String PSW_CHECK_RESTRICTED_OCCURENCE_FLAG = "password_check_restricted_occurrence_flag";
    public static final String PSW_CHECK_MAX_OCCURENCE = "password_check_max_occurrence";
    public static final String PSW_CHECK_SPECIAL_CHARACTERS = "password_check_special_characters";


    private SatConfigFactory() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * return satellite configuration boolean value for a specified key
     * @param key key
     * @return value boolean value
     */
    public static boolean getSatConfigBooleanValue(String key) {
        return BooleanUtils.toBoolean(getSatConfigValue(key));
    }

    /**
     * return satellite configuration long value for a specified key
     * @param key key
     * @param defaultValue value to use if no property value could be found
     * @return value long value
     */
    public static Long getSatConfigLongValue(String key, Long defaultValue) {
        String stringValue = getSatConfigValue(key);
        if (stringValue != null) {
            try {
                return Long.parseLong(stringValue);
            }
            catch (NumberFormatException nfe) {
                LOG.error("Satellite configuration '{}' value ({}) cannot be converted to Long.", key, stringValue);
            }
        }
        return defaultValue;
    }

    /**
     * return satellite configuration value for a specified key
     * @param key key
     * @return value
     */
    public static String getSatConfigValue(String key) {
        Map<String, Object> params = new HashMap<>();
        params.put("key", key);
        SelectMode m = ModeFactory.getMode("util_queries", "get_satconfig_value");
        DataResult<Row> dr = m.execute(params);
        if (!dr.isEmpty()) {
            return (String) dr.get(0).get("value");
        }
        LOG.error("'{}' not found within the satellite configuration.", key);
        return null;
    }

    /**
     * set a satellite configuration value for a specified key
     * @param key key
     * @param value value
     */
    public static void setSatConfigValue(String key, String value) {
        Map<String, Object> params = new HashMap<>();
        params.put("key", key);
        if (StringUtils.isEmpty(value)) {
            params.put("value", null);
        }
        else {
            params.put("value", value);
        }
        WriteMode m = ModeFactory.getWriteMode("util_queries",
            "set_satconfig_value");
        m.executeUpdate(params);
    }

    /**
     * set a satellite configuration value for a specified key
     * @param key key
     * @param value value
     */
    public static void setSatConfigBooleanValue(String key, Boolean value) {
        setSatConfigValue(key, Objects.requireNonNullElse(value, Boolean.FALSE).toString());
    }

    /**
     * reset a satellite configuration value to a default value for a specified key
     * @param key key
     */
    public static void resetSatConfigDefaultValue(String key) {
        Map<String, Object> params = new HashMap<>();
        params.put("key", key);
        WriteMode m = ModeFactory.getWriteMode("util_queries",
            "reset_satconfig_default_value");
        m.executeUpdate(params);
    }
}
