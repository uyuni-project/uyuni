/**
 * Copyright (c) 2015 SUSE LLC
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

import java.util.Optional;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService;
import org.apache.commons.io.IOUtils;
import spark.Request;
import spark.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static spark.Spark.halt;

/**
 * Controller for downloading the public salt-ssh key.
 */
public class SaltSSHController {

    private SaltSSHController() { }

    /**
     * Generate the salt-ssh if it's missing and return the public key to the client.
     *
     * @param request the http request
     * @param response the http response
     * @return the public key content
     */
    public static byte[] getPubKey(Request request, Response response) {
        File pubKey = new File(SaltSSHService.SSH_KEY_PATH + ".pub");
        if (!pubKey.isFile()) {
            Map<String, Object> res = SaltService.INSTANCE
                    .generateSSHKey(SaltSSHService.SSH_KEY_PATH);
            Optional<Integer> retcode = Optional.ofNullable(res.get("returncode"))
                    .filter(r -> r instanceof Integer)
                    .map(r -> (Integer)r);
            if (!retcode.isPresent()) {
                halt(500, "Key could not be generated");
            }

            if (!(retcode.get() == 0 || retcode.get() == -1)) {
                halt(500, res.get("stderr") + "");
            }
        }

        response.header("Content-Type", "application/octet-stream");
        response.header("Content-Disposition", "attachment; filename=" + pubKey.getName());
        try {
            try (InputStream fin = new FileInputStream(pubKey)) {
                return IOUtils.toByteArray(fin);
            }
        }
        catch (IOException e) {
            halt(500, e.getMessage());
        }
        return null;
    }

}
