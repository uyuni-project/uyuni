/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.common.security.acl;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.common.BadParameterException;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Some acl implementation for configuration management
 */
public class ConfigAclHandler extends BaseHandler {

    /**
     * Tell whether a file is a directory.
     * @param ctx Our current context, containing a crid or cfid.
     * @param params The parameters containing a config revision id or nothing.
     * @return whether the found revision is a file.
     */
    public boolean aclIsFile(Map<String, Object> ctx, String[] params) {
        ConfigRevision revision = getRevision(ctx, params);
        return revision.isFile();
    }

    /**
     * Tell whether a file is of type sls.
     * @param ctx Our current context, containing a crid or cfid.
     * @param params The parameters containing a config revision id or nothing.
     * @return whether the found revision is a file.
     */
    public boolean aclIsSls(Map<String, Object> ctx, String[] params) {
        ConfigRevision revision = getRevision(ctx, params);
        return revision.isSls();
    }

    /**
     * Tell whether a file belongs to a state channel.
     * @param ctx Our current context, containing a crid or cfid.
     * @param params The parameters containing a config revision id or nothing.
     * @return whether the found revision belongs to a state channel.
     */
    public boolean aclIsInStateChannel(Map<String, Object> ctx, String[] params) {
        ConfigRevision revision = getRevision(ctx, params);
        return revision.getConfigFile().getConfigChannel().isStateChannel();
    }

    /**
     * Tell whether the logged in user can edit the give channel.
     * @param ctx Our current context, containing the user
     * @param params The parameters containing a config channel id.
     * @return whether the config channel and objects inside it are editable
     *         by the current user.
     */
    public boolean aclConfigChannelEditable(Map<String, Object> ctx, String[] params) {
        User user = (User) ctx.get("user");
        ConfigChannel cc = getChannel(ctx, params);

        //This happens if the channel is there but is not accessible.
        if (cc == null) {
            return false;
        }

        if (cc.isGlobalChannel()) {
            return user.hasRole(RoleFactory.CONFIG_ADMIN);
        }

        //You have only gotton this far if you have access to the channel
        //and it is not a global channel, which means that you administer the
        //particular server that the channel is for.
        return true;
    }

    /**
     * Whether the config channel of the current context is of the given type.
     * The possible types are the labels of the different <code>ConfigChannelType</code>
     * statics found in com.redhat.rhn.domain.config.ConfigurationFactory.
     * @param ctx The current context.
     * @param params The params, must include desired channel type label.
     * @return Whether the current channel is of the given type.
     */
    public boolean aclConfigChannelType(Map<String, Object> ctx, String[] params) {
        if (params.length < 1 || StringUtils.isEmpty(params[0])) {
            throw new IllegalArgumentException("Parameters must include type.");
        }
        ConfigChannel cc = getChannel(ctx, params);
        //does the type in params match the channels type.
        return cc.getConfigChannelType().getLabel().equalsIgnoreCase(params[0]);
    }

    /**
     * Tell whether the config channel represented by a <code>ccid</code> in
     * the current context has files.
     * @param ctx The current context
     * @param params The parameters unused.
     * @return whether the config channel has files.
     */
    public boolean aclConfigChannelHasFiles(Map<String, Object> ctx, String[] params) {
        //We are using hibernate mappings to decide if the config channel
        //has files. This makes things much easier, but it also could be
        //somewhat slow because it loads up every file instead of justing
        //finding one and then bailing.
        ConfigChannel cc = getChannel(ctx, params);
        return (!cc.getConfigFiles().isEmpty());
    }

    /**
     * Tell us whether the selected channel has any subscribed systems or not
     * @param ctx Current context
     * @param params parameters (unused)
     * @return true if there is at least one system subscribed to this channel
     */
    public boolean aclConfigChannelHasSystems(Map<String, Object> ctx, String[] params) {
        ConfigChannel cc = getChannel(ctx, params);
        User user = (User) ctx.get("user");

        return ConfigurationManager.getInstance().getSystemCount(user, cc) > 0;
    }
    /**
     * Returns the revision. This really should be two methods with the same
     * name, but different number of parameters. We can't do that because all
     * acls are handled the same way. Therefore, we decide how we are being called
     * by whether there is a parameter in the String[].
     *
     * Case 1:
     * First it looks in the params, and if it finds one, it tries to parse it as
     * a Long representing the config revision id.
     *
     * If there are no parameters, it gets the config revision id from the context.
     * Case 2:
     * The context can either have a crid representing the revision id or,
     * Case 3: a cfid representing the config file, from which we will get
     * the latest config revision.
     * @param map Context that contains
     *            <ol>
     *              <li>Case 1: Nothing important</li>
     *              <li>Case 2: crid as a Long</li>
     *              <li>Case 3: cfid as a Long</li>
     *            </ol>
     * @param params Parameters that contains
     *            <ol>
     *              <li>Case 1: A single revision id parsable as a Long</li>
     *              <li>Case 2: Nothing</li>
     *              <li>Case 3: Nothing</li>
     *            </ol>
     * @return The revision found.
     */
    private ConfigRevision getRevision(Map<String, Object> map, String[] params) {
        Long crid;
        User user = (User) map.get("user");
        ConfigurationManager cm = ConfigurationManager.getInstance();

        //Case 1:
        if (params != null && params.length == 1) {
            try {
                crid = Long.valueOf(params[0]);
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("Parameter must be a parsable long.");
            }
            return cm.lookupConfigRevision(user, crid);
        }
        crid = getAsLong(map.get("crid"));
        //Case 2:
        if (crid != null) {

            return cm.lookupConfigRevision(user, crid);
        }
        Long cfid = getAsLong(map.get("cfid"));
        if (cfid == null) {
            throw new BadParameterException("Missing crid and cfid!");
        }
        return cm.lookupConfigFile(user, cfid).getLatestConfigRevision();
    }

    private ConfigChannel getChannel(Map<String, Object> map, String[] params) {
        User user = (User) map.get("user");
        Long ccid;
        //Look for ccid from parameters
        try {
            ccid = getAsLong(params);
        }
        catch (NumberFormatException e) {
            //We are catching this because it is possible that there is
            // another parameter and the ccid is solely in the context map.
            ccid = null;
        }

        //Look for ccid from parameters
        if (ccid == null) {
            ccid = getAsLong(map.get("ccid"));
        }

        //Finally, look for the revision, and figure out the config channel from that
        if (ccid == null) {
            ConfigRevision cr = getRevision(map, params);
            if (cr != null) {
                return cr.getConfigFile().getConfigChannel();
            }
            //else, ccid is still null and the following if will throw exception.
        }

        if (user == null || ccid == null) {
            throw new IllegalArgumentException("Context must have a user" +
                    " and config channel id must be a parameter");
        }

        ConfigurationManager cm = ConfigurationManager.getInstance();

        //check the user's access to channel. This will prevent a LookupException.
        if (!cm.accessToChannel(user.getId(), ccid)) {
            return null;
        }

        return cm.lookupConfigChannel(user, ccid);
    }

    /**
     * Check if a System is config enabled
     * @param ctx Context Map to pass in
     * @param params Parameters to use to fetch from Context
     * @return true if system is config enabled, false otherwise
     */
    public boolean aclConfigEnabled(Map<String, Object> ctx, String[] params) {
        User user = (User) ctx.get("user");
        Long sid = getAsLong(ctx.get("sid"));
        if (user == null || sid == null) {
            throw new IllegalArgumentException("Context must have a user" +
                    " and server id.");
        }

        Server server = SystemManager.lookupByIdAndUser(sid, user);
        if (server == null) {
            String format = "Server with sid [%s] could not be found in org [%s]";
            throw new IllegalArgumentException(String.format(format,
                                                sid, user.getOrg()));
        }

        ConfigurationManager cm = ConfigurationManager.getInstance();

        return cm.isConfigEnabled(server, user);
    }
}
