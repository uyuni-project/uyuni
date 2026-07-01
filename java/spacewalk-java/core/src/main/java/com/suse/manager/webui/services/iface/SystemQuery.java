/*
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.webui.services.iface;

import com.redhat.rhn.domain.server.MinionServer;

import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Zypper;
import com.suse.salt.netapi.exception.SaltException;

import java.util.List;
import java.util.Optional;

/**
 * Interface containing methods for directly interacting and getting information from a system.
 * Note: This interface should be split up further at some point.
 */
public interface SystemQuery {

    /**
     * Get the machine id for a given minion.
     *
     * @param minionId id of the target minion
     * @return the machine id as a string
     */
    Optional<String> getMachineId(String minionId);

    /**
     * Send notification about a system id to be generated.
     * @param minion target minion.
     * @throws InstantiationException if signature generation fails
     * @throws SaltException if anything goes wrong.
     */
    void notifySystemIdGenerated(MinionServer minion) throws InstantiationException, SaltException;

    /**
     * Query product information.
     * @param minionId of the target minion.
     * @return product information
     */
    Optional<List<Zypper.ProductInfo>> getProducts(String minionId);


    /**
     * Synchronously executes a salt function on a single minion.
     * If a SaltException is thrown, re-throw a RuntimeException.
     *
     * @param call salt function to call
     * @param minionId minion id to target
     * @param <R> result type of the salt function
     * @return Optional holding the result of the function
     * or empty if the minion did not respond.
     */
    <R> Optional<R> callSync(LocalCall<R> call, String minionId);

    /**
     * Upload built Kiwi image to SUSE Manager
     *
     * @param minion     the minion
     * @param filepath   the filepath of the image to upload, in the build host
     * @param imageStore the image store location
     * @return the execution result
     */
    Optional<MgrUtilRunner.ExecResult> collectKiwiImage(MinionServer minion, String filepath,
            String imageStore);

    /**
     * Get redhat product information
     * @param minionId id of the target minion
     * @return redhat product information
     */
    Optional<RedhatProductInfo> redhatProductInfo(String minionId);
}
