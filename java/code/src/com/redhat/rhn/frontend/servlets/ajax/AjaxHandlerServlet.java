/*
 * Copyright (c) 2022 SUSE LLC
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
package com.redhat.rhn.frontend.servlets.ajax;

import com.redhat.rhn.frontend.action.renderers.ActionChainEntryRenderer;
import com.redhat.rhn.frontend.action.renderers.CriticalSystemsRenderer;
import com.redhat.rhn.frontend.action.renderers.InactiveSystemsRenderer;
import com.redhat.rhn.frontend.action.renderers.LatestErrataRenderer;
import com.redhat.rhn.frontend.action.renderers.PendingActionsRenderer;
import com.redhat.rhn.frontend.action.renderers.RecentSystemsRenderer;
import com.redhat.rhn.frontend.action.renderers.SubscriptionWarningRenderer;
import com.redhat.rhn.frontend.action.renderers.SystemGroupsRenderer;
import com.redhat.rhn.frontend.action.renderers.TasksRenderer;
import com.redhat.rhn.frontend.action.renderers.setupwizard.MirrorCredentialsRenderer;
import com.redhat.rhn.frontend.action.renderers.setupwizard.ProxySettingsRenderer;
import com.redhat.rhn.frontend.action.schedule.ActionChainSaveAction;
import com.redhat.rhn.frontend.servlets.ajax.dto.ActionChainEntriesDto;
import com.redhat.rhn.frontend.servlets.ajax.dto.ActionChainSaveDto;
import com.redhat.rhn.frontend.servlets.ajax.dto.DeleteMirrorCredentialsDto;
import com.redhat.rhn.frontend.servlets.ajax.dto.ItemSelectorDto;
import com.redhat.rhn.frontend.servlets.ajax.dto.ListMirrorSubscriptionsDto;
import com.redhat.rhn.frontend.servlets.ajax.dto.MakePrimaryMirrorCredentialsDto;
import com.redhat.rhn.frontend.servlets.ajax.dto.SaveMirrorCredentialsDto;
import com.redhat.rhn.frontend.servlets.ajax.dto.VerifyMirrorCredentialsDto;
import com.redhat.rhn.frontend.servlets.ajax.dto.VerifyProxySettingsDto;
import com.redhat.rhn.frontend.taglibs.ItemSelector;
import com.redhat.rhn.manager.setup.ProxySettingsDto;

import com.google.gson.Gson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handler for ajax requests previously handled by the DWR library that was removed
 */
@WebServlet("/ajax/*")
public class AjaxHandlerServlet extends HttpServlet {

    private static Logger logger = LogManager.getLogger(AjaxHandlerServlet.class);
    private static final Map<String, ProcessAjaxRequest> HANDLERS = new HashMap<>();
    public static final String AJAX_PREFIX = "ajax/";
    private static Gson gson = new Gson();
    private static MirrorCredentialsRenderer mirrorCredentialsRenderer = new MirrorCredentialsRenderer();
    private static SystemGroupsRenderer systemGroupsRenderer = new SystemGroupsRenderer();
    private static TasksRenderer tasksRenderer = new TasksRenderer();
    private static InactiveSystemsRenderer inactiveSystemsRenderer = new InactiveSystemsRenderer();
    private static CriticalSystemsRenderer criticalSystemsRenderer = new CriticalSystemsRenderer();
    private static PendingActionsRenderer pendingActionsRenderer = new PendingActionsRenderer();
    private static LatestErrataRenderer latestErrataRenderer = new LatestErrataRenderer();
    private static RecentSystemsRenderer recentSystemsRenderer = new RecentSystemsRenderer();
    private static ProxySettingsRenderer proxySettingsRenderer = new ProxySettingsRenderer();
    private static ActionChainEntryRenderer actionChainEntryRenderer = new ActionChainEntryRenderer();
    private static ActionChainSaveAction actionChainSaveAction = new ActionChainSaveAction();
    private static SubscriptionWarningRenderer subscriptionWarningRenderer = new SubscriptionWarningRenderer();

    @FunctionalInterface
    interface ProcessAjaxRequest {
        String doProcess(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
    }

    static {
        HANDLERS.put("systems-groups", systemGroupsRenderer::renderAsync);
        HANDLERS.put("tasks", tasksRenderer::renderAsync);
        HANDLERS.put("inactive-systems", inactiveSystemsRenderer::renderAsync);
        HANDLERS.put("critical-systems", criticalSystemsRenderer::renderAsync);
        HANDLERS.put("pending-actions", pendingActionsRenderer::renderAsync);
        HANDLERS.put("latest-errata", latestErrataRenderer::renderAsync);
        HANDLERS.put("recent-systems", recentSystemsRenderer::renderAsync);
        HANDLERS.put("subscription-warning", subscriptionWarningRenderer::renderAsync);

        HANDLERS.put("retrieve-proxy-settings",
            (req, resp) -> gson.toJson(proxySettingsRenderer.retrieveProxySettings()));
        HANDLERS.put("verify-proxy-settings",
            (req, resp) -> gson.toJson(
                proxySettingsRenderer.verifyProxySettings(
                    req, parseBody(req, VerifyProxySettingsDto.class).isForceRefresh()
                )
            )
        );
        HANDLERS.put("save-proxy-settings",
            (req, resp) -> gson.toJson(
                proxySettingsRenderer.saveProxySettings(req, parseBody(req, ProxySettingsDto.class))
            )
        );

        HANDLERS.put("verify-mirror-credentials", (req, resp) -> {
            VerifyMirrorCredentialsDto dto = parseBody(req, VerifyMirrorCredentialsDto.class);
            return mirrorCredentialsRenderer.verifyCredentials(req, resp, dto.getId(), dto.isRefresh());
        });
        HANDLERS.put("list-mirror-subscriptions", (req, resp) -> {
            ListMirrorSubscriptionsDto dto = parseBody(req, ListMirrorSubscriptionsDto.class);
            return mirrorCredentialsRenderer.listSubscriptions(req, resp, dto.getSubscriptionsId());
        });
        HANDLERS.put("render-mirror-credentials", mirrorCredentialsRenderer::renderCredentials);
        HANDLERS.put("save-mirror-credentials", (req, resp) -> {
            SaveMirrorCredentialsDto dto = parseBody(req, SaveMirrorCredentialsDto.class);
            return mirrorCredentialsRenderer.saveCredentials(req, dto.getId(), dto.getUser(), dto.getPassword());
        });
        HANDLERS.put("make-primary-mirror-credentials", (req, resp) -> {
            MakePrimaryMirrorCredentialsDto dto = parseBody(req, MakePrimaryMirrorCredentialsDto.class);
            return mirrorCredentialsRenderer.makePrimaryCredentials(req, resp, dto.getId());
        });
        HANDLERS.put("delete-mirror-credentials", (req, resp) -> {
            DeleteMirrorCredentialsDto dto = parseBody(req, DeleteMirrorCredentialsDto.class);
            return mirrorCredentialsRenderer.deleteCredentials(req, resp, dto.getId());
        });

        HANDLERS.put("action-chain-entries", (req, resp) -> {
            ActionChainEntriesDto dto = parseBody(req, ActionChainEntriesDto.class);
            return actionChainEntryRenderer.renderAsync(req, resp, dto.getActionChainId(), dto.getSortOrder());
        });
        HANDLERS.put("action-chain-save", ((req, resp) -> {
            ActionChainSaveDto dto = parseBody(req, ActionChainSaveDto.class);
            return actionChainSaveAction.save(
                dto.getActionChainId(),
                dto.getLabel(),
                dto.getDeletedEntries(),
                dto.getDeletedSortOrders(),
                dto.getReorderedSortOrders(),
                req
            );
        }));

        HANDLERS.put("item-selector", (req, resp) -> {
            ItemSelectorDto item = parseBody(req, ItemSelectorDto.class);
            return gson.toJson(new ItemSelector().select(req, item.getLabel(), item.getValues(), item.isChecked()));
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String url = req.getRequestURL().toString().split(AJAX_PREFIX)[1];
        try {
            String response = HANDLERS.get(url).doProcess(req, resp);
            resp.getOutputStream().print(response);
            resp.getOutputStream().close();
        }
        catch (ServletException | IOException e) {
            logger.error("Error processing ajax request.", e);
        }
    }

    private static <T> T parseBody(HttpServletRequest req, Class<T> type) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return gson.fromJson(sb.toString(), type);
    }
}
