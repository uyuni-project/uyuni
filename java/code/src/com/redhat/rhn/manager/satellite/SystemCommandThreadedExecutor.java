/*
 * Copyright (c) 2013--2017 Red Hat, Inc.
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

import com.redhat.rhn.common.RhnRuntimeException;

import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * SystemCommandThreadedExecutor - implementation of the Executor interface that
 * will take in the list of arguments and call Runtime.exec().
 */
public class SystemCommandThreadedExecutor implements Executor {

    private String lastCommandOutput;
    private String lastCommandError;
    private boolean logError;
    private boolean stdoutLog;

    private class StreamThread extends Thread {
        private InputStream inputStream;
        private boolean logError;
        private Logger logger;
        private String message;

        StreamThread(InputStream in, boolean err, Logger log) {
                inputStream = in;
                logError = err;
                logger = log;
                message = "";
        }
        @Override
        public void run() {
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader input = new BufferedReader(
                                                new InputStreamReader(inputStream));

                String line;
                while ((line = input.readLine()) != null) {
                    sb.append(line);
                    sb.append('\n');
                }
            }
            catch (IOException e) {
                logger.warn("Error reading from process ", e);
            }
            if (sb.length() > 0) {
                message = sb.toString();
                if (logError) {
                    logger.error(message);
                }
                else {
                    logger.info(message);
                }
            }
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Logger for this class
     */
    private final Logger logger;

    /**
     * Constructor
     * @param log Desired logger
     * @param stdoutLogIn log StdOut
     */
    public SystemCommandThreadedExecutor(Logger log, boolean stdoutLogIn) {
        logError = true;
        logger   = log;
        stdoutLog = stdoutLogIn;
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

        int retval;
        Runtime r = Runtime.getRuntime();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("execute() - Calling r.exec ..");
            }
            Process p = r.exec(args);

            StreamThread errStream = new StreamThread(p.getErrorStream(), logError, logger);

            StreamThread inStream = null;
            if (stdoutLog) {
                inStream = new StreamThread(p.getInputStream(), false, logger);
                inStream.start();
            }
            errStream.start();

            try {
                logger.debug("execute() - Calling p.waitfor ..");
                retval = p.waitFor();
                if (inStream != null) {
                    inStream.join();
                }
                errStream.join();
            }
            catch (InterruptedException e) {
                throw new RhnRuntimeException("InterruptedException while trying to exec: " + e);
            }
            lastCommandError = errStream.getMessage();
            if (inStream != null) {
                lastCommandOutput = inStream.getMessage();
            }
        }
        catch (IOException ioe) {
            logger.error("execute(String[])", ioe);

            StringBuilder message = new StringBuilder();
            for (String argIn : args) {
                message.append(argIn).append(" ");
            }
            logger.error("IOException while trying to exec: {}", message, ioe);
            throw new RhnRuntimeException("IOException while trying to exec: " + message, ioe);
        }

        return retval;
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

}
