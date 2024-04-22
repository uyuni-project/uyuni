/*
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
package com.redhat.rhn.common.security.acl;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ClonedChannel;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.dto.ChannelPerms;
import com.redhat.rhn.frontend.dto.OrgProxyServer;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.user.UserManager;

import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.utils.ViewHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Access is a concrete implementation of an AclHandler.
 * This is default implementation which is always included
 * when evaluating {@link Acl Acls}.
 */
public class Access extends BaseHandler {

    protected static final Logger LOG = LogManager.getLogger(Access.class);

    /**
     * Returns true if the User whose uid matches the given uid, is
     * in the given Role. Requires a uid String in the Context.
     * @param ctx Context Map to pass in
     * @param params Parameters to use to fetch from Context
     * @return true if access is granted, false otherwise
     */
    public boolean aclUidRole(Map<String, Object> ctx, String[] params) {
        Long uid = getAsLong(ctx.get("uid"));
        User user = UserFactory.lookupById(uid);
        return user.hasRole(RoleFactory.lookupByLabel(params[0]));
    }

    /**
     * Returns true if current User is in the Role.
     * Requires a User in the Context.
     * @param ctx Context Map to pass in
     * @param params Parameters to use to fetch from Context
     * @return true if access is granted, false otherwise
     */
    public boolean aclUserRole(Map<String, Object> ctx, String[] params) {
        User user = (User) ctx.get("user");
        if (user != null) {
            boolean retval = user.hasRole(RoleFactory.lookupByLabel(params[0]));
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} aclUserRole | A returning {}", params[0], retval);
            }
            return retval;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} aclUserRole | B returning false ..", params[0]);
        }
        return false;
    }

    /**
     * Returns true if the given value in the param is found in
     * the global configuration.
     * @param ctx Context Map to pass in
     * @param params Parameters to use to fetch from Context
     * @return true if access is granted, false otherwise
     */
    public boolean aclIs(Map<String, Object> ctx, String[] params) {
        if (params == null || params.length < 1) {
            // FIXME: need to localize exception text
            throw new IllegalArgumentException("Invalid number of parameters.");
        }
        return Config.get().getBoolean(params[0]);
    }

    /**
     * TODO: Right now this method calls a small little query
     * very similar to how the perl code decides this acl.
     * IMO, there is a better way, and we should fix this when
     * we migrate the channels tab.
     * @param ctx Context Map to pass in
     * @param params Parameters to use to fetch from Context
     * @return true if access is granted, false otherwise
     */
    public boolean aclOrgChannelFamily(Map<String, Object> ctx, String[] params) {
        User user = (User) ctx.get("user");
        String label = params[0];

        SelectMode m = ModeFactory.getMode("Org_queries",
                "has_channel_family_entitlement");
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("label", label);
        queryParams.put("org_id", user.getOrg().getId());
        DataResult<Row> dr = m.execute(queryParams);
        return (!dr.isEmpty());
    }

    /**
     * Does the org have any proxies?
     * @param ctx Context Map to pass in
     * @param params Parameters to use to fetch from Context
     * @return true if the org has proxies, false otherwise
     */
    public boolean aclOrgHasProxies(Map<String, Object> ctx, String[] params) {
        User user = (User) ctx.get("user");

        SelectMode m = ModeFactory.getMode("System_queries",
                "org_proxy_servers");
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("org_id", user.getOrg().getId());
        DataResult<OrgProxyServer> dr = m.execute(queryParams);
        return (!dr.isEmpty());
    }

    /**
     * Check if a System has a feature
     * @param ctx Context Map to pass in
     * @param params Parameters to use to fetch from Context
     * @return true if access is granted, false otherwise
     */
    public boolean aclSystemFeature(Map<String, Object> ctx, String[] params) {
        Long sid = getAsLong(ctx.get("sid"));
        String feature = params[0];

        return SystemManager.serverHasFeature(sid, feature);
    }

    /**
     * Check if a system has virtualization entitlements.
     * @param ctx Context map to pass in.
     * @param params Parameters to use to fetch from context.
     * @return True if system has virtualization entitlement, false otherwise.
     */
    public boolean aclSystemHasVirtualizationEntitlement(Map<String, Object> ctx, String[] params) {
        Long sid = getAsLong(ctx.get("sid"));
        User user = (User) ctx.get("user");

        return SystemManager.serverHasVirtuaizationEntitlement(sid, user.getOrg());
    }

    /**
     * Check if a system has bootstrap entitlements.
     * @param ctx Context map to pass in.
     * @param params Parameters to use to fetch from context.
     * @return True if system has virtualization entitlement, false otherwise.
     */
    public boolean aclSystemHasBootstrapEntitlement(Map<String, Object> ctx, String[] params) {
        Long sid = getAsLong(ctx.get("sid"));

        return SystemManager.serverHasBootstrapEntitlement(sid);
    }

    /**
     * Check if all systems in the current system have a certain feature.
     * @param ctx Context map to pass in.
     * @param params Parameters to use to fetch from context.
     * @return true if at all systems in the set have the feature passed as params[0]
     */
    public boolean aclAllSystemsInSetHaveFeature(Map<String, Object> ctx, String[] params) {
        User user = (User) ctx.get("user");

        return SystemManager.countSystemsInSetWithoutFeature(user,
                RhnSetDecl.SYSTEMS.getLabel(), params[0]) == 0;
    }

    /**
     * Check if any system has a management entitlement
     * @param ctx Context map to pass in.
     * @param params Parameters to use to fetch from context.
     * @return True if system has management entitlement, false otherwise.
     */
    public boolean aclSystemHasManagementEntitlement(Map<String, Object> ctx, String[] params) {
        Long sid = getAsLong(ctx.get("sid"));
        User user = (User) ctx.get("user");
        try {
            Server server = SystemManager.lookupByIdAndUser(sid, user);
            if (server == null) {
                return false;
            }
            return server.hasEntitlement(EntitlementManager.MANAGEMENT);
        }
        catch (LookupException e) {
            return false;
        }
    }

    /**
     * Check if any system has a Salt entitlement.
     * @param ctx Context map to pass in.
     * @param params Parameters to use to fetch from context.
     * @return True if system has salt entitlement, false otherwise.
     */
    public boolean aclSystemHasSaltEntitlement(Map<String, Object> ctx, String[] params) {
        Long sid = getAsLong(ctx.get("sid"));
        boolean ret = false;
        if (sid != null) {
            User user = (User) ctx.get("user");
            try {
                Server server = SystemManager.lookupByIdAndUser(sid, user);
                if (server != null) {
                    ret = server.hasEntitlement(EntitlementManager.SALT);
                }
            }
            catch (LookupException e) {
                // expected
            }
        }
        return ret;
    }

    /**
     * Check if any system has an Ansible Control Node entitlement.
     * @param ctx Context map to pass in.
     * @param params Parameters to use to fetch from context.
     * @return True if system has salt entitlement, false otherwise.
     */
    public boolean aclSystemHasAnsibleControlNodeEntitlement(Map<String, Object> ctx, String[] params) {
        Long sid = getAsLong(ctx.get("sid"));
        boolean ret = false;
        if (sid != null) {
            User user = (User) ctx.get("user");
            Server server = SystemManager.lookupByIdAndUser(sid, user);
            if (server != null) {
                ret = server.hasEntitlement(EntitlementManager.ANSIBLE_CONTROL_NODE);
            }
        }
        return ret;
    }

    /**
     * Check if any system has a Foreign entitlement.
     * @param ctx Context map to pass in.
     * @param params Parameters to use to fetch from context.
     * @return True if system has foreign entitlement, false otherwise.
     */
    public boolean aclSystemHasForeignEntitlement(Map<String, Object> ctx, String[] params) {
        Long sid = getAsLong(ctx.get("sid"));
        boolean ret = false;
        if (sid != null) {
            User user = (User) ctx.get("user");
            Server server = SystemManager.lookupByIdAndUser(sid, user);
            if (server != null) {
                ret = server.hasEntitlement(EntitlementManager.FOREIGN);
            }
        }
        return ret;
    }

    /**
     * Check if a system is a {@link com.redhat.rhn.domain.server.MinionServer} which has a bootstrap entitlement
     * @param ctx Context map to pass in.
     * @param params Parameters to use to fetch from context.
     * @return True if system is a MinionServer with bootstrap entitlement, false otherwise.
     */
    public boolean aclSystemIsBootstrapMinionServer(Map<String, Object> ctx, String[] params) {
        Long sid = getAsLong(ctx.get("sid"));
        boolean ret = false;
        if (sid != null) {
            User user = (User) ctx.get("user");
            Server server = SystemManager.lookupByIdAndUser(sid, user);
            if (server != null) {
                ret = server.asMinionServer().isPresent();
            }
        }
        return ret && aclSystemHasBootstrapEntitlement(ctx, params);
    }

    /**
     * Check if any system has a Salt entitlement.
     * Check single system if used in an action for a single system or
     * SSM in case of an SSM action.
     * @param ctx Context map to pass in.
     * @param params Parameters to use to fetch from context.
     * @return True if any system has salt entitlement, false otherwise.
     */
    public boolean aclAnySystemWithSaltEntitlement(Map<String, Object> ctx, String[] params) {
        Long sid = getAsLong(ctx.get("sid"));
        User user = (User) ctx.get("user");
        boolean ret = false;
        if (sid != null) {
            Server server = SystemManager.lookupByIdAndUser(sid, user);
            if (server != null) {
                ret = server.hasEntitlement(EntitlementManager.SALT);
            }
        }
        else {
            // SSM
            ret = SystemManager.countSystemsInSetWithoutEntitlement(user,
                    RhnSetDecl.SYSTEMS.getLabel(),
                    Arrays.asList("enterprise_entitled")) > 0;
        }
        return ret;
    }

    /**
     * Uses the sid param to decide if a system is a virtual guest
     * @param ctx Context Map to pass in
     * @param params Parameters to use (unused)
     * @return true if a system is a satellite, false otherwise
     */
    public boolean aclSystemIsVirtual(Map<String, Object> ctx, String[] params) {
        Long sid = getAsLong(ctx.get("sid"));
        User user = (User) ctx.get("user");
        Server lookedUp = SystemManager.lookupByIdAndUser(sid, user);

        return lookedUp.isVirtualGuest();
    }

    /**
     * Uses the sid param to decide if a system is a proxy
     * @param ctx Context Map to pass in
     * @param params Parameters to use (unused)
     * @return true if a system is a proxy, false otherwise
     */
    public boolean aclSystemIsProxy(Map<String, Object> ctx, String[] params) {
        Long sid = getAsLong(ctx.get("sid"));
        User user = (User) ctx.get("user");
        Server lookedUp = SystemManager.lookupByIdAndUser(sid, user);

        return lookedUp.isProxy();
    }

    /**
     * Check if a system has a management entitlement
     * @param ctx Context map to pass in.
     * @param params Parameters to use to fetch from context.
     * @return True if system has management entitlement, false otherwise.
     */
    public boolean aclSystemIsInSSM(Map<String, Object> ctx, String[] params) {
        Long sid = getAsLong(ctx.get("sid"));
        User user = (User) ctx.get("user");
        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        return set.contains(sid);
    }

    /**
     * Checks if the User's Org has the requested Role.
     * Requires a User in the Context object.
     * @param ctx Context Map to pass in
     * @param params Used to specify the Role label
     * @return true if access is granted, false otherwise
     */
    public boolean aclOrgRole(Map<String, Object> ctx, String[] params) {
        User user = (User)ctx.get("user");
        if (user != null) {
            Org org = user.getOrg();
            return org.hasRole(RoleFactory.lookupByLabel(params[0]));
        }

        return false;
    }

    /**
     * Returns true if the User has been authenticated by the system.
     * @param ctx Context Map to pass in
     * @param params Not used
     * @return true if access is granted, false otherwise
     */
    public boolean aclUserAuthenticated(Map<String, Object> ctx, String[] params) {
        User user = (User)ctx.get("user");
        return (user != null);
    }

    /**
     * Returns true if the system is a satellite and has any users.
     * NOTE: this is an expensive call with many many users.  It is intended
     * to be called from the installer.
     * @param ctx acl context
     * @param p parameters for acl (ignored)
     * @return true if the system is a satellite and has any users.
     */
    public boolean aclNeedFirstUser(Map<String, Object> ctx, String[] p) {
        return !(UserFactory.satelliteHasUsers());
    }

    /**
     * returns true or false ifthe user has access to a channel
     * @param ctx acl context
     * @param params params need the channel id as param 0
     * @return true if has read access false otherwise
     */
    public boolean aclCanAccessChannel(Map<String, Object> ctx, String[] params) {
        User user = (User) ctx.get("user");

        try {
            if (user != null) {
                Channel chan = ChannelManager.lookupByIdAndUser(
                        Long.parseLong(params[0]), user);
                return chan != null;
            }
        }
        catch (Exception e) {
            return false;
        }
        return false;
    }


    /**
     * Returns true if the user is either a channel administrator or an
     * org administrator
     * @param ctx acl context
     * @param params parameters for acl (ignored)
     * @return true if the user is either a channel admin or org admin
     */
    public boolean aclUserCanManageChannels(Map<String, Object> ctx, String[] params) {
        User user = (User) ctx.get("user");
        if (user != null) {
            List<ChannelPerms> chans = UserManager.channelManagement(user, null);
            return (user.hasRole(RoleFactory.CHANNEL_ADMIN)) || !chans.isEmpty();
        }

        return false;
    }

    /**
     * Returns true if it is a modular channel
     * @param ctx acl context (includes the channel cid and the user name)
     * @param params parameters for acl (ignored)
     * @return true if the user is channel admin of the corresponding channel.
     */
    public boolean aclIsModularChannel(Map<String, Object> ctx, String[] params) {
        Long cid = getAsLong(ctx.get("cid"));
        User user = (User) ctx.get("user");
        Channel chan = ChannelManager.lookupByIdAndUser(cid, user);

        return chan.isModular();
    }

    /**
     * Returns true if the user is channel admin of the corresponding channel.
     * If the channel is a vendor channel, the return value is false.
     * @param ctx acl context (includes the channel cid and the user name)
     * @param params parameters for acl (ignored)
     * @return true if the user is channel admin of the corresponding channel.
     */
    public boolean aclUserIsChannelAdmin(Map<String, Object> ctx, String[] params) {
        Long cid = getAsLong(ctx.get("cid"));
        User user = (User) ctx.get("user");
        Channel chan = ChannelManager.lookupByIdAndUser(cid, user);

        return UserManager.verifyChannelAdmin(user, chan);
    }

    /**
     * Returns true if the query param exists.
     * @param ctx acl context
     * @param params parameters for acl (ignored)
     * @return true if the query param exists.
     */
    public boolean aclFormvarExists(Map<String, Object> ctx, String[] params) {
        if (params.length < 1) {
            return false;
        }

        return ctx.get(params[0]) != null;
    }

    /**
     *
     * @param ctx acl context
     * @param params parameters for acl (ignored)
     * @return true if user org is owner of channel
     */
    public boolean aclTrustChannelAccess(Map<String, Object> ctx, String[] params) {
        User user = (User) ctx.get("user");
        Long cid = getAsLong(ctx.get("cid"));
        Channel c = ChannelFactory.lookupById(cid);

        return c.getOrg().getId().equals(user.getOrg().getId());
    }

    /**
     *
     * @param ctx acl context
     * @param params parameters for acl
     * @return if channel is protected
     */
    public boolean aclIsProtected(Map<String, Object> ctx, String[] params) {
        Long cid = getAsLong(ctx.get("cid"));
        Channel c = ChannelFactory.lookupById(cid);
        return c.isProtected();
    }

    /**
     * See if the erratum isn't a Red Hat erratum
     * @param ctx Our current context, containing the erratum
     * @param params nevim, dal
     * @return whether the erratum isn't a Red Hat erratum
     */
    public boolean aclErrataEditable(Map<String, Object> ctx, String[] params) {
        Long eid = getAsLong(ctx.get("eid"));
        Errata e = ErrataFactory.lookupById(eid);
        return e != null && e.getOrg() != null;
    }

    /**
     * Checks a value from a formula for equality with the given argument.
     * @param ctx acl context
     * @param params parameters for acl
     * @return whether the formula values is equal to the given arg
     */
    public boolean aclFormulaValueEquals(Map<String, Object> ctx, String[] params) {
        Long sid = getAsLong(ctx.get("sid"));
        User user = (User) ctx.get("user");
        if (params == null || params.length < 3) {
            return false;
        }
        String formulaName = params[0];
        String valueName = params[1];
        String valueToCheck = params[2];
        Server server = SystemManager.lookupByIdAndUser(sid, user);
        return ViewHelper.getInstance().formulaValueEquals(server, formulaName, valueName, valueToCheck);
    }

    /**
     * Checks if a server uses ssh-push or ssh-push-tunnel contact methods
     * @param ctx acl context
     * @param params parameters for acl
     * @return true if the server uses ssh-push or ssh-push-tunnel
     */
    public boolean aclHasSshPushContactMethod(Map<String, Object> ctx, String[] params) {
        Long sid = getAsLong(ctx.get("sid"));
        User user = (User) ctx.get("user");
        Server lookedUp = SystemManager.lookupByIdAndUser(sid, user);
        return ContactMethodUtil.isSSHPushContactMethod(lookedUp.getContactMethod());
    }

    /**
     * Checks if a server can support Program Temporary Fixes (PTFs).
     * @param ctx acl context
     * @param params parameters for acl
     * @return true if the server can have ptf
     */
    public boolean aclHasPtfRepositories(Map<String, Object> ctx, String[] params) {
        Long sid = getAsLong(ctx.get("sid"));
        User user = (User) ctx.get("user");

        Server server = SystemManager.lookupByIdAndUser(sid, user);
        if (server == null || !server.doesOsSupportPtf()) {
            return false;
        }

        // Evaluate if any of the subscript channel refers to a PTF repository
        return server.getChannels()
                     .stream()
                     .map(channel -> channel instanceof ClonedChannel ? channel.getOriginal() : channel)
                     .flatMap(c -> c.getSources().stream())
                     .map(ContentSource::getSourceUrl)
                     .anyMatch(url -> url.contains("/PTF/"));
    }

    /**
     * Checks if a system allows manual uninstallation of ptfs without user intervention
     * @param ctx acl context
     * @param params parameters for acl
     * @return true if the system support automated ptf uninstallation
     */
    public boolean aclSystemSupportsPtfRemoval(Map<String, Object> ctx, String[] params) {
        Long sid = getAsLong(ctx.get("sid"));
        User user = (User) ctx.get("user");

        Server server = SystemManager.lookupByIdAndUser(sid, user);
        if (server == null) {
            return false;
        }

        return SystemManager.serverHasFeature(sid, "ftr_package_remove") &&
            ServerFactory.isPtfUninstallationSupported(server);
    }

    /**
     * Checks if a system is Pay-as-you-go
     * @param ctx acl context
     * @param params parameters for acl
     * @return true if the system is Pay-as-you-go
     */
    public boolean aclSystemIsPayg(Map<String, Object> ctx, String[] params) {
        Long sid = getAsLong(ctx.get("sid"));
        User user = (User) ctx.get("user");

        Server server = SystemManager.lookupByIdAndUser(sid, user);
        if (server == null) {
            return false;
        }

        return server.isPayg();
    }


    /**
     * Checks if a system supports confidential computing and has a configuration
     * @param ctx
     * @param params
     * @return true if the system supports confidential computing
     */
    public boolean aclSystemHasCoCoConfig(Map<String, Object> ctx, String[] params) {
        Long sid = getAsLong(ctx.get("sid"));
        User user = (User) ctx.get("user");

        Server server = SystemManager.lookupByIdAndUser(sid, user);
        if (server == null) {
            return false;
        }

        return server.doesOsSupportCoCoAttestation();
    }
}
