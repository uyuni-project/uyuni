/*
 * Copyright (c) 2024--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.domain.action;


import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionSummary;

import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.model.attestation.CoCoAttestationStatus;
import com.suse.manager.model.attestation.ServerCoCoAttestationReport;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.SaltParameters;
import com.suse.manager.webui.utils.salt.custom.CoCoAttestationRequestData;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.utils.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * CoCoAttestationAction - Class representing TYPE_COCO_ATTESTATION
 */
@Entity
@DiscriminatorValue("523")
public class CoCoAttestationAction extends Action {
    private static final Logger LOG = LogManager.getLogger(CoCoAttestationAction.class);

    @Override
    public void onFailAction(ServerAction serverActionIn) {
        AttestationManager attestationManager = GlobalInstanceHolder.ATTESTATION_MANAGER;
        if (!Objects.equals(serverActionIn.getParentAction(), this)) {
            LOG.error("This is not the action which belongs to the passed server action");
            return;
        }
        try {
            Optional<ServerCoCoAttestationReport> report = attestationManager.lookupReportByServerAndAction(
                    serverActionIn.getServer(), this);
            report.ifPresent(rep -> {
                if (rep.getResults().isEmpty()) {
                    // results are not initialized yet. So we need to set the report status
                    // directly to failed.
                    rep.setStatus(CoCoAttestationStatus.FAILED);
                }
            });
        }
        catch (Exception e) {
            LOG.log(Level.ERROR, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        return Map.of(
                State.apply(Collections.singletonList(SaltParameters.COCOATTEST_REQUESTDATA), Optional.empty()),
                minionSummaries
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        AttestationManager mgr = new AttestationManager();

        Optional<ServerCoCoAttestationReport> optReport =
                mgr.lookupReportByServerAndAction(serverAction.getServer(), this);
        if (optReport.isEmpty()) {
            serverAction.setStatusFailed();
            serverAction.setResultMsg("Failed to find a report entry");
            return;
        }
        ServerCoCoAttestationReport report = optReport.get();

        if (jsonResult == null) {
            serverAction.setStatusFailed();
            if (StringUtils.isBlank(serverAction.getResultMsg())) {
                serverAction.setResultMsg("Error while request attestation data from target system:\n" +
                        "Got no result from system");
            }
            return;
        }

        try {
            CoCoAttestationRequestData requestData = Json.GSON.fromJson(jsonResult, CoCoAttestationRequestData.class);
            report.setOutData(requestData.asMap());
            mgr.initializeResults(report);
        }
        catch (JsonSyntaxException e) {
            String msg = "Failed to parse the attestation result:\n";
            msg += Optional.of(jsonResult)
                    .map(JsonElement::toString)
                    .orElse("Got no result");
            LOG.error(msg);
            serverAction.setStatusFailed();
            serverAction.setResultMsg(msg);
            return;
        }
        if (serverAction.isStatusFailed()) {
            String msg = "Error while request attestation data from target system:\n";
            msg += SaltUtils.getJsonResultWithPrettyPrint(jsonResult);
            serverAction.setResultMsg(msg);
            if (report.getResults().isEmpty()) {
                // results are not initialized yet. So we need to set the report status
                // directly to failed.
                report.setStatus(CoCoAttestationStatus.FAILED);
            }
        }
        else {
            serverAction.setResultMsg("Successfully collected attestation data");
        }
    }
}
