/**
 * Copyright (c) 2011 Novell
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.util.download.DownloadUtils;
import com.redhat.rhn.common.util.download.DownloadException;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * {@link RhnJavaJob} for downloading image files.
 */
public class ImageDownloadTask extends RhnJavaJob {

    /**
     * Used to log stats in the RHNDAEMONSTATE table
     */
    public static final String DISPLAY_NAME = "image_download";

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        try {
            String pkgDir = Config.get().getString("web.mount_point");

            // Retrieve list of images to download
            List candidates = findCandidates();

            // Bail if no work to do
            if (candidates == null || candidates.size() == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("No images to download");
                }
            } else if (log.isDebugEnabled()) {
                log.debug("Found " + candidates.size() + " images to download");
            }

            // download images
            for (Iterator iter = candidates.iterator(); iter.hasNext();) {
                Map row = (Map) iter.next();
                long id = (Long) row.get("id");
                String path = (String) row.get("path");
                String url = (String) row.get("download_url");
                if (log.isDebugEnabled()) {
                    log.debug("Download image to " + path);
                }
                if (path == null) {
                    continue;
                }
                updateImage(id, "RUNNING");
                if (downloadImage(pkgDir, path, url)) {
                    updateImage(id, "DONE");
                } else {
                    updateImage(id, "ERROR");
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void updateImage(long id, String status) {
        Map params = new HashMap();
        params.put("id", id);
        params.put("status", status);
        WriteMode update = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_UPDATE_IMAGE_STAT);
        update.executeUpdate(params);
        HibernateFactory.commitTransaction();
        HibernateFactory.closeSession();
    }

    private boolean downloadImage(String pkgDir, String path, String url) {
        boolean ret = false;
        File f = new File(pkgDir, path);
        if (!f.exists()) {
            try {
                ret = DownloadUtils.downloadToFile(url, f.getAbsolutePath());
            } catch (DownloadException e) {
                log.error(e.getMessage(), e);
                ret = false;
            }
            if (!ret) {
                log.error("Download error");
            }
        } else {
            log.error(f.getAbsolutePath() + " already exists");
            ret = true;
        }
        return ret;
    }

    private List findCandidates() {
        SelectMode query = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_IMAGE_DOWNLOAD);
        DataResult dr = query.execute(Collections.EMPTY_MAP);
        return dr;
    }
}
