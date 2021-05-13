/**
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
package com.suse.manager.webui.services.pillar;

import static com.suse.manager.webui.services.SaltConstants.SUMA_PILLAR_DATA_PATH;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.GROUP_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.server.MinionServer;

import com.suse.manager.webui.utils.SaltPillar;
import com.suse.manager.webui.utils.SaltStateGenerator;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Abstract manager class for generating or removing minion pillar files
 */
public class MinionPillarFileManager {

    private static final Logger LOG = Logger.getLogger(MinionPillarManager.class);

    private Path pillarDataPath = Paths.get(SUMA_PILLAR_DATA_PATH);

    private MinionPillarGenerator minionPillarGenerator;

    /**
     * Constructor for MinionPillarFileManager
     * @param minionPillarGeneratorIn the minion pillar generator
     */
    public MinionPillarFileManager(MinionPillarGenerator minionPillarGeneratorIn) {
        super();
        this.minionPillarGenerator = minionPillarGeneratorIn;
    }

    /**
     * Generates pillar containing the information of the server groups the the passed minion is member of
     * @param minion the minion server
     */
    public void updatePillarFile(MinionServer minion) {
        this.minionPillarGenerator.generatePillarData(minion).ifPresentOrElse(
                (pillar) -> this.saveFileToDisk(pillar, this.minionPillarGenerator.getFilename(minion.getMinionId())),
                () -> removePillarFile(minion.getMinionId())
        );
    }

    private void saveFileToDisk(SaltPillar pillar, String filename) {
        try {
            Files.createDirectories(this.pillarDataPath);
            File file = this.pillarDataPath.resolve(filename).toFile();
            new SaltStateGenerator(file).generate(pillar);
            FileUtils.setAttributes(file.toPath(), "tomcat", "susemanager",
                    Set.of(OWNER_READ, OWNER_WRITE, GROUP_READ, GROUP_WRITE));
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Remove the corresponding pillar file for the passed minion.
     * @param minionId the minion Id
     */
    public void removePillarFile(String minionId) {
        Path filePath = this.pillarDataPath.resolve(this.minionPillarGenerator.getFilename(minionId));
        try {
            Files.deleteIfExists(filePath);
        }
        catch (IOException e) {
            LOG.error("Could not remove pillar file " + filePath);
        }
    }

    /**
     * @param pillarDataPathIn the root path where pillar files are generated
     */
    public void setPillarDataPath(Path pillarDataPathIn) {
        this.pillarDataPath = pillarDataPathIn;
    }

}
