/*
 * Copyright (c) 2014 SUSE LLC
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
/**
 * Copyright (c) 2014 Red Hat, Inc.
 */
package com.redhat.rhn.domain.action;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.ObjectNotFoundException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * Creates Action Chain related objects.
 * @author Silvio Moioli {@literal <smoioli@suse.de>}
 */
public class ActionChainFactory extends HibernateFactory {

    /** Logger instance */
    private static Logger log = LogManager.getLogger(ActionChainFactory.class);

    /** Singleton instance */
    private static ActionChainFactory singleton = new ActionChainFactory();

    /** Taskomatic API **/
    private static TaskomaticApi taskomaticApi = new TaskomaticApi();

    /**
     * Default constructor.
     */
    private ActionChainFactory() {
        super();
    }

    /**
     * Gets an action chain by label.
     * @param requestor the user whose chain we're looking for
     * @param label the label
     * @return the Action Chain or null if not found
     */
    public static ActionChain getActionChain(User requestor, String label) {
        log.debug("Looking up Action Chain with label {}", label);
        return singleton.lookupObjectByNamedQuery(
                "ActionChain.getActionChainByLabel",
                Map.of("user", requestor, "label", label)
        );
    }

    /**
     * Gets an Action Chain by id.
     * @param requestor the user whose chain we're looking for
     * @param id the id
     * @return the Action Chain
     * @throws ObjectNotFoundException if there is no such id accessible to the requestor
     */
    public static ActionChain getActionChain(User requestor, Long id)
    throws ObjectNotFoundException {
        log.debug("Looking up Action Chain with id {}", id);
        if (id == null) {
            return null;
        }
        ActionChain ac = singleton.lookupObjectByNamedQuery("ActionChain.getActionChain",
           Map.of("user", requestor, "id", id)
        );
        if (ac == null) {
            throw new ObjectNotFoundException(ActionChain.class,
                            "ActionChain Id " + id + " not found for User " + requestor.getLogin());
        }
        return ac;
    }

    /**
     * Returns an Action Chain given its ID
     * @param id the chain ID
     * @return an Action Chain or empty
     */
    public static Optional<ActionChain> getActionChain(long id) {
        return Optional.ofNullable(getSession().get(ActionChain.class, id));
    }

    /**
     * Gets an Action Chain Entry by id.
     * @param requestor the user whose entry we're looking for
     * @param id the action chain entry id
     * @return the Action Chain Entry
     * @throws ObjectNotFoundException if there is no such id accessible to the requestor
     */
    public static ActionChainEntry getActionChainEntry(User requestor, Long id) throws ObjectNotFoundException {
        if (id == null) {
            return null;
        }
        ActionChainEntry ace = getSession().load(ActionChainEntry.class, id);

        if (ace.getActionChain().getUser().getId().longValue() == requestor.getId().longValue()) {
            return ace;
        }
        throw new ObjectNotFoundException(ActionChainEntry.class,
            "ActionChainEntry Id " + id + " not found for User " + requestor.getLogin());
    }

    /**
     * Creates a new ActionChain object.
     * @param label the label
     * @param user the user
     * @return the action chain
     */
    public static ActionChain createActionChain(String label, User user) {
        log.debug("Creating Action Chain with label {}", label);
        ActionChain result = new ActionChain();
        result.setLabel(label);
        result.setUser(user);

        singleton.saveObject(result);
        return result;
    }

    /**
     * Looks for an action chain by label, and creates one if not found.
     * @param label the label
     * @param user the currently logged in user
     * @return the action chain
     */
    public static ActionChain getOrCreateActionChain(String label, User user) {
        ActionChain result = getActionChain(user, label);

        if (result != null) {
            return result;
        }

        return createActionChain(label, user);
    }

    /**
     * Creates a new entry in an Action Chain object, sort order will be
     * incremented.
     * @param action the action
     * @param actionChain the action chain
     * @param server the server
     * @return the action chain entry
     */
    public static ActionChainEntry queueActionChainEntry(Action action,
        ActionChain actionChain, Server server) {
        int nextSortOrder = getNextSortOrderValue(actionChain);
        return queueActionChainEntry(action, actionChain, server, nextSortOrder);
    }

    /**
     * Creates a new entry in an Action Chain object.
     * @param action the action
     * @param actionChain the action chain
     * @param server the server
     * @param sortOrder the required sort order
     * @return the action chain entry
     */
    public static ActionChainEntry queueActionChainEntry(Action action,
        ActionChain actionChain, Server server, int sortOrder) {
        log.debug("Queuing action {} to Action Chain {} with sort order {}", action, actionChain, sortOrder);
        ActionChainEntry result = new ActionChainEntry();

        result.setAction(action);
        result.setServer(server);
        result.setSortOrder(sortOrder);
        result.setActionChain(actionChain);
        actionChain.getEntries().add(result);
        actionChain.setModified(new Date());

        return result;
    }

    /**
     * Creates a new entry in an Action Chain object.
     * @param action the action
     * @param actionChain the action chain
     * @param serverId the server id
     * @param sortOrder the required sort order
     * @return the action chain entry
     */
    public static ActionChainEntry queueActionChainEntry(Action action,
        ActionChain actionChain, Long serverId, int sortOrder) {
        Server server = getSession().load(Server.class, serverId);
        return queueActionChainEntry(action, actionChain, server, sortOrder);
    }

    /**
     * Gets all action chains for a user that are not scheduled for execution.
     * @param requestor the user whose chains we're looking for
     * @return action chains
     */
    public static List<ActionChain> getActionChains(User requestor) {
        return singleton.listObjectsByNamedQuery("ActionChain.getActionChains", Map.of("user", requestor));
    }

    /**
     * Gets all action chains, by modification date.
     * @param requestor the user whose chain we're looking for
     * @return action chains
     */
    public static List<ActionChain> getActionChainsByModificationDate(User requestor) {
        return singleton.listObjectsByNamedQuery("ActionChain.getActionChainsByModificationDate",
                Map.of("user", requestor)
        );
    }

    /**
     * Returns ActionChainEntryGroupDto objects corresponding to groups of
     * Action Chain entries with the same sort order and action type.
     * @param actionChain an Action Chain
     * @return a list of corresponding groups
     */
    public static List<ActionChainEntryGroup> getActionChainEntryGroups(final ActionChain actionChain) {
        return singleton.listObjectsByNamedQuery("ActionChainEntry.getGroups", Map.of("id", actionChain.getId()));
    }

    /**
     * Returns entries from a chain having a certain sort order number
     * @param actionChain the chain
     * @param sortOrder the sort order
     * @return an entry list
     */
    public static List<ActionChainEntry> getActionChainEntries(final ActionChain actionChain, final Integer sortOrder) {
        return singleton.listObjectsByNamedQuery("ActionChainEntry.getActionChainEntries",
           Map.of("id", actionChain.getId(), "sortOrder", sortOrder)
        );
    }

    /**
     * Gets all action chains scheduled for given server
     * @param server the server
     * @return action chains
     */
    public static List<ActionChain> getActionChainsByServer(Server server) {
        return singleton.listObjectsByNamedQuery(
                "ActionChain.getActionChainsByServer", Map.of("id", server.getId()));
    }

    /**
     * Get the action chain scheduled for given action
     * @param action the action
     * @return action chains or empty when the action is not part of an action chain
     */
    public static Optional<ActionChain> getActionChainsByAction(Action action) {
        return getSession().createQuery(
                "SELECT DISTINCT ac " +
                        "FROM ActionChain ac " +
                        "JOIN ActionChainEntry ace" +
                        "  ON ace.actionChain = ac " +
                        "WHERE ace.action = :action", ActionChain.class)
                .setParameter("action", action)
                .uniqueResultOptional();
    }


    /**
     * Gets the next sort order value.
     * @param actionChain the action chain
     * @return the next sort order value
     */
    public static int getNextSortOrderValue(final ActionChain actionChain) {
        return singleton.lookupObjectByNamedQuery("ActionChain.getNextSortOrderValue",
            Map.of("id", actionChain.getId()));
    }

    /**
     * Deletes an Action Chain and all associated objects.
     * @param actionChain the action chain to delete
     */
    public static void delete(ActionChain actionChain) {
        log.debug("Deleting Action Chain {}", actionChain);
        singleton.removeObject(actionChain);
    }

    /**
     * Schedules an Action Chain for execution.
     * @param actionChain the action chain to execute
     * @param date first action's minimum timestamp
     * @throws TaskomaticApiException if there was a Taskomatic error
     */
    public static void schedule(ActionChain actionChain, Date date) throws TaskomaticApiException {
        log.debug("Scheduling Action Chain {} to date {}", actionChain, date);
        Map<Server, Action> latest = new HashMap<>();
        int maxSortOrder = getNextSortOrderValue(actionChain);
        Date dateInOrder = new Date(date.getTime());

        for (int sortOrder = 0; sortOrder < maxSortOrder; sortOrder++) {
            for (ActionChainEntry entry : getActionChainEntries(actionChain, sortOrder)) {
                Server server = entry.getServer();
                Action action = entry.getAction();

                log.debug("Scheduling Action {} to server {}", action, server);
                action.setPrerequisite(latest.get(server));
                action.setEarliestAction(dateInOrder);
                ActionFactory.addServerToAction(server.getId(), action);

                // Increment 'earliest' time by a millisecond for each chain action in
                // order to sort them correctly for display
                dateInOrder = DateUtils.addMilliseconds(dateInOrder, 1);
                latest.put(server, action);
            }
        }

        // Trigger Action Chain execution for Minions via Taskomatic
        taskomaticApi.scheduleActionChainExecution(actionChain);
        log.debug("Action Chain {} scheduled to date {}", actionChain, date);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Remove gaps from entries in an action chain
     *
     * @param actionChain An ActionChain from which to be removed.
     * @param removedOrder sort order of the (already) removed entry
     */
    public static void removeActionChainEntrySortGaps(ActionChain actionChain, int removedOrder) {
        Set<ActionChainEntry> entries = actionChain.getEntries();

        for (ActionChainEntry entry : entries) {
            if (entry.getSortOrder() == removedOrder) {
                return;
            }
        }

        for (ActionChainEntry entry : entries) {
            if (entry.getSortOrder() > removedOrder) {
                entry.setSortOrder(entry.getSortOrder() - 1);
            }
        }
    }

    /**
     * Remove an entry from the action chain
     *
     * @param actionChain An ActionChain from which to be removed.
     * @param entry entry to remove
     */
    public static void removeActionChainEntry(ActionChain actionChain,
            ActionChainEntry entry) {
        actionChain.getEntries().remove(entry);
        removeActionChainEntrySortGaps(actionChain, entry.getSortOrder());
    }

    /**
     * Set the TaskomatiApi for unit tests.
     * @param taskomaticApiIn the TaskomatiApi to set
     */
    public static void setTaskomaticApi(TaskomaticApi taskomaticApiIn) {
        ActionChainFactory.taskomaticApi = taskomaticApiIn;
    }

    /**
     * Check if an Action Chain contains any minion as target
     * @param actionChain the action chain
     * @return if the action chains contains any minion
     */
    public static boolean isActionChainTargettingMinions(final ActionChain actionChain) {
        return ((Long)singleton.lookupObjectByNamedQuery("ActionChain.countMinionsInActionChain",
            Map.of("actionchain_id", actionChain.getId())
        ) > 0);
    }

    /**
     * Return active actions of an action chain. Always the first action of every server
     * @param actionChainIn the action chain
     * @return returns a list of actions which are still active
     */
    public static List<Action> getActiveActionsForChain(ActionChain actionChainIn) {
        return getSession().createQuery(
                "SELECT sa.parentAction " +
                        "FROM ActionChainEntry entry " +
                        "JOIN ServerAction sa ON sa.server = entry.server AND sa.parentAction = entry.action " +
                        "WHERE entry.actionChain = :actionChain " +
                        "AND entry.sortOrder = 0", Action.class)
                .setParameter("actionChain", actionChainIn)
                .list();
    }
}
