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

import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.AccessTokenFactory;
import com.redhat.rhn.domain.server.MinionServer;

import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Manager class for generating or removing minion pillar files.
 */
public class MinionPillarManager {

    /** Logger */
    private static final Logger LOG = Logger.getLogger(MinionPillarManager.class);

    public static final MinionPillarManager INSTANCE = new MinionPillarManager(
            Arrays.asList(new MinionPillarFileManager(MinionGeneralPillarGenerator.INSTANCE),
                    new MinionPillarFileManager(MinionGroupMembershipPillarGenerator.INSTANCE)));

    private List<MinionPillarFileManager> pillarFileManagers;

    /**
     * Constructor for MinionPillarManager
     * @param pillarFileManagersIn a list of minion pillar file managers
     */
    public MinionPillarManager(List<MinionPillarFileManager> pillarFileManagersIn) {
        this.pillarFileManagers = pillarFileManagersIn;
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
     * @param tokensToActivate channels access tokens to activate when refreshing the tokens
     */
    public void generatePillar(MinionServer minion, boolean refreshAccessTokens,
                               Collection<AccessToken> tokensToActivate) {
        LOG.debug("Generating pillar file for minion: " + minion.getMinionId());

        if (refreshAccessTokens) {
            AccessTokenFactory.refreshTokens(minion, tokensToActivate);
        }
        this.pillarFileManagers.stream().forEach(m -> m.generatePillarFile(minion));
    }

    public void generatePillar(MinionServer minion, MinionPillarGenerator generator) {
        new MinionPillarFileManager(generator).generatePillarFile(minion);
    }

    /**
     * Removes the corresponding pillar files for the passed minion
     * @param minionId the minion Id
     */
    public void removePillar(String minionId) {
        this.pillarFileManagers.stream().forEach(m -> m.removePillarFile(minionId));
    }

    public void removePillar(MinionServer minion, MinionPillarGenerator generator) {
        new MinionPillarFileManager(generator).removePillarFile(minion.getMinionId());
    }

}
