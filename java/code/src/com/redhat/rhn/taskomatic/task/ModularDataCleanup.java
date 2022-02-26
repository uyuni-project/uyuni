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

package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Clean up unused channel modular data files (modules.yaml).
 */
public class ModularDataCleanup extends RhnJavaJob {

    private static final String MOUNT_POINT_PATH = Config.get().getString(ConfigDefaults.MOUNT_POINT);
    private static final String MODULES_REL_PATH = "rhn/modules";

    @Override
    public String getConfigNamespace() {
        return "modular_data_cleanup";
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Set<String> usedModularDataPaths = HibernateFactory.getSession()
                .createQuery("SELECT DISTINCT(m.relativeFilename) FROM Modules m", String.class)
                .stream()
                .collect(Collectors.toSet());
        Set<Path> usedModularDataAbsolutePaths = usedModularDataPaths.stream()
                .map(relPath -> Path.of(MOUNT_POINT_PATH, relPath))
                .collect(Collectors.toSet());

        File modulesPath = Path.of(MOUNT_POINT_PATH, MODULES_REL_PATH).toFile();
        if (!modulesPath.exists()) {
            log.info(String.format("Modules directory " + modulesPath + " does not exist. Skipping cleanup"));
            return;
        }

        Collection<File> modularDataFiles = FileUtils.listFiles(
                modulesPath,
                new SuffixFileFilter("modules.yaml"),
                TrueFileFilter.TRUE);

        List<Path> unusedModularPaths = modularDataFiles
                .stream()
                .map(File::toPath)
                .filter(path -> !usedModularDataAbsolutePaths.contains(path))
                .collect(Collectors.toList());

        logCleaning(unusedModularPaths);
        unusedModularPaths.forEach(path -> path.toFile().delete());
    }

    private void logCleaning(List<Path> unusedModularPaths) {
        if (unusedModularPaths.size() > 0) {
            log.info(String.format("Cleaning %d unused modular data files", unusedModularPaths.size()));
            if (log.isDebugEnabled()) {
                log.debug("Cleaning: " +
                        unusedModularPaths.stream().map(Path::toString).collect(Collectors.joining(", ")));
            }
        }
    }
}
