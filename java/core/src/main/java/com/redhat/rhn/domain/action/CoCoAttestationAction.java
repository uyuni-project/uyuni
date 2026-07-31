/*
 * Copyright (c) 2024--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.action;


import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionSummary;

import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.model.attestation.ServerCoCoAttestationReport;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.SaltParameters;
import com.suse.manager.webui.utils.salt.custom.coco.CoCoAttestationResponseDataParser;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * CoCoAttestationAction - Class representing TYPE_COCO_ATTESTATION
 */
@Entity
@DiscriminatorValue("523")
public class CoCoAttestationAction extends Action {
    private static final Logger LOG = LogManager.getLogger(CoCoAttestationAction.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReadyToRun() {
        AttestationManager attestationManager = GlobalInstanceHolder.ATTESTATION_MANAGER;
        List<ServerCoCoAttestationReport> actionReportList =
                attestationManager.listCoCoAttestationReportsForAction(this);
        if (actionReportList.isEmpty()) {
            LOG.debug("Failed to find a report entry while checking for input data");
            return false;
        }

        return actionReportList.stream().allMatch(attestationManager::hasAllInputDataFromResults);
    }

    @Override
    public void onFailAction(ServerAction serverActionIn) {
        if (!Objects.equals(serverActionIn.getParentAction(), this)) {
            LOG.error("This is not the action which belongs to the passed server action");
            return;
        }

        AttestationManager attestationManager = GlobalInstanceHolder.ATTESTATION_MANAGER;
        Optional<ServerCoCoAttestationReport> optReport =
                attestationManager.lookupReportByServerAndAction(serverActionIn.getServer(), this);
        if (optReport.isEmpty()) {
            LOG.warn("Failed to find a report entry while reacting to a failed server action");
            return;
        }
        ServerCoCoAttestationReport report = optReport.get();

        attestationManager.setFailed(report, serverActionIn.getResultMsg());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        AttestationManager attestationManager = GlobalInstanceHolder.ATTESTATION_MANAGER;
        List<ServerCoCoAttestationReport> actionReportList =
                attestationManager.listCoCoAttestationReportsForAction(this);
        if (actionReportList.isEmpty()) {
            LOG.warn("Failed to find any report entry while creating the pillar data");
            return Map.of();
        }

        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        minionSummaries.forEach(minionSummary -> {

            Optional<ServerCoCoAttestationReport> optReport = actionReportList.stream()
                    .filter(r -> r.getServer().getId().equals(minionSummary.getServerId()))
                    .findAny();

            Optional<Map<String, Object>> pillarData = Optional.empty();
            if (optReport.isPresent()) {
                ServerCoCoAttestationReport report = optReport.get();
                attestationManager.mergeInputDataFromResults(report);

                //pillar data sent to minion during coco attestation is cryptographically safe by design!
                pillarData = createPillarData(report);
            }
            else {
                LOG.debug("Failed to find a report entry while creating the pillar data for minion {}",
                        minionSummary);
            }

            ret.put(State.apply(Collections.singletonList(SaltParameters.COCOATTEST_REQUESTDATA), pillarData),
                    List.of(minionSummary));
        });

        return ret;
    }

    private Optional<Map<String, Object>> createPillarData(ServerCoCoAttestationReport report) {
        Map<String, Object> attestationPillar = new HashMap<>(report.getInData());
        attestationPillar.put("environment_type", report.getEnvironmentType().name());

        List<String> resultTypes = report.getEnvironmentType().getSupportedResultTypes().stream()
                .map(Enum::name).toList();
        attestationPillar.put("result_types", resultTypes);

        Map<String, Object> pillarData = new HashMap<>();
        pillarData.put("attestation_data", attestationPillar);

        return Optional.of(pillarData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        AttestationManager attestationManager = GlobalInstanceHolder.ATTESTATION_MANAGER;
        Optional<ServerCoCoAttestationReport> optReport =
                attestationManager.lookupReportByServerAndAction(serverAction.getServer(), this);
        if (optReport.isEmpty()) {
            LOG.warn("Failed to find a report entry while parsing salt state apply result");
            serverAction.fail("Failed to find a report entry");
            return;
        }
        ServerCoCoAttestationReport report = optReport.get();

        if (jsonResult == null) {
            String msg = serverAction.getResultMsg();
            if (StringUtils.isBlank(msg)) {
                msg = "Error while request attestation data from target system:\nGot no result from system";
            }
            setFailure(msg, serverAction, attestationManager, report);
            return;
        }

        try {
            CoCoAttestationResponseDataParser responseDataParser = new CoCoAttestationResponseDataParser();
            responseDataParser.parse(jsonResult);

            report.setOutData(responseDataParser.asMap());
            attestationManager.setPendingResults(report);
            attestationManager.saveReport(report);
        }
        catch (JsonSyntaxException e) {
            String msg = "Failed to parse the attestation result:\n";
            msg += Optional.of(jsonResult)
                    .map(JsonElement::toString)
                    .orElse("Got no result");

            setFailure(msg, serverAction, attestationManager, report);
            return;
        }
        if (serverAction.isStatusFailed()) {
            String msg = "Error while request attestation data from target system:\n";
            msg += SaltUtils.getJsonResultWithPrettyPrint(jsonResult);
            serverAction.setResultMsg(msg);
        }
        else {
            serverAction.setResultMsg("Successfully collected attestation data");
        }
    }

    private void setFailure(String message, ServerAction serverAction,
                            AttestationManager attestationManager, ServerCoCoAttestationReport report) {
        LOG.error(message);
        serverAction.fail(message);
        attestationManager.setFailed(report, message);
    }

}
