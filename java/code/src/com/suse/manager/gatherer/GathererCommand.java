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

package com.suse.manager.gatherer;

import com.redhat.rhn.manager.satellite.Executor;
import com.redhat.rhn.manager.satellite.SystemCommandExecutor;

import com.suse.manager.model.gatherer.GathererModule;

import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * GathererCommand - simple Command class to run the gatherer.
 */
public class GathererCommand {

    private static final String GATHERER_CMD = "/usr/bin/gatherer";
    private static final String LOG_DESTINATION = "/var/log/rhn/gatherer.log";
    /**
     * Logger for this class
     */
    private final Logger logger = Logger.getLogger(this.getClass());

    /**
     * Call gatherer --list-modules and return the result
     * @return the available modules with details
     */
    public Map<String, GathererModule> listModules() {
        Executor e = new SystemCommandExecutor();
        List<String> args = new LinkedList<>();
        args.add(GATHERER_CMD);
        args.add("--list-modules");
        args.add("--logfile");
        args.add(LOG_DESTINATION);

        int exitcode = e.execute(args.toArray(new String[0]));
        if (exitcode != 0) {
            logger.error(e.getLastCommandErrorMessage());
            return null;
        }
        return new GathererJsonIO().readGathererModules(e.getLastCommandOutput());
    }
}
