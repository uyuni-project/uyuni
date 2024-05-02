/*
 * Copyright (c) 2024 SUSE LLC
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
package com.redhat.rhn.domain.action;


import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.domain.action.server.ServerAction;

import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.model.attestation.CoCoAttestationStatus;
import com.suse.manager.model.attestation.ServerCoCoAttestationReport;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;

/**
 * CoCoAttestationAction - Class representing TYPE_COCO_ATTESTATION
 */
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
}
