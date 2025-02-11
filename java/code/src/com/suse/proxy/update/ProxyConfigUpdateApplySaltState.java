/*
 * Copyright (c) 2025 SUSE LLC
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

package com.suse.proxy.update;

import static com.suse.utils.Predicates.isAbsent;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ProxyConfigurationApplyAction;
import com.redhat.rhn.manager.action.ActionManager;

import com.suse.salt.netapi.calls.LocalCall;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;

/**
 * Applies proxy configuration salt state
 */
public class ProxyConfigUpdateApplySaltState implements ProxyConfigUpdateContextHandler {
    private static final Logger LOG = LogManager.getLogger(ProxyConfigUpdateApplySaltState.class);

    public static final String FAIL_APPLY_MESSAGE = "Failed to apply proxy configuration salt state.";

    @Override
    public void handle(ProxyConfigUpdateContext context) {
        ProxyConfigurationApplyAction action =
                new ProxyConfigurationApplyAction(context.getPillar(), context.getProxyConfigFiles());
        action.setActionType(ActionFactory.TYPE_PROXY_CONFIGURATION_APPLY);
        action.setOrg(context.getUser().getOrg());
        action.setName("Apply proxy configuration: " + context.getProxyMinion().getMinionId());
        ActionManager.addServerToAction(context.getProxyMinion(), action);
        Map<LocalCall<?>, Optional<JsonElement>> applySaltStateResponse =
                GlobalInstanceHolder.SALT_SERVER_ACTION_SERVICE.executeSSHAction(action, context.getProxyMinion());

        if (isAbsent(applySaltStateResponse)) {
            context.getErrorReport().register(FAIL_APPLY_MESSAGE);
            LOG.error("Failed to apply proxy configuration salt state. No response.");
            return;
        }
        else if (applySaltStateResponse.size() != 1) {
            context.getErrorReport().register(FAIL_APPLY_MESSAGE);
            LOG.error("Failed to apply proxy configuration salt state. Unexpected response size. {}", applySaltStateResponse);
            return;
        }

        JsonElement jsonElement = applySaltStateResponse.values().iterator().next().orElse(null);
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            context.getErrorReport().register(FAIL_APPLY_MESSAGE);
            LOG.error("Failed to apply proxy configuration salt state. Unexpected response format. {}", jsonElement);
            return;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        for (String key : jsonObject.keySet()) {
            if (!jsonObject.get(key).getAsJsonObject().get("result").getAsBoolean()) {
                context.getErrorReport().register(FAIL_APPLY_MESSAGE);
                LOG.error("Failed to apply proxy configuration salt state. Failing entry: {}", jsonElement);
            }
        }

    }
}
