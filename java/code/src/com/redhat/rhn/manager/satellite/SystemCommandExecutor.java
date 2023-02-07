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
package com.redhat.rhn.manager.satellite;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * SystemCommandExecutor - implementation of the Executor interface that
 * will take in the list of arguments and call Runtime.exec().
 */
public class SystemCommandExecutor implements Executor {

    private String lastCommandOutput;
    private String lastCommandError;
    private boolean logError;

    /**
     * Logger for this class
     */
    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Constructor
     */
    public SystemCommandExecutor() {
        logError = true;
    }

    /**
     * Whether to log errors as an ERROR within log4j
     *  Even if this is set to false, the error will still be logged
     *  with DEBUG priority
     * @param toLog true to log as an error
     */
    public void setLogError(boolean toLog) {
        logError = toLog;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int execute(String[] args) {
        if (logger.isDebugEnabled()) {
            logger.debug("execute(String[] args={}) - start", Arrays.asList(args));
        }

        Runtime r = Runtime.getRuntime();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("execute() - Calling r.exec ..");
            }
            Process p = r.exec(args);

            lastCommandOutput = inputStreamToString(p.getInputStream());
            if (logger.isDebugEnabled()) {
                logger.debug("Output from process execution: {}", lastCommandOutput);
            }

            lastCommandError = inputStreamToString(p.getErrorStream());
            if (lastCommandError != null && !lastCommandError.trim().isEmpty()) {
                String msg1 = "Error encountered executing (args=" +
                        Arrays.asList(args) + ")";
                String msg2 = "Error message from process: " + lastCommandError;
                if (logError) {
                    logger.error(msg1);
                    logger.error(msg2);
                }
                else {
                    logger.debug(msg1);
                    logger.debug(msg2);
                }
            }

            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("execute() - Calling p.waitfor ..");
                }
                return p.waitFor();
            }
            catch (InterruptedException e) {
                throw new RuntimeException(
                        "InterruptedException while trying to exec: " + e);
            }
        }
        catch (IOException ioe) {
            logger.error("execute(String[])", ioe);

            String message = "";
            for (String argIn : args) {
                message = message + argIn + " ";
            }
            logger.error("IOException while trying to exec: {}", message, ioe);
            throw new RuntimeException(
                    "IOException while trying to exec: " + message, ioe);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLastCommandOutput() {
        return lastCommandOutput;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLastCommandErrorMessage() {
        return lastCommandError;
    }

    /**
     * Reads the given input stream and returns a string containing its contents.
     *
     * @param in cannot be <code>null</code>
     * @return will not be <code>null</code> but may be the empty string
     */
    private String inputStreamToString(InputStream in) {
        StringBuilder sb = new StringBuilder();

        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = input.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
        }
        catch (IOException e) {
            logger.warn("Error reading input from process input stream", e);
        }

        return sb.toString();
    }

}
