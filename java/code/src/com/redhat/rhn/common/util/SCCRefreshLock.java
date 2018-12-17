/**
 * Copyright (c) 2018 SUSE LLC
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
package com.redhat.rhn.common.util;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;

/**
 * SCCRefreshLock - locks the SCC refresh process
 */
public class SCCRefreshLock {

    private static File f;
    private static FileChannel channel;
    private static FileLock lock;
    // Logger instance
    private static Logger log = Logger.getLogger(SCCRefreshLock.class);

    /**
     * create the lock
     */
    private SCCRefreshLock() {
    }

    /**
     * tries to obtain the lock or throws an exception
     */
    public static void tryGetLock() {
        try {
            f = new File("/var/lib/spacewalk/scc/sccrefresh.lock");
            synchronized (f) {
                // create the lock file
                channel = new RandomAccessFile(f, "rw").getChannel();
                // create it with correct user
                FileSystem fileSystem = FileSystems.getDefault();
                UserPrincipalLookupService service = fileSystem.getUserPrincipalLookupService();
                UserPrincipal rootUser = service.lookupPrincipalByName("root");
                UserPrincipal tomcatUser = service.lookupPrincipalByName("tomcat");
                if (Files.getOwner(f.toPath(), LinkOption.NOFOLLOW_LINKS).equals(rootUser)) {
                    Files.setOwner(f.toPath(), tomcatUser);
                }
                lock = channel.tryLock();
                if (lock == null) {
                    // File is lock by other application
                    channel.close();
                    log.warn("SCC refresh is already running.");
                    throw new OverlappingFileLockException();
                }
                log.info("Got the Lock for scc refresh");
                // Add on exit handler to release lock when application shutdown
                OnExitHandler onExitHandler = new OnExitHandler();
                Runtime.getRuntime().addShutdownHook(onExitHandler);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Could not start process.", e);
        }
    }

    /**
     * unlock
     */
    public static void unlockFile() {
        try {
            if (lock != null) {
                lock.release();
                channel.close();
                f.delete();
                log.info("SCC refresh lock released and removed");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * the on exit handler
     */
    static class OnExitHandler extends Thread {
        public void run() {
            unlockFile();
        }
    }
}
