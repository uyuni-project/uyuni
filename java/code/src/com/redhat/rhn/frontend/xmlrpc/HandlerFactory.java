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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * HandlerFactory, simple factory class that uses ManifestFactory to
 * return RPC Handlers.
 *
 * @version $Rev$
 */

public class HandlerFactory {
    private final Map<String, BaseHandler> handlers;

    public HandlerFactory() {
        this.handlers = new HashMap<>();
    }

    public HandlerFactory(Map<String, BaseHandler> handlers) {
        this.handlers = handlers;
    }

    public static HandlerFactory mockHandlers() {
        HandlerFactory factory = new HandlerFactory();
        factory.addHandler("registration", new com.redhat.rhn.frontend.xmlrpc.test.RegistrationHandler());
        factory.addHandler("unittest", new com.redhat.rhn.frontend.xmlrpc.test.UnitTestHandler());
        return factory;
    }

    public void addHandler(String namespace, BaseHandler handler) {
       handlers.put(namespace, handler);
    }

    public static HandlerFactory  defaultHandlers() {
        HandlerFactory factory = new HandlerFactory();
        factory.addHandler("actionchain", new com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler());
        factory.addHandler("activationkey", new com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler());
        factory.addHandler("admin.monitoring", new com.redhat.rhn.frontend.xmlrpc.admin.monitoring.AdminMonitoringHandler());
        factory.addHandler("api", new com.redhat.rhn.frontend.xmlrpc.api.ApiHandler(factory));
        factory.addHandler("audit", new com.redhat.rhn.frontend.xmlrpc.audit.CVEAuditHandler());
        factory.addHandler("auth", new com.redhat.rhn.frontend.xmlrpc.auth.AuthHandler());
        factory.addHandler("channel", new com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler());
        factory.addHandler("channel.access", new com.redhat.rhn.frontend.xmlrpc.channel.access.ChannelAccessHandler());
        factory.addHandler("channel.org", new com.redhat.rhn.frontend.xmlrpc.channel.org.ChannelOrgHandler());
        factory.addHandler("channel.software", new com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler());
        factory.addHandler("configchannel", new com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler());
        factory.addHandler("contentmanagement", new com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler());
        factory.addHandler("distchannel", new com.redhat.rhn.frontend.xmlrpc.distchannel.DistChannelHandler());
        factory.addHandler("errata", new com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler());
        factory.addHandler("formula", new com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler());
        factory.addHandler("image.store", new com.redhat.rhn.frontend.xmlrpc.image.store.ImageStoreHandler());
        factory.addHandler("image.profile", new com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler());
        factory.addHandler("image", new com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler());
        factory.addHandler("kickstart", new com.redhat.rhn.frontend.xmlrpc.kickstart.KickstartHandler());
        factory.addHandler("kickstart.filepreservation", new com.redhat.rhn.frontend.xmlrpc.kickstart.filepreservation.FilePreservationListHandler());
        factory.addHandler("kickstart.keys", new com.redhat.rhn.frontend.xmlrpc.kickstart.keys.CryptoKeysHandler());
        factory.addHandler("kickstart.profile", new com.redhat.rhn.frontend.xmlrpc.kickstart.profile.ProfileHandler());
        factory.addHandler("kickstart.profile.keys", new com.redhat.rhn.frontend.xmlrpc.kickstart.profile.keys.KeysHandler());
        factory.addHandler("kickstart.profile.software", new com.redhat.rhn.frontend.xmlrpc.kickstart.profile.software.SoftwareHandler());
        factory.addHandler("kickstart.profile.system", new com.redhat.rhn.frontend.xmlrpc.kickstart.profile.system.SystemDetailsHandler());
        factory.addHandler("kickstart.snippet", new com.redhat.rhn.frontend.xmlrpc.kickstart.snippet.SnippetHandler());
        factory.addHandler("kickstart.tree", new com.redhat.rhn.frontend.xmlrpc.kickstart.tree.KickstartTreeHandler());
        factory.addHandler("org", new com.redhat.rhn.frontend.xmlrpc.org.OrgHandler());
        factory.addHandler("org.trusts", new com.redhat.rhn.frontend.xmlrpc.org.trusts.OrgTrustHandler());
        factory.addHandler("packages", new com.redhat.rhn.frontend.xmlrpc.packages.PackagesHandler());
        factory.addHandler("packages.provider", new com.redhat.rhn.frontend.xmlrpc.packages.provider.PackagesProviderHandler());
        factory.addHandler("packages.search", new com.redhat.rhn.frontend.xmlrpc.packages.search.PackagesSearchHandler());
        factory.addHandler("preferences.locale", new com.redhat.rhn.frontend.xmlrpc.preferences.locale.PreferencesLocaleHandler());
        factory.addHandler("proxy", new com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler());
        factory.addHandler("satellite", new com.redhat.rhn.frontend.xmlrpc.satellite.SatelliteHandler());
        factory.addHandler("schedule", new com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler());
        factory.addHandler("subscriptionmatching.pinnedsubscription", new com.redhat.rhn.frontend.xmlrpc.subscriptionmatching.PinnedSubscriptionHandler());
        factory.addHandler("sync.master", new com.redhat.rhn.frontend.xmlrpc.sync.master.MasterHandler());
        factory.addHandler("sync.slave", new com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler());
        factory.addHandler("sync.content", new com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler());
        factory.addHandler("system", new com.redhat.rhn.frontend.xmlrpc.system.SystemHandler());
        factory.addHandler("system.config", new com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler());
        factory.addHandler("system.crash", new com.redhat.rhn.frontend.xmlrpc.system.crash.CrashHandler());
        factory.addHandler("system.custominfo", new com.redhat.rhn.frontend.xmlrpc.system.custominfo.CustomInfoHandler());
        factory.addHandler("system.provisioning.snapshot", new com.redhat.rhn.frontend.xmlrpc.system.provisioning.snapshot.SnapshotHandler());
        factory.addHandler("system.scap", new com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler());
        factory.addHandler("system.search", new com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler());
        factory.addHandler("virtualhostmanager", new com.redhat.rhn.frontend.xmlrpc.virtualhostmanager.VirtualHostManagerHandler());
        factory.addHandler("systemgroup", new com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler());
        factory.addHandler("taskomatic", new com.redhat.rhn.frontend.xmlrpc.taskomatic.TaskomaticHandler());
        factory.addHandler("taskomatic.org", new com.redhat.rhn.frontend.xmlrpc.taskomatic.TaskomaticOrgHandler());
        factory.addHandler("user", new com.redhat.rhn.frontend.xmlrpc.user.UserHandler());
        factory.addHandler("user.external", new com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler());
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
