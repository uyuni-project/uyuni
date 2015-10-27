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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import static spark.Spark.*;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DownloadController {

    private static final int BUF_SIZE = 4096;
    private static final Logger LOG = LoggerFactory.getLogger(Request.class);

    private DownloadController() {
    }

    public static HttpServletResponse downloadPackage(Request request, Response response) {
        File file = new File("/var/spacewalk/packages/1/ffe/x11-input-wizardpen-tools/0.8.1-18.1/x86_64/ffe153c66858c070c9298e4f9a0f16bede4d1a158f600b559f73df0b2db5f28c/x11-input-wizardpen-tools-0.8.1-18.1.x86_64.rpm");
        HttpServletResponse raw = response.raw();

        response.raw().setContentType("application/octet-stream");
        response.raw().setHeader("Content-Disposition", "attachment; filename=" + file.getName());

        try {
            OutputStream out = raw.getOutputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));

            byte[] buffer = new byte[BUF_SIZE];
            int len;
            int off = 0;
            while ((len = bufferedInputStream.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            out.flush();
            out.close();
        } catch (IOException e) {
            halt(500, e.getMessage());
        }

        return raw;
    }
}
