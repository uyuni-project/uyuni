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

import static spark.Spark.halt;

import org.apache.commons.httpclient.HttpStatus;

public class DownloadController {

    private static final int BUF_SIZE = 4096;
    private static final Key KEY = TokenUtils.getServerKey();
    private static final JwtConsumer JWT_CONSUMER = new JwtConsumerBuilder()
            .setDecryptionKey(KEY)
            .setDisableRequireSignature()
            .setEnableRequireEncryption()
            .build();

    private DownloadController() {
    }

    private static void validateToken(String token, String channel, String filename) {
        try {
            JwtClaims jwtClaims = JWT_CONSUMER.processToClaims(token);

            Set<String> channels = new HashSet<String>();

            // add all the organization channels
            if (jwtClaims.hasClaim("org")) {
                long orgId = jwtClaims.getClaimValue("org", Long.class);
                List<String> orgChannels =
                        ChannelFactory.getAccessibleChannelsByOrg(orgId)
                                .stream()
                                .map(i -> i.getLabel())
                                .collect(Collectors.toList());
                channels.addAll(orgChannels);
            }

            if (jwtClaims.hasClaim("channels")) {
                // add the extra claimed channels
                channels.addAll(jwtClaims.getStringListClaimValue("channels"));
            }

            if (!channels.contains(channel)) {
                halt(HttpStatus.SC_FORBIDDEN,
                     String.format("Token is not provide access to channel %s",
                                   channel));
            }
        }
        catch (InvalidJwtException|MalformedClaimException e) {
            halt(HttpStatus.SC_FORBIDDEN,
                 String.format("Token is not valid to access %s in %s: %s",
                               filename, channel, e.getMessage()));
        }

    }

    public static Object downloadMetadata(Request request, Response response) {
        String channel = request.params(":channel");
        String filename = request.params(":file");

        String token = getTokenFromRequest(request);
        validateToken(token, channel, filename);

        File file = new File(new File("/var/cache/rhn/repodata", channel), filename).getAbsoluteFile();
        downloadFile(request, response, file);
        // make spark happy
        return null;
    }

    /**
     * Returns the token from the request and sends the appropiate response if it
     * is not there
     * @param request the request object
     * @return the token
     */
    private static String getTokenFromRequest(Request request) {
        Set<String> queryParams = request.queryParams();
        if (queryParams.size() < 1) {
            halt(HttpStatus.SC_FORBIDDEN,
                 String.format("You need a token to access %s", request.pathInfo()));
        }

        if (queryParams.size() > 1) {
            halt(HttpStatus.SC_BAD_REQUEST,
                 "Only one token is accepted");
        }
        return queryParams.iterator().next();
    }

    /**
     * Endpoint to download a package from the given channel and filename
     */
    public static String downloadPackage(Request request, Response response) {

        String channel = request.params(":channel");
        String filename = request.params(":file");

        String basename = FilenameUtils.getBaseName(filename);
        String arch = StringUtils.substringAfterLast(basename, ".");
        String rest = StringUtils.substringBeforeLast(basename, ".");
        String release = StringUtils.substringAfterLast(rest, "-");
        rest = StringUtils.substringBeforeLast(rest, "-");
        String version = StringUtils.substringAfterLast(rest, "-");
        String name = StringUtils.substringBeforeLast(rest, "-");

        String token = getTokenFromRequest(request);

        validateToken(token, channel, filename);


        Package pkg = PackageFactory.lookupByChannelLabelNevra(channel, name, version, release, null, arch);
        if (pkg == null) {
            halt(HttpStatus.SC_NOT_FOUND,
                 String.format("%s not found in %s", filename, channel));
        }

        File file = new File("/var/spacewalk", pkg.getPath()).getAbsoluteFile();
        downloadFile(request, response, file);
        // make spark happy
        return null;
    }

    /**
     * Downloads a file and writes it to the response
     * @param response the response object
     * @param file a file description of the file to download
     */
    private static void downloadFile(Request request, Response response, File file) {

        if (file.exists()) {
            response.status(HttpStatus.SC_OK);
        } else {
            response.status(HttpStatus.SC_NOT_FOUND);
        }

        // skip download
        if (request.requestMethod().equals("HEAD")) {
            return;
        }

        HttpServletResponse raw = response.raw();

        response.raw().setContentType("application/octet-stream");
        response.raw().setHeader("Content-Disposition", "attachment; filename=" + file.getName());

        try (BufferedInputStream bufferedInputStream =
                new BufferedInputStream(new FileInputStream(file))) {
            OutputStream out = raw.getOutputStream();

            byte[] buffer = new byte[BUF_SIZE];
            int len;
            while ((len = bufferedInputStream.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            out.flush();
            out.close();
        } catch (IOException e) {
            halt(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                 e.getMessage());
        }
    }
}
