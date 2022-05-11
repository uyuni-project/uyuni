/**
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.configchannel;

import static java.util.Optional.empty;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigChannelType;
import com.redhat.rhn.domain.config.ConfigFile;
import com.redhat.rhn.domain.config.ConfigFileType;
import com.redhat.rhn.domain.config.ConfigInfo;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.config.ConfigurationFactory;
import com.redhat.rhn.domain.config.EncodedConfigRevision;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ConfigChannelDto;
import com.redhat.rhn.frontend.dto.ConfigFileDto;
import com.redhat.rhn.frontend.dto.ConfigRevisionDto;
import com.redhat.rhn.frontend.dto.ConfigSystemDto;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.ConfigFileErrorException;
import com.redhat.rhn.frontend.xmlrpc.InvalidOperationException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchConfigFilePathException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchConfigRevisionException;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.frontend.xmlrpc.serializer.ConfigRevisionSerializer;
import com.redhat.rhn.manager.MissingCapabilityException;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.configuration.ConfigChannelCreationHelper;
import com.redhat.rhn.manager.configuration.ConfigFileBuilder;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.manager.configuration.file.SLSFileData;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.webui.services.ConfigChannelSaltManager;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ConfigHandler
 * @xmlrpc.namespace configchannel
 * @xmlrpc.doc Provides methods to access and modify many aspects of
 * configuration channels.
 */
public class ConfigChannelHandler extends BaseHandler {

    /**
     * Creates a new 'normal' config channel based on the values provided..
     * @param loggedInUser The current user
     * @param label label of the config channel
     * @param name name of the config channel
     * @param description description of the config channel
     * @return the newly created config channel
     *
     * @xmlrpc.doc Create a new global config channel. Caller must be at least a
     * config admin or an organization admin.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.param #param("string", "name")
     * @xmlrpc.param #param("string", "description")
     * @xmlrpc.returntype
     * $ConfigChannelSerializer
     */
    public ConfigChannel create(User loggedInUser, String label,
                                            String name,
                                            String description) {
       return create(loggedInUser, label, name, description, ConfigChannelType.normal().getLabel());
    }
    /**
     * Creates a new global config channel based on the values provided..
     * @param user The current user
     * @param label label of the config channel
     * @param name name of the config channel
     * @param description description of the config channel
     * @param type type of config channel(possible values are 'state', 'normal')
     * @return the newly created config channel
     *
     * @xmlrpc.doc Create a new global config channel. Caller must be at least a
     * config admin or an organization admin.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.param #param("string", "name")
     * @xmlrpc.param #param("string", "description")
     * @xmlrpc.param #param_desc("string", "type", "the channel type either 'normal' or 'state'")
     * @xmlrpc.returntype
     * $ConfigChannelSerializer
     */
    public ConfigChannel create(User user, String label, String name, String description, String type) {
        return create(user, label, name, description, type, new HashMap<>());
    }

    /**
     * Creates a new global config channel based on the values provided..
     * @param user The current user
     * @param label label of the config channel
     * @param name name of the config channel
     * @param description description of the config channel
     * @param type type of config channel(possible values are 'state', 'normal')
     * @param pathInfo a map containing 'content' and 'contents_enc64' (only applicable for channelType 'state')
     * @return the newly created config channel
     *
     * @xmlrpc.doc Create a new global config channel. Caller must be at least a
     * config admin or an organization admin.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.param #param("string", "name")
     * @xmlrpc.param #param("string", "description")
     * @xmlrpc.param #param_desc("string", "type", "the channel type either 'normal' or 'state'")
     * @xmlrpc.param
     *  #struct_desc("pathInfo", "the path info")
     *      #prop_desc("string", "contents", "contents of the init.sls file")
     *      #prop_desc("boolean", "contents_enc64", "identifies base64 encoded content(default: disabled)")
     *  #struct_end()
     * @xmlrpc.returntype
     * $ConfigChannelSerializer
     */
    public ConfigChannel create(User user, String label, String name, String description, String type,
                                Map<String, Object> pathInfo) {
        ensureConfigAdmin(user);

        ConfigChannelCreationHelper helper = new ConfigChannelCreationHelper();
        XmlRpcConfigChannelHelper ccHelper = XmlRpcConfigChannelHelper.getInstance();
        try {
            helper.validate(label, name, description);
            ConfigChannelType ct = helper.getGlobalChannelType(type);
            ConfigChannel cc = helper.create(user, ct);
            helper.update(cc, name, label, description);
            ConfigurationManager.getInstance().save(cc, empty());
            cc  = HibernateFactory.reload(cc);
            String contents = "";
            if (pathInfo.containsKey(ConfigRevisionSerializer.CONTENTS)) {
                try {
                    contents = ccHelper.getContents(pathInfo);
                }
                catch (UnsupportedEncodingException e) {
                    throw new ConfigFileErrorException(e.getMessage());
                }
            }
            helper.createInitSlsFile(user, cc, contents);
            return cc;
        }
        catch (ValidatorException ve) {
            String msg = "Exception encountered during channel creation.\n" +
                    ve.getMessage();
            throw new FaultException(1021, "ConfigChannelCreationException", msg);
        }

    }

    /**
     *  Delete specified revisions of a given configuration file
     *  @param loggedInUser The current user
     *  @param label Config channel label.
     *  @param filePath The configuration file path.
     *  @param revisions List of configuration file revisions to delete.
     *  @return 1 if deletion succeeds, errors out otherwise.
     *
     * @xmlrpc.doc Delete specified revisions of a given configuration file
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label",
     *                          "label of config channel to lookup on")
     * @xmlrpc.param #param_desc("string", "filePath",
     *                          "configuration file path")
     * @xmlrpc.param #array_single_desc("int", "revisions", "list of revisions to delete")
     * @xmlrpc.returntype #return_int_success()
     */
    public int deleteFileRevisions(User loggedInUser, String label,
                                   String filePath, List<Integer> revisions) {
        XmlRpcConfigChannelHelper configHelper = XmlRpcConfigChannelHelper.getInstance();
        ConfigChannel cc = configHelper.lookupGlobal(loggedInUser, label);
        ConfigurationManager cm = ConfigurationManager.getInstance();
        ConfigFile cf = cm.lookupConfigFile(loggedInUser, cc.getId(), filePath);
        if (cf == null) {
            throw new NoSuchConfigFilePathException(filePath, label);
        }
        for (Integer revId : revisions) {
            ConfigRevision cr = cm.lookupConfigRevisionByRevId(loggedInUser, cf,
                revId.longValue());
            cm.deleteConfigRevision(loggedInUser, cr);
        }

        return 1;
    }

    /**
     * Get list of revisions for specified config file
     * @param loggedInUser The current user
     * @param label Config channel label.
     * @param filePath The configuration file path.
     * @return List of revisions of the configuration file, errors out otherwise.
     *
     * @xmlrpc.doc Get list of revisions for specified config file
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label",
     *                          "label of config channel to lookup on")
     * @xmlrpc.param #param_desc("string", "filePath",
     *                          "config file path to examine")
     * @xmlrpc.returntype
     * #return_array_begin()
     * $ConfigRevisionSerializer
     * #array_end()
     */
    public List getFileRevisions(User loggedInUser, String label, String filePath) {
        XmlRpcConfigChannelHelper configHelper = XmlRpcConfigChannelHelper.getInstance();
        ConfigChannel cc = configHelper.lookupGlobal(loggedInUser, label);
        ConfigurationManager cm = ConfigurationManager.getInstance();
        ConfigFile cf = cm.lookupConfigFile(loggedInUser, cc.getId(), filePath);

        if (cf == null) {
            throw new FaultException(1022, "InvalidConfigFileException",
                "Could not find configuration file with filePath=: " + filePath);
        }

        return cm.lookupConfigRevisions(cf);
    }

    /**
     * Get revision for specified config file
     * @param loggedInUser The current user
     * @param label Config channel label.
     * @param filePath The configuration file path.
     * @param revision The configuration file revision.
     * @return Revisions of the configuration file, errors out otherwise.
     *
     * @xmlrpc.doc Get revision of the specified config file
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label",
     *                          "label of config channel to lookup on")
     * @xmlrpc.param #param_desc("string", "filePath", "config file path to examine")
     * @xmlrpc.param #param_desc("int", "revision", "config file revision to examine")
     * @xmlrpc.returntype
     * $ConfigRevisionSerializer
     */
    public ConfigRevision getFileRevision(User loggedInUser, String label, String filePath, Integer revision) {
        XmlRpcConfigChannelHelper configHelper = XmlRpcConfigChannelHelper.getInstance();
        ConfigChannel cc = configHelper.lookupGlobal(loggedInUser, label);
        ConfigurationManager cm = ConfigurationManager.getInstance();
        ConfigFile cf = cm.lookupConfigFile(loggedInUser, cc.getId(), filePath);

        if (cf == null) {
            throw new FaultException(1022, "InvalidConfigFileException",
                "Could not find configuration file with filePath=: " + filePath);
        }

        return cm.lookupConfigRevisionByRevId(loggedInUser, cf, revision.longValue());
    }

    /**
     * Synchronize all files on the disk to the current state of the database.
     * @param loggedInUser The current user
     * @param labels the list of global channels to synchronize files from.
     * @return 1 if successful with the operation, errors out otherwise.
     *
     * @xmlrpc.doc Synchronize all files on the disk to the current state of the database.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param
     * #array_single_desc("string", "labels", "configuration channel labels to synchronize files from")
     * @xmlrpc.returntype #return_int_success()
     */
    public int syncSaltFilesOnDisk(User loggedInUser, List<String> labels) {
        var manager = ConfigurationManager.getInstance();
        var saltManager = ConfigChannelSaltManager.getInstance();
        for (var label : labels) {
            try {
                var channel = manager.lookupGlobalConfigChannel(loggedInUser, label);
                saltManager.generateConfigChannelFiles(channel);
            }
            catch (Exception e) {
                throw new ConfigFileErrorException(e.getMessage());
            }
        }
        return 1;
    }

    /**
     * Get base64 encoded revision for specified config file
     * @param loggedInUser The current user
     * @param label Config channel label.
     * @param filePath The configuration file path.
     * @param revision The configuration file revision.
     * @return Revisions of the configuration file, errors out otherwise.
     *
     * @xmlrpc.doc Get revision of the specified configuration file and transmit the
     *             contents as base64 encoded.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "label of config channel to lookup on")
     * @xmlrpc.param #param_desc("string", "filePath", "config file path to examine")
     * @xmlrpc.param #param_desc("int", "revision", "config file revision to examine")
     * @xmlrpc.returntype
     * $ConfigRevisionSerializer
     */
    public EncodedConfigRevision getEncodedFileRevision(User loggedInUser,
            String label, String filePath, Integer revision) {
         return new EncodedConfigRevision(getFileRevision(loggedInUser, label,
                 filePath, revision));
    }

    /**
     * Return a struct of config channel details.
     * @param loggedInUser The current user
     * @param label Config channel label.
     * @return the Config channel details
     *
     * @xmlrpc.doc Lookup config channel details.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.returntype
     *   $ConfigChannelSerializer
     */
    public ConfigChannel getDetails(User loggedInUser, String label) {
        XmlRpcConfigChannelHelper helper = XmlRpcConfigChannelHelper.getInstance();
        return helper.lookupGlobal(loggedInUser, label);
    }

    /**
     * Return a struct of config channel details.
     * @param loggedInUser The current user
     * @param id Config channel ID.
     * @return the Config channel details
     *
     * @xmlrpc.doc Lookup config channel details.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("int", "id", "the channel ID")
     * @xmlrpc.returntype
     *    $ConfigChannelSerializer
     */
    public ConfigChannel getDetails(User loggedInUser, Integer id) {
        ConfigurationManager manager = ConfigurationManager.getInstance();
        return manager.lookupConfigChannel(loggedInUser, id.longValue());
    }

    /**
     *Updates a global config channel based on the values provided..
     * @param loggedInUser The current user
     * @param label label of the config channel
     * @param name name of the config channel
     * @param description description of the config channel
     * @return the newly created config channel
     *
     * @xmlrpc.doc Update a global config channel. Caller must be at least a
     * config admin or an organization admin, or have access to a system containing this
     * config channel.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.param #param("string", "name")
     * @xmlrpc.param #param("string", "description")
     * @xmlrpc.returntype
     * $ConfigChannelSerializer
     */
    public ConfigChannel update(User loggedInUser, String label, String name, String description) {
        XmlRpcConfigChannelHelper helper = XmlRpcConfigChannelHelper.getInstance();
        ConfigChannel cc = helper.lookupGlobal(loggedInUser, label);

        ConfigChannelCreationHelper cchelper = new ConfigChannelCreationHelper();

        try {
            cchelper.validate(label, name, description);
            cc.setName(name);
            cc.setDescription(description);
            // we don't update label here
            ConfigurationManager.getInstance().save(cc, empty());
            return cc;
        }
        catch (ValidatorException ve) {
            String msg = "Exception encountered during channel creation.\n" +
                            ve.getMessage();
            throw new FaultException(1021, "ConfigChannelCreationException", msg);
        }
    }

    /**
     * Lists details on a list of channels given their channel labels.
     * @param loggedInUser The current user
     * @param labels the list of channel labels to lookup on
     * @return a list of config channels.
     *
     * @xmlrpc.doc Lists details on a list of channels given their channel labels.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param
     * #array_single_desc("string", "labels", "the channel labels")
     * @xmlrpc.returntype
     * #return_array_begin()
     *  $ConfigChannelSerializer
     * #array_end()
     */
    public List<ConfigChannel> lookupChannelInfo(User loggedInUser,
                                                    List<String> labels) {
        XmlRpcConfigChannelHelper helper = XmlRpcConfigChannelHelper.getInstance();
        return helper.lookupGlobals(loggedInUser, labels);
    }

    /**
     * List all the global channels accessible to the logged-in user
     * @param loggedInUser The current user
     * @return a list of accessible global config channels
     *
     * @xmlrpc.doc List all the global config channels accessible to the logged-in user.
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     * #return_array_begin()
     *  $ConfigChannelDtoSerializer
     * #array_end()
     */
    public List<ConfigChannelDto> listGlobals(User loggedInUser) {
        ConfigurationManager manager = ConfigurationManager.getInstance();
        DataResult<ConfigChannelDto> list = manager.
                                    listGlobalChannels(loggedInUser, null);
        list.elaborate(list.getElaborationParams());
        return  list;
    }
    /**
     * Update the init.sls file for the given state channel with the given contents.
     * @param user The current user
     * @param label the label of the config channel.
     * @param pathInfo a map containing 'content', 'contents_enc64' and 'revision'
     * @return returns the updated config revision..
     *
     * @xmlrpc.doc Update the init.sls file for the given state channel. User can only update contents, nothing else.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "the channel label")
     * @xmlrpc.param
     *  #struct_begin("pathInfo", "the path info")
     *      #prop_desc("string","contents", "contents of the init.sls file")
     *      #prop_desc("boolean","contents_enc64", "identifies base64 encoded content(default: disabled)")
     *      #prop_desc("int", "revision", "next revision number, auto increment for null")
     *  #struct_end()
     * @xmlrpc.returntype
     * $ConfigRevisionSerializer
     */
     public ConfigRevision updateInitSls(User user, String label, Map<String, Object> pathInfo) {
        String path = "/init.sls";
        //confirm that the user only provided valid keys in the map
        Set<String> validKeys = new HashSet<String>();
        validKeys.add(ConfigRevisionSerializer.CONTENTS);
        validKeys.add(ConfigRevisionSerializer.CONTENTS_ENC64);
        validKeys.add(ConfigRevisionSerializer.REVISION);
        validateMap(validKeys, pathInfo);
        XmlRpcConfigChannelHelper helper = XmlRpcConfigChannelHelper.getInstance();
        ConfigChannel channel = helper.lookupGlobal(user, label);
        if (!channel.isStateChannel()) {
            throw new InvalidOperationException(LocalizationService.getInstance()
                    .getMessage("editable.init.sls.only.for.state.channels"));
        }
        ConfigFile configFile = ConfigurationManager.getInstance().lookupConfigFile(user, channel.getId(), path);
        ConfigInfo configInfo = configFile.getLatestConfigRevision().getConfigInfo();
        ConfigRevision result;
        try {
            SLSFileData form = new SLSFileData(helper.getContents(pathInfo));
            //Only contents can be updated, rest is inherited from existing revision info
            form.setGroup(configInfo.getGroupname());
            form.setOwner(configInfo.getUsername());
            form.setPermissions(configInfo.getFilemode().toString());
            if (pathInfo.containsKey(ConfigRevisionSerializer.REVISION)) {
                form.setRevNumber(String.valueOf(pathInfo.get(ConfigRevisionSerializer.REVISION)));
            }
            result = ConfigFileBuilder.getInstance().update(form, user, configFile);

         }
         catch (UnsupportedEncodingException e) {
             throw new ConfigFileErrorException(e.getMessage());
        }
        return result;
    }

    /**
     * Creates a NEW path(file/directory) with the given path or updates an existing path
     * with the given contents in a given channel.
     * @param loggedInUser The current user
     * @param label the label of the config channel.
     * @param path the path of the given text file.
     * @param isDir true if this is a directory path, false if its to be a file path
     * @param pathInfo a map containing properties pertaining to the given path..
     * for directory paths - 'pathInfo' will hold values for -&gt;
     *  owner, group, permissions
     * for file paths -  'pathInfo' will hold values for-&gt;
     *  contents, owner, group, permissions, macro-start-delimiter, macro-end-delimiter
     * @return returns the new created or updated config revision..
     * @since 10.2
     *
     * @xmlrpc.doc Create a new file or directory with the given path, or
     * update an existing path.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "the channel label")
     * @xmlrpc.param #param("string", "path")
     * @xmlrpc.param #param_desc("boolean","isDir",
     *              "true if the path is a directory, False if it is a file")
     * @xmlrpc.param
     *  #struct_begin("pathInfo", "the path info")
     *      #prop_desc("string","contents",
     *              "contents of the file (text or base64 encoded if binary or want to preserve
     *                         control characters like LF, CR etc.)(only for non-directories)")
     *      #prop_desc("boolean","contents_enc64", "identifies base64 encoded content
     *                   (default: disabled, only for non-directories)")
     *      #prop_desc("string", "owner", "owner of the file/directory")
     *      #prop_desc("string", "group", "group name of the file/directory")
     *      #prop_desc("string", "permissions",
     *                              "octal file/directory permissions (eg: 644)")
     *      #prop_desc("string", "selinux_ctx", "SELinux Security context (optional)")
     *      #prop_desc("string", "macro-start-delimiter",
     *                  "config file macro start delimiter. Use null or empty
     *                  string to accept the default. (only for non-directories)")
     *      #prop_desc("string", "macro-end-delimiter",
     *              "config file macro end delimiter. Use null or
     *  empty string to accept the default. (only for non-directories)")
     *      #prop_desc("int", "revision", "next revision number, auto increment for null")
     *      #prop_desc("boolean", "binary", "mark the binary content, if True,
     *      base64 encoded content is expected (only for non-directories)")
     *
     *  #struct_end()
     * @xmlrpc.returntype
     * $ConfigRevisionSerializer
     */
    public ConfigRevision createOrUpdatePath(User loggedInUser,
                                                String label,
                                                String path,
                                                boolean isDir,
                                                Map<String, Object> pathInfo) {

        // confirm that the user only provided valid keys in the map
        Set<String> validKeys = new HashSet<String>();
        validKeys.add(ConfigRevisionSerializer.OWNER);
        validKeys.add(ConfigRevisionSerializer.GROUP);
        validKeys.add(ConfigRevisionSerializer.PERMISSIONS);
        validKeys.add(ConfigRevisionSerializer.REVISION);
        validKeys.add(ConfigRevisionSerializer.SELINUX_CTX);
        if (!isDir) {
            validKeys.add(ConfigRevisionSerializer.CONTENTS);
            validKeys.add(ConfigRevisionSerializer.CONTENTS_ENC64);
            validKeys.add(ConfigRevisionSerializer.MACRO_START);
            validKeys.add(ConfigRevisionSerializer.MACRO_END);
            validKeys.add(ConfigRevisionSerializer.BINARY);
        }
        validateMap(validKeys, pathInfo);

        if (pathInfo.get(ConfigRevisionSerializer.SELINUX_CTX) == null) {
            pathInfo.put(ConfigRevisionSerializer.SELINUX_CTX, "");
        }

        XmlRpcConfigChannelHelper helper = XmlRpcConfigChannelHelper.getInstance();
        ConfigChannel channel = helper.lookupGlobal(loggedInUser, label);
        if (channel.isStateChannel() && isDir) {
            throw new InvalidOperationException(LocalizationService.getInstance()
                    .getMessage("directories.symlink.not.supported.for.minions"));
        }

        if (channel.isStateChannel() && path.equals("/init.sls")) {
            throw new InvalidParameterException(LocalizationService.getInstance()
                    .getMessage("update.init.sls.with.updateinitsls.method"));
        }
        return helper.createOrUpdatePath(loggedInUser, channel, path,
                            isDir ? ConfigFileType.dir() : ConfigFileType.file(), pathInfo);
    }


    /**
     * Creates a NEW symbolic link with the given path or updates an existing path
     * with the given target_path in a given channel(Only 'normal' channels)).
     * @param loggedInUser The current user
     * @param label the label of the config channel.
     * @param path the path of the given text file.
     * @param pathInfo a map containing properties pertaining to the given path..
     * 'pathInfo' will hold values for -&gt;
     *      target_paths, selinux_ctx
     * @return returns the new created or updated config revision..
     * @since 10.2
     *
     * @xmlrpc.doc Create a new symbolic link with the given path, or
     * update an existing path in config channel of 'normal' type.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "label")
     * @xmlrpc.param #param("string", "path")
     * @xmlrpc.param
     *  #struct_begin("pathInfo", "the path info")
     *      #prop_desc("string","target_path",
     *              "the target path for the symbolic link")
     *      #prop_desc("string", "selinux_ctx", "SELinux Security context (optional)")
     *      #prop_desc("int", "revision", "next revision number,
     *       skip this field for automatic revision number assignment")
     *  #struct_end()
     * @xmlrpc.returntype
     * $ConfigRevisionSerializer
     */
    public ConfigRevision createOrUpdateSymlink(User loggedInUser,
                                                String label,
                                                String path,
                                                Map<String, Object> pathInfo) {

        // confirm that the user only provided valid keys in the map
        Set<String> validKeys = new HashSet<String>();
        validKeys.add(ConfigRevisionSerializer.TARGET_PATH);
        validKeys.add(ConfigRevisionSerializer.REVISION);
        validKeys.add(ConfigRevisionSerializer.SELINUX_CTX);
        validateMap(validKeys, pathInfo);
        pathInfo.putIfAbsent(ConfigRevisionSerializer.SELINUX_CTX, "");

        XmlRpcConfigChannelHelper helper = XmlRpcConfigChannelHelper.getInstance();
        ConfigChannel channel = helper.lookupGlobal(loggedInUser, label);
        if (channel.isStateChannel()) {
            throw new InvalidOperationException(LocalizationService.getInstance()
                    .getMessage("directories.symlink.not.supported.for.minions"));
        }
        return helper.createOrUpdatePath(loggedInUser, channel, path,
                                    ConfigFileType.symlink(), pathInfo);
    }


    /**
     * Given a list of paths and a channel the method returns details about the latest
     * revisions of the paths.
     * @param loggedInUser The current user
     * @param label the channel label
     * @param paths a list of paths to examine.
     * @return a list containing the latest config revisions of the requested paths.
     * @since 10.2
     *
     * @xmlrpc.doc Given a list of paths and a channel, returns details about
     * the latest revisions of the paths.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "label of config channel to lookup on")
     * @xmlrpc.param #array_single_desc("string", "paths", "list of paths to examine")
     * @xmlrpc.returntype
     * #return_array_begin()
     * $ConfigRevisionSerializer
     * #array_end()
     */
    public List<ConfigRevision> lookupFileInfo(User loggedInUser, String label, List<String> paths) {
        XmlRpcConfigChannelHelper configHelper = XmlRpcConfigChannelHelper.getInstance();
        ConfigChannel channel = configHelper.lookupGlobal(loggedInUser, label);
        ConfigurationManager cm = ConfigurationManager.getInstance();
        List<ConfigRevision> revisions = new LinkedList<ConfigRevision>();
        for (String path : paths) {
            ConfigFile cf = cm.lookupConfigFile(loggedInUser, channel.getId(), path);
            if (cf == null) {
                throw new NoSuchConfigFilePathException(path, label);
            }
            revisions.add(cf.getLatestConfigRevision());
        }
        return revisions;
    }


    /**
     * Given a path and revision number, return the revision
     * @param loggedInUser The current user
     * @param label the channel label
     * @param path path to examine.
     * @param revision the revision to fetch
     * @return the specified config revision of the requested path.
     * @since 10.12
     *
     * @xmlrpc.doc Given a path, revision number, and a channel, returns details about
     * the latest revisions of the paths.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "label of config channel to lookup on")
     * @xmlrpc.param #param_desc("string", "path", "path of file/directory")
     * @xmlrpc.param #param_desc("int", "revision", "the revision number")
     *
     * @xmlrpc.returntype
     * $ConfigRevisionSerializer
     */
    public ConfigRevision lookupFileInfo(User loggedInUser, String label, String path, Integer revision) {
        XmlRpcConfigChannelHelper configHelper = XmlRpcConfigChannelHelper.getInstance();
        ConfigChannel channel = configHelper.lookupGlobal(loggedInUser,
                                                                label);
        ConfigurationManager cm = ConfigurationManager.getInstance();
        ConfigFile cf = cm.lookupConfigFile(loggedInUser, channel.getId(), path);
        if (cf == null) {
            throw new NoSuchConfigFilePathException(path, label);
        }
        List<ConfigRevisionDto> revs = cm.listRevisionsForFile(loggedInUser, cf, null);
        for (ConfigRevisionDto rev : revs) {
            if (rev.getRevisionNumber().equals(revision)) {
                return ConfigurationFactory.lookupConfigRevisionById(rev.getId());
            }
        }
        throw new NoSuchConfigRevisionException();
    }


    /**
     * List files in a given channel
     * @param loggedInUser The current user
     * @param label the label of the config channel
     * @return a list of dto's holding this info.
     *
     * @xmlrpc.doc Return a list of files in a channel.
     * @xmlrpc.param  #session_key()
     * @xmlrpc.param #param_desc("string", "label", "label of config channel to list files on")
     * @xmlrpc.returntype
     * #return_array_begin()
     * $ConfigFileDtoSerializer
     * #array_end()
     */
    public List<ConfigFileDto> listFiles(User loggedInUser, String label) {
        XmlRpcConfigChannelHelper configHelper = XmlRpcConfigChannelHelper.getInstance();
        ConfigChannel channel = configHelper.lookupGlobal(loggedInUser, label);
        ConfigurationManager cm = ConfigurationManager.getInstance();
        return cm.listCurrentFiles(loggedInUser, channel, null, null, true);
    }



    /**
     * Deletes a list of  global channels..
     * Need to be a config admin to do this operation.
     * @param loggedInUser The current user
     *  key
     * @param labels the the list of global channels.
     * @return 1 if successful with the operation errors out otherwise.
     *
     * @xmlrpc.doc Delete a list of global config channels.
     * Caller must be a config admin.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #array_single_desc("string","labels", "configuration channel labels to delete")
     * @xmlrpc.returntype #return_int_success()
     *
     */
    public int deleteChannels(User loggedInUser, List<String> labels) {
        ensureConfigAdmin(loggedInUser);
        XmlRpcConfigChannelHelper configHelper = XmlRpcConfigChannelHelper.getInstance();
        List<ConfigChannel> channels = configHelper.lookupGlobals(loggedInUser,
                                                                labels);
        ConfigurationManager cm = ConfigurationManager.getInstance();
        for (ConfigChannel channel : channels) {
            cm.deleteConfigChannel(loggedInUser, channel);
        }
        return 1;
    }

    /**
     * Removes a list of paths from a global channel..
     * @param loggedInUser The current user
     * @param label the channel to remove the files from..
     * @param paths the list of paths to delete.
     * @return 1 if successful with the operation errors out otherwise.
     *
     *
     * @xmlrpc.doc Remove file paths from a global channel.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "channel to remove the files from")
     * @xmlrpc.param #array_single("string", "paths", "file paths to remove")
     * @xmlrpc.returntype #return_int_success()
     */
     public int deleteFiles(User loggedInUser, String label, List<String> paths) {
        XmlRpcConfigChannelHelper configHelper = XmlRpcConfigChannelHelper.getInstance();
        ConfigChannel channel = configHelper.lookupGlobal(loggedInUser, label);
        ConfigurationManager cm = ConfigurationManager.getInstance();
        List<ConfigFile> cfList = new ArrayList<>();
        for (String path : paths) {
            ConfigFile cf = cm.lookupConfigFile(loggedInUser, channel.getId(), path);
            if (cf == null) {
                throw new NoSuchConfigFilePathException(path, label);
            }
            cfList.add(cf);
        }
        for (ConfigFile cf : cfList) {
            cm.deleteConfigFile(loggedInUser, cf);
        }
        return 1;
     }

     /**
      * Schedule a comparison of the latest revision of a file
      * against the version deployed on a list of systems.
      * @param loggedInUser The current user
      * @param label label of the config channel
      * @param path the path of file to be compared
      * @param sids the list of server ids that the comparison will be
      * performed on
      * @return the id of the action scheduled
      *
      *
      * @xmlrpc.doc Schedule a comparison of the latest revision of a file
      * against the version deployed on a list of systems.
      * @xmlrpc.param #session_key()
      * @xmlrpc.param #param_desc("string", "label", "label of config channel")
      * @xmlrpc.param #param_desc("string", "path", "file path")
      * @xmlrpc.param #array_single("long", "sids", "the list of system IDs that the comparison will be performed on")
      * @xmlrpc.returntype #param_desc("int", "actionId", "the action ID of the scheduled action")
      */
     public Integer scheduleFileComparisons(User loggedInUser, String label, String path, List<Integer> sids) {

         XmlRpcConfigChannelHelper configHelper = XmlRpcConfigChannelHelper.getInstance();
         ConfigChannel channel = configHelper.lookupGlobal(loggedInUser, label);
         ConfigurationManager cm = ConfigurationManager.getInstance();

         // obtain the latest revision for the file provided by 'path'
         Set<Long> revisions = new HashSet<Long>();
         ConfigFile cf = cm.lookupConfigFile(loggedInUser, channel.getId(), path);
         if (cf == null) {
             throw new NoSuchConfigFilePathException(path, label);
         }
         revisions.add(cf.getLatestConfigRevision().getId());

         // schedule the action for the servers specified
         Set<Long> serverIds = new HashSet<>();
         for (Integer sid : sids) {
             serverIds.add(sid.longValue());
         }

        try {
            Action action =
                    ActionManager.createConfigDiffAction(loggedInUser, revisions, serverIds);
            ActionFactory.save(action);

            return action.getId().intValue();
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Check for the existence of the config channel provided.
     * @param loggedInUser The current user
     * @param label the channel to check for.
     * @return 1 if exists, 0 otherwise.
     *
     * @xmlrpc.doc Check for the existence of the config channel provided.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "channel to check for")
     * @xmlrpc.returntype #param_desc("int", "existence", "1 if exists, 0 otherwise")
     */
    public int channelExists(User loggedInUser, String label) {
        ConfigurationManager manager = ConfigurationManager.getInstance();
        return Objects.isNull(manager.lookupGlobalConfigChannel(loggedInUser, label)) ? 0 : 1;

    }


    /**
     * Schedule a configuration deployment for all systems in a config channel immediately
     * @param loggedInUser The current user
     * @param label the channel to remove the files from..
     * @return 1 if successful with the operation errors out otherwise.
     *
     *
     * @xmlrpc.doc Schedule an immediate configuration deployment for all systems
     *    subscribed to a particular configuration channel.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "the configuration channel's label")
     * @xmlrpc.returntype #return_int_success()
     */
    public int deployAllSystems(User loggedInUser, String label) {
        return deployAllSystems(loggedInUser, label, new Date());
    }


    /**
     * Schedule a configuration deployment for all systems in a config channel
     * @param loggedInUser The current user
     * @param label the channel to remove the files from..
     * @param date the date to schedule
     * @return 1 if successful with the operation errors out otherwise.
     *
     *
     * @xmlrpc.doc Schedule a configuration deployment for all systems
     *    subscribed to a particular configuration channel.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "the configuration channel's label")
     * @xmlrpc.param #param_desc("$date", "date", "the date to schedule the action")
     * @xmlrpc.returntype #return_int_success()
     */
    public int deployAllSystems(User loggedInUser, String label, Date date) {
        XmlRpcConfigChannelHelper configHelper = XmlRpcConfigChannelHelper.getInstance();
        ConfigurationManager manager = ConfigurationManager.getInstance();

        ConfigChannel channel = configHelper.lookupGlobal(loggedInUser,
                label);
        if (channel.isStateChannel()) {
            throw new InvalidParameterException(LocalizationService.getInstance()
                    .getMessage("state.channel.through.apply.config.channel.method"));
        }
        List<ConfigSystemDto> dtos = manager.listChannelSystems(loggedInUser, channel,
                null);
        List<Server> servers = new ArrayList<Server>();
        for (ConfigSystemDto m : dtos) {
            Server s = SystemManager.lookupByIdAndUser(m.getId(), loggedInUser);
            if (s != null) {
                servers.add(s);
            }
        }

        try {
            manager.deployConfiguration(loggedInUser, servers, channel, date);
        }
        catch (MissingCapabilityException e) {
            throw new com.redhat.rhn.frontend.xmlrpc.MissingCapabilityException(
                e.getCapability(), e.getServer());
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
        return 1;

    }

    /**
     * @param loggedInUser The current user
     * @param label  the channel to deploy the files from..
     * @param filePath config file path
     * @return 1 if successful with the operation errors out otherwise.
     * @xmlrpc.doc Schedule a configuration deployment of a certain file for all systems
     *    subscribed to a particular configuration channel.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "the configuration channel's label")
     * @xmlrpc.param #param_desc("string", "filePath", "the configuration file path")
     * @xmlrpc.returntype #return_int_success()
     */
    public int deployAllSystems(User loggedInUser, String label, String filePath) {
        return deployAllSystems(loggedInUser, label, filePath, new Date());
    }

    /**
     * @param loggedInUser The current user
     * @param label  the channel to deploy the files from..
     * @param filePath config file path
     * @param date the date to schedule
     * @return 1 if successful with the operation errors out otherwise.
     * @xmlrpc.doc Schedule a configuration deployment of a certain file for all systems
     *    subscribed to a particular configuration channel.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "label", "the configuration channel's label")
     * @xmlrpc.param #param_desc("string", "filePath", "the configuration file path")
     * @xmlrpc.param #param_desc("$date","date", "the date to schedule the action")
     * @xmlrpc.returntype #return_int_success()
     */
    public int deployAllSystems(User loggedInUser, String label, String filePath, Date date) {
        XmlRpcConfigChannelHelper configHelper = XmlRpcConfigChannelHelper.getInstance();
        ConfigurationManager manager = ConfigurationManager.getInstance();

        ConfigChannel channel = configHelper.lookupGlobal(loggedInUser,
                label);
        if (channel.isStateChannel()) {
            throw new InvalidParameterException(LocalizationService.getInstance()
                    .getMessage("state.channel.through.apply.config.channel.method"));
        }
        List<ConfigSystemDto> dtos = manager.listChannelSystems(loggedInUser, channel,
                null);
        Set<Long> servers = new HashSet<Long>();
        for (ConfigSystemDto m : dtos) {
            Server s = SystemManager.lookupByIdAndUser(m.getId(), loggedInUser);
            if (s != null) {
                servers.add(s.getId());
            }
        }
        Set<Long> fileIds = new HashSet<Long>();
        fileIds.add(manager.lookupConfigFile(loggedInUser,
                channel.getId(), filePath).getId());

        try {
            manager.deployFiles(loggedInUser, fileIds, servers, date);
        }
        catch (MissingCapabilityException e) {
            throw new com.redhat.rhn.frontend.xmlrpc.MissingCapabilityException(
                e.getCapability(), e.getServer());
        }
        return 1;
    }

    /**
     * List the systems subscribed to a configuration channel
     * @param loggedInUser The current user
     * @param label the label of the config channel
     * @return a list of dto's holding this info.
     *
     * @xmlrpc.doc Return a list of systems subscribed to a configuration channel
     * @xmlrpc.param  #session_key()
     * @xmlrpc.param #param_desc("string", "label", "label of the config channel to list subscribed systems")
     * @xmlrpc.returntype
     * #return_array_begin()
     * $ConfigSystemDtoSerializer
     * #array_end()
     */
    public List<ConfigSystemDto> listSubscribedSystems(User loggedInUser, String label) {
        XmlRpcConfigChannelHelper configHelper = XmlRpcConfigChannelHelper.getInstance();
        ConfigChannel channel = configHelper.lookupGlobal(loggedInUser,
                                                          label);
        ConfigurationManager cm = ConfigurationManager.getInstance();
        return cm.listChannelSystems(loggedInUser, channel, null);
    }

    /**
     * List the Groups where a given configuration channel is assigned to
     * @param loggedInUser the user
     * @param label the label for the configuration channels
     * @return a list of Groups
     *
     * @xmlrpc.doc Return a list of Groups where a given configuration channel is assigned to
     * @xmlrpc.param  #session_key()
     * @xmlrpc.param #param_desc("string", "label", "label of the config channel to list assigned groups")
     * @xmlrpc.returntype
     * #return_array_begin()
     * $ManagedServerGroupSerializer
     * #array_end()
     */
    public List<ManagedServerGroup> listAssignedSystemGroups(User loggedInUser, String label) {
        XmlRpcConfigChannelHelper configHelper = XmlRpcConfigChannelHelper.getInstance();
        ConfigChannel channel = configHelper.lookupGlobal(loggedInUser, label);

        List<Long> groupIds = StateFactory.listConfigChannelsSubscribedGroupIds(channel);
        return groupIds.stream()
            .map(gid -> ServerGroupFactory.lookupByIdAndOrg(gid, loggedInUser.getOrg()))
            .collect(Collectors.toList());
    }
}
