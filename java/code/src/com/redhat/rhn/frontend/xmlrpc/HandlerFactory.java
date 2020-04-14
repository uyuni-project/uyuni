/**
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

import com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler;
import com.redhat.rhn.frontend.xmlrpc.admin.monitoring.AdminMonitoringHandler;
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
import com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringActionHandler;
import com.redhat.rhn.frontend.xmlrpc.satellite.SatelliteHandler;
import com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler;
import com.redhat.rhn.frontend.xmlrpc.subscriptionmatching.PinnedSubscriptionHandler;
import com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler;
import com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler;
import com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler;
import com.redhat.rhn.frontend.xmlrpc.system.SystemHandler;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler;
import com.redhat.rhn.frontend.xmlrpc.system.crash.CrashHandler;
import com.redhat.rhn.frontend.xmlrpc.system.custominfo.CustomInfoHandler;
import com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler;
import com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler;
import com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler;
import com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler;
import com.redhat.rhn.frontend.xmlrpc.taskomatic.TaskomaticHandler;
import com.redhat.rhn.frontend.xmlrpc.taskomatic.TaskomaticOrgHandler;
import com.redhat.rhn.frontend.xmlrpc.user.UserHandler;
import com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler;
import com.redhat.rhn.frontend.xmlrpc.virtualhostmanager.VirtualHostManagerHandler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.suse.manager.webui.controllers.utils.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.utils.SSHMinionBootstrapper;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.impl.SaltService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * HandlerFactory for XMLRPC Handlers.
 *
 * @version $Rev$
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
        SystemQuery systemQuery = SaltService.INSTANCE;
        RegularMinionBootstrapper regularMinionBootstrapper = RegularMinionBootstrapper.getInstance(systemQuery);
        SSHMinionBootstrapper sshMinionBootstrapper = SSHMinionBootstrapper.getInstance(systemQuery);
        XmlRpcSystemHelper xmlRpcSystemHelper = new XmlRpcSystemHelper(
                regularMinionBootstrapper,
                sshMinionBootstrapper
        );
        ProxyHandler proxyHandler = new ProxyHandler(xmlRpcSystemHelper);

        factory.addHandler("actionchain", new ActionChainHandler());
        factory.addHandler("activationkey", new ActivationKeyHandler());
        factory.addHandler("admin.monitoring", new AdminMonitoringHandler());
        factory.addHandler("api", new ApiHandler(factory));
        factory.addHandler("audit", new CVEAuditHandler());
        factory.addHandler("auth", new AuthHandler());
        factory.addHandler("channel", new ChannelHandler());
        factory.addHandler("channel.access", new ChannelAccessHandler());
        factory.addHandler("channel.org", new ChannelOrgHandler());
        factory.addHandler("channel.software", new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper));
        factory.addHandler("configchannel", new ConfigChannelHandler());
        factory.addHandler("contentmanagement", new ContentManagementHandler());
        factory.addHandler("distchannel", new DistChannelHandler());
        factory.addHandler("errata", new ErrataHandler());
        factory.addHandler("formula", new FormulaHandler());
        factory.addHandler("image.store", new ImageStoreHandler());
        factory.addHandler("image.profile", new ImageProfileHandler());
        factory.addHandler("image", new ImageInfoHandler());
        factory.addHandler("kickstart", new KickstartHandler());
        factory.addHandler("kickstart.filepreservation", new FilePreservationListHandler());
        factory.addHandler("kickstart.keys", new CryptoKeysHandler());
        factory.addHandler("kickstart.profile", new ProfileHandler());
        factory.addHandler("kickstart.profile.keys", new KeysHandler());
        factory.addHandler("kickstart.profile.software", new SoftwareHandler());
        factory.addHandler("kickstart.profile.system", new SystemDetailsHandler());
        factory.addHandler("kickstart.snippet", new SnippetHandler());
        factory.addHandler("kickstart.tree", new KickstartTreeHandler());
        factory.addHandler("org", new OrgHandler());
        factory.addHandler("org.trusts", new OrgTrustHandler());
        factory.addHandler("packages", new PackagesHandler());
        factory.addHandler("packages.provider", new PackagesProviderHandler());
        factory.addHandler("packages.search", new PackagesSearchHandler());
        factory.addHandler("preferences.locale", new PreferencesLocaleHandler());
        factory.addHandler("proxy", proxyHandler);
        factory.addHandler("recurringaction", new RecurringActionHandler());
        factory.addHandler("satellite", new SatelliteHandler(proxyHandler));
        factory.addHandler("schedule", new ScheduleHandler());
        factory.addHandler("subscriptionmatching.pinnedsubscription", new PinnedSubscriptionHandler());
        factory.addHandler("sync.master", new MasterHandler());
        factory.addHandler("sync.slave", new SlaveHandler());
        factory.addHandler("sync.content", new ContentSyncHandler());
        factory.addHandler("system", new SystemHandler(taskomaticApi, xmlRpcSystemHelper));
        factory.addHandler("system.config", new ServerConfigHandler(taskomaticApi, xmlRpcSystemHelper));
        factory.addHandler("system.crash", new CrashHandler(xmlRpcSystemHelper));
        factory.addHandler("system.custominfo", new CustomInfoHandler());
        factory.addHandler("system.provisioning.snapshot", new SnapshotHandler(xmlRpcSystemHelper));
        factory.addHandler("system.scap", new SystemScapHandler());
        factory.addHandler("system.search", new SystemSearchHandler());
        factory.addHandler("virtualhostmanager", new VirtualHostManagerHandler());
        factory.addHandler("systemgroup", new ServerGroupHandler(xmlRpcSystemHelper));
        factory.addHandler("taskomatic", new TaskomaticHandler());
        factory.addHandler("taskomatic.org", new TaskomaticOrgHandler());
        factory.addHandler("user", new UserHandler());
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
