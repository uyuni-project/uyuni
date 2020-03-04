/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.extensions;

import com.redhat.rhn.common.conf.ConfigDefaults;
import org.apache.log4j.Logger;
import org.pf4j.DefaultPluginManager;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;
import org.pf4j.PluginManager;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class Plugins {

    private static Logger log = Logger.getLogger(Plugins.class);

    private static Plugins instance;

    private PluginManager pluginManager;

    private Plugins() {
        pluginManager = new DefaultPluginManager(Paths.get(ConfigDefaults.get().getPluginDir()));
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
    }

    public static Plugins instance() {
        if (instance == null) {
            synchronized (Plugins.class) {
                if (instance == null) {
                    instance = new Plugins();
                }
            }
        }
        return instance;
    }

    public <T> List<T> getExtensions(Class<T> extensionInterface) {
        return pluginManager.getExtensions(extensionInterface);
    }

    public <T> List<T> getExtensions(String interfaceName, Class<T> baseExtensionInterface) {
        try {
            Class extensionInterface = Class.forName(interfaceName);
            if (!extensionInterface.isInterface()) {
                log.error("Type " + interfaceName + " is not an interface");
                return Collections.emptyList();

            }
//            if () { // TODO check if interface extends ExtensionPoint
//                log.error("Interface " + interfaceName + " is expected to inherit " + ExtensionPoint.class.getName());
//                return Collections.emptyList();
//            }
//            if () { // TODO check if interface extends baseExtensionInterface
//                log.error("Interface " + interfaceName + " is expected to inherit " + baseExtensionInterface.getName());
//                return Collections.emptyList();
//            }
            return getExtensions(extensionInterface);
        } catch (ClassNotFoundException e) {
            log.error("Error finding plugin interface " + interfaceName, e);
        }
        return Collections.emptyList();
    }

}
