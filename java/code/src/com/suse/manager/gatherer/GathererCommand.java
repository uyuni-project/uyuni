package com.suse.manager.gatherer;

import com.redhat.rhn.manager.satellite.Executor;
import com.redhat.rhn.manager.satellite.SystemCommandExecutor;

import com.suse.manager.model.gatherer.GathererModule;

import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * GathererCommand - simple Command class to run the gatherer. User
 * must be SAT_ADMIN to use this command.
 *
 */
public class GathererCommand {

    private static String GATHERER_CMD = "/usr/bin/gatherer";

    /**
     * Logger for this class
     */
    private final Logger logger = Logger.getLogger(this.getClass());

    public GathererCommand() { }

    public Map<String, GathererModule> listModules() {
        Executor e = new SystemCommandExecutor();
        List<String> args = new LinkedList<String>();
        args.add(GATHERER_CMD);
        args.add("--list-modules");

        int exitcode = e.execute((String[]) args.toArray(new String[0]));
        if (exitcode != 0) {
            logger.error(e.getLastCommandErrorMessage());
            return null;
        }
        return new GathererJsonIO().readGathererModules(e.getLastCommandOutput());
    }
}
