/*
 * Copyright (c) 2015--2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.scc;

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static spark.Spark.get;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.HubSCCCredentials;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.product.ChannelTemplate;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.scc.SCCRepository;

import com.suse.manager.hub.RouteWithSCCAuth;
import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.IssPeripheralChannelToken;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.utils.token.DownloadTokenBuilder;
import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenParsingException;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCConfigBuilder;
import com.suse.scc.client.SCCFileClient;
import com.suse.scc.client.SCCWebClient;
import com.suse.scc.model.SCCRepositoryJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.servlet.http.HttpServletResponse;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

public class SCCEndpoints {

    /**
     * A Hub deliver custom repositories via organization/repositories SCC endpoint.
     * We need a fake repo ID for it.
     */
    public static final Long CUSTOM_REPO_FAKE_SCC_ID = Long.MIN_VALUE;

    private final String uuid;
    private final URI sccUrl;

    private final HubFactory hubFactory;

    /**
     * Constructor
     * @param uuidIn the UUID
     * @param sccUrlIn the SCC URL
     */
    public SCCEndpoints(String uuidIn, URI sccUrlIn) {
        this.uuid = uuidIn;
        this.sccUrl = sccUrlIn;
        this.hubFactory = new HubFactory();
    }

    private Route withSCCAuth(RouteWithSCCAuth route) {
        return (request, response) -> {
            String authorization = request.headers("Authorization");
            if (authorization == null) {
                response.header("www-authenticate", "Basic realm=\"SCC Connect API\"");
                Spark.halt(HttpServletResponse.SC_UNAUTHORIZED);
            }

            String[] auth = authorization.split(" ", 2);
            if (auth.length != 2 || !auth[0].equalsIgnoreCase("basic")) {
                Spark.halt(HttpServletResponse.SC_BAD_REQUEST);
            }
            var decoded = new String(Base64.getDecoder().decode(auth[1]), StandardCharsets.UTF_8);
            var userpass = decoded.split(":", 2);
            if (userpass.length != 2) {
                Spark.halt(HttpServletResponse.SC_BAD_REQUEST);
            }

            var username = userpass[0];
            var password = userpass[1];

            Optional<HubSCCCredentials> credentials =
                    CredentialsFactory.listCredentialsByType(HubSCCCredentials.class).stream().filter(c ->
                            c.getUsername().equals(username) &&
                                    MessageDigest.isEqual(c.getPassword().getBytes(), password.getBytes())
                    ).findFirst();


            return credentials
                    .map(c -> route.handle(request, response, c))
                    .orElseGet(() -> {
                        Spark.halt(HttpServletResponse.SC_UNAUTHORIZED);
                        return null;
                    });
        };
    }

    /**
     * Initialize routs
     * @param jade jade
     */
    public void initRoutes(JadeTemplateEngine jade) {
        get("/manager/api/scc/connect/organizations/products/unscoped", asJson(withSCCAuth(this::unscoped)));
        get("/manager/api/scc/connect/organizations/repositories", asJson(withSCCAuth(this::repositories)));
        get("/manager/api/scc/connect/organizations/subscriptions", asJson(withSCCAuth(this::subscriptions)));
        get("/manager/api/scc/connect/organizations/orders", asJson(withSCCAuth(this::orders)));
        get("/manager/api/scc/suma/product_tree.json", asJson(this::productTree));
    }

    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .create();

    /**
     * organization/products/unscoped endpoint
     * @param request the request
     * @param response the response
     * @param credentials the Hub credentials
     * @return return unscoped json as string
     */
    public String unscoped(Request request, Response response, HubSCCCredentials credentials) {
        return serveEndpoint(SCCClient::listProducts);
    }

    private void refreshTokens(List<IssPeripheral> peripherals) {
        peripherals.forEach(p -> {
            p.getPeripheralChannels().forEach(pc -> {
                DownloadTokenBuilder builder = new DownloadTokenBuilder(0)
                        .usingServerSecret()
                        .allowingOnlyChannels(Set.of(pc.getChannel().getLabel()));
                Instant now = Instant.now();
                try {
                    if (pc.getToken() != null) {
                        IssPeripheralChannelToken pct = pc.getToken();
                        if (now.isAfter(pct.getExpirationDate().toInstant())) {
                            Token newToken = builder.build();
                            pct.setToken(newToken.getSerializedForm());
                            pct.setValid(true);
                            pct.setExpirationDate(Date.from(newToken.getExpirationTime()));
                        }
                    }
                    else {
                        Token newToken = builder.build();
                        IssPeripheralChannelToken pct = new IssPeripheralChannelToken(
                                newToken.getSerializedForm(), Date.from(newToken.getExpirationTime()));
                        pc.setToken(pct);
                    }

                }
                catch (TokenBuildingException | TokenParsingException e) {
                    throw new RhnRuntimeException(e);
                }
            });
            hubFactory.save(p);
        });
    }

    /**
     * Build a {@link SCCRepositoryJson} object for a Hub custom repository
     * @param label the label
     * @param hostname the hostname
     * @param token the token
     * @return return {@link SCCRepositoryJson} object for a Hub custom repository
     */
    public static SCCRepositoryJson buildCustomRepoJson(String label, String hostname, String token) {
        SCCRepositoryJson json = new SCCRepositoryJson();
        json.setSCCId(CUSTOM_REPO_FAKE_SCC_ID);
        json.setEnabled(true);
        json.setName(label);
        json.setDescription("");
        json.setUrl("https://%1$s/rhn/manager/download/hubsync/%2$s/?%3$s".formatted(hostname, label, token));
        json.setInstallerUpdates(false);
        json.setAutorefresh(false);
        json.setDistroTarget("");
        return json;
    }

    /**
     * Build a {@link SCCRepositoryJson} object for a Hub Vendor repository
     * @param channelTemplate the channel template
     * @param hostname the hostname
     * @param token the token
     * @return return {@link SCCRepositoryJson} object for a Hub vendor repository
     */
    private static SCCRepositoryJson buildVendorRepoJson(ChannelTemplate channelTemplate, String hostname,
                                                        String token) {
        SCCRepository repository = channelTemplate.getRepository();
        SCCRepositoryJson json = new SCCRepositoryJson();
        json.setSCCId(repository.getSccId());
        json.setEnabled(channelTemplate.isMandatory());
        json.setName(repository.getName());
        json.setDescription(repository.getDescription());
        json.setUrl("https://%1$s/rhn/manager/download/hubsync/%2$d/?%3$s".formatted(
                hostname, repository.getSccId(), token));
        json.setInstallerUpdates(repository.isInstallerUpdates());
        json.setAutorefresh(repository.isAutorefresh());
        json.setDistroTarget(repository.getDistroTarget());
        return json;
    }


    /**
     * Endpoint serving ISS peripheral channel information in scc repository format
     *
     * @param request
     * @param response
     * @param credentials
     * @return return the repositories
     */
    public String repositories(Request request, Response response, HubSCCCredentials credentials) {
        var hostname = ConfigDefaults.get().getJavaHostname();
        var peripheral = credentials.getIssPeripheral();
        refreshTokens(List.of(peripheral));
        var channels = peripheral.getPeripheralChannels();
        var jsonRepos = channels.stream().map(c -> {
            Channel channel = c.getChannel();
            String label = channel.getLabel();
            IssPeripheralChannelToken peripheralChannelToken = c.getToken();
            String tokenString = peripheralChannelToken.getToken();
            return SUSEProductFactory.lookupByChannelLabelFirst(label)
                    .map(channelTemplate -> buildVendorRepoJson(channelTemplate, hostname, tokenString))
                    .orElseGet(() -> buildCustomRepoJson(label, hostname, tokenString));
        }).toList();
        return gson.toJson(jsonRepos);
    }

    /**
     * Endpoint serving ISS peripheral subscription information
     *
     * @param request
     * @param response
     * @param credentials
     * @return return always empty list
     */
    public String subscriptions(Request request, Response response, HubSCCCredentials credentials) {
        return "[]";
    }

    /**
     * Endpoint serving ISS peripheral order information
     *
     * @param request
     * @param response
     * @param credentials
     * @return return always empty list
     */
    public String orders(Request request, Response response, HubSCCCredentials credentials) {
        return "[]";
    }

    private <T> String serveEndpoint(Function<SCCClient, T> fn) {
        return ConfigDefaults.get().getOfflineMirrorDir()
                .map(path -> fn.apply(new SCCFileClient(Paths.get(path))))
                .or(() -> CredentialsFactory.listSCCCredentials().stream()
                        .filter(SCCCredentials::isPrimary)
                        .findFirst()
                        .map(cred -> {
                            String username = cred.getUsername();
                            Path path = Paths.get(SCCConfig.DEFAULT_LOGGING_DIR).resolve(username);
                            try {
                                return fn.apply(new SCCFileClient(path));
                            }
                            catch (SCCClientException e) {
                                String password = cred.getPassword();

                                SCCConfig config = new SCCConfigBuilder()
                                        .setUrl(sccUrl)
                                        .setUsername(username)
                                        .setPassword(password)
                                        .setUuid(uuid)
                                        .setLoggingDir(SCCConfig.DEFAULT_LOGGING_DIR)
                                        .setSkipOwner(false)
                                        .createSCCConfig();

                                return fn.apply(new SCCWebClient(config));
                            }
                        })
                ).map(gson::toJson).orElse("[]");
    }

    /**
     * Endpoint serving ISS peripheral product tree
     *
     * @param request
     * @param response
     * @return return the product tree
     */
    public String productTree(Request request, Response response) {
        return serveEndpoint(SCCClient::productTree);
    }
}
