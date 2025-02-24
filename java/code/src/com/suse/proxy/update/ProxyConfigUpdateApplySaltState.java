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

import com.redhat.rhn.domain.action.ProxyConfigurationApplyAction;

import com.suse.salt.netapi.calls.modules.State;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Applies proxy configuration salt state
 */
public class ProxyConfigUpdateApplySaltState implements ProxyConfigUpdateContextHandler {
    private static final Logger LOG = LogManager.getLogger(ProxyConfigUpdateApplySaltState.class);

    private static final String FAIL_APPLY_MESSAGE = "Failed to apply proxy configuration salt state.";

    @Override
    public void handle(ProxyConfigUpdateContext context) {

        ProxyConfigurationApplyAction action = new ProxyConfigurationApplyAction(
                context.getPillar(),
                context.getProxyConfigFiles(),
                context.getUser().getOrg()
        );

        Optional<Map<String, State.ApplyResult>> stringApplyResultMap = context.getSaltApi().callSync(
                        action.getApplyProxyConfigCall(),
                        context.getProxyMinion().getMinionId())
                .map(
                        result -> result.fold(
                                error -> {
                                    context.getErrorReport().register(error);
                                    return null;
                                },
                                applyResults -> {
                                    if (applyResults.isEmpty()) {
                                        context.getErrorReport().register(FAIL_APPLY_MESSAGE);
                                        LOG.error(
                                                FAIL_APPLY_MESSAGE + " Unexpected response size. {}",
                                                applyResults.size()
                                        );
                                        return null;
                                    }

                                    List<String> failedStates = applyResults.entrySet().stream()
                                            .filter(p -> !p.getValue().isResult())
                                            .map(e ->
                                                    String.format(
                                                            "name: %s, comment: %s",
                                                            e.getKey(),
                                                            e.getValue().getComment()
                                                    ))
                                            .toList();
                                    if (!failedStates.isEmpty()) {
                                        context.getErrorReport().register(FAIL_APPLY_MESSAGE);
                                        LOG.debug(FAIL_APPLY_MESSAGE + " Fail applying: {}", failedStates);
                                    }
                                    return applyResults;
                                }));

        if (stringApplyResultMap.isEmpty()) {
            context.getErrorReport().register(FAIL_APPLY_MESSAGE);
            LOG.error(FAIL_APPLY_MESSAGE + " No apply results.");
        }
    }
}
