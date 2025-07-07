/*
 * Copyright (c) 2012 SUSE LLC
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

package com.redhat.rhn.domain.action.dup;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductUpgrade;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.ServerFactory;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.webui.services.pillar.MinionPillarManager;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.utils.Opt;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DistUpgradeAction - Class representation of distribution upgrade action.
 */
public class DistUpgradeAction extends Action {
    public static final String ALLOW_VENDOR_CHANGE = "allow_vendor_change";

    private static final long serialVersionUID = 1585401756449185047L;
    private DistUpgradeActionDetails details;

    /**
     * Return the details.
     * @return details
     */
    public DistUpgradeActionDetails getDetails() {
        return details;
    }

    /**
     * Set the details.
     * @param detailsIn details
     */
    public void setDetails(DistUpgradeActionDetails detailsIn) {
        detailsIn.setParentAction(this);
        this.details = detailsIn;
    }


    /**
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @param action action which has all the revisions
     * @return minion summaries grouped by local call
     */
    public static Map<LocalCall<?>, List<MinionSummary>> distUpgradeAction(
            List<MinionSummary> minionSummaries, DistUpgradeAction action) {
        Map<Boolean, List<Channel>> collect = action.getDetails().getChannelTasks()
                .stream().collect(Collectors.partitioningBy(
                        ct -> ct.getTask() == DistUpgradeChannelTask.SUBSCRIBE,
                        Collectors.mapping(DistUpgradeChannelTask::getChannel,
                                Collectors.toList())
                ));

        List<Channel> subbed = collect.get(true);
        List<Channel> unsubbed = collect.get(false);

        action.getServerActions()
                .stream()
                .flatMap(s -> Opt.stream(s.getServer().asMinionServer()))
                .forEach(minion -> {
                    Set<Channel> currentChannels = minion.getChannels();
                    unsubbed.forEach(currentChannels::remove);
                    currentChannels.addAll(subbed);
                    MinionPillarManager.INSTANCE.generatePillar(minion);
                    ServerFactory.save(minion);
                });

        Map<String, Object> pillar = new HashMap<>();
        Map<String, Object> susemanager = new HashMap<>();
        pillar.put("susemanager", susemanager);
        Map<String, Object> distupgrade = new HashMap<>();
        susemanager.put("distupgrade", distupgrade);
        distupgrade.put("dryrun", action.getDetails().isDryRun());
        distupgrade.put(ALLOW_VENDOR_CHANGE, action.getDetails().isAllowVendorChange());
        distupgrade.put("channels", subbed.stream()
                .sorted()
                .map(c -> "susemanager:" + c.getLabel())
                .collect(Collectors.toList()));
        if (Objects.nonNull(action.getDetails().getMissingSuccessors())) {
            pillar.put("missing_successors", Arrays.asList(action.getDetails().getMissingSuccessors().split(",")));
        }
        action.getDetails().getProductUpgrades().stream()
                .map(SUSEProductUpgrade::getToProduct)
                .filter(SUSEProduct::isBase)
                .forEach(tgt -> {
                    Map<String, String> baseproduct = new HashMap<>();
                    baseproduct.put("name", tgt.getName());
                    baseproduct.put("version", tgt.getVersion());
                    baseproduct.put("arch", tgt.getArch().getLabel());
                    distupgrade.put("targetbaseproduct", baseproduct);
                });

        HibernateFactory.commitTransaction();

        LocalCall<Map<String, State.ApplyResult>> distUpgrade = State.apply(
                Collections.singletonList(ApplyStatesEventMessage.DISTUPGRADE),
                Optional.of(pillar)
        );
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        ret.put(distUpgrade, minionSummaries);

        return ret;
    }

}
