/**
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

import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.log4j.Logger;
import spark.Request;
import spark.Response;

/**
 * Generate and retrieve the salt-ssh public key.
 */
public class SaltSSHController {

    // Logger
    private static final Logger LOG = Logger.getLogger(SaltSSHController.class);

    private SaltSSHController() { }

    /**
     * Generate the salt-ssh public key if it's missing and return it to the client.
     *
     * @param request the http request
     * @param response the http response
     * @return the public key content
     */
    public static synchronized byte[] getPubKey(Request request, Response response) {
        File pubKey = new File(SaltSSHService.SSH_KEY_PATH + ".pub");

        Optional<MgrUtilRunner.ExecResult> res = SaltService.INSTANCE
                .generateSSHKey(SaltSSHService.SSH_KEY_PATH);

        if (!res.isPresent()) {
            LOG.error("Could not generate salt-ssh public key.");
            halt(500, "Could not generate salt-ssh public key.");
        }
        if (!(res.get().getReturnCode() == 0 || res.get().getReturnCode() == -1)) {
            LOG.error("Generating salt-ssh public key failed: " + res.get().getStderr());
            halt(500, res.get().getStderr());
        }

        response.header("Content-Type", "application/octet-stream");
        response.header("Content-Disposition", "attachment; filename=" + pubKey.getName());

        try (InputStream fin = new FileInputStream(pubKey)) {
            return IOUtils.toByteArray(fin);
        }
        catch (IOException e) {
            LOG.error("Could not read salt-ssh public key " + pubKey, e);
            halt(500, e.getMessage());
        }
        return null;
    }

}
