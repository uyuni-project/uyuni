/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.satellite;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.manager.satellite.SatelliteConfigurator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * BaseConfigAction - contains common methods for Struts Actions needing to
 * config a sat.
 */
public abstract class BaseConfigAction extends RhnAction {

    /**
     * Logger for this class
     */
    private static Logger logger = LogManager.getLogger(BaseConfigAction.class);

    /**
     * Get the command this Action will use.  This method uses the
     * config value:
     *
     * web.com.redhat.rhn.frontend.action.satellite.GeneralConfigAction.command
     *
     * to determine a dynamic classname to use to instantiate the
     * ConfigureSatelliteCommand. This can be useful if you want to
     * specify a different class to use for the Command at runtime.
     *
     * @param currentUser who is requesting this config.
     * @return ConfigureSatelliteCommand instance
     */
    protected SatelliteConfigurator getCommand(User currentUser) {
        if (logger.isDebugEnabled()) {
            logger.debug("getCommand(User currentUser={}) - start", currentUser);
        }

        try {
            String className = getCommandClassName();

            @SuppressWarnings("unchecked")
            var clazz = (Class<? extends SatelliteConfigurator>) Class.forName(className);
            var constructor = clazz.getDeclaredConstructor(User.class);
            // Setting accessible for backwards compatibility (CGLib does this automatically)
            constructor.setAccessible(true);

            SatelliteConfigurator sc = constructor.newInstance(currentUser);
            logger.debug("getCommand(User) - end - return value={}", sc);

            return sc;
        }
        catch (ReflectiveOperationException e) {
            logger.error("getCommand(User)", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Subclasses implement this to indicate the name of the class to
     * use when fetching the Command instance
     *
     * @return String classname
     */
    protected abstract String getCommandClassName();

}
