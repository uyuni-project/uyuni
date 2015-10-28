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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.suse.manager.webui.utils.TokenUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Key;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DownloadController {

    private static final int BUF_SIZE = 4096;
    private static final Logger LOG = LoggerFactory.getLogger(Request.class);
    private static final Gson GSON = new GsonBuilder().create();

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

        Set<String> queryParams = request.queryParams();
        if (queryParams.size() < 1) {
            response.status(403);
            response.body(String.format("You need a token to access %s in %s", filename, channel));
            return null;
        }

        if (queryParams.size() > 1) {
            response.status(400);
            response.body("Only one token is accepted");
            return null;
        }

        String token = queryParams.iterator().next();

        Key key = TokenUtils.getServerKey();
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                //.setRequireExpirationTime()
                //.setAllowedClockSkewInSeconds(30)
                //.setRequireSubject()
                //.setExpectedIssuer("Issuer")
                //.setExpectedAudience("Audience")
                .setVerificationKey(key)
                .build();

        try {
            JwtClaims jwtClaims = jwtConsumer.processToClaims(token);

            Set<String> channels = new HashSet<String>();

            // add all the organization channels
            long orgId = jwtClaims.getClaimValue("org", Long.class);
            List<String> orgChannels =
                    ChannelFactory.getAccessibleChannelsByOrg(orgId)
                            .stream()
                            .map(i -> i.toString())
                            .collect(Collectors.toList());
            channels.addAll(orgChannels);
            // add the extra claimed channels
            channels.addAll(jwtClaims.getStringListClaimValue("channels"));

            if (!channels.contains(channel)) {
                response.status(403);
                response.body(String.format("Token is not provide access to channel %s", channel));
                return null;
            }
        }
        catch (InvalidJwtException|MalformedClaimException e) {
            response.status(403);
            response.body(String.format("Token is not valid to access %s in %s: %s", filename, channel, e.getMessage()));
            return null;
        }

        Package pkg = PackageFactory.lookupByChannelLabelNevra(channel, name, version, release, null, arch);
        if (pkg == null) {
            response.status(404);
            response.body(String.format("%s not found in %s", filename, channel));
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
            response.status(500);
            response.body(e.getMessage());
            return null;
        }

        return raw;
    }
}
