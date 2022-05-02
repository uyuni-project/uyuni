/*
 * Copyright (c) 2009--2011 Red Hat, Inc.
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
package com.redhat.rhn.taskomatic.task.repomd;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.manager.satellite.Executor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 *
 *
 */
public abstract class RepositoryWriter {

    protected Logger log = LogManager.getLogger(RepositoryWriter.class);
    protected final String pathPrefix;
    protected final String mountPoint;
    protected final Executor cmdExecutor;

    /**
     * Constructor takes in pathprefix and mountpoint
     * @param pathPrefixIn prefix to package path
     * @param mountPointIn mount point package resides
     * @param cmdExecutorIn {@link Executor} instance to run system commands
     */
    protected RepositoryWriter(String pathPrefixIn, String mountPointIn, Executor cmdExecutorIn) {
        this.pathPrefix = pathPrefixIn;
        this.mountPoint = mountPointIn;
        this.cmdExecutor = cmdExecutorIn;
    }

    /**
    *
    * @param channel channel info
    * @return repodata sanity
    */
    public abstract boolean isChannelRepodataStale(Channel channel);

    /**
    *
    * @param channel channelinfo for repomd file creation
    */
   public abstract void writeRepomdFiles(Channel channel);

   /**
    * Deletes repository cache files
    * @param channelLabelToProcess channel label
    * @param deleteDir directory to delete
    */
   public void deleteRepomdFiles(String channelLabelToProcess, boolean deleteDir) {
       log.info("Removing {}", channelLabelToProcess);
       String prefix = mountPoint + File.separator + pathPrefix + File.separator +
               channelLabelToProcess;
       File theDirectory = new File(prefix);

       String[] children = theDirectory.list();
       if (theDirectory.isDirectory() && children != null) {
           for (String childIn : children) {
               File file = new File(prefix + File.separator + childIn);
               if (!file.delete()) {
                   log.info("Couldn't remove {}", file.getAbsolutePath());
               }
           }
       }
       if (deleteDir) {
           if (!theDirectory.delete()) {
               log.info("Couldn't remove {}", prefix);
           }
       }
   }
}
