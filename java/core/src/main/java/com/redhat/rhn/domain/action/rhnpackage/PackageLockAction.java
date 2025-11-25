/*
 * Copyright (c) 2013--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.domain.action.rhnpackage;

import static java.util.Collections.singletonMap;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;

import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.SaltParameters;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import com.google.gson.JsonElement;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * PackageLockAction
 */
@Entity
@DiscriminatorValue("502")
public class PackageLockAction extends PackageAction {
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelAction() {
        for (ServerAction action : this.getServerActions()) {
            try {
                PackageManager.syncLockedPackages(action.getServer().getId(), this.getId());
            }
            catch (Exception e) {
                LogManager.getLogger(PackageLockAction.class.getName()).log(Level.ERROR, e);
            }
        }

        ActionManager.deleteActionsByIdAndType(this.getId(), this.getActionType().getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        for (MinionSummary m : minionSummaries) {
            DataResult<PackageListItem> setLockPkg = PackageManager.systemSetLockedPackages(
                    m.getServerId(), getId(), null);
            List<List<String>> pkgs = setLockPkg.stream().map(d -> Arrays.asList(d.getName(), d.getArch(),
                    new PackageEvr(d.getEpoch(), d.getVersion(), d.getRelease(), d.getPackageType())
                            .toUniversalEvrString())).toList();
            LocalCall<Map<String, State.ApplyResult>> localCall =
                    State.apply(List.of(SaltParameters.PACKAGES_PKGLOCK),
                            Optional.of(singletonMap(SaltParameters.PARAM_PKGS, pkgs)));
            List<MinionSummary> mSums = ret.getOrDefault(localCall, new ArrayList<>());
            mSums.add(m);
            ret.put(localCall, mSums);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        if (serverAction.isStatusFailed()) {
            String msg = "Error while changing the lock status";
            SaltUtils.jsonEventToStateApplyResults(jsonResult).ifPresentOrElse(
                    r -> {
                        if (r.containsKey("pkg_|-pkg_locked_|-pkg_locked_|-held")) {
                            serverAction.setResultMsg(msg + ":\n" +
                                    r.get("pkg_|-pkg_locked_|-pkg_locked_|-held").getComment());
                        }
                        else {
                            serverAction.setResultMsg(msg);
                        }
                    },
                    () -> serverAction.setResultMsg(msg));
            serverAction.getServer().asMinionServer()
                    .ifPresent(minionServer -> PackageManager.syncLockedPackages(minionServer.getId(), getId()));
        }
        else {
            String msg = "Successfully changed lock status";
            SaltUtils.jsonEventToStateApplyResults(jsonResult).ifPresentOrElse(
                    r -> serverAction.setResultMsg(msg + ":\n" +
                            r.get("pkg_|-pkg_locked_|-pkg_locked_|-held").getComment()),
                    () -> serverAction.setResultMsg(msg));
            serverAction.getServer().asMinionServer().ifPresent(minionServer -> {
                PackageManager.updateLockedPackages(minionServer.getId(), getId());
                PackageManager.updateUnlockedPackages(minionServer.getId(), getId());
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPackageParameter() {
        return "lock";
    }
}
