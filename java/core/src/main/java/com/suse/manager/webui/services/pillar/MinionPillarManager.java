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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Logger LOG = LogManager.getLogger(MinionPillarManager.class);

    public static final MinionPillarManager INSTANCE = new MinionPillarManager(
                    MinionGeneralPillarGenerator.INSTANCE,
                    MinionGroupMembershipPillarGenerator.INSTANCE,
                    MinionVirtualizationPillarGenerator.INSTANCE,
                    MinionCustomInfoPillarGenerator.INSTANCE);

    private MinionPillarGenerator generalPillarGenerator;
    private MinionPillarGenerator groupMembershipPillarGenerator;
    private MinionPillarGenerator virtualizationPillarGenerator;
    private MinionPillarGenerator customInfoPillarGenerator;

    /**
     * Constructor for MinionPillarManager
     * @param generalPillarGeneratorIn general pillar generator
     * @param groupMembershipPillarGeneratorIn group membership pillar generator
     * @param virtualizationPillarGeneratorIn virtualization pillar generator
     * @param customInfoPillarGeneratorIn custom info pillar generator
     */
    public MinionPillarManager(MinionPillarGenerator generalPillarGeneratorIn,
                               MinionPillarGenerator groupMembershipPillarGeneratorIn,
                               MinionPillarGenerator virtualizationPillarGeneratorIn,
                               MinionPillarGenerator customInfoPillarGeneratorIn) {
        this.generalPillarGenerator = generalPillarGeneratorIn;
        this.groupMembershipPillarGenerator = groupMembershipPillarGeneratorIn;
        this.virtualizationPillarGenerator = virtualizationPillarGeneratorIn;
        this.customInfoPillarGenerator = customInfoPillarGeneratorIn;
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
                    generalPillarGenerator.generatePillarData(minion);
                    break;
                case GROUP_MEMBERSHIP:
                    groupMembershipPillarGenerator.generatePillarData(minion);
                    break;
                case VIRTUALIZATION:
                    virtualizationPillarGenerator.generatePillarData(minion);
                    break;
                case CUSTOM_INFO:
                    customInfoPillarGenerator.generatePillarData(minion);
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
        LOG.debug("Generating pillar file for minion: {}", minion.getMinionId());

        if (refreshAccessTokens) {
            AccessTokenFactory.refreshTokens(minion, tokensToActivate);
        }
        generalPillarGenerator.generatePillarData(minion);
        groupMembershipPillarGenerator.generatePillarData(minion);
        virtualizationPillarGenerator.generatePillarData(minion);
        customInfoPillarGenerator.generatePillarData(minion);
    }

    /**
     * Removes the corresponding pillars for the passed minion
     * @param minion the salt minion server
     */
    public void removePillar(MinionServer minion) {
        generalPillarGenerator.removePillar(minion);
        groupMembershipPillarGenerator.removePillar(minion);
        virtualizationPillarGenerator.removePillar(minion);
        customInfoPillarGenerator.removePillar(minion);
    }
}
