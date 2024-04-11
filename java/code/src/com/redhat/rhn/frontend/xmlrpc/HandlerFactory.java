/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

package com.redhat.rhn.frontend.xmlrpc;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler;
import com.redhat.rhn.frontend.xmlrpc.admin.configuration.AdminConfigurationHandler;
import com.redhat.rhn.frontend.xmlrpc.admin.monitoring.AdminMonitoringHandler;
import com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler;
import com.redhat.rhn.frontend.xmlrpc.api.ApiHandler;
import com.redhat.rhn.frontend.xmlrpc.audit.CVEAuditHandler;
import com.redhat.rhn.frontend.xmlrpc.auth.AuthHandler;
import com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler;
import com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler;
import com.redhat.rhn.frontend.xmlrpc.channel.access.ChannelAccessHandler;
import com.redhat.rhn.frontend.xmlrpc.channel.org.ChannelOrgHandler;
import com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler;
import com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler;
import com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler;
import com.redhat.rhn.frontend.xmlrpc.distchannel.DistChannelHandler;
import com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler;
import com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler;
import com.redhat.rhn.frontend.xmlrpc.image.DeltaImageInfoHandler;
import com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler;
import com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler;
import com.redhat.rhn.frontend.xmlrpc.image.store.ImageStoreHandler;
import com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler;
import com.redhat.rhn.frontend.xmlrpc.kickstart.filepreservation.FilePreservationListHandler;
import com.redhat.rhn.frontend.xmlrpc.kickstart.keys.CryptoKeysHandler;
import com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler;
import com.redhat.rhn.frontend.xmlrpc.kickstart.profile.keys.KeysHandler;
import com.redhat.rhn.frontend.xmlrpc.kickstart.profile.software.SoftwareHandler;
import com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler;
import com.redhat.rhn.frontend.xmlrpc.kickstart.snippet.SnippetHandler;
import com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler;
import com.redhat.rhn.frontend.xmlrpc.org.OrgHandler;
import com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler;
import com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler;
import com.redhat.rhn.frontend.xmlrpc.packages.provider.PackagesProviderHandler;
import com.redhat.rhn.frontend.xmlrpc.packages.search.PackagesSearchHandler;
import com.redhat.rhn.frontend.xmlrpc.preferences.locale.PreferencesLocaleHandler;
import com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler;
import com.redhat.rhn.frontend.xmlrpc.recurringaction.LegacyRecurringActionHandler;
import com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringActionHandler;
import com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringCustomStateHandler;
import com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringHighstateHandler;
import com.redhat.rhn.frontend.xmlrpc.saltkey.SaltKeyHandler;
import com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler;
import com.redhat.rhn.frontend.xmlrpc.subscriptionmatching.PinnedSubscriptionHandler;
import com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler;
import com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler;
import com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler;
import com.redhat.rhn.frontend.xmlrpc.system.SystemHandler;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler;
import com.redhat.rhn.frontend.xmlrpc.system.custominfo.CustomInfoHandler;
import com.redhat.rhn.frontend.xmlrpc.system.monitoring.SystemMonitoringHandler;
import com.redhat.rhn.frontend.xmlrpc.system.provisioning.powermanagement.PowerManagementHandler;
import com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler;
import com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler;
import com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler;
import com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler;
import com.redhat.rhn.frontend.xmlrpc.taskomatic.TaskomaticHandler;
import com.redhat.rhn.frontend.xmlrpc.taskomatic.TaskomaticOrgHandler;
import com.redhat.rhn.frontend.xmlrpc.user.UserHandler;
import com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler;
import com.redhat.rhn.frontend.xmlrpc.virtualhostmanager.VirtualHostManagerHandler;
import com.redhat.rhn.manager.formula.FormulaManager;
import com.redhat.rhn.manager.org.MigrationManager;
import com.redhat.rhn.manager.system.AnsibleManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;

import com.suse.manager.utils.SaltKeyUtils;
import com.suse.manager.webui.controllers.bootstrap.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.bootstrap.SSHMinionBootstrapper;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.xmlrpc.admin.AdminPaygHandler;
import com.suse.manager.xmlrpc.maintenance.MaintenanceHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * HandlerFactory for XMLRPC Handlers.
 *
 */

public class HandlerFactory {
    private final Map<String, BaseHandler> handlers;

    /**
     *  Creates an empty HandlerFactory.
     */
    public HandlerFactory() {
        this.handlers = new HashMap<>();
    }

    /**
     * Add a handler to this HandlerFactory.
     * @param namespace the xmlrpc namespace of this handler.
     * @param handler xml rpc handler.
     */
    public void addHandler(String namespace, BaseHandler handler) {
       handlers.put(namespace, handler);
    }

    /**
     * HandlerFactory prepopulated with the handlers used in production.
     * @return HandlerFactory used in production.
     */
    public static HandlerFactory getDefaultHandlerFactory() {
        HandlerFactory factory = new HandlerFactory();
        TaskomaticApi taskomaticApi = new TaskomaticApi();
        SystemEntitlementManager systemEntitlementManager = GlobalInstanceHolder.SYSTEM_ENTITLEMENT_MANAGER;
        SystemManager systemManager = new SystemManager(ServerFactory.SINGLETON, ServerGroupFactory.SINGLETON,
                GlobalInstanceHolder.SALT_API);
        FormulaManager formulaManager = GlobalInstanceHolder.FORMULA_MANAGER;
        SaltApi saltApi = GlobalInstanceHolder.SALT_API;
        SaltKeyUtils saltKeyUtils = GlobalInstanceHolder.SALT_KEY_UTILS;
        ServerGroupManager serverGroupManager = GlobalInstanceHolder.SERVER_GROUP_MANAGER;
        MigrationManager migrationManager = new MigrationManager(serverGroupManager);

        RegularMinionBootstrapper regularMinionBootstrapper = GlobalInstanceHolder.REGULAR_MINION_BOOTSTRAPPER;
        SSHMinionBootstrapper sshMinionBootstrapper = GlobalInstanceHolder.SSH_MINION_BOOTSTRAPPER;
        XmlRpcSystemHelper xmlRpcSystemHelper = new XmlRpcSystemHelper(
                regularMinionBootstrapper,
                sshMinionBootstrapper
        );
        ProxyHandler proxyHandler = new ProxyHandler(xmlRpcSystemHelper, systemManager);
        SystemHandler systemHandler = new SystemHandler(taskomaticApi, xmlRpcSystemHelper, systemEntitlementManager,
                systemManager, serverGroupManager, GlobalInstanceHolder.PAYG_MANAGER,
                GlobalInstanceHolder.ATTESTATION_MANAGER);

        OrgHandler orgHandler = new OrgHandler(migrationManager);
        ServerGroupHandler serverGroupHandler = new ServerGroupHandler(xmlRpcSystemHelper, serverGroupManager);
        UserHandler userHandler = new UserHandler(serverGroupManager);
        ActivationKeyHandler activationKeyHandler = new ActivationKeyHandler(serverGroupManager);
        ChannelHandler channelHandler = new ChannelHandler();
        ChannelSoftwareHandler channelSoftwareHandler = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
        AdminConfigurationHandler adminConfigurationHandler = new AdminConfigurationHandler(
                                  orgHandler, serverGroupHandler, userHandler, activationKeyHandler,
                                  systemHandler, channelHandler, channelSoftwareHandler, saltApi);

        factory.addHandler("actionchain", new ActionChainHandler());
        factory.addHandler("activationkey", activationKeyHandler);
        factory.addHandler("admin.configuration", adminConfigurationHandler);
        factory.addHandler("admin.monitoring", new AdminMonitoringHandler());
        factory.addHandler("admin.payg", new AdminPaygHandler(taskomaticApi));
        factory.addHandler("ansible", new AnsibleHandler(new AnsibleManager(GlobalInstanceHolder.SALT_API)));
        factory.addHandler("api", new ApiHandler(factory));
        factory.addHandler("audit", new CVEAuditHandler());
        factory.addHandler("auth", new AuthHandler());
        factory.addHandler("channel", channelHandler);
        factory.addHandler("channel.access", new ChannelAccessHandler());
        factory.addHandler("channel.org", new ChannelOrgHandler());
        factory.addHandler("channel.software", channelSoftwareHandler);
        factory.addHandler("configchannel", new ConfigChannelHandler());
        factory.addHandler("contentmanagement", new ContentManagementHandler());
        factory.addHandler("distchannel", new DistChannelHandler());
        factory.addHandler("errata", new ErrataHandler());
        factory.addHandler("formula", new FormulaHandler(formulaManager, saltApi));
        factory.addHandler("image.delta", new DeltaImageInfoHandler());
        factory.addHandler("image.store", new ImageStoreHandler());
        factory.addHandler("image.profile", new ImageProfileHandler());
        factory.addHandler("image", new ImageInfoHandler(saltApi));
        factory.addHandler("kickstart", new KickstartHandler());
        factory.addHandler("kickstart.filepreservation", new FilePreservationListHandler());
        factory.addHandler("kickstart.keys", new CryptoKeysHandler());
        factory.addHandler("kickstart.profile", new ProfileHandler());
        factory.addHandler("kickstart.profile.keys", new KeysHandler());
        factory.addHandler("kickstart.profile.software", new SoftwareHandler());
        factory.addHandler("kickstart.profile.system", new SystemDetailsHandler());
        factory.addHandler("kickstart.snippet", new SnippetHandler());
        factory.addHandler("kickstart.tree", new KickstartTreeHandler());
        factory.addHandler("maintenance", new MaintenanceHandler());
        factory.addHandler("org", orgHandler);
        factory.addHandler("org.trusts", new OrgTrustHandler());
        factory.addHandler("packages", new PackagesHandler());
        factory.addHandler("packages.provider", new PackagesProviderHandler());
        factory.addHandler("packages.search", new PackagesSearchHandler());
        factory.addHandler("preferences.locale", new PreferencesLocaleHandler());
        factory.addHandler("proxy", proxyHandler);
        // TODO: 'recurringaction' is deprecated in favor of the 'recurring' namespace. Remove this after 4.3.6
        factory.addHandler("recurringaction", new LegacyRecurringActionHandler());
        factory.addHandler("recurring", new RecurringActionHandler());
        factory.addHandler("recurring.highstate", new RecurringHighstateHandler());
        factory.addHandler("recurring.custom", new RecurringCustomStateHandler());
        factory.addHandler("saltkey", new SaltKeyHandler(saltKeyUtils));
        factory.addHandler("schedule", new ScheduleHandler());
        factory.addHandler("subscriptionmatching.pinnedsubscription", new PinnedSubscriptionHandler());
        factory.addHandler("sync.master", new MasterHandler());
        factory.addHandler("sync.slave", new SlaveHandler());
        factory.addHandler("sync.content", new ContentSyncHandler());
        factory.addHandler("system", systemHandler);
        factory.addHandler("system.config", new ServerConfigHandler(taskomaticApi, xmlRpcSystemHelper));
        factory.addHandler("system.custominfo", new CustomInfoHandler());
        factory.addHandler("system.monitoring", new SystemMonitoringHandler(formulaManager));
        factory.addHandler("system.provisioning.powermanagement", new PowerManagementHandler());
        factory.addHandler("system.provisioning.snapshot", new SnapshotHandler(xmlRpcSystemHelper));
        factory.addHandler("system.scap", new SystemScapHandler());
        factory.addHandler("system.search", new SystemSearchHandler());
        factory.addHandler("virtualhostmanager", new VirtualHostManagerHandler());
        factory.addHandler("systemgroup", serverGroupHandler);
        factory.addHandler("taskomatic", new TaskomaticHandler());
        factory.addHandler("taskomatic.org", new TaskomaticOrgHandler());
        factory.addHandler("user", userHandler);
        factory.addHandler("user.external", new UserExternalHandler());
        return factory;
    }

    /**
     * getHandler - function to, given a handlerName (corresponding to
     * an entry in handler-manifest.xml) return the Handler object
     * @param handlerName the name of the handler
     * @return Object of the handler in question.
     */
    public Optional<BaseHandler> getHandler(String handlerName) {
        return Optional.ofNullable(handlers.get(handlerName));
    }

    /**
     * Get all keys from the Factory.
     * @return All keys from the Factory.
     */
    public Set<String> getKeys() {
        return handlers.keySet();
    }
}
