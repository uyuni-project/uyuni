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
import static com.suse.manager.webui.utils.SparkApplicationHelper.badRequest;
import static com.suse.manager.webui.utils.SparkApplicationHelper.internalServerError;
import static com.suse.manager.webui.utils.SparkApplicationHelper.unprocessableEntity;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.put;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.HubSCCCredentials;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.product.ChannelTemplate;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.frontend.xmlrpc.sync.content.SCCContentSyncSource;

import com.suse.manager.hub.RouteWithSCCAuth;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.utils.token.DownloadTokenBuilder;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCFileClient;
import com.suse.scc.client.SCCWebClient;
import com.suse.scc.model.SCCOrganizationSystemsUpdateResponse;
import com.suse.scc.model.SCCRegisterSystemItem;
import com.suse.scc.model.SCCRepositoryJson;
import com.suse.scc.model.SCCSystemCredentialsJson;
import com.suse.scc.model.SCCVirtualizationHostJson;
import com.suse.scc.proxy.SCCProxyManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    private static final Logger LOG = LogManager.getLogger(SCCEndpoints.class);

    private final SCCProxyManager sccProxyManager;

    /**
     * Constructor
     *
     * @param uuidIn   the UUID
     * @param sccUrlIn the SCC URL
     */
    public SCCEndpoints(String uuidIn, URI sccUrlIn) {
        this(uuidIn, sccUrlIn, new SCCProxyManager());
    }

    /**
     * Constructor
     *
     * @param uuidIn   the UUID
     * @param sccUrlIn the SCC URL
     * @param sccProxyManagerIn the SCC proxy manager
     */
    public SCCEndpoints(String uuidIn, URI sccUrlIn, SCCProxyManager sccProxyManagerIn) {
        this.uuid = uuidIn;
        this.sccUrl = sccUrlIn;
        this.sccProxyManager = sccProxyManagerIn;
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
        get("/hub/scc/connect/organizations/products/unscoped", asJson(withSCCAuth(this::unscoped)));
        get("/hub/scc/connect/organizations/repositories", asJson(withSCCAuth(this::repositories)));
        get("/hub/scc/connect/organizations/subscriptions", asJson(withSCCAuth(this::subscriptions)));
        get("/hub/scc/connect/organizations/orders", asJson(withSCCAuth(this::orders)));
        get("/hub/scc/suma/product_tree.json", asJson(this::productTree));
        put("/hub/scc/connect/organizations/systems", asJson(withSCCAuth(this::createOrUpdateSystems)));
        delete("/hub/scc/connect/organizations/systems/:id", asJson(withSCCAuth(this::deleteSystem)));
        put("/hub/scc/connect/organizations/virtualization_hosts", asJson(withSCCAuth(this::setVirtualizationHosts)));
    }


    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .create();

    private final Gson gsonSccProxy = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
            .serializeNulls()
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
     * Build and return a short living token for a Hub repository sync
     * @param channel the channel to the create the token for
     * @return the token
     */
    public static Optional<String> buildHubRepositoryToken(Channel channel) {
        String channelLabel = channel.getLabel();
        try {
            Long oid = Optional.ofNullable(channel.getOrg()).map(Org::getId).orElse(0L);
            DownloadTokenBuilder builder = new DownloadTokenBuilder(oid)
                    .usingServerSecret()
                    // Short lived 2 day + 4 hours tokens refreshed on ever sync
                    .expiringAfterMinutes(2L * (24 + 2) * 60)
                    .allowingOnlyChannels(Set.of(channelLabel));
            return Optional.of(builder.build().getSerializedForm());
        }
        catch (TokenBuildingException e) {
            LOG.error("Error creating token for channel: {}", channelLabel, e);
            return Optional.empty();
        }
    }

    /**
     * Build a {@link SCCRepositoryJson} object for a Hub Vendor repository
     * @param channelTemplate the channel template
     * @param hostname the hostname
     * @param token the token
     * @return return {@link SCCRepositoryJson} object for a Hub vendor repository
     */
    public static SCCRepositoryJson buildVendorRepoJson(ChannelTemplate channelTemplate, String hostname,
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
        var channels = peripheral.getPeripheralChannels();
        var jsonRepos = channels.stream().map(c -> {
            Channel channel = c.getChannel();
            String label = channel.getLabel();
            String tokenString = buildHubRepositoryToken(channel).orElse("");
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
                            SCCContentSyncSource contentSync = new SCCContentSyncSource(cred);
                            SCCWebClient sccWebClient = contentSync.getClient(uuid,
                                    Paths.get(SCCConfig.DEFAULT_LOGGING_DIR), false);
                            try {
                                return fn.apply(new SCCFileClient(sccWebClient.getCacheDir()));
                            }
                            catch (SCCClientException e) {
                                return fn.apply(sccWebClient);
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

    /**
     * SCC proxy endpoint: register a list of system (put /connect/organizations/systems)
     *
     * @param request
     * @param response
     * @param credentials
     * @return a {@Link SCCOrganizationSystemsUpdateResponse} json object
     */
    public String createOrUpdateSystems(Request request, Response response, HubSCCCredentials credentials) {
        try {
            TypeToken<Map<String, List<SCCRegisterSystemItem>>> typeToken = new TypeToken<>() { };
            Map<String, List<SCCRegisterSystemItem>> payload = gson.fromJson(request.body(), typeToken.getType());
            if (!payload.containsKey("systems")) {
                return badRequest(response, "wrong json input: missing systems key");
            }

            List<SCCRegisterSystemItem> systemsList = payload.get("systems");

            List<SCCSystemCredentialsJson> systemsResponse = sccProxyManager.createOrUpdateSystems(systemsList,
                            credentials.getPeripheralUrl())
                    .stream()
                    .map(r -> new SCCSystemCredentialsJson(
                            r.getSccLogin(),
                            r.getSccPasswd(),
                            r.getProxyId(),
                            new Date(0)))
                    .toList();

            response.status(HttpStatus.SC_CREATED);
            return gsonSccProxy.toJson(new SCCOrganizationSystemsUpdateResponse(systemsResponse));
        }
        catch (JsonSyntaxException eIn) {
            return badRequest(response, eIn.getMessage());
        }
        catch (Exception ex) {
            return internalServerError(response, ex.getMessage());
        }
    }

    /**
     * SCC proxy endpoint: delete a system (delete /connect/organizations/systems/id)
     *
     * @param request
     * @param response
     * @param credentials
     * @return empty body and http status code 204 (successfully deleted) or 404 (not found)
     */
    public String deleteSystem(Request request, Response response, HubSCCCredentials credentials) {
        try {
            long systemProxyId = Long.parseLong(request.params("id"));

            if (sccProxyManager.deleteSystem(systemProxyId)) {
                response.status(HttpStatus.SC_NO_CONTENT); //204
                return "";
            }
            response.status(HttpStatus.SC_NOT_FOUND); //404
            return "";
        }
        catch (NumberFormatException ex) {
            return badRequest(response, "wrong input: invalid id (%s)".formatted(request.params("id")));
        }
        catch (Exception ex) {
            return internalServerError(response, ex.getMessage());
        }
    }

    private String setVirtualizationHosts(Request request, Response response, HubSCCCredentials credentials) {
        try {
            TypeToken<Map<String, List<SCCVirtualizationHostJson>>> typeToken = new TypeToken<>() { };
            Map<String, List<SCCVirtualizationHostJson>> payload = gson.fromJson(request.body(), typeToken.getType());
            if (!payload.containsKey("virtualization_hosts")) {
                return unprocessableEntity(response, "wrong json input: missing virtualization_hosts key");
            }
            List<SCCVirtualizationHostJson> virtHostsList = payload.get("virtualization_hosts");

            sccProxyManager.setVirtualizationHosts(virtHostsList, credentials.getPeripheralUrl());
            response.status(HttpStatus.SC_OK); //200
        }
        catch (Exception ex) {
            return unprocessableEntity(response, ex.getMessage());
        }
        return null;
    }
}
