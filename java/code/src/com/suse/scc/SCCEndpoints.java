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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.HubSCCCredentials;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.suse.manager.iss.RouteWithSCCAuth;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.IssPeripheralChannels;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.client.SCCConfig;
import com.suse.scc.client.SCCConfigBuilder;
import com.suse.scc.client.SCCFileClient;
import com.suse.scc.client.SCCWebClient;
import com.suse.scc.model.SCCProductJson;
import com.suse.scc.model.SCCRepositoryJson;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static spark.Spark.get;

public class SCCEndpoints {

    public final String UUID;
    public final URI SCC_URL;

    public SCCEndpoints(String UUIDIn, URI SCC_URLIn) {
        this.UUID = UUIDIn;
        this.SCC_URL = SCC_URLIn;
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
                    CredentialsFactory.listCredentialsByType(HubSCCCredentials.class).stream().filter(c -> {
                            return c.getUsername().equals(username) &&
                                    MessageDigest.isEqual(c.getPassword().getBytes(), password.getBytes());
                    }).findFirst();


            return credentials
                    .map(c -> route.handle(request, response, c))
                    .orElseGet(() -> {
                        Spark.halt(HttpServletResponse.SC_UNAUTHORIZED);
                        return null;
                    });
        };
    }

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

    private String changeHost(String url, String host) throws URISyntaxException {
        URI uri = new URI(url);
        URI adjusted = new URI(uri.getScheme(), uri.getUserInfo(), host, uri.getPort(),
                uri.getPath(), uri.getQuery(), uri.getFragment());
        return adjusted.toString();
    }

    private SCCProductJson adjustProduct(SCCProductJson json, String hubFqdn) {
        List<SCCRepositoryJson> repositories = json.getRepositories().stream().map(repo -> {
            try {
                repo.setUrl(changeHost(repo.getUrl(), hubFqdn));
                return repo;
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }).toList();
        List<SCCProductJson> extensions = json.getExtensions().stream()
                .map(product -> adjustProduct(product, hubFqdn)).toList();
        return json.copy()
                .setRepositories(repositories)
                .setExtensions(extensions)
                .build();
    }

    public String unscoped(Request request, Response response, HubSCCCredentials credentials) {
        var hostname = ConfigDefaults.get().getJavaHostname();
        return serveEndpoint(c -> c.listProducts().stream()
                .map(product -> adjustProduct(product, hostname)).toList());
    }


    /**
     * Endpoint serving ISS peripheral channel information in scc repository format
     * @param request
     * @param response
     * @param credentials
     * @return
     */
    public String repositories(Request request, Response response, HubSCCCredentials credentials) {
        var hostname = ConfigDefaults.get().getJavaHostname();
        var peripheral = credentials.getIssPeripheral();
        var channels = peripheral.getPeripheralChannels();
        var jsonRepos = channels.stream().map(c -> {
            Channel channel = c.getChannel();
            String label = channel.getLabel();
            return SUSEProductFactory.lookupByChannelLabelFirst(label).map(channelTemplate -> {
                SCCRepository repository = channelTemplate.getRepository();
                SCCRepositoryJson json = new SCCRepositoryJson();
                json.setSCCId(repository.getSccId());
                json.setEnabled(channelTemplate.isMandatory());
                json.setName(repository.getName());
                json.setDescription(repository.getDescription());
                json.setUrl("https://" + hostname + "/rhn/manager/download/" + label + "?token");
                json.setInstallerUpdates(repository.isInstallerUpdates());
                json.setAutorefresh(repository.isAutorefresh());
                json.setDistroTarget(repository.getDistroTarget());
                return json;
            }).orElseGet(() -> {
                //TODO: Handle custom channels
                SCCRepositoryJson json = new SCCRepositoryJson();
                json.setSCCId(-1_000_000L);
                json.setEnabled(true);
                json.setName(label);
                json.setDescription("");
                json.setUrl("https://" + hostname + "/rhn/manager/download/" + label + "?token");
                json.setInstallerUpdates(false);
                json.setAutorefresh(false);
                json.setDistroTarget("");
                return json;
            });
        }).toList();
        return gson.toJson(jsonRepos);
    }

    public String subscriptions(Request request, Response response, HubSCCCredentials credentials) {
        return "[]";
    }

    public String orders(Request request, Response response, HubSCCCredentials credentials) {
        return "[]";
    }

    private <T> String serveEndpoint(Function<SCCClient, T> fn) {
        return ConfigDefaults.get().getOfflineMirrorDir()
                .map(path -> fn.apply(new SCCFileClient(Paths.get(path))))
                .or(() -> {
                   return CredentialsFactory.listSCCCredentials().stream()
                           .filter(c -> c.isPrimary())
                           .findFirst()
                           .map(cred -> {

                               String username = cred.getUsername();

                               Path path = Paths.get(SCCConfig.DEFAULT_LOGGING_DIR).resolve(username);

                               try {
                                   return fn.apply(new SCCFileClient(path));
                               } catch (SCCClientException e) {
                                   String password = cred.getPassword();

                                   SCCConfig config = new SCCConfigBuilder()
                                           .setUrl(SCC_URL)
                                           .setUsername(username)
                                           .setPassword(password)
                                           .setUuid(UUID)
                                           .setLoggingDir(SCCConfig.DEFAULT_LOGGING_DIR)
                                           .setSkipOwner(false)
                                           .createSCCConfig();

                                   return fn.apply(new SCCWebClient(config));
                               }

                           });
                }).map(gson::toJson).orElse("");
    }

    public String productTree(Request request, Response response) {
        return serveEndpoint(c -> c.productTree());
    }
}
