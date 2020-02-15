package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;
import static com.redhat.rhn.frontend.events.TransactionHelper.handlingTransaction;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.ssm.SsmOperationManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;

public class SsmVerifyPackagesActor implements Actor {

    private final static Logger LOG = Logger.getLogger(SsmVerifyPackagesActor.class);

    public static class Message implements Command {
        private final Long userId;
        private final Date earliest;
        private final Optional<Long> actionChainId;
        private final Long operationId;

        public Message(Long userId, Date earliest, Optional<Long> actionChainId, Long operationId) {
            this.userId = userId;
            this.earliest = earliest;
            this.actionChainId = actionChainId;
            this.operationId = operationId;
        }
    }

    public Behavior<Command> create(ActorRef<Command> guardian) {
        return setup(context -> receive(Command.class)
                .onMessage(Message.class, message -> onMessage(message))
                .build());
    }

    private Behavior<Command> onMessage(Message message) {
        handlingTransaction(() -> execute(message),
                e -> { SsmOperationManager.completeOperation(UserFactory.lookupById(message.userId), message.operationId); });
        return same();
    }

    public void execute(Message message) {
        User user = UserFactory.lookupById(message.userId);

        try {
            scheduleVerifications(message, user);
            SsmOperationManager.completeOperation(user, message.operationId);
        }
        catch (Exception e) {
            LOG.error("Error scheduling package installations for event " + message, e);
            throw new RuntimeException(e);
        }
    }

    private void scheduleVerifications(Message msg, User user)
            throws TaskomaticApiException {

        Date earliest = msg.earliest;
        ActionChain actionChain = ActionChainFactory.getActionChain(
                user, msg.actionChainId.orElse(null));

        DataResult result = SystemManager.ssmSystemPackagesToRemove(user,
                RhnSetDecl.SSM_VERIFY_PACKAGES_LIST.getLabel(), false);
        result.elaborate();

        // Loop over each server that will have packages upgraded
        for (Iterator it = result.iterator(); it.hasNext();) {

            // Add action for each package found in the elaborator
            Map data = (Map) it.next();

            // Load the server
            Long sid = (Long)data.get("id");
            Server server = SystemManager.lookupByIdAndUser(sid, user);

            // Get the packages out of the elaborator
            List elabList = (List) data.get("elaborator0");

            List<PackageListItem> items = new ArrayList<PackageListItem>(elabList.size());
            for (Iterator elabIt = elabList.iterator(); elabIt.hasNext();) {
                Map elabData = (Map) elabIt.next();
                String idCombo = (String) elabData.get("id_combo");
                PackageListItem item = PackageListItem.parse(idCombo);
                items.add(item);
            }

            // Convert to list of maps for the action call
            List<Map<String, Long>> packageListData = PackageListItem.toKeyMaps(items);

            // Create the action(s)
            ActionChainManager.schedulePackageVerify(user, server, packageListData,
                    earliest, actionChain);
        }
    }
}
