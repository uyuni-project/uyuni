/*
 * Copyright (c) 2020 SUSE LLC
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import java.util.function.Supplier;

/**
 * Utility for file locks for cross process locking between tomcat and taskomatic
 */
public class FileLocks {

    // Logger instance
    private static Logger log = LogManager.getLogger(FileLocks.class);

    /**
     * Lock for the scc refresh process
     */
    public static final FileLocks SCC_REFRESH_LOCK =
            new FileLocks("/var/lib/spacewalk/scc/sccrefresh.lock");

    private final String filePath;

    /**
     * create the lock
     * @param filePathIn file path for the lock file
     */
    public FileLocks(String filePathIn) {
        this.filePath = filePathIn;
    }

    /**
     * Checks if the underlying file is locked by trying to acquire the lock temporarily.
     * @return if the file is locked.
     * @throws RuntimeException in case there is any exception related to the underling file which is used for locking
     */
    public boolean isLocked() {
        try {
            return withFileLock(() -> false);
        }
        catch (OverlappingFileLockException e) {
            return true;
        }
    }

    /**
     * Runs fn if the file lock can be acquired otherwise it will throw OverlappingFileLockException.
     * @param fn a function to be called while holding the file lock.
     * @throws RuntimeException in case there is any exception related to the underling file which is used for locking
     */
    public void withFileLock(Runnable fn) {
        withFileLock(() -> {
            fn.run();
            return null;
        });
    }

    /**
     * Runs fn if the file lock can be acquired otherwise it will throw OverlappingFileLockException.
     * @param fn a function to be called while holding the file lock.
     * @param <T> return type of fn
     * @return the result of calling fn
     * @throws RuntimeException in case there is any exception related to the underling file which is used for locking
     */
    public <T> T withFileLock(Supplier<T> fn) {
        File file = new File(filePath);
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
             FileChannel channel = randomAccessFile.getChannel();
             FileLock fileLock = channel.tryLock()
        ) {
            if (fileLock != null) {
                try {
                    log.info("File lock " + filePath + " acquired.");
                    try {
                        // Set the user to tomcat so both taskomatic (root) and tomcat (tomcat) can use it.
                        FileSystem fileSystem = FileSystems.getDefault();
                        UserPrincipalLookupService service = fileSystem.getUserPrincipalLookupService();
                        UserPrincipal tomcatUser = service.lookupPrincipalByName("tomcat");
                        if (!Files.getOwner(file.toPath(), LinkOption.NOFOLLOW_LINKS).equals(tomcatUser)) {
                            Files.setOwner(file.toPath(), tomcatUser);
                        }
                    }
                    catch (IOException e) {
                        log.error("Error adjusting lock file user.", e);
                    }
                    return fn.get();
                }
                finally {
                    // Cleanup the file while we still have the lock
                    file.delete();
                }
            }
            else {
                log.warn("File lock " + filePath + " already in use.");
                throw new OverlappingFileLockException();
            }
        }
        catch (IOException e) {
            log.error("File lock " + filePath + " error", e);
            throw new RuntimeException(e);
        }
    }

}
