/**
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.manager.download;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.security.SessionSwap;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageSource;
import com.redhat.rhn.domain.server.CrashFile;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.BaseManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Provides methods for downloading packages and files and getting urls
 * DownloadManager
 * @version $Rev$
 */
public class DownloadManager extends BaseManager {

    public static final String DOWNLOAD_TYPE_KICKSTART = "kickstart";
    public static final String DOWNLOAD_TYPE_COBBLER = "cobbler";
    public static final String DOWNLOAD_TYPE_COBBLER_API = "cobbler_api";
    public static final String DOWNLOAD_TYPE_PACKAGE = "package";
    public static final String DOWNLOAD_TYPE_SOURCE = "srpm";
    public static final String DOWNLOAD_TYPE_REPO_LOG = "repolog";
    public static final String DOWNLOAD_TYPE_CRASHFILE = "crashfile";

    /**
     * Get a download path (part of the url) that is used to download a package.
     *  The url will be in the form of
     *  /download/SHA1_TOKEN/EXPIRE_TIME/userId/packId/filename.rpm
     * @param pack the package
     * @param user the user
     * @return the path/url
     */
    public static String getPackageDownloadPath(Package pack, User user) {

        //If the package is on our list of non-expiring packages, then generate
        //   a non-expiring URL
        List packs = Config.get().getList(ConfigDefaults.NON_EXPIRABLE_PACKAGE_URLS);
        if (packs != null && packs.contains(pack.getPackageName().getName())) {
            return getNonExpiringDownloadPath(pack.getId(), pack.getFile(), user,
                    DownloadManager.DOWNLOAD_TYPE_PACKAGE);
        }
        return getDownloadPath(pack.getId(), pack.getFile(), user,
                DownloadManager.DOWNLOAD_TYPE_PACKAGE);
    }

    /**
     * Get a download path (part of the url) that is used to download a package.
     *  The url will be in the form of
     *  /download/SHA1_TOKEN/0/userId/packId/filename.rpm
     *  The url will NOT expire and should generally only be used
     * @param pack the package
     * @param user the user
     * @return the path/url
     */
    public static String getPackageDownloadPathNoExpiration(Package pack, User user) {
        return getNonExpiringDownloadPath(pack.getId(), pack.getFile(), user,
                DownloadManager.DOWNLOAD_TYPE_PACKAGE);
    }


    /**
     * Get a download path that is used to download a srpm.
     *  The url will be in the form of
     *  /download/SHA1_TOKEN/EXPIRE_TIME/userId/packId/filename.rpm
     * @param pkg the package
     * @param src the package source
     * @param user the user
     * @return the path/url
     */
    public static String getPackageSourceDownloadPath(Package pkg,
                                        PackageSource src, User user) {
        return getDownloadPath(pkg.getId(), src.getFile(), user,
                DownloadManager.DOWNLOAD_TYPE_SOURCE);
    }

    /**
     * Get a download path that is used to download a repo log file.
     *
     * @param c the Channel
     * @param user the user
     * @return the path/url
     */
    public static String getChannelSyncLogDownloadPath(Channel c,
                                        User user) {
        return getNonExpiringDownloadPath(c.getId(), c.getLabel(), user,
                DownloadManager.DOWNLOAD_TYPE_REPO_LOG);
    }

    private static String getDownloadPath(Long fileId, String filename,
            User user, String type) {
        Long time = 0L;
        if (Config.get().getInt(ConfigDefaults.DOWNLOAD_URL_LIFETIME) > 0) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, Config.get().getInt(
                    ConfigDefaults.DOWNLOAD_URL_LIFETIME));
            time = cal.getTimeInMillis();
        }

        return "/download/" + type + "/" + getFileSHA1Token(fileId,
                filename, user, time, type) + "/" +
                time + "/" + user.getId() + "/" + fileId + "/" +
                filename;
    }

    private static String getNonExpiringDownloadPath(Long fileId, String filename,
            User user, String type) {
        Long time = 0L;
        return "/download/" + type + "/" + getFileSHA1Token(fileId,
                filename, user, time, type) + "/" +
                time + "/" + user.getId() + "/" + fileId + "/" +
                filename;

    }

    /**
     * get the Hmac SHA1 token use in constructing a package download url
     *      also useful if verifying a package download url
     * @param fileId the file id
     * @param filename the filename of the file
     * @param user the user requesting the file
     * @param expire the expire time
     * @param type the type of the download (i.e. package, iso, etc..)
     * @return a string representing the hash
     */
    public static String getFileSHA1Token(Long fileId, String filename,
            User user, Long expire, String type) {

        List<String> data = new ArrayList<String>();
        data.add(expire.toString());
        data.add(user.getId().toString());
        data.add(fileId.toString());
        data.add(filename);
        data.add(type);

        return SessionSwap.rhnHmacData(data);
    }

    /**
     * Checks to see if a file exists
     * @param path the path to the file
     * @return true if available, false otherwise
     */
    public static boolean isFileAvailable(String path) {
        String file = Config.get().getString(ConfigDefaults.MOUNT_POINT) + "/" + path;
        return new File(file).exists();
    }

    /**
     * Get a download path (part of the url) that is used to download a crash file.
     *  The url will be in the form of
     *  /download/SHA1_TOKEN/EXPIRE_TIME/userId/crashId/crashfile
     * @param crashFile the package
     * @param user the user
     * @return the path/url
     */
    public static String getCrashFileDownloadPath(CrashFile crashFile, User user) {
        return getDownloadPath(crashFile.getId(), crashFile.getFilename(), user,
                DownloadManager.DOWNLOAD_TYPE_CRASHFILE);
    }
}
