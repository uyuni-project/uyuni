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

import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.AccessTokenFactory;
import com.redhat.rhn.domain.server.MinionServer;

import org.apache.log4j.Logger;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

/**
 * Manager class for generating or removing minion pillar files.
 */
public class MinionPillarManager {

    public enum PillarSubset {
        GENERAL,
        GROUP_MEMBERSHIP,
        VIRTUALIZATION,
        CUSTOM_INFO
    }

    /** Logger */
    private static final Logger LOG = Logger.getLogger(MinionPillarManager.class);

    public static final MinionPillarManager INSTANCE = new MinionPillarManager(
                    new MinionPillarFileManager(MinionGeneralPillarGenerator.INSTANCE),
                    new MinionPillarFileManager(MinionGroupMembershipPillarGenerator.INSTANCE),
                    new MinionPillarFileManager(MinionVirtualizationPillarGenerator.INSTANCE),
                    new MinionPillarFileManager(MinionCustomInfoPillarGenerator.INSTANCE));

    private MinionPillarFileManager generalPillarFileManager;
    private MinionPillarFileManager groupMembershipPillarFileManager;
    private MinionPillarFileManager virtualizationPillarFileManager;
    private MinionPillarFileManager customInfoPillarFileManager;

    /**
     * Constructor for MinionPillarManager
     * @param generalPillarFileManagerIn general pillar file manager
     * @param groupMembershipPillarFileManagerIn group membership pillar file manager
     * @param virtualizationPillarFileManagerIn virtualization pillar file manager
     * @param customInfoPillarFileManagerIn custom info pillar file manager
     */
    public MinionPillarManager(MinionPillarFileManager generalPillarFileManagerIn,
                               MinionPillarFileManager groupMembershipPillarFileManagerIn,
                               MinionPillarFileManager virtualizationPillarFileManagerIn,
                               MinionPillarFileManager customInfoPillarFileManagerIn) {
        this.generalPillarFileManager = generalPillarFileManagerIn;
        this.groupMembershipPillarFileManager = groupMembershipPillarFileManagerIn;
        this.virtualizationPillarFileManager = virtualizationPillarFileManagerIn;
        this.customInfoPillarFileManager = customInfoPillarFileManagerIn;
    }

    /**
     * Generates specific pillar for the passed minion
     * @param minion the minion server
     */
    public void generatePillar(MinionServer minion) {
        generatePillar(minion, true, Collections.emptySet());
    }

    /**
     * Generates specific pillar for the passed minion
     * @param minion the minion server
     * @param refreshAccessTokens if access tokens should be refreshed first
     * @param subsets subsets of pillar, that should be generated
     */
    public void generatePillar(MinionServer minion, boolean refreshAccessTokens,
                               PillarSubset... subsets) {
        if (refreshAccessTokens) {
            AccessTokenFactory.refreshTokens(minion, Collections.emptySet());
        }
        for (PillarSubset subset : subsets) {
            switch (subset) {
                case GENERAL:
                    generalPillarFileManager.updatePillarFile(minion);
                    break;
                case GROUP_MEMBERSHIP:
                    groupMembershipPillarFileManager.updatePillarFile(minion);
                    break;
                case VIRTUALIZATION:
                    virtualizationPillarFileManager.updatePillarFile(minion);
                    break;
                case CUSTOM_INFO:
                    customInfoPillarFileManager.updatePillarFile(minion);
                    break;
                default:
                    throw new RuntimeException("unreachable");
            }
        }
    }

    /**
     * Generates specific pillar for the passed minion
     * @param minion the minion server
     * @param refreshAccessTokens if access tokens should be refreshed first
     * @param tokensToActivate channels access tokens to activate when refreshing the tokens
     */
    public void generatePillar(MinionServer minion, boolean refreshAccessTokens,
                               Collection<AccessToken> tokensToActivate) {
        LOG.debug("Generating pillar file for minion: " + minion.getMinionId());

        if (refreshAccessTokens) {
            AccessTokenFactory.refreshTokens(minion, tokensToActivate);
        }
        generalPillarFileManager.updatePillarFile(minion);
        groupMembershipPillarFileManager.updatePillarFile(minion);
        virtualizationPillarFileManager.updatePillarFile(minion);
        customInfoPillarFileManager.updatePillarFile(minion);
    }

    /**
     * Removes the corresponding pillar files for the passed minion
     * @param minion the salt minion server
     */
    public void removePillar(MinionServer minion) {
        generalPillarFileManager.removePillar(minion);
        groupMembershipPillarFileManager.removePillar(minion);
        virtualizationPillarFileManager.removePillar(minion);
        customInfoPillarFileManager.removePillar(minion);
    }

    /**
     * @param pillarDataPathIn the root path where pillar files are generated
     */
    public void setPillarDataPath(Path pillarDataPathIn) {
        generalPillarFileManager.setPillarDataPath(pillarDataPathIn);
        groupMembershipPillarFileManager.setPillarDataPath(pillarDataPathIn);
        virtualizationPillarFileManager.setPillarDataPath(pillarDataPathIn);
        customInfoPillarFileManager.setPillarDataPath(pillarDataPathIn);
    }

}
