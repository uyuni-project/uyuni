/*
 * Copyright (c) 2026 SUSE LLC
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
package com.suse.manager.api;

import com.redhat.rhn.frontend.xmlrpc.access.AccessHandler;
import com.redhat.rhn.frontend.xmlrpc.admin.configuration.AdminConfigurationHandler;
import com.redhat.rhn.frontend.xmlrpc.admin.monitoring.AdminMonitoringHandler;
import com.redhat.rhn.frontend.xmlrpc.admin.ssh.AdminSshHandler;
import com.redhat.rhn.frontend.xmlrpc.ansible.AnsibleHandler;
import com.redhat.rhn.frontend.xmlrpc.api.ApiHandler;
import com.redhat.rhn.frontend.xmlrpc.audit.CVEAuditHandler;
import com.redhat.rhn.frontend.xmlrpc.chain.ActionChainHandler;
import com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler;
import com.redhat.rhn.frontend.xmlrpc.channel.access.ChannelAccessHandler;
import com.redhat.rhn.frontend.xmlrpc.channel.appstreams.ChannelAppStreamHandler;
import com.redhat.rhn.frontend.xmlrpc.channel.org.ChannelOrgHandler;
import com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler;
import com.redhat.rhn.frontend.xmlrpc.contentmgmt.ContentManagementHandler;
import com.redhat.rhn.frontend.xmlrpc.distchannel.DistChannelHandler;
import com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler;
import com.redhat.rhn.frontend.xmlrpc.formula.FormulaHandler;
import com.redhat.rhn.frontend.xmlrpc.image.DeltaImageInfoHandler;
import com.redhat.rhn.frontend.xmlrpc.image.ImageInfoHandler;
import com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler;
import com.redhat.rhn.frontend.xmlrpc.image.store.ImageStoreHandler;
import com.redhat.rhn.frontend.xmlrpc.kickstart.filepreservation.FilePreservationListHandler;
import com.redhat.rhn.frontend.xmlrpc.kickstart.keys.CryptoKeysHandler;
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
import com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringActionHandler;
import com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringCustomStateHandler;
import com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringHighstateHandler;
import com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringPlaybookHandler;
import com.redhat.rhn.frontend.xmlrpc.saltkey.SaltKeyHandler;
import com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler;
import com.redhat.rhn.frontend.xmlrpc.subscriptionmatching.PinnedSubscriptionHandler;
import com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler;
import com.redhat.rhn.frontend.xmlrpc.system.appstreams.SystemAppStreamHandler;
import com.redhat.rhn.frontend.xmlrpc.system.custominfo.CustomInfoHandler;
import com.redhat.rhn.frontend.xmlrpc.system.monitoring.SystemMonitoringHandler;
import com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler;
import com.redhat.rhn.frontend.xmlrpc.system.search.SystemSearchHandler;
import com.redhat.rhn.frontend.xmlrpc.user.UserNotificationsHandler;
import com.redhat.rhn.frontend.xmlrpc.user.external.UserExternalHandler;
import com.redhat.rhn.frontend.xmlrpc.virtualhostmanager.VirtualHostManagerHandler;

import com.suse.manager.api.docs.UyuniSwaggerReader;
import com.suse.manager.xmlrpc.admin.AdminPaygHandler;
import com.suse.manager.xmlrpc.iss.HubHandler;
import com.suse.manager.xmlrpc.maintenance.MaintenanceHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * Generates the OpenAPI specification for supported XML-RPC handlers.
 */
public final class OpenApiConfig {

    private static final UyuniSwaggerReader READER = new UyuniSwaggerReader();
    private static final Logger LOGGER = LogManager.getLogger(OpenApiConfig.class);

    /**
     * Utility class.
     */
    private OpenApiConfig() {
    }

    /**
     * Writes the generated OpenAPI specification to the configured output directory.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        String outputDirPath = System.getProperty("apidoc.output");

        if (outputDirPath == null || outputDirPath.isEmpty()) {
            LOGGER.error("Missing or empty 'apidoc.output' system property.");
            System.exit(1);
        }

        File outputDir = new File(outputDirPath);
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            LOGGER.error("Failed to create output directory: " + outputDirPath);
            System.exit(1);
        }

        if (!outputDir.isDirectory()) {
            LOGGER.error("The path is not a directory: " + outputDirPath);
            System.exit(1);
        }

        String json = Json.pretty(processHandlers());

        File outFile = new File(outputDir, "openapi.json");
        try (FileWriter writer = new FileWriter(outFile)) {
            writer.write(json);
            LOGGER.info("openapi.json written to: " + outFile.getAbsolutePath());
        }
        catch (IOException e) {
            LOGGER.error("Failed to write openapi.json: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Processes configured handlers and returns the generated OpenAPI specification.
     *
     * @return generated OpenAPI specification
     */
    public static OpenAPI processHandlers() {
        getHandlerClasses().forEach((key, value) -> READER.read(value, key));
        return READER.getSpec();
    }

    /**
     * Returns a map of API namespaces to their corresponding handler classes.
     *
     * @return API namespaces mapped to handler classes
     */
    public static Map<String, Class<?>> getHandlerClasses() {
        Map<String, Class<?>> handlers = new LinkedHashMap<>();
        handlers.put("access", AccessHandler.class);
        handlers.put("actionchain", ActionChainHandler.class);
        handlers.put("admin.configuration", AdminConfigurationHandler.class);
        handlers.put("admin.monitoring", AdminMonitoringHandler.class);
        handlers.put("admin.payg", AdminPaygHandler.class);
        handlers.put("admin.ssh", AdminSshHandler.class);
        handlers.put("ansible", AnsibleHandler.class);
        handlers.put("api", ApiHandler.class);
        handlers.put("audit", CVEAuditHandler.class);
        handlers.put("channel", ChannelHandler.class);
        handlers.put("channel.access", ChannelAccessHandler.class);
        handlers.put("channel.appstreams", ChannelAppStreamHandler.class);
        handlers.put("channel.org", ChannelOrgHandler.class);
        handlers.put("channel.software", ChannelSoftwareHandler.class);
        handlers.put("contentmanagement", ContentManagementHandler.class);
        handlers.put("distchannel", DistChannelHandler.class);
        handlers.put("errata", ErrataHandler.class);
        handlers.put("formula", FormulaHandler.class);
        handlers.put("image", ImageInfoHandler.class);
        handlers.put("image.delta", DeltaImageInfoHandler.class);
        handlers.put("image.profile", ImageProfileHandler.class);
        handlers.put("image.store", ImageStoreHandler.class);
        handlers.put("kickstart.filepreservation", FilePreservationListHandler.class);
        handlers.put("kickstart.keys", CryptoKeysHandler.class);
        handlers.put("kickstart.profile.software", SoftwareHandler.class);
        handlers.put("kickstart.profile.system", SystemDetailsHandler.class);
        handlers.put("kickstart.snippet", SnippetHandler.class);
        handlers.put("kickstart.tree", KickstartTreeHandler.class);
        handlers.put("maintenance", MaintenanceHandler.class);
        handlers.put("org", OrgHandler.class);
        handlers.put("org.trusts", OrgTrustHandler.class);
        handlers.put("packages", PackagesHandler.class);
        handlers.put("packages.provider", PackagesProviderHandler.class);
        handlers.put("packages.search", PackagesSearchHandler.class);
        handlers.put("preferences.locale", PreferencesLocaleHandler.class);
        handlers.put("recurring", RecurringActionHandler.class);
        handlers.put("recurring.custom", RecurringCustomStateHandler.class);
        handlers.put("recurring.highstate", RecurringHighstateHandler.class);
        handlers.put("recurring.playbook", RecurringPlaybookHandler.class);
        handlers.put("saltkey", SaltKeyHandler.class);
        handlers.put("schedule", ScheduleHandler.class);
        handlers.put("subscriptionmatching.pinnedsubscription", PinnedSubscriptionHandler.class);
        handlers.put("system.appstreams", SystemAppStreamHandler.class);
        handlers.put("system.custominfo", CustomInfoHandler.class);
        handlers.put("system.monitoring", SystemMonitoringHandler.class);
        handlers.put("system.scap", SystemScapHandler.class);
        handlers.put("system.search", SystemSearchHandler.class);
        handlers.put("sync.content", ContentSyncHandler.class);
        handlers.put("sync.hub", HubHandler.class);
        handlers.put("user.external", UserExternalHandler.class);
        handlers.put("user.notifications", UserNotificationsHandler.class);
        handlers.put("virtualhostmanager", VirtualHostManagerHandler.class);
        return handlers;
    }
}
