/*
 * Copyright (c) 2010 Red Hat, Inc.
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

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.AccessTokenFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.pillar.MinionPillarManager;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * TokenCleanup
 */
public class TokenCleanup extends RhnJavaJob {

    private final SaltApi saltApi = GlobalInstanceHolder.SALT_API;

    @Override
    public String getConfigNamespace() {
        return "token_cleanup";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext arg0In)
        throws JobExecutionException {
        if (log.isDebugEnabled()) {
            log.debug("start token cleanup");
        }
        try {
            Stream<MinionServer> changedMinions = MinionServerFactory.listMinions().stream().flatMap(minionServer -> {
                try {
                    if (AccessTokenFactory.refreshTokens(minionServer, Collections.emptySet())) {
                        // TODO schedule state.apply channels to refresh channels on minion ?
                        MinionPillarManager.INSTANCE.generatePillar(minionServer, false, Collections.emptySet());
                        return Stream.of(minionServer);
                    }
                    else {
                        return Stream.empty();
                    }
                }
                catch (Exception e) {
                    log.error("error refreshing access tokens for minion " +
                            minionServer.getMinionId(), e);
                    return Stream.empty();
                }
            });

            List<String> changedMinionIds = changedMinions.map(m -> m.getMinionId()).collect(Collectors.toList());
            if (Config.get().getBoolean(ConfigDefaults.TOKEN_REFRESH_AUTO_DEPLOY)) {
                saltApi.deployChannels(changedMinionIds);
            }
            else {
                log.warn("The following minions got channel tokens changed and" +
                        " need them deployed before the old one expires: " + changedMinionIds.stream()
                        .collect(Collectors.joining(", ")));
            }
            AccessTokenFactory.cleanupUnusedExpired();
        }
        catch (Exception e) {
            log.error("error while token cleanup", e);
        }
    }
}
