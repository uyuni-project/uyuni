/*
 * Copyright (c) 2017 SUSE LLC
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
package com.suse.manager.webui.controllers;

import static spark.Spark.halt;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Optional;

import spark.Request;
import spark.Response;

/**
 * Generate and retrieve the salt-ssh public key.
 */
public class SaltSSHController {

    // Logger
    private static final Logger LOG = LogManager.getLogger(SaltSSHController.class);

    private final SaltApi saltApi;

    /**
     * @param saltApiIn instance for getting information from a system.
     */
    public SaltSSHController(SaltApi saltApiIn) {
        this.saltApi = saltApiIn;
    }

    /**
     * Generate the salt-ssh public key if it's missing and return it to the client.
     *
     * @param request the http request
     * @param response the http response
     * @return the public key content
     */
    public synchronized byte[] getPubKey(Request request, Response response) {
        File pubKey = new File(SaltSSHService.SSH_KEY_PATH + ".pub");

        Optional<MgrUtilRunner.SshKeygenResult> res = saltApi
                .generateSSHKey(SaltSSHService.SSH_KEY_PATH, SaltSSHService.SUMA_SSH_PUB_KEY);

        res.ifPresentOrElse(result -> {
            if (!(result.getReturnCode() == 0 || result.getReturnCode() == -1)) {
                LOG.error("Generating salt-ssh public key failed: {}", result.getStderr());
                halt(500, result.getStderr());
            }
        }, () -> {
            LOG.error("Could not generate salt-ssh public key.");
            halt(500, "Could not generate salt-ssh public key.");
        });

        response.header("Content-Type", "application/octet-stream");
        response.header("Content-Disposition", "attachment; filename=" + pubKey.getName());

        String key = res.orElseThrow().getPublicKey();
        if (key != null) {
            return key.getBytes();
        }
        LOG.error("Could not read salt-ssh public key {}", pubKey);
        halt(500, "Could not read salt-ssh public key");
        return new byte[0];
    }

}
