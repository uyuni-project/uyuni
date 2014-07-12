/**
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
package com.redhat.rhn.domain.monitoring.config;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.monitoring.notification.ContactGroup;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MonitoringConfigFactory - the singleton class used to fetch and store
 * com.redhat.rhn.domain.monitoring.config.* objects from the
 * database.
 * @version $Rev: 51602 $
 */
public class MonitoringConfigFactory extends HibernateFactory {

    private static MonitoringConfigFactory singleton = new MonitoringConfigFactory();
    private static Logger log = Logger.getLogger(MonitoringConfigFactory.class);

    private MonitoringConfigFactory() {
        super();
    }

    /**
     * Get the Logger for the derived class so log messages
     * show up on the correct class
     */
    protected Logger getLogger() {
        return log;
    }

    /**
     * Get the list of com.redhat.rhn.domain.monitoring.config.ConfigMacro
     * objects from the DB.  The editable param indicates if you want the
     * editable or non editable items.
     * @param editable if you want editable ConfigMacro items or not
     * @return List of ConfigMacro objects
     */
    public static List lookupConfigMacros(boolean editable) {
        Map<String, Object> params = new HashMap<String, Object>();
        if (editable) {
            params.put("editable", "1");
        }
        else {
            params.put("editable", "0");
        }
        return singleton.listObjectsByNamedQuery(
                "ConfigMacro.loadAllByEditable", params);
    }

    /**
     * Lookup a ConfigMacro by its name
     * @param name of ConfigMacro to lookup
     * @return ConfigMacro if found.
     */
    public static ConfigMacro lookupConfigMacroByName(String name) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", name);
        return (ConfigMacro) singleton.lookupObjectByNamedQuery(
                "ConfigMacro.lookupByName", params);
    }


    /**
     * Commit a ConfigMacro to the DB
     * @param cIn ConfigMacro to be saved
     */
    public static void saveConfigMacro(ConfigMacro cIn) {
        singleton.saveObject(cIn);
    }

    /**
     *
     * @return The Panic Destination for this Sat
     */
    public static ContactGroup lookupPanicDestination() {
        return null;
    }

    /**
     * Get the database name being used by Hibernate.
     * @return String db name
     */
    public static String getDatabaseName() {
        String dbName = Config.get().getString(ConfigDefaults.DB_NAME);
        return dbName.toUpperCase(Locale.ENGLISH);
    }


    /**
     * Get the database username being used by Hibernate.
     * @return String username
     */
    public static String getDatabaseUsername() {
        return Config.get().getString(ConfigDefaults.DB_USER);
    }


    /**
     * Get the database password being used by Hibernate.
     * @return String password
     */
    public static String getDatabasePassword() {
        return Config.get().getString(ConfigDefaults.DB_PASSWORD);
    }

}

