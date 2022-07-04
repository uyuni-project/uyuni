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

package com.redhat.rhn.frontend.xmlrpc.chain;

import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainEntry;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchActionException;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;

import com.suse.manager.api.ReadOnly;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @apidoc.namespace actionchain
 * @apidoc.doc Provides the namespace for the Action Chain methods.
 */
public class ActionChainHandler extends BaseHandler {

    private static Logger log = LogManager.getLogger(ActionChainHandler.class);

    private final ActionChainRPCCommon acUtil;

    /**
     * Parameters collector.
     */
    public ActionChainHandler() {
        this.acUtil = new ActionChainRPCCommon();
    }

    /**
     * List currently available action chains.
     *
     * @param loggedInUser The current user
     * @return list of action chains.
     *
     * @apidoc.doc List currently available action chains.
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_array_begin()
     *                      #struct_begin("chain")
     *                        #prop_desc("string", "label", "Label of an Action Chain")
     *                        #prop_desc("string", "entrycount",
     *                                   "Number of entries in the Action Chain")
     *                      #struct_end()
     *                    #array_end()
     */
    @ReadOnly
    public List<Map<String, Object>> listChains(User loggedInUser) {
        List<Map<String, Object>> chains = new ArrayList<>();
        for (ActionChain actionChain : ActionChainFactory.getActionChains(loggedInUser)) {
            Map<String, Object> info = new HashMap<>();
            info.put("label", actionChain.getLabel());
            info.put("entrycount", actionChain.getEntries().size());
            chains.add(info);
        }

        return chains;
    }

    /**
     * List all actions in the particular Action Chain.
     *
     * @param loggedInUser The current user
     * @param chainLabel The label of the Action Chain.
     * @return List of entries in the particular action chain, if any.
     *
     * @apidoc.doc List all actions in the particular Action Chain.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "chainLabel", "Label of the chain")
     * @apidoc.returntype #return_array_begin()
     *                      #struct_begin("entry")
     *                        #prop_desc("int", "id", "Action ID")
     *                        #prop_desc("string", "label", "Label of an Action")
     *                        #prop_desc("string", "created", "Created date/time")
     *                        #prop_desc("string", "earliest",
     *                                   "Earliest scheduled date/time")
     *                        #prop_desc("string", "type", "Type of the action")
     *                        #prop_desc("string", "modified", "Modified date/time")
     *                        #prop_desc("string", "cuid", "Creator UID")
     *                      #struct_end()
     *                    #array_end()
     */
    @ReadOnly
    public List<Map<String, Object>> listChainActions(User loggedInUser,
                                                      String chainLabel) {
        List<Map<String, Object>> entries = new ArrayList<>();
        ActionChain chain = this.acUtil.getActionChainByLabel(loggedInUser, chainLabel);

        if (chain.getEntries() != null && !chain.getEntries().isEmpty()) {
            for (ActionChainEntry entry : chain.getEntries()) {
                String label = entry.getAction().getName();
                Map<String, Object> info = new HashMap<>();
                info.put("id", entry.getAction().getId());
                info.put("label", StringUtil.nullOrValue(label) == null ?
                                  entry.getAction().getActionType().getName() :
                                  label);
                info.put("created", entry.getAction().getCreated());
                info.put("earliest", entry.getAction().getEarliestAction());
                info.put("type", entry.getAction().getActionType().getName());
                info.put("modified", entry.getAction().getModified());
                info.put("cuid", entry.getAction().getSchedulerUser().getLogin());
                entries.add(info);
            }
        }

        return entries;
    }

    /**
     * Remove an action from the Action Chain.
     *
     * @param loggedInUser The current user
     * @param chainLabel The label of the Action Chain.
     * @param actionId Action ID.
     * @return 1 if successful, exception otherwise
     *
     * @apidoc.doc Remove an action from an Action Chain.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "chainLabel", "Label of the chain")
     * @apidoc.param #param_desc("int", "actionId", "Action ID")
     * @apidoc.returntype #return_int_success()
     */
    public Integer removeAction(User loggedInUser,
                                String chainLabel,
                                Integer actionId) {
        ActionChain chain = this.acUtil.getActionChainByLabel(loggedInUser, chainLabel);

        for (ActionChainEntry entry : chain.getEntries()) {
            if (entry.getAction().getId().equals(Long.valueOf(actionId))) {
                ActionChainFactory.removeActionChainEntry(chain, entry);
                return BaseHandler.VALID;
            }
        }

        throw new NoSuchActionException("ID: " + actionId);
    }

    /**
     * Remove Action Chains by label.
     *
     * @param loggedInUser The current user
     * @param chainLabel Action Chain label.
     * @return 1 if successful, exception otherwise
     *
     * @apidoc.doc Delete action chain by label.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "chainLabel", "Label of the chain")
     * @apidoc.returntype #return_int_success()
     */
    public Integer deleteChain(User loggedInUser, String chainLabel) {
        ActionChainFactory.delete(
                        this.acUtil.getActionChainByLabel(loggedInUser, chainLabel));

        return BaseHandler.VALID;
    }

    /**
     * Create an Action Chain.
     *
     * @param loggedInUser The current user
     * @param chainLabel Label of the action chain
     * @return id of the created action chain
     *
     * @apidoc.doc Create an Action Chain.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "chainLabel", "Label of the chain")
     * @apidoc.returntype #param_desc("int", "actionId", "The ID of the created action chain")
     */
    public Integer createChain(User loggedInUser,
                                     String chainLabel) {
        if (StringUtil.nullOrValue(chainLabel) == null) {
            throw new InvalidParameterException("Chain label is missing");
        }

        if (ActionChainFactory.getActionChain(loggedInUser, chainLabel) != null) {
            throw new InvalidParameterException(
                    "Another Action Chain with the same label already exists");
        }

        return ActionChainFactory.createActionChain(
                chainLabel, loggedInUser).getId().intValue();
    }

    /**
     * Schedule system reboot.
     *
     * @param loggedInUser The current user
     * @param sid Server ID.
     * @param chainLabel Label of the action chain
     * @return list of action ids, exception thrown otherwise
     *
     * @apidoc.doc Add system reboot to an Action Chain.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "System ID")
     * @apidoc.param #param_desc("string", "chainLabel", "Label of the chain")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public Integer addSystemReboot(User loggedInUser,
                                   Integer sid,
                                   String chainLabel) {

        Server server = this.acUtil.getServerById(sid, loggedInUser);

        try {
            return ActionChainManager.scheduleRebootAction(
                    loggedInUser, server,
                    new Date(), this.acUtil.getActionChainByLabel(loggedInUser, chainLabel)
            ).getId().intValue();
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Schedule Errata update.
     *
     * @param loggedInUser The current user
     * @param sids a list of Server IDs
     * @param errataIds a list of erratas IDs
     * @param chainLabel Label of the action chain
     * @return action id if successful, exception otherwise
     *
     * @apidoc.doc Adds Errata update to an Action Chain.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single_desc("int", "sids", "System IDs")
     * @apidoc.param #array_single_desc("int", "errataIds", "Errata ID")
     * @apidoc.param #param_desc("string", "chainLabel", "Label of the chain")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action")
     */
    public Integer addErrataUpdate(User loggedInUser,
                                    List<Integer> sids,
                                    List<Integer> errataIds,
                                    String chainLabel) {
        if (errataIds.isEmpty()) {
            throw new InvalidParameterException("No specified Erratas.");
        }

        List<Long> actionIds = null;
        try {
            List<Server> serverIds = sids.stream().map(sid ->
                    this.acUtil.getServerById(sid, loggedInUser)).collect(Collectors.toList());

            actionIds = ActionChainManager.scheduleErrataUpdate(loggedInUser,
                    serverIds,
                    errataIds, new Date(), this.acUtil.getActionChainByLabel(loggedInUser, chainLabel));
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            // this err should never be thrown
            log.error("Taskomatic exception", e);
            throw new TaskomaticApiException("Error scheduling staging jobs with Taskomatic.");
        }

        if (actionIds.isEmpty()) {
            throw new NoSuchActionException("No actions created for applying errata.");
        }

        return actionIds.get(0).intValue();
    }


    /**
     * Adds an action to remove installed packages on the system.
     *
     * @param loggedInUser The current user
     * @param sid System ID
     * @param packageIds List of packages
     * @param chainLabel Label of the action chain
     * @return list of action ids, exception thrown otherwise
     *
     * @apidoc.doc Adds an action to remove installed packages on the system to an Action
     * Chain.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "System ID")
     * @apidoc.param #array_single("int", "packageIds")
     * @apidoc.param #param_desc("string", "chainLabel", "Label of the chain")
     * @apidoc.returntype #param_desc("int", "actionId", "The action id of the scheduled action or exception")
     */
    public Integer addPackageRemoval(User loggedInUser,
                                     Integer sid,
                                     List<Integer> packageIds,
                                     String chainLabel) {
        if (packageIds.isEmpty()) {
            throw new InvalidParameterException("No specified packages.");
        }

        Server server = this.acUtil.getServerById(sid, loggedInUser);

        try {
            return ActionChainManager
                    .schedulePackageRemoval(loggedInUser, server,
                            this.acUtil.resolvePackages(packageIds, loggedInUser), new Date(),
                            this.acUtil.getActionChainByLabel(loggedInUser, chainLabel))
                    .getId().intValue();
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Schedule package installation to an Action Chain.
     *
     * @param loggedInUser The current user
     * @param sid System ID.
     * @param packageIds List of packages.
     * @param chainLabel Label of the Action Chain.
     * @return True or false in XML-RPC representation: 1 or 0 respectively.
     *
     * @apidoc.doc Adds package installation action to an Action Chain.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "System ID")
     * @apidoc.param #array_single("int", "packageIds")
     * @apidoc.param #param("string", "chainLabel")
     * @apidoc.returntype #return_int_success()
     */
    public Integer addPackageInstall(User loggedInUser,
                                     Integer sid,
                                     List<Integer> packageIds,
                                     String chainLabel) {
        if (packageIds.isEmpty()) {
            throw new InvalidParameterException("No specified packages.");
        }

        Server server = this.acUtil.getServerById(sid, loggedInUser);

        try {
            return ActionChainManager
                    .schedulePackageInstall(loggedInUser, server,
                            this.acUtil.resolvePackages(packageIds, loggedInUser), new Date(),
                            this.acUtil.getActionChainByLabel(loggedInUser, chainLabel))
                    .getId().intValue();
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Adds an action to verify installed packages on the system.
     *
     * @param loggedInUser The current user
     * @param sid System ID
     * @param packageIds List of packages
     * @param chainLabel Label of the action chain
     * @return True or false in XML-RPC representation (1 or 0 respectively)
     *
     * @apidoc.doc Adds an action to verify installed packages on the system to an Action
     * Chain.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "System ID")
     * @apidoc.param #array_single("int", "packageIds")
     * @apidoc.param #param_desc("string", "chainLabel", "Label of the chain")
     * @apidoc.returntype #return_int_success()
     */
    public Integer addPackageVerify(User loggedInUser,
                                    Integer sid,
                                    List<Integer> packageIds,
                                    String chainLabel) {
        if (packageIds.isEmpty()) {
            throw new InvalidParameterException("No specified packages.");
        }

        Server server = this.acUtil.getServerById(sid, loggedInUser);
        this.acUtil.ensureNotSalt(server);

        try {
            return ActionChainManager
                    .schedulePackageVerify(loggedInUser, server,
                            this.acUtil.resolvePackages(packageIds, loggedInUser), new Date(),
                            this.acUtil.getActionChainByLabel(loggedInUser, chainLabel))
                    .getId().intValue();
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Adds an action to upgrade installed packages on the system.
     *
     * @param loggedInUser The current user
     * @param sid System ID
     * @param packageIds List of packages
     * @param chainLabel Label of the action chain
     * @return True or false in XML-RPC representation (1 or 0 respectively)
     *
     * @apidoc.doc Adds an action to upgrade installed packages on the system to an Action
     * Chain.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "System ID")
     * @apidoc.param #array_single("int", "packageIds")
     * @apidoc.param #param_desc("string", "chainLabel", "Label of the chain")
     * @apidoc.returntype #param_desc("int", "actionId", "The id of the action or throw an exception")
     */
    public int addPackageUpgrade(User loggedInUser,
                                 Integer sid,
                                 List<Integer> packageIds,
                                 String chainLabel) {
        if (packageIds.isEmpty()) {
            throw new InvalidParameterException("No specified packages.");
        }

        Server server = this.acUtil.getServerById(sid, loggedInUser);

        try {
            return ActionChainManager
                    .schedulePackageUpgrade(loggedInUser, server,
                            this.acUtil.resolvePackages(packageIds, loggedInUser), new Date(),
                            this.acUtil.getActionChainByLabel(loggedInUser, chainLabel))
                    .getId().intValue();
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Add a remote command with label as a script.
     *
     * @param loggedInUser The current user
     * @param sid System ID
     * @param chainLabel Label of the action chain.
     * @param scriptLabel Description/label for script
     * @param uid User ID on the remote system.
     * @param scriptBody Base64 encoded script.
     * @param gid Group ID on the remote system.
     * @param timeout Timeout
     * @return True or false in XML-RPC representation (1 or 0 respectively)
     *
     * @apidoc.doc Add an action with label to run a script to an Action Chain.
     * NOTE: The script body must be Base64 encoded!
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "System ID")
     * @apidoc.param #param_desc("string", "chainLabel", "Label of the chain")
     * @apidoc.param #param_desc("string", "scriptLabel", "Label of the script")
     * @apidoc.param #param_desc("string", "uid", "User ID on the particular system")
     * @apidoc.param #param_desc("string", "gid", "Group ID on the particular system")
     * @apidoc.param #param_desc("int", "timeout", "Timeout")
     * @apidoc.param #param_desc("string", "scriptBody", "Base64 encoded script body")
     * @apidoc.returntype #param_desc("int", "actionId", "The id of the action or throw an exception")
     */
    public Integer addScriptRun(User loggedInUser, Integer sid, String chainLabel,
            String scriptLabel, String uid, String gid,
            Integer timeout, String scriptBody) {
        List<Long> systems = new ArrayList<>();
        systems.add((long) sid);

        ScriptActionDetails script = ActionManager.createScript(
                uid, gid, (long) timeout, new String(
                        Base64.getDecoder().decode(scriptBody)));
        try {
            return ActionChainManager
                    .scheduleScriptRuns(loggedInUser, systems,
                            scriptLabel, script, new Date(),
                            this.acUtil.getActionChainByLabel(loggedInUser, chainLabel))
                    .iterator().next().getId().intValue();
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Add a remote command as a script.
     *
     * @param loggedInUser The current user
     * @param sid System ID
     * @param chainLabel Label of the action chain.
     * @param uid User ID on the remote system.
     * @param scriptBody Base64 encoded script.
     * @param gid Group ID on the remote system.
     * @param timeout Timeout
     * @return True or false in XML-RPC representation (1 or 0 respectively)
     *
     * @apidoc.doc Add an action to run a script to an Action Chain.
     * NOTE: The script body must be Base64 encoded!
     *
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "sid", "System ID")
     * @apidoc.param #param_desc("string", "chainLabel", "Label of the chain")
     * @apidoc.param #param_desc("string", "uid", "User ID on the particular system")
     * @apidoc.param #param_desc("string", "gid", "Group ID on the particular system")
     * @apidoc.param #param_desc("int", "timeout", "Timeout")
     * @apidoc.param #param_desc("string", "scriptBody", "Base64 encoded script body")
     * @apidoc.returntype #param_desc("int", "actionId", "The id of the action or throw an exception")
     */
    public Integer addScriptRun(User loggedInUser, Integer sid, String chainLabel,
            String uid, String gid, Integer timeout, String scriptBody) {

        return addScriptRun(
                loggedInUser, sid, chainLabel, null, uid, gid, timeout, scriptBody);
    }

    /**
     * Schedule action chain.
     *
     * @param loggedInUser The current user
     * @param chainLabel Label of the action chain
     * @param date Earliest date
     * @return 1 if successful, exception otherwise
     *
     * @apidoc.doc Schedule the Action Chain so that its actions will actually occur.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "chainLabel", "Label of the chain")
     * @apidoc.param #param_desc("$date", "date", "Earliest date")
     * @apidoc.returntype #return_int_success()
     */
    public Integer scheduleChain(User loggedInUser, String chainLabel, Date date) {
        try {
            ActionChainFactory.schedule(
                            this.acUtil.getActionChainByLabel(loggedInUser, chainLabel), date);

            return BaseHandler.VALID;
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Deploy configuration.
     *
     * @param loggedInUser The current user
     * @param chainLabel Label of the action chain
     * @param sid System ID
     * @param revisionSpecifiers List of maps specifying a revision
     * @return 1 if successful, exception otherwise
     *
     * @apidoc.doc Adds an action to deploy a configuration file to an Action Chain.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "chainLabel", "Label of the chain")
     * @apidoc.param #param_desc("int", "sid", "System ID")
     * @apidoc.param #array_begin("revisionSpecifiers")
     *                   #struct_begin("config revision specifier")
     *                       #prop_desc("string", "channelLabel", "Channel label")
     *                       #prop_desc("string", "filePath",
     *                                  "Path of the configuration file")
     *                       #prop_desc("int", "revision", "Revision number")
     *                   #struct_end()
     *               #array_end()
     * @apidoc.returntype #return_int_success()
     */
    public Integer addConfigurationDeployment(User loggedInUser,
            String chainLabel,
            Integer sid,
            List<Map<String, Object>> revisionSpecifiers) {
        if (revisionSpecifiers.isEmpty()) {
            throw new InvalidParameterException("At least one revision should be given.");
        }

        Set<String> validKeys = new HashSet<>();
        validKeys.add("channelLabel");
        validKeys.add("filePath");
        validKeys.add("revision");
        revisionSpecifiers.stream().forEach(specifier -> validateMap(validKeys, specifier));

        List<Long> server = new ArrayList<>();
        server.add(sid.longValue());

        ConfigChannelHandler configChannelHandler = new ConfigChannelHandler();
        List<Long> revisionIds = revisionSpecifiers.stream().map(specifier ->
                    configChannelHandler.lookupFileInfo(loggedInUser,
                            (String) specifier.get("channelLabel"),
                            (String) specifier.get("filePath"),
                            (Integer) specifier.get("revision")).getId()
        ).collect(Collectors.toList());

        try {
            ActionChainManager.createConfigActions(loggedInUser, revisionIds, server,
                    ActionFactory.TYPE_CONFIGFILES_DEPLOY, new Date(),
                    this.acUtil.getActionChainByLabel(loggedInUser, chainLabel));
            return BaseHandler.VALID;
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Rename Action Chain.
     *
     * @param loggedInUser The current user
     * @param previousLabel Previous (existing) label of the Action Chain
     * @param newLabel New (desired) label of the Action Chain
     * @return 1 if successful, exception otherwise
     *
     * @apidoc.doc Rename an Action Chain.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "previousLabel", "Previous chain label")
     * @apidoc.param #param_desc("string", "newLabel", "New chain label")
     * @apidoc.returntype #return_int_success()
     */
    public Integer renameChain(User loggedInUser,
                               String previousLabel,
                               String newLabel) {
        if (previousLabel.equals(newLabel)) {
            throw new InvalidParameterException("New label of the Action Chain should " +
                    "not be the same as previous!");
        }
        else if (previousLabel.isEmpty()) {
            throw new InvalidParameterException("Previous label cannot be empty.");
        }
        else if (newLabel.isEmpty()) {
            throw new InvalidParameterException("New label cannot be empty.");
        }

        if (ActionChainFactory.getActionChain(loggedInUser, newLabel) != null) {
            throw new InvalidParameterException(
                    "Another Action Chain with the same label already exists");
        }

        this.acUtil.getActionChainByLabel(loggedInUser, previousLabel).setLabel(newLabel);

        return BaseHandler.VALID;
    }
}
