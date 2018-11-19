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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.AccessTokenFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.Comps;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;

import com.suse.manager.webui.utils.TokenBuilder;
import com.suse.utils.Opt;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import spark.Request;
import spark.Response;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static spark.Spark.halt;


/**
 * Controller for the download endpoint serving packages and metadata to managed clients.
 */
public class DownloadController {

    private static final Key KEY = TokenBuilder.getKeyForSecret(
            TokenBuilder.getServerSecret().orElseThrow(
                        () -> new IllegalArgumentException(
                                "Server has no key configured")));
    private static final JwtConsumer JWT_CONSUMER = new JwtConsumerBuilder()
            .setVerificationKey(KEY)
            .build();

    // cached value to avoid multiple calls
    private static final String MOUNT_POINT_PATH = Config.get()
            .getString(ConfigDefaults.MOUNT_POINT);

    /**
     * If true, check via JWT tokens that files requested by a minion are actually accessible by that minon. Turning
     * this flag to false disables the checks.
     */
    private static boolean checkTokens = Config.get().getBoolean(ConfigDefaults.SALT_CHECK_DOWNLOAD_TOKENS);

    private DownloadController() {
    }

    /**
     * Toggles token checking (only for unit tests).
     *
     * @param checkTokensIn true if tokens should be checked
     * accessible by that minon
     */
    public static void setCheckTokens(boolean checkTokensIn) {
        checkTokens = checkTokensIn;
    }

    /**
     * Download metadata taking the channel and filename from the request path.
     *
     * @param request the request object
     * @param response the response object
     * @return an object to make spark happy
     */
    public static Object downloadMetadata(Request request, Response response) {
        String channelLabel = request.params(":channel");
        String filename = request.params(":file");
        File file = new File(new File("/var/cache/rhn/repodata", channelLabel),
                filename).getAbsoluteFile();

        if (!file.exists() && (filename.endsWith(".asc") || filename.endsWith(".key"))) {
            halt(HttpStatus.SC_NOT_FOUND,
                    String.format("Key or signature file not provided: %s", filename));
        }

        if (checkTokens) {
            String token = getTokenFromRequest(request);
            validateToken(token, channelLabel, filename);
        }

        if (!file.exists() && filename.equals("comps.xml")) {
            File compsFile = getCompsFile(ChannelFactory.lookupByLabel(channelLabel));
            if (compsFile != null && compsFile.exists()) {
                file = compsFile;
            }
        }

        return downloadFile(request, response, file);
    }

    /**
     * Determines the comps file for a channel.
     * If the channel doesn't have comps file associated and it's a cloned one, try to
     * use the comps of the original channel.
     *
     * @param channel - the channel
     * @return comps file to be used for this channel
     */
    private static File getCompsFile(Channel channel) {
        Comps comps = channel.getComps();

        if (comps == null && channel.isCloned()) {
            comps = channel.getOriginal().getComps();
        }
        if (comps != null) {
            return new File(MOUNT_POINT_PATH, comps.getRelativeFilename())
                    .getAbsoluteFile();
        }

        return null;
    }

    /**
     * Download a package taking the channel and RPM filename from the request path.
     *
     * @param request the request object
     * @param response the response object
     * @return an object to make spark happy
     */
    public static Object downloadPackage(Request request, Response response) {

        // we can't use request.params(:file)
        // See https://bugzilla.suse.com/show_bug.cgi?id=972158
        // https://github.com/perwendel/spark/issues/490
        String channel = request.params(":channel");
        String path = "";
        try {
            URL url = new URL(request.url());
            path = url.getPath();
        }
        catch (MalformedURLException e) {
            halt(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    String.format("url '%s' is malformed", request.url()));
        }

        String basename = FilenameUtils.getBaseName(path);
        String arch = StringUtils.substringAfterLast(basename, ".");
        String rest = StringUtils.substringBeforeLast(basename, ".");
        String release = StringUtils.substringAfterLast(rest, "-");
        rest = StringUtils.substringBeforeLast(rest, "-");
        String version = StringUtils.substringAfterLast(rest, "-");
        String name = StringUtils.substringBeforeLast(rest, "-");

        // Debian packages names need spacial handling
        Channel channelBean = ChannelFactory.lookupByLabel(channel);
        if (channelBean.getChannelArch().getArchType().getLabel().equalsIgnoreCase("deb")) {
            version = StringUtils.substringAfterLast(rest, "_");
            name = StringUtils.substringBeforeLast(rest, "_");
        }

        if (checkTokens) {
            String token = getTokenFromRequest(request);
            validateToken(token, channel, basename);
        }

        Package pkg = PackageFactory.lookupByChannelLabelNevra(
                channel, name, version, release, null, arch);
        if (pkg == null) {
            halt(HttpStatus.SC_NOT_FOUND,
                 String.format("%s not found in %s", basename, channel));
        }

        File file = new File(Config.get().getString(ConfigDefaults.MOUNT_POINT),
                pkg.getPath()).getAbsoluteFile();

        return downloadFile(request, response, file);
    }

    /**
     * Return the token from the request or send an appropriate response if it is not there.
     *
     * @param request the request object
     * @return the token
     */
    private static String getTokenFromRequest(Request request) {
        Set<String> queryParams = request.queryParams();
        String header = request.headers("X-Mgr-Auth");
        header = StringUtils.isNotBlank(header) ? header : getTokenForDebian(request);
        if (queryParams.isEmpty() && StringUtils.isBlank(header)) {
            halt(HttpStatus.SC_FORBIDDEN,
                 String.format("You need a token to access %s", request.pathInfo()));
        }
        if ((queryParams.size() > 1 && header == null) ||
                (!queryParams.isEmpty() && header != null)) {
            halt(HttpStatus.SC_BAD_REQUEST, "Only one token is accepted");
        }
        if (!queryParams.isEmpty()) {
            return queryParams.iterator().next();
        }
        else {
            return header;
        }
    }

    /**
     * For Debian, we are getting token from 'Authorization' header
     * @param request the request object
     * @return the token header
     */
    private static String getTokenForDebian(Request request) {
        String authorizationHeader = request.headers("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Basic")) {
            String encodedData = authorizationHeader.substring("Basic".length()).trim();
            return new String(Base64.getDecoder().decode(encodedData), UTF_8);
        }
        return "";
    }

    /**
     * Validate a given token for a given channel.
     *
     * @param token the token to validate
     * @param channel the channel
     * @param filename the filename
     */
    private static void validateToken(String token, String channel, String filename) {
        AccessTokenFactory.lookupByToken(token).ifPresent(obj -> {
            if (!obj.getValid()) {
                halt(HttpStatus.SC_FORBIDDEN, "This token is not valid");
            }
        });
        try {
            JwtClaims claims = JWT_CONSUMER.processToClaims(token);

            // enforce channel claim
            Optional<List<String>> channelClaim = Optional.ofNullable(claims.getStringListClaimValue("onlyChannels"))
                    // new versions of getStringListClaimValue() return an empty list instead of null
                    .filter(l -> !l.isEmpty());
            if (Opt.fold(channelClaim, () -> false, channels -> !channels.contains(channel))) {
                halt(HttpStatus.SC_FORBIDDEN, "Token does not provide access to channel " + channel);
            }

            // enforce org claim
            Optional<Long> orgClaim = Optional.ofNullable(claims.getClaimValue("org", Long.class));
            Opt.consume(orgClaim, () -> {
                halt(HttpStatus.SC_BAD_REQUEST, "Token does not specify the organization");
            }, orgId -> {
                if (!ChannelFactory.isAccessibleBy(channel, orgId)) {
                    halt(HttpStatus.SC_FORBIDDEN, "Token does not provide access to channel %s" + channel);
                }
            });
        }
        catch (InvalidJwtException | MalformedClaimException e) {
            halt(HttpStatus.SC_FORBIDDEN,
                 String.format("Token is not valid to access %s in %s: %s", filename, channel, e.getMessage()));
        }
    }

    /**
     * Write the actual file contents to the response.
     *
     * @param request the request object
     * @param response the response object
     * @param file description of the file to send
     */
    private static Object downloadFile(Request request, Response response, File file) {
        response.header("Content-Type", "application/octet-stream");
        response.header("Content-Disposition", "attachment; filename=" + file.getName());
        response.header("X-Sendfile", file.getAbsolutePath());
        return response;
    }
}
