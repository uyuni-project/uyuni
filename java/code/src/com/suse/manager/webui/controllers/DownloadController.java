/*
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.head;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.AccessTokenFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.Comps;
import com.redhat.rhn.domain.channel.MediaProducts;
import com.redhat.rhn.domain.channel.Modules;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.MinionServer;

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.webui.utils.TokenBuilder;
import com.suse.utils.Opt;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import spark.Request;
import spark.Response;


/**
 * Controller for the download endpoint serving packages and metadata to managed clients.
 */
public class DownloadController {

    private static final Logger LOG = LogManager.getLogger(DownloadController.class);

    private static final Key KEY = TokenBuilder.getKeyForSecret(
            TokenBuilder.getServerSecret().orElseThrow(
                        () -> new IllegalArgumentException("Server has no key configured")));
    private static final JwtConsumer JWT_CONSUMER = new JwtConsumerBuilder()
            .setVerificationKey(KEY)
            .build();

    // cached value to avoid multiple calls
    private final String mountPointPath;

    private final CloudPaygManager cloudPaygManager;
    private static final long EXPIRATION_TIME_MINUTES_IN_THE_FUTURE_TEMP_TOKEN = Config.get().getInt(
            ConfigDefaults.TEMP_TOKEN_LIFETIME,
            60
    );

    /**
     * If true, check via JWT tokens that files requested by a minion are actually accessible by that minon. Turning
     * this flag to false disables the checks.
     */
    private boolean checkTokens;

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     */
    public void initRoutes() {
        get("/manager/download/:channel/getPackage/:file", this::downloadPackage);
        get("/manager/download/:channel/getPackage/:org/:checksum/:file", this::downloadPackage);
        get("/manager/download/:channel/repodata/:file", this::downloadMetadata);
        get("/manager/download/:channel/media.1/:file", this::downloadMediaFiles);
        head("/manager/download/:channel/getPackage/:file", this::downloadPackage);
        head("/manager/download/:channel/getPackage/:org/:checksum/:file", this::downloadPackage);
        head("/manager/download/:channel/repodata/:file", this::downloadMetadata);
        head("/manager/download/:channel/media.1/:file", this::downloadMediaFiles);
    }

    /**
     * Encapsulates package info.
     * Public only for unit tests.
     */
    public static class PkgInfo {
        private final String name;
        private final String version;
        private final String release;
        private final String epoch;
        private final String arch;
        private Optional<Long> orgId = Optional.empty();
        private Optional<String> checksum = Optional.empty();

        /**
         * Constructor
         * @param nameIn package name
         * @param epochIn epoch
         * @param versionIn version
         * @param releaseIn release
         * @param archIn architecture
         */
        public PkgInfo(String nameIn, String epochIn, String versionIn, String releaseIn, String archIn) {
            this.name = nameIn;
            this.version = versionIn;
            this.release = releaseIn;
            this.epoch = epochIn;
            this.arch = archIn;
        }

        /**
         * @return package name
         */
        public String getName() {
            return name;
        }

        /**
         * @return version
         */
        public String getVersion() {
            return version;
        }

        /**
         * @return release
         */
        public String getRelease() {
            return release;
        }

        /**
         * @return epoch
         */
        public String getEpoch() {
            return epoch;
        }

        /**
         * @return arch
         */
        public String getArch() {
            return arch;
        }

        /**
         * Set the checksum
         * @param checksumIn the checksum
         */
        public void setChecksum(String checksumIn) {
            checksum = Optional.ofNullable(checksumIn);
        }

        /**
         * Return the checksum if available
         * @return the optional checksum
         */
        public Optional<String> getChecksum() {
            return checksum;
        }

        /**
         * Set the org id
         * @param orgIdIn the org id
         */
        public void setOrgId(Long orgIdIn) {
            orgId = Optional.ofNullable(orgIdIn);
        }

        /**
         * Set the org id as string
         * @param orgIdIn the org is as string
         */
        public void setOrgId(String orgIdIn) {
            try {
                orgId = Optional.of(Long.valueOf(orgIdIn));
            }
            catch (NumberFormatException e) {
                orgId = Optional.empty();
            }
        }

        /**
         * Return the org id if available
         * @return the optional org id
         */
        public Optional<Long> getOrgId() {
            return orgId;
        }
    }

    /**
     * Constructor
     */
    public DownloadController() {
        this(GlobalInstanceHolder.PAYG_MANAGER);
    }

    /**
     * Constructor
     * @param cloudPaygManagerIn the cloud payg manager
     */
    public DownloadController(CloudPaygManager cloudPaygManagerIn) {
        cloudPaygManager = cloudPaygManagerIn;
        checkTokens = Config.get().getBoolean(ConfigDefaults.SALT_CHECK_DOWNLOAD_TOKENS);
        mountPointPath = Config.get().getString(ConfigDefaults.MOUNT_POINT);
    }

    private void processToken(Request request, String channelLabel, String filename) {
        if (!cloudPaygManager.isPaygInstance() && !checkTokens) {
            return;
        }

        String token = getTokenFromRequest(request);
        // payg token checks should always run if we are payg
        if (cloudPaygManager.isPaygInstance()) {
            validateMinionInPayg(token);
        }
        // additional token checks can be disabled via config
        if (checkTokens) {
            validateToken(token, channelLabel, filename);
        }
    }

    /**
     * Toggles token checking (only for unit tests).
     *
     * @param checkTokensIn true if tokens should be checked
     * accessible by that minon
     */
    public void setCheckTokens(boolean checkTokensIn) {
        checkTokens = checkTokensIn;
    }

    /**
     * Download metadata taking the channel and filename from the request path.
     *
     * @param request the request object
     * @param response the response object
     * @return an object to make spark happy
     */
    public Object downloadMetadata(Request request, Response response) {
        String channelLabel = request.params(":channel");
        String filename = request.params(":file");
        String mountPoint = Config.get().getString(ConfigDefaults.REPOMD_CACHE_MOUNT_POINT, "/var/cache");
        String prefix = Config.get().getString(ConfigDefaults.REPOMD_PATH_PREFIX, "rhn/repodata");

        File file = Path.of(mountPoint, prefix, channelLabel, filename).toAbsolutePath().toFile();

        if (!file.exists() && (filename.endsWith(".asc") || filename.endsWith(".key"))) {
            halt(HttpStatus.SC_NOT_FOUND,
                    String.format("Key or signature file not provided: %s", filename));
        }
        validatePaygCompliant();

        processToken(request, channelLabel, filename);

        if (!file.exists()) {
            // Check if a comps.xml/modules.yaml file is being requested and if we have it
            File mdFile = null;
            if (filename.equals("comps.xml")) {
                mdFile = getCompsFile(ChannelFactory.lookupByLabel(channelLabel));
            }
            else if (filename.equals("modules.yaml")) {
                mdFile = getModulesFile(ChannelFactory.lookupByLabel(channelLabel));
            }

            if (mdFile != null && mdFile.exists()) {
                file = mdFile;
            }
        }

        return downloadFile(request, response, file);
    }


    /**
     * Download media metadata taking the channel and filename from the request path.
     *
     * @param request the request object
     * @param response the response object
     * @return an object to make spark happy
     */
    public Object downloadMediaFiles(Request request, Response response) {
        String channelLabel = request.params(":channel");
        String filename = request.params(":file");
        validatePaygCompliant();

        processToken(request, channelLabel, filename);

        if (filename.equals("products")) {
            File file = getMediaProductsFile(ChannelFactory.lookupByLabel(channelLabel));
            if (file != null && file.exists()) {
                return downloadFile(request, response, file);
            }
        }
        halt(HttpStatus.SC_NOT_FOUND, String.format("%s not found", filename));
        return null;
    }

    /**
     * Determines the comps file for a channel.
     * If the channel doesn't have comps file associated and it's a cloned one, try to
     * use the comps of the original channel.
     *
     * @param channel - the channel
     * @return comps file to be used for this channel
     */
    private File getCompsFile(Channel channel) {
        Comps comps = channel.getComps();

        if (comps == null && channel.isCloned()) {
            comps = channel.getOriginal().getComps();
        }
        if (comps != null) {
            return new File(mountPointPath, comps.getRelativeFilename())
                    .getAbsoluteFile();
        }

        return null;
    }

    /**
     * Determines the modules file for a channel.
     * If the channel doesn't have modules file associated and it's a cloned one, try to
     * use the modules of the original channel.
     *
     * @param channel - the channel
     * @return modules file to be used for this channel
     */
    private File getModulesFile(Channel channel) {
        Modules modules = channel.getModules();

        if (modules == null && channel.isCloned()) {
            modules = channel.getOriginal().getModules();
        }
        if (modules != null) {
            return new File(mountPointPath, modules.getRelativeFilename()).getAbsoluteFile();
        }

        return null;
    }

    /**
     * Determines the media products file for a channel.
     * If the channel doesn't have products file associated and it's a cloned one, try to
     * use the products file of the original channel.
     *
     * @param channel - the channel
     * @return media products file to be used for this channel
     */
    private File getMediaProductsFile(Channel channel) {
        MediaProducts product = channel.getMediaProducts();

        if (product == null && channel.isCloned()) {
            product = channel.getOriginal().getMediaProducts();
        }
        if (product != null) {
            return new File(mountPointPath, product.getRelativeFilename())
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
    public Object downloadPackage(Request request, Response response) {

        // we can't use request.params(:file)
        // See https://bugzilla.suse.com/show_bug.cgi?id=972158
        // https://github.com/perwendel/spark/issues/490
        String channel = request.params(":channel");
        String path = "";
        try {
            URI uri = new URI(request.url());
            // URL decode the path to support ^ in package versions
            path = uri.getPath();

        }
        catch (URISyntaxException e) {
            LOG.error("Unable to parse: {}", request.url());
            halt(HttpStatus.SC_INTERNAL_SERVER_ERROR, String.format("url '%s' is malformed", request.url()));
        }

        String basename = FilenameUtils.getBaseName(path);
        validatePaygCompliant();

        processToken(request, channel, basename);

        String mountPoint = Config.get().getString(ConfigDefaults.MOUNT_POINT);
        PkgInfo pkgInfo = parsePackageFileName(path);
        Package pkg = PackageFactory.lookupByChannelLabelNevraCs(channel, pkgInfo.getName(),
                pkgInfo.getVersion(), pkgInfo.getRelease(), pkgInfo.getEpoch(), pkgInfo.getArch(),
                pkgInfo.getChecksum());
        if (pkg == null) {
            if (LOG.isDebugEnabled()) {
                LOG.error("{}: Package not found in channel: {}", path, StringUtil.sanitizeLogInput(channel));
            }
            halt(HttpStatus.SC_NOT_FOUND, String.format("%s not found in %s", basename, channel));
        }

        File file = new File(mountPoint, pkg.getPath()).getAbsoluteFile();

        return downloadFile(request, response, file);
    }

    /**
     * Parse URL path to extract package info.
     * Only public for unit tests.
     * @param path url path
     * @return name, epoch, vesion, release, arch of package
     */
    public PkgInfo parsePackageFileName(String path) {
        List<String> parts = Arrays.asList(path.split("/"));
        String extension = FilenameUtils.getExtension(path);
        String basename = FilenameUtils.getBaseName(path);
        String arch = StringUtils.substringAfterLast(basename, ".");
        String rest = StringUtils.substringBeforeLast(basename, ".");
        String release;
        String name;
        String version;
        String epoch;

        // Debian packages names need spacial handling
        if ("deb".equalsIgnoreCase(extension) || "udeb".equalsIgnoreCase(extension)) {
            name = StringUtils.substringBeforeLast(rest, "_");
            rest = StringUtils.substringAfterLast(rest, "_");
            PackageEvr pkgEv = PackageEvr.parseDebian(rest);
            epoch = pkgEv.getEpoch();
            version = pkgEv.getVersion();
            release = pkgEv.getRelease();
        }
        else {
            release = StringUtils.substringAfterLast(rest, "-");
            rest = StringUtils.substringBeforeLast(rest, "-");
            version = StringUtils.substringAfterLast(rest, "-");
            name = StringUtils.substringBeforeLast(rest, "-");
            epoch = null;
        }
        PkgInfo p = new PkgInfo(name, epoch, version, release, arch);
        // path is getPackage/<org>/<checksum>/filename
        if (parts.size() == 9 && parts.get(5).equals("getPackage")) {
            p.setOrgId(parts.get(6));
            p.setChecksum(parts.get(7));
        }
        return p;
    }

    /**
     * Return the token from the request or send an appropriate response if it is not there.
     *
     * @param request the request object
     * @return the token
     */
    @SuppressWarnings({"javasecurity:S5145"}) // controlled by log level
    private String getTokenFromRequest(Request request) {
        Set<String> queryParams = request.queryParams();
        if (LOG.isDebugEnabled()) {
            LOG.debug("URL: {}", request.url());
            LOG.debug("Query Params: {}", request.queryString());
            for (String hdr: request.headers()) {
                LOG.debug("Header: {}: {}", hdr, request.headers(hdr));
            }
        }
        String header = request.headers("X-Mgr-Auth");
        header = StringUtils.isNotBlank(header) ? header : getTokenForDebian(request);
        if (queryParams.isEmpty() && StringUtils.isBlank(header)) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Forbidden: You need a token to access {}", StringUtil.sanitizeLogInput(request.pathInfo()));
            }
            halt(HttpStatus.SC_FORBIDDEN, String.format("You need a token to access %s", request.pathInfo()));
        }
        if ((queryParams.size() > 1 && header == null) ||
                (!queryParams.isEmpty() && header != null)) {
            LOG.info("Bad Request: Only one token is accepted");
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
    private String getTokenForDebian(Request request) {
        String authorizationHeader = request.headers("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Basic")) {
            String encodedData = authorizationHeader.substring("Basic".length()).trim();
            return new String(Base64.getDecoder().decode(encodedData), UTF_8);
        }
        return null;
    }

    /**
     * @param token the token
     * @return the last characters of the token
     */
    private static String sanitizeToken(String token) {
        if (token == null) {
            return null;
        }

        String tk = StringUtil.sanitizeLogInput(token);
        return tk.substring(tk.length() - 20);
    }

    /**
     * Validate a given token for a given channel.
     *
     * @param token the token to validate
     * @param channel the channel
     * @param filename the filename
     */
    private void validateToken(String token, String channel, String filename) {

        AccessTokenFactory.lookupByToken(token).ifPresentOrElse(obj -> {
            Instant now = Instant.now();
            if (!obj.getValid() || now.isAfter(obj.getExpiration().toInstant())) {
                LOG.info("Forbidden: invalid token ...{} to access {}", sanitizeToken(token), filename);
                halt(HttpStatus.SC_FORBIDDEN, "This token is not valid");
            }
        }, () -> LOG.debug("Token ...{} to access {} doesn't exists in the database - could be an image build token",
                sanitizeToken(token), filename)
        );
        try {
            JwtClaims claims = JWT_CONSUMER.processToClaims(token);
            if (Optional.ofNullable(claims.getExpirationTime())
                .map(exp -> exp.isBefore(NumericDate.now()))
                .orElse(false)) {
                LOG.info("Forbidden: Token expired");
                halt(HttpStatus.SC_FORBIDDEN, "Token expired");
            }

            // enforce channel claim
            Optional<List<String>> channelClaim = Optional.ofNullable(claims.getStringListClaimValue("onlyChannels"))
                    // new versions of getStringListClaimValue() return an empty list instead of null
                    .filter(l -> !l.isEmpty());
            Opt.consume(channelClaim,
                    () -> LOG.info("Token ...{} does provide access to any channel",
                            sanitizeToken(token)),
                    channels -> {
                if (!channels.contains(channel)) {
                    LOG.info("Forbidden: Token ...{} does not provide access to channel {}",
                            sanitizeToken(token), channel);
                    LOG.info("Token allow access only to the following channels: {}", String.join(",", channels));
                    halt(HttpStatus.SC_FORBIDDEN, "Token does not provide access to channel " + channel);
                }
            });

            // enforce org claim
            Optional<Long> orgClaim = Optional.ofNullable(claims.getClaimValue("org", Long.class));
            Opt.consume(orgClaim, () -> {
                LOG.info("Forbidden: Token does not specify the organization");
                halt(HttpStatus.SC_BAD_REQUEST, "Token does not specify the organization");
            }, orgId -> {
                if (!ChannelFactory.isAccessibleBy(channel, orgId)) {
                    LOG.info("Forbidden: Token does not provide access to channel {}", channel);
                    halt(HttpStatus.SC_FORBIDDEN, "Token does not provide access to channel " + channel);
                }
            });
        }
        catch (InvalidJwtException | MalformedClaimException e) {
            LOG.info("Forbidden: Token ...{} is not valid to access {} in {}: {}",
                    sanitizeToken(token), filename, channel, e.getMessage());
            halt(HttpStatus.SC_FORBIDDEN,
                 String.format("Token is not valid to access %s in %s: %s", filename, channel, e.getMessage()));
        }
    }

    /**
     * Validate if the server is PAYG any compliant.
     *
     */
    private void validatePaygCompliant() {
        if (!cloudPaygManager.isCompliant()) {
            LOG.info("Forbidden: SUSE Manager PAYG Server is not compliant");
            halt(HttpStatus.SC_FORBIDDEN, "Server is not compliant. Please check the logs");
        }
    }

    /**
     * Write the actual file contents to the response.
     *
     * @param request the request object
     * @param response the response object
     * @param file description of the file to send
     */
    private Object downloadFile(Request request, Response response, File file) {
        if (!file.exists()) {
            LOG.info("404 - File not found: {}", file.getAbsolutePath());
            halt(HttpStatus.SC_NOT_FOUND, "File not found: " + request.url());
        }
        response.header("Content-Type", "application/octet-stream");
        response.header("Content-Disposition", "attachment; filename=" + file.getName());
        response.header("X-Sendfile", file.getAbsolutePath());
        return response;
    }

    /**
     * validateMinionInPayg In SUMA payg instances, checks if the Minion that corresponds to the given token is BYOS
     * and if SCC credentials are added. In BYOS and no SCC credentials it halts, and it does nothing otherwise.
     * @param token the token used to retrieve the Minion from
     */
    public void validateMinionInPayg(String token) {
        if (!cloudPaygManager.isPaygInstance()) {
            return;
        }

        Optional<AccessToken> accessToken = AccessTokenFactory.lookupByToken(token);
        if (accessToken.isPresent()) {
            cloudPaygManager.checkRefreshCache(true); // Refresh to have latest SCC data
            MinionServer minion = accessToken.get().getMinion();
            if (!cloudPaygManager.hasSCCCredentials() && !minion.isPayg()) {
                halt(HttpStatus.SC_FORBIDDEN,
                        "Bring-your-own-subscription instances in Pay-as-you-go SUMA instances is not " +
                        "allowed without SCC credentials.");
            }
        }
        else {
            try {
                JwtClaims claims = JWT_CONSUMER.processToClaims(token);
                boolean isValid = Optional.ofNullable(claims.getExpirationTime())
                        .map(exp -> {
                            long timeDeltaSeconds = Duration.ofMinutes(EXPIRATION_TIME_MINUTES_IN_THE_FUTURE_TEMP_TOKEN)
                                    .toSeconds();
                            long expireDeltaSeconds = exp.getValue() - NumericDate.now().getValue();
                            return expireDeltaSeconds > 0 && expireDeltaSeconds < timeDeltaSeconds;
                        })
                        .orElse(false);

                if (!isValid) {
                    LOG.info("Forbidden: Token is expired or is not a short-token");
                    halt(HttpStatus.SC_FORBIDDEN, "Forbidden: Token is expired or is not a short-token");
                }
            }
            catch (InvalidJwtException | MalformedClaimException e) {
                LOG.info("Forbidden: Short-token ...{} is not valid or is expired: {}",
                        sanitizeToken(token), e.getMessage());
                halt(HttpStatus.SC_FORBIDDEN,
                        "Forbidden: Short-token is not valid or is expired");
            }
        }
    }
}
