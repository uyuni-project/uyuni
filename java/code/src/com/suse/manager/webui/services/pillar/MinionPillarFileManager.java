/**
 * Copyright (c) 2020 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.webui.services.pillar;

import static com.suse.manager.webui.services.SaltConstants.SUMA_PILLAR_DATA_PATH;

import com.redhat.rhn.domain.server.MinionServer;

import com.suse.manager.webui.utils.SaltPillar;
import com.suse.manager.webui.utils.SaltStateGenerator;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    public void generatePillarFile(MinionServer minion) {
        SaltPillar pillar = this.minionPillarGenerator.generatePillarData(minion);
        this.saveFileToDisk(pillar, this.minionPillarGenerator.getFilename(minion.getMinionId()));
    }

    private void saveFileToDisk(SaltPillar pillar, String filename) {
        try {
            Files.createDirectories(this.pillarDataPath);
            new SaltStateGenerator(this.pillarDataPath.resolve(filename).toFile()).generate(pillar);
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
