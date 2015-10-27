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

import com.redhat.rhn.domain.rhnpackage.*;
import com.redhat.rhn.domain.rhnpackage.Package;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
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

        String channel = request.params(":channel");
        String filename = request.params(":file");

        String basename = FilenameUtils.getBaseName(filename);
        String arch = StringUtils.substringAfterLast(basename, ".");
        String rest = StringUtils.substringBeforeLast(basename, ".");
        String release = StringUtils.substringAfterLast(rest, "-");
        rest = StringUtils.substringBeforeLast(rest, "-");
        String version = StringUtils.substringAfterLast(rest, "-");
        String name = StringUtils.substringBeforeLast(rest, "-");

        Package pkg = PackageFactory.lookupByChannelLabelNevra(channel, name, version, release, null, arch);
        if (pkg == null) {
            halt(404, String.format("%s not found in %s", filename, channel));
            return null;
        }

        File file = new File("/var/spacewalk", pkg.getPath()).getAbsoluteFile();
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
