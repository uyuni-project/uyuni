package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import akka.actor.typed.Behavior;

public class SsmRemovePackagesActor implements Actor {

    private final static Logger LOG = Logger.getLogger(SsmRemovePackagesActor.class);

    public static class Message implements Command {
        private final long userId;
        private final Date earliest;
        private final Optional<Long> actionChainId;

        public Message(long userId, Date earliest, Optional<Long> actionChainId) {
            this.userId = userId;
            this.earliest = earliest;
            this.actionChainId = actionChainId;
        }
    }

    public Behavior<Command> create() {
        return setup(context -> receive(Command.class)
                .onMessage(Message.class, message -> onMessage(message))
                .build());
    }

    private Behavior<Command> onMessage(Message message) {
        handlingTransaction(() -> execute(message));
        return same();
    }

    private void execute(Message message) {
        var user = UserFactory.lookupById(message.userId);
        SsmPackagesActor.execute(user, "ssm.package.remove.operationname", () -> doSchedule(message, user));
    }

    protected List<Action> doSchedule(Message msg, User user) {
        // TODO: move this in caller
        DataResult<Map<String, Object>> result =
                SystemManager.ssmSystemPackagesToRemove(user,
                        RhnSetDecl.SSM_REMOVE_PACKAGES_LIST.getLabel(), false);
        /*
         * 443500 - The following was changed to be able to stuff all of the package
         * removals into a single action. The schedule package removal page will display a
         * fine grained mapping of server to package removed (taking into account to only
         * show packages that exist on the server).
         *
         * However, there is no issue in requesting a client delete a package it doesn't
         * have. So when we create the action, populate it with all packages and for every
         * server to which any package removal applies. This will let us keep all of the
         * removals coupled under a single scheduled action and won't cause an issue on
         * the client when the scheduled removals are picked up.
         *
         * jdobies, Apr 8, 2009
         */

        // The package collection is a set to prevent duplicates when keeping a running
        // total of all packages selected
        Set<PackageListItem> allPackages = new HashSet<PackageListItem>();

        Set<Long> allServerIds = new HashSet<Long>();

        // Iterate the data, which is essentially each unique package/server combination
        // to remove. Note that this is only for servers that we have marked as having the
        // package installed.
        LOG.debug("Iterating data.");

        // Add action for each package found in the elaborator
        for (Map<String, Object> data : result) {
            // Load the server
            Long sid = (Long) data.get("id");
            allServerIds.add(sid);

            // Get the packages out of the elaborator
            List<Map> elabList = (List<Map>) data.get("elaborator0");
            if (elabList != null) {
                for (Map elabMap : elabList) {
                    String idCombo = (String) elabMap.get("id_combo");
                    PackageListItem item = PackageListItem.parse(idCombo);
                    allPackages.add(item);
                }
            }
        }

        LOG.debug("Converting data to maps.");
        List<PackageListItem> allPackagesList = new ArrayList<PackageListItem>(allPackages);
        List<Map<String, Long>> packageListData = PackageListItem
                .toKeyMaps(allPackagesList);

        LOG.debug("Scheduling package removals.");
        try {
            var actionChain = msg.actionChainId.map(id -> ActionChainFactory.getActionChain(user, id)).orElse(null);
            List<Action> actions = ActionChainManager.schedulePackageRemovals(user,
                    allServerIds, packageListData, msg.earliest, actionChain);
            LOG.debug("Done.");
            return actions;
        }
        catch (TaskomaticApiException e) {
            throw new RuntimeException(e);
        }
    }
}
