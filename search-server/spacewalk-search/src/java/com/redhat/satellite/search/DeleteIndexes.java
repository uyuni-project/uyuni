/*
 * Copyright (c) 2008--2012 Red Hat, Inc.
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

package com.redhat.satellite.search;

import com.redhat.satellite.search.config.Configuration;
import com.redhat.satellite.search.db.DatabaseManager;
import com.redhat.satellite.search.db.WriteQuery;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Reindex - cleans up indexes on filesystem and database so reindexing will occur
 */
public class DeleteIndexes {
    private static final Logger LOG = LogManager.getLogger(DeleteIndexes.class);

    private DeleteIndexes() {
    }

    protected static boolean deleteDirectory(File dir) {
        boolean warning = true;
        File[] files = Optional.ofNullable(dir.listFiles()).orElse(ArrayUtils.toArray());
        for (File file : files) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            }
            try {
                Files.delete(file.toPath());
                LOG.debug("Deleted: {}", file.getAbsolutePath());
            }
            catch (IOException e) {
                LOG.warn("*ERROR* unable to delete: {}: {}", file.getAbsolutePath(), e);
                warning = false;
            }
        }
        try {
            Files.delete(dir.toPath());
        }
        catch (IOException e) {
            LOG.warn("*ERROR* unable to delete: {}: {}", dir.getAbsolutePath(), e);
            warning = false;
        }
        return warning;
    }

    protected static boolean deleteIndexPath(String path) {
        File dir = new File(path);
        if ("/".equals(dir.getAbsolutePath())) {
            LOG.warn("Error, passed in path is <{}> this looks wrong", path);
            return false;
        }
        if (!dir.exists()) {
            LOG.debug("Path <{}> doesn't exist", dir.getAbsolutePath());
            return true;  // dir doesn't exist, so just as good as deleted
        }
        if (!dir.isDirectory()) {
            LOG.warn("Error, passed in path <{}> is not a directory", path);
            return false;
        }
        LOG.info("Attempting to delete {}", dir.getAbsolutePath());
        return deleteDirectory(dir);
    }

    protected static void deleteQuery(DatabaseManager databaseManager,
                                      String queryName) throws SQLException {
        LOG.info("Running query: {}", queryName);
        WriteQuery query = databaseManager.getWriterQuery(queryName);
        query.delete(null);
        query.close();
    }

    /**
     * @param args Args
     */
    public static void main(String[] args) {
        try {
            Configuration config = new Configuration();
            DatabaseManager databaseManager = new DatabaseManager(config);
            String indexWorkDir = config.getString("search.index_work_dir", null);
            if (StringUtils.isBlank(indexWorkDir)) {
                LOG.warn("Couldn't find path for where index files are stored.");
                LOG.warn("Looked in config for property: search.index_work_dir");
                return;
            }
            List<IndexInfo> indexes = new ArrayList<>();
            indexes.add(new IndexInfo("deleteLastErrata",
                    indexWorkDir + File.separator + "errata"));
            indexes.add(new IndexInfo("deleteLastPackage",
                    indexWorkDir + File.separator + "package"));
            indexes.add(new IndexInfo("deleteLastServer",
                    indexWorkDir + File.separator + "server"));
            indexes.add(new IndexInfo("deleteLastHardwareDevice",
                    indexWorkDir + File.separator + "hwdevice"));
            indexes.add(new IndexInfo("deleteLastSnapshotTag",
                    indexWorkDir + File.separator + "snapshotTag"));
            indexes.add(new IndexInfo("deleteLastServerCustomInfo",
                    indexWorkDir + File.separator + "serverCustomInfo"));
            indexes.add(new IndexInfo("deleteLastXccdfIdent",
                    indexWorkDir + File.separator + "xccdfIdent"));
            for (IndexInfo info : indexes) {
                deleteQuery(databaseManager, info.getQueryName());
                if (!deleteIndexPath(info.getDirPath())) {
                    LOG.warn("Failed to delete index for {}", info.getDirPath());
                }
            }
        }
        catch (SQLException e) {
            LOG.error("Caught Exception: ", e);
            if (e.getErrorCode() == 17002) {
                LOG.error("Unable to establish database connection.");
                LOG.error("Ensure database is available and connection details are correct, then retry");
            }
            System.exit(1);
        }
        catch (IOException e) {
            LOG.error("Caught Exception: ", e);
            System.exit(1);
        }
        LOG.info("Index files have been deleted and database has been cleaned up, " +
            "ready to reindex");
    }

    /**
     * IndexInfo
     */
    protected static class IndexInfo {
        protected String queryName;
        protected String dirPath;

        /**
         *
         * @param query query name to delete all records
         * @param path string pointing to where index files reside
         */
        public IndexInfo(String query, String path) {
            queryName = query;
            dirPath = path;
        }

        /**
         * Set the query name to delete all records for this index
         * @param queryNameIn name of the query
         */
        public void setQueryName(String queryNameIn) {
            queryName = queryNameIn;
        }
        /**
         * @return the query name
         */
        public String getQueryName() {
            return queryName;
        }
        /**
         * Set the path where the index files exist
         * @param path String which points to index files
         */
        public void setDirPath(String path) {
            dirPath = path;
        }
        /**
         * Returns the path
         * @return a string which points to the index files
         */
        public String getDirPath() {
            return dirPath;
        }
    }
}
