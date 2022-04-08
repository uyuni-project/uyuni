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
package com.suse.manager.webui.services.pillar;

import static com.suse.manager.webui.services.SaltConstants.SUMA_PILLAR_DATA_PATH;

import com.redhat.rhn.domain.server.MinionServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manager class for generating DB pillar data and removing minion pillar files
 * The file aspect of it is legacy: there used to be pillar files, but those should be removed now!
 */
public class MinionPillarFileManager {

    private static final Logger LOG = LogManager.getLogger(MinionPillarManager.class);

    private Path pillarDataPath = Paths.get(SUMA_PILLAR_DATA_PATH);

    private final MinionPillarGenerator minionPillarGenerator;

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
        if (this.minionPillarGenerator.generatePillarData(minion).isEmpty()) {
            removePillar(minion);
        }
        // Progressively move away from pillar files
        removePillarFile(minion.getMinionId());
    }

    /**
     * Remove the pillar data from the given minion
     * @param minion the minion server
     */
    public void removePillar(MinionServer minion) {
        removePillarFile(minion.getMinionId());
        minion.getPillarByCategory(this.minionPillarGenerator.getCategory())
                .ifPresent(pillar -> minion.getPillars().remove(pillar));
    }

    /**
     * Remove the corresponding pillar file for the passed minion.
     * @param minionId the minion server ID
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
