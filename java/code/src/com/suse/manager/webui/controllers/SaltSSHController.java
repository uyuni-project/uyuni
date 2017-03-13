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

import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import org.apache.commons.io.IOUtils;
import spark.Request;
import spark.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static spark.Spark.halt;

/**
 * Generate and retrieve the salt-ssh public key.
 */
public class SaltSSHController {

    private SaltSSHController() { }

    /**
     * Generate the salt-ssh public key if it's missing and return it to the client.
     *
     * @param request the http request
     * @param response the http response
     * @return the public key content
     */
    public synchronized static byte[] getPubKey(Request request, Response response) {
        File pubKey = new File(SaltSSHService.SSH_KEY_PATH + ".pub");
        if (!pubKey.isFile()) {
            MgrUtilRunner.ExecResult res = SaltService.INSTANCE
                    .generateSSHKey(SaltSSHService.SSH_KEY_PATH);

            if (!(res.getReturnCode() == 0 || res.getReturnCode() == -1)) {
                halt(500, res.getStderr() + "");
            }
        }

        response.header("Content-Type", "application/octet-stream");
        response.header("Content-Disposition", "attachment; filename=" + pubKey.getName());

        try (InputStream fin = new FileInputStream(pubKey)) {
            return IOUtils.toByteArray(fin);
        }
        catch (IOException e) {
            halt(500, e.getMessage());
        }
        return null;
    }

}
