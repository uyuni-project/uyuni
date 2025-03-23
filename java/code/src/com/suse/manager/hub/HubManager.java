/*
 * Copyright (c) 2024--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.hub;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.HubSCCCredentials;
import com.redhat.rhn.domain.credentials.ReportDBCredentials;
import com.redhat.rhn.domain.credentials.SCCCredentials;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MgrServerInfo;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFQDN;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.SystemManagerUtils;
import com.redhat.rhn.manager.system.SystemsExistException;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.model.hub.AccessTokenDTO;
import com.suse.manager.model.hub.ChannelInfoDetailsJson;
import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.model.hub.IssHub;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.IssPeripheralChannels;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.model.hub.IssServer;
import com.suse.manager.model.hub.ManagerInfoJson;
import com.suse.manager.model.hub.OrgInfoJson;
import com.suse.manager.model.hub.TokenType;
import com.suse.manager.model.hub.UpdatableServerData;
import com.suse.manager.webui.controllers.ProductsController;
import com.suse.manager.webui.controllers.admin.beans.ChannelSyncModel;
import com.suse.manager.webui.controllers.admin.beans.IssV3ChannelResponse;
import com.suse.manager.webui.utils.token.IssTokenBuilder;
import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenParser;
import com.suse.manager.webui.utils.token.TokenParsingException;
import com.suse.utils.CertificateUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Transaction;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Business logic to manage ISSv3 Sync
 */
public class HubManager {

    private final MirrorCredentialsManager mirrorCredentialsManager;

    private final HubFactory hubFactory;

    private final HubClientFactory clientFactory;

    private final SystemEntitlementManager systemEntitlementManager;

    private final TaskomaticApi taskomaticApi;

    private static final Logger LOG = LogManager.getLogger(HubManager.class);

    private static final String ROOT_CA_FILENAME_TEMPLATE = "%s_%s_root_ca.pem";

    /**
     * A Hub deliver custom repositories via organization/repositories SCC endpoint.
     * We need a fake repo ID for it.
     */
    public static final Long CUSTOM_REPO_FAKE_SCC_ID = Long.MIN_VALUE;

    /**
     * Default constructor
     */
    public HubManager() {
        this(new HubFactory(), new HubClientFactory(), new MirrorCredentialsManager(), new TaskomaticApi(),
                GlobalInstanceHolder.SYSTEM_ENTITLEMENT_MANAGER);
    }

    /**
     * Builds an instance with the given dependencies
     * @param hubFactoryIn the hub factory
     * @param clientFactoryIn the ISS client factory
     * @param mirrorCredentialsManagerIn the mirror credentials manager
     * @param taskomaticApiIn the TaskomaticApi object
     * @param systemEntitlementManagerIn the system entitlement manager
     */
    public HubManager(HubFactory hubFactoryIn, HubClientFactory clientFactoryIn,
                      MirrorCredentialsManager mirrorCredentialsManagerIn, TaskomaticApi taskomaticApiIn,
                      SystemEntitlementManager systemEntitlementManagerIn) {
        this.hubFactory = hubFactoryIn;
        this.clientFactory = clientFactoryIn;
        this.mirrorCredentialsManager = mirrorCredentialsManagerIn;
        this.taskomaticApi = taskomaticApiIn;
        this.systemEntitlementManager = systemEntitlementManagerIn;
    }

    /**
     * Create a new access token for the given FQDN and store it in the database
     * @param user the user performing the operation
     * @param fqdn the FQDN of the peripheral/hub
     * @return the serialized form of the token
     * @throws TokenBuildingException when an error occurs during generation
     * @throws TokenParsingException when the generated token cannot be parsed
     */
    public String issueAccessToken(User user, String fqdn) throws TokenBuildingException, TokenParsingException {
        ensureSatAdmin(user);

        Token token = createAndSaveToken(fqdn);
        return token.getSerializedForm();
    }

    /**
     * Stores in the database the access token of the given FQDN
     * @param user the user performing the operation
     * @param fqdn the FQDN of the peripheral/hub that generated this token
     * @param token the token
     * @throws TokenParsingException when it's not possible to process the token
     */
    public void storeAccessToken(User user, String fqdn, String token) throws TokenParsingException {
        ensureSatAdmin(user);

        parseAndSaveToken(fqdn, token);
    }

    /**
     * Deletes the access token with the specified id
     * @param user the user performing the operation
     * @param tokenId the id of the access token to delete
     * @return true if the token was deleted, false otherwise
     */
    public boolean deleteAccessToken(User user, long tokenId) {
        ensureSatAdmin(user);

        return hubFactory.removeAccessTokenById(tokenId);
    }

    /**
     * Stores in the database the access token of the given FQDN
     * @param accessToken the access token granting access and identifying the caller
     * @param tokenToStore the token
     * @throws TokenParsingException when it's not possible to process the token
     */
    public void storeAccessToken(IssAccessToken accessToken, String tokenToStore) throws TokenParsingException {
        ensureValidToken(accessToken);

        parseAndSaveToken(accessToken.getServerFqdn(), tokenToStore);
    }

    /**
     * Returns the ISS of the specified role, if present
     * @param accessToken the access token granting access and identifying the caller
     * @param role the role of the server
     * @return an {@link IssHub} or {@link IssPeripheral} depending on the specified role, null if the FQDN is unknown
     */
    public IssServer findServer(IssAccessToken accessToken, IssRole role) {
        ensureValidToken(accessToken);

        return lookupServerByFqdnAndRole(accessToken.getServerFqdn(), role);
    }

    /**
     * Returns the ISS of the specified role, if present
     * @param user the user performing the operation
     * @param serverFqdn the fqdn of the server
     * @param role the role of the server
     * @return an {@link IssHub} or {@link IssPeripheral} depending on the specified role, null if the FQDN is unknown
     */
    public IssServer findServer(User user, String serverFqdn, IssRole role) {
        ensureSatAdmin(user);

        return lookupServerByFqdnAndRole(serverFqdn, role);
    }

    /**
     * Returns the ISS of the specified role, if present
     * @param user the user performing the operation
     * @param id the id of the server
     * @param role the role of the server
     * @return an {@link IssHub} or {@link IssPeripheral} depending on the specified role, null if the FQDN is unknown
     */
    public IssServer findServer(User user, long id, IssRole role) {
        ensureSatAdmin(user);

        return lookupServerByIdAndRole(id, role);
    }

    /**
     * Save the given remote server as hub or peripheral depending on the specified role
     * @param accessToken the access token granting access and identifying the caller
     * @param role the role of the server
     * @param rootCA the root certificate, if needed
     * @param gpgKey the gpg key, if needed
     * @return the persisted remote server
     */
    public IssServer saveNewServer(IssAccessToken accessToken, IssRole role, String rootCA, String gpgKey)
            throws TaskomaticApiException {
        ensureValidToken(accessToken);

        return createServer(role, accessToken.getServerFqdn(), rootCA, gpgKey, null);
    }

    /**
     * Deregister the server with the given FQDN. The de-registration can be optionally performed also on the
     * remote server.
     * @param user the user
     * @param fqdn the FQDN
     * @param onlyLocal specify if the de-registration has to be performed also on the remote server
     * @throws CertificateException when it's not possible to use remote server certificate
     * @throws IOException when the connection with the remote server fails
     */
    public void deregister(User user, String fqdn, boolean onlyLocal) throws CertificateException, IOException {
        ensureSatAdmin(user);

        IssRole remoteRole = hubFactory.isISSPeripheral() ? IssRole.HUB : IssRole.PERIPHERAL;
        IssServer server = findServer(user, fqdn, remoteRole);

        if (!onlyLocal) {
            IssAccessToken accessToken = hubFactory.lookupAccessTokenFor(server.getFqdn());
            var internalClient = clientFactory.newInternalClient(fqdn, accessToken.getToken(), server.getRootCa());
            internalClient.deregister();
        }

        switch (remoteRole) {
            case HUB -> deleteHub(fqdn);
            case PERIPHERAL -> deletePeripheral(fqdn);
            default -> throw new IllegalStateException("Role should either be HUB or PERIPHERAL");
        }
    }

    /**
     * Delete locally all ISS artifacts for the hub or peripheral server identified by the FQDN
     * @param accessToken the token
     * @param fqdn the FQDN
     */
    public void deleteIssServerLocal(IssAccessToken accessToken, String fqdn) {
        ensureValidToken(accessToken);
        if (hubFactory.isISSPeripheral()) {
            deleteHub(fqdn);
        }
        else {
            deletePeripheral(fqdn);
        }
    }

    private void deletePeripheral(String peripheralFqdn) {
        Optional<IssPeripheral> issPeripheral = hubFactory.lookupIssPeripheralByFqdn(peripheralFqdn);
        if (issPeripheral.isEmpty()) {
            LOG.info("Peripheral Server with name {} not found", peripheralFqdn);
            return; // no error as the state is already as wanted.
        }

        IssPeripheral peripheral = issPeripheral.get();
        deletePeripheral(peripheral);
    }

    private void deletePeripheral(IssPeripheral peripheral) {
        CredentialsFactory.removeCredentials(peripheral.getMirrorCredentials());
        hubFactory.remove(peripheral);
        hubFactory.removeAccessTokensFor(peripheral.getFqdn());
    }

    private void deleteHub(String hubFqdn) {
        Optional<IssHub> issHub = hubFactory.lookupIssHubByFqdn(hubFqdn);
        if (issHub.isEmpty()) {
            LOG.info("Hub Server with name {} not found", hubFqdn);
            return; // no error as the state is already as wanted.
        }
        IssHub hub = issHub.get();
        deleteHub(hub);
    }

    private void deleteHub(IssHub hub) {
        CredentialsFactory.removeCredentials(hub.getMirrorCredentials());
        hubFactory.remove(hub);
        hubFactory.removeAccessTokensFor(hub.getFqdn());
    }

    /**
     * Replace locally the current token with a new created one and return it.
     * Store the provided new token for the remote server.
     * @param currentAccessToken the old/current token
     * @param newRemoteToken the new token
     * @return the new generated local token for the calling side.
     * @throws TokenBuildingException when an error occurs during generation
     * @throws TokenParsingException when the generated token cannot be parsed
     */
    public String replaceTokens(IssAccessToken currentAccessToken, String newRemoteToken)
            throws TokenBuildingException, TokenParsingException {
        ensureValidToken(currentAccessToken);

        // store the new token to access the remote side
        parseAndSaveToken(currentAccessToken.getServerFqdn(), newRemoteToken);

        // Generate a new token to access this server for the remote side
        Token localToken = createAndSaveToken(currentAccessToken.getServerFqdn());
        return localToken.getSerializedForm();
    }

    /**
     * Replace the local Hub and remote Peripheral tokens for an ISS connection.
     * A new local hub token is issued and send to the peripheral side. The peripheral
     * side store this token and issue a new one for the calling hub server which store it.
     *
     * @param user the user
     * @param remoteServer the remote peripheral server FQDN
     * @throws CertificateException if the specified certificate is not parseable
     * @throws TokenParsingException if the specified token is not parseable
     * @throws TokenBuildingException if an error occurs while generating the token for the server
     * @throws IOException when connecting to the server fails
     */
    public void replaceTokensHub(User user, String remoteServer)
            throws CertificateException, IOException, TokenParsingException, TokenBuildingException {
        ensureSatAdmin(user);

        IssPeripheral issPeripheral = hubFactory.lookupIssPeripheralByFqdn(remoteServer).orElseThrow(() ->
                new IllegalStateException("Server " + remoteServer + " is not registered as peripheral"));

        // Generate a token for this server on the remote
        String newLocalToken = issueAccessToken(user, remoteServer);

        // Create a client to connect to the internal API of the remote server
        IssAccessToken currentAccessToken = hubFactory.lookupAccessTokenFor(issPeripheral.getFqdn());
        var internalApi = clientFactory.newInternalClient(issPeripheral.getFqdn(), currentAccessToken.getToken(),
                issPeripheral.getRootCa());
        String newRemoteToken = internalApi.replaceTokens(newLocalToken);
        parseAndSaveToken(remoteServer, newRemoteToken);
    }

    /**
     * Register a remote PERIPHERAL server
     *
     * @param user the user performing the operation
     * @param remoteServer the peripheral server FQDN
     * @param username the username of a {@link RoleFactory#SAT_ADMIN} of the remote server
     * @param password the password of the specified user
     * @param rootCA the optional root CA of the remote server. can be null
     *
     * @throws CertificateException if the specified certificate is not parseable
     * @throws TokenParsingException if the specified token is not parseable
     * @throws TokenBuildingException if an error occurs while generating the token for the server
     * @throws IOException when connecting to the server fails
     */
    public void register(User user, String remoteServer, String username, String password, String rootCA)
        throws CertificateException, TokenBuildingException, IOException, TokenParsingException,
            TaskomaticApiException {
        ensureSatAdmin(user);

        // Verify this server is not already registered as hub or peripheral
        ensureServerNotRegistered(remoteServer);

        // Generate a token for this server on the remote
        String remoteToken;
        try (var externalClient = clientFactory.newExternalClient(remoteServer, username, password, rootCA)) {
            remoteToken = externalClient.generateAccessToken(ConfigDefaults.get().getHostname());
        }

        registerWithToken(user, remoteServer, rootCA, remoteToken);
    }

    /**
     * Register a remote PERIPHERAL server
     *
     * @param user the user performing the operation
     * @param remoteServer the peripheral server FQDN
     * @param remoteToken the token used to connect to the peripheral server
     * @param rootCA the optional root CA of the peripheral server
     *
     * @throws CertificateException if the specified certificate is not parseable
     * @throws TokenParsingException if the specified token is not parseable
     * @throws TokenBuildingException if an error occurs while generating the token for the peripheral server
     * @throws IOException when connecting to the peripheral server fails
     */
    public void register(User user, String remoteServer, String remoteToken, String rootCA)
        throws CertificateException, TokenBuildingException, IOException, TokenParsingException,
            TaskomaticApiException {
        ensureSatAdmin(user);

        // Verify this server is not already registered as hub or peripheral
        ensureServerNotRegistered(remoteServer);

        registerWithToken(user, remoteServer, rootCA, remoteToken);
    }

    /**
     * Generate SCC credentials for the specified peripheral
     * @param accessToken the access token granting access and identifying the caller
     * @return the generated {@link HubSCCCredentials}
     */
    public HubSCCCredentials generateSCCCredentials(IssAccessToken accessToken) {
        ensureValidToken(accessToken);

        IssPeripheral peripheral = hubFactory.lookupIssPeripheralByFqdn(accessToken.getServerFqdn())
            .orElseThrow(() -> new IllegalArgumentException("Access token does not identify a peripheral server"));

        return generateCredentials(peripheral);
    }

    /**
     * Store the given SCC credentials into the credentials database
     * @param accessToken the access token granting access and identifying the caller
     * @param username the username
     * @param password the password
     * @return the stored {@link SCCCredentials}
     */
    public SCCCredentials storeSCCCredentials(IssAccessToken accessToken, String username, String password) {
        ensureValidToken(accessToken);

        IssHub hub = hubFactory.lookupIssHubByFqdn(accessToken.getServerFqdn())
            .orElseThrow(() -> new IllegalArgumentException("Access token does not identify a hub server"));

        return saveCredentials(hub, username, password);
    }

    /**
     * Set the User and Password for the report database in MgrServerInfo.
     * That trigger also a state apply to set this user in the report database.
     *
     * @param user the user
     * @param server the Mgr Server
     * @param forcePwChange force a password change
     */
    public void setReportDbUser(User user, Server server, boolean forcePwChange)
            throws CertificateException, IOException {
        ensureSatAdmin(user);
        // Create a report db user when system is a mgr server
        if (!server.isMgrServer()) {
            return;
        }
        // create default user with random password
        MgrServerInfo mgrServerInfo = server.getMgrServerInfo();
        if (StringUtils.isAnyBlank(mgrServerInfo.getReportDbName(), mgrServerInfo.getReportDbHost())) {
            // no reportdb configured
            return;
        }

        String password = RandomStringUtils.random(24, 0, 0, true, true, null, new SecureRandom());
        ReportDBCredentials credentials = Optional.ofNullable(mgrServerInfo.getReportDbCredentials())
                .map(existingCredentials -> {
                    if (forcePwChange) {
                        existingCredentials.setPassword(password);
                        CredentialsFactory.storeCredentials(existingCredentials);
                    }

                    return existingCredentials;
                })
                .orElseGet(() -> {
                    String randomSuffix = RandomStringUtils.random(8, 0, 0, true, false, null, new SecureRandom());
                    // Ensure the username is stored lowercase in the database, since the script
                    // uyuni-setup-reportdb-user will convert it to lowercase anyway
                    String username = "hermes_" + randomSuffix.toLowerCase();

                    ReportDBCredentials reportCreds = CredentialsFactory.createReportCredentials(username, password);
                    CredentialsFactory.storeCredentials(reportCreds);
                    return reportCreds;
                });

        mgrServerInfo.setReportDbCredentials(credentials);

        Optional<IssPeripheral> issPeripheral = server.getFqdns().stream()
                .flatMap(fqdn -> hubFactory.lookupIssPeripheralByFqdn(fqdn.getName()).stream())
                .findFirst();
        if (issPeripheral.isPresent()) {
            IssPeripheral remoteServer = issPeripheral.get();
            IssAccessToken token = hubFactory.lookupAccessTokenFor(remoteServer.getFqdn());

            HubInternalClient internalApi = clientFactory.newInternalClient(remoteServer.getFqdn(),
                    token.getToken(), remoteServer.getRootCa());
            internalApi.storeReportDbCredentials(credentials.getUsername(), credentials.getPassword());
            String summary = "Report Database credentials changed by " + user.getLogin();
            String details = MessageFormat.format("""
            The Report Database credentials were changed by {0}.
            Report Database User: {1}
            """, user.getLogin(), credentials.getUsername());
            SystemManager.addHistoryEvent(server, summary, details);
        }
    }

    /**
     * Collect data about a Manager Server
     * @param accessToken the accesstoken
     * @return return {@link ManagerInfoJson}
     */
    public ManagerInfoJson collectManagerInfo(IssAccessToken accessToken) {
        ensureValidToken(accessToken);
        return collectManagerInfo();
    }

    /**
     * Set server details
     *
     * @param token the access token
     * @param fqdn the FQDN identifying the Hub or Peripheral Server
     * @param role the role which should be changed
     * @param data the new data
     */
    public void updateServerData(IssAccessToken token, String fqdn, IssRole role, UpdatableServerData data)
        throws TaskomaticApiException {
        ensureValidToken(token);
        updateServerData(fqdn, role, data);
    }

    /**
     * Set server details
     *
     * @param user The current user
     * @param fqdn the FQDN identifying the Hub or Peripheral Server
     * @param role the role which should be changed
     * @param data the new data
     * @throws TaskomaticApiException when it's not possible to schedule the certificate refresh
     */
    public void updateServerData(User user, String fqdn, IssRole role, UpdatableServerData data)
        throws TaskomaticApiException {
        ensureSatAdmin(user);
        updateServerData(fqdn, role, data);
    }

    private void updateServerData(String fqdn, IssRole role, UpdatableServerData data) throws TaskomaticApiException {
        Optional<? extends IssServer> server = switch (role) {
            case HUB -> hubFactory.lookupIssHubByFqdn(fqdn);
            case PERIPHERAL -> hubFactory.lookupIssPeripheralByFqdn(fqdn);
        };

        server.ifPresentOrElse(issServer -> {
            if (data.hasRootCA()) {
                issServer.setRootCa(data.getRootCA());
            }

            if (data.hasGpgKey() && issServer instanceof IssHub issHub) {
                issHub.setGpgKey(data.getGpgKey());
            }

            hubFactory.save(issServer);
        }, () -> {
            LOG.error("Server {} not found with role {}", fqdn, role);
            throw new IllegalArgumentException("Server not found");
        });

        if (data.hasRootCA()) {
            taskomaticApi.scheduleSingleRootCaCertUpdate(computeRootCaFileName(role, fqdn), data.getRootCA());
        }
    }

    /**
     * Count currently issued and consumed access tokens
     * @param user the user performing the operation
     * @return the number of token existing in the database
     */
    public long countAccessToken(User user) {
        ensureSatAdmin(user);

        return hubFactory.countAccessToken();
    }

    /**
     * List the currently issued and consumed access tokens
     * @param user the user performing the operation
     * @param pc the pagination settings
     * @return the existing tokens retrieved from the database using the specified pagination settings
     */
    public List<AccessTokenDTO> listAccessToken(User user, PageControl pc) {
        ensureSatAdmin(user);

        return hubFactory.listAccessToken(pc.getStart() - 1, pc.getPageSize());
    }

    /**
     * Retrieve the access token with the specied id
     * @param user the user performing the operation
     * @param tokenId the id of the token
     * @return the access token, if present
     */
    public Optional<IssAccessToken> lookupAccessTokenById(User user, long tokenId) {
        ensureSatAdmin(user);

        return hubFactory.lookupAccessTokenById(tokenId);
    }

    /**
     * Updates the give token in the database
     * @param user the user performing the operation
     * @param issAccessToken the token
     */
    public void updateToken(User user, IssAccessToken issAccessToken) {
        ensureSatAdmin(user);

        hubFactory.updateToken(issAccessToken);
    }

    private ManagerInfoJson collectManagerInfo() {
        String reportDbName = Config.get().getString(ConfigDefaults.REPORT_DB_NAME, "");
        // we need to provide the external hostname
        String reportDbHost = Config.get().getString(ConfigDefaults.SERVER_HOSTNAME, "");
        int reportDbPort = Config.get().getInt(ConfigDefaults.REPORT_DB_PORT, 5432);
        String version = ConfigDefaults.get().getProductVersion().split("\\s")[0];

        return new ManagerInfoJson(
                version,
                StringUtils.isNoneBlank(reportDbName, reportDbHost),
                reportDbName, reportDbHost, reportDbPort);
    }

    private void registerWithToken(User user, String remoteServer, String rootCA, String remoteToken)
        throws TokenParsingException, CertificateException, TokenBuildingException, IOException,
            TaskomaticApiException {
        parseAndSaveToken(remoteServer, remoteToken);

        IssServer registeredServer = createServer(IssRole.PERIPHERAL, remoteServer, rootCA, null, user);
        registerToRemote(user, registeredServer, remoteToken, rootCA);
    }

    private void registerToRemote(User user, IssServer remoteServer, String remoteToken, String rootCA)
            throws CertificateException, TokenParsingException, TokenBuildingException, IOException {

        // Ensure the remote server is a peripheral
        if (!(remoteServer instanceof IssPeripheral peripheral)) {
            throw new IllegalStateException("Server " + remoteServer + "is not a peripheral server");
        }

        // Create a client to connect to the internal API of the remote server
        var internalApi = clientFactory.newInternalClient(remoteServer.getFqdn(), remoteToken, rootCA);
        try {
            // Issue a token for granting access to the remote server
            Token localAccessToken = createAndSaveToken(remoteServer.getFqdn());
            // Send the local trusted root, if we needed a different certificate to connect
            String localRootCA = rootCA != null ? CertificateUtils.loadLocalTrustedRoot() : null;
            // Send the local GPG key used to sign metadata, if configured.
            // This force metadata checking on the peripheral server when mirroring from the Hub
            String localGpgKey =
                    (ConfigDefaults.get().isMetadataSigningEnabled()) ? CertificateUtils.loadGpgKey() : null;

            // Register this server on the remote with the hub role
            internalApi.registerHub(localAccessToken.getSerializedForm(), localRootCA, localGpgKey);

            // Generate the scc credentials and send them to the peripheral
            HubSCCCredentials credentials = generateCredentials(peripheral);
            internalApi.storeCredentials(credentials.getUsername(), credentials.getPassword());

            // Query Report DB connection values and set create a User
            ManagerInfoJson managerInfo = internalApi.getManagerInfo();
            Server peripheralServer = getOrCreateManagerSystem(systemEntitlementManager, user,
                    remoteServer.getFqdn(), Set.of(remoteServer.getFqdn()));
            boolean changed = SystemManager.updateMgrServerInfo(peripheralServer, managerInfo);
            if (changed) {
                setReportDbUser(user, peripheralServer, false);
            }
            internalApi.scheduleProductRefresh();
        }
        catch (Exception ex) {
            // cleanup the remote side
            internalApi.deregister();
            throw ex;
        }
    }

    private void ensureServerNotRegistered(String peripheralServer) {
        Optional<IssHub> issHub = hubFactory.lookupIssHubByFqdn(peripheralServer);
        if (issHub.isPresent()) {
            throw new IllegalStateException("Server " + peripheralServer + " is already registered as hub");
        }

        Optional<IssPeripheral> issPeripheral = hubFactory.lookupIssPeripheralByFqdn(peripheralServer);
        if (issPeripheral.isPresent()) {
            throw new IllegalStateException("Server " + peripheralServer + " is already registered as peripheral");
        }
    }

    private HubSCCCredentials generateCredentials(IssPeripheral peripheral) {
        String username = "peripheral-%06d".formatted(peripheral.getId());
        String password = RandomStringUtils.random(24, 0, 0, true, true, null, new SecureRandom());

        var hubSCCCredentials = CredentialsFactory.createHubSCCCredentials(username, password, peripheral.getFqdn());
        CredentialsFactory.storeCredentials(hubSCCCredentials);

        peripheral.setMirrorCredentials(hubSCCCredentials);
        saveServer(peripheral);

        return hubSCCCredentials;
    }

    private SCCCredentials saveCredentials(IssHub hub, String username, String password) {
        // Delete any existing SCC Credentials
        CredentialsFactory.listSCCCredentials()
            .forEach(creds -> mirrorCredentialsManager.deleteMirrorCredentials(creds.getId(), null));

        // Create the new credentials for the hub
        SCCCredentials credentials = CredentialsFactory.createSCCCredentials(username, password);

        credentials.setUrl("https://" + hub.getFqdn());
        CredentialsFactory.storeCredentials(credentials);

        hub.setMirrorCredentials(credentials);
        saveServer(hub);

        return credentials;
    }

    private Token createAndSaveToken(String fqdn) throws TokenBuildingException, TokenParsingException {
        Token token = new IssTokenBuilder(fqdn)
            .usingServerSecret()
            .build();

        hubFactory.saveToken(fqdn, token.getSerializedForm(), TokenType.ISSUED, token.getExpirationTime());
        return token;
    }

    private void parseAndSaveToken(String fqdn, String token) throws TokenParsingException {
        // We do not need to verify the signature as this token is for accessing another system.
        // That system will take care of ensuring its authenticity
        Token parsedToken = new TokenParser()
            .skippingSignatureVerification()
            .verifyingExpiration()
            .verifyingNotBefore()
            .parse(token);

        // Verify if this token is for this system
        String targetFqdn = parsedToken.getClaim("fqdn", String.class);
        String hostname = ConfigDefaults.get().getHostname();

        if (targetFqdn == null || !targetFqdn.equals(hostname)) {
            throw new TokenParsingException("FQDN do not match. Expected %s got %s".formatted(hostname, targetFqdn));
        }

        hubFactory.saveToken(fqdn, token, TokenType.CONSUMED, parsedToken.getExpirationTime());
    }

    private static String computeRootCaFileName(IssRole role, String serverFqdn) {
        return String.format(ROOT_CA_FILENAME_TEMPLATE, role.getLabel(), serverFqdn);
    }

    private IssServer lookupServerByFqdnAndRole(String serverFqdn, IssRole role) {
        return switch (role) {
            case HUB -> hubFactory.lookupIssHubByFqdn(serverFqdn).orElse(null);
            case PERIPHERAL -> hubFactory.lookupIssPeripheralByFqdn(serverFqdn).orElse(null);
        };
    }

    private IssServer lookupServerByIdAndRole(long id, IssRole role) {
        return switch (role) {
            case HUB -> hubFactory.findHubById(id);
            case PERIPHERAL -> hubFactory.findPeripheralById(id);
        };
    }

    private IssServer createServer(IssRole role, String serverFqdn, String rootCA, String gpgKey, User user)
            throws TaskomaticApiException {
        taskomaticApi.scheduleSingleRootCaCertUpdate(computeRootCaFileName(role, serverFqdn), rootCA);
        return switch (role) {
            case HUB -> {
                IssHub hub = new IssHub(serverFqdn, rootCA);
                hub.setGpgKey(gpgKey);
                hubFactory.save(hub);
                taskomaticApi.scheduleSingleGpgKeyImport(gpgKey);
                yield hub;
            }
            case PERIPHERAL -> {
                IssPeripheral peripheral = new IssPeripheral(serverFqdn, rootCA);
                hubFactory.save(peripheral);
                getOrCreateManagerSystem(systemEntitlementManager, user, serverFqdn, Set.of(serverFqdn));
                yield peripheral;
            }
        };
    }

    private void saveServer(IssServer server) {
        if (server instanceof IssHub hub) {
            hubFactory.save(hub);
        }
        else if (server instanceof IssPeripheral peripheral) {
            hubFactory.save(peripheral);
        }
        else {
            throw new IllegalArgumentException("Unknown server class " + server.getClass().getName());
        }
    }

    private static void ensureSatAdmin(User user) {
        if (!user.hasRole(RoleFactory.SAT_ADMIN)) {
            throw new PermissionException(RoleFactory.SAT_ADMIN);
        }
    }

    private static void ensureValidToken(IssAccessToken accessToken) {
        // Must be a valid not expired ISSUED token
        // Actual verification of the JWT signature is not done: since the token was stored in the database we can
        // consider it already verified
        if (accessToken == null || accessToken.getType() != TokenType.ISSUED ||
            !accessToken.isValid() || accessToken.isExpired()) {
            throw new PermissionException("You do not have permissions to perform this action. Invalid token provided");
        }
    }

    /**
     * Retrieves or create a server system
     *
     * @param systemEntitlementManagerIn the system entitlement manager
     * @param creator                  the user creating the server system
     * @param serverName               the FQDN of the proxy system
     * @return the proxy system
     */
    private Server getOrCreateManagerSystem(
            SystemEntitlementManager systemEntitlementManagerIn,
            User creator, String serverName, Set<String> fqdns
    ) {
        Optional<Server> existing = ServerFactory.findByAnyFqdn(fqdns);
        if (existing.isPresent()) {
            Server server = existing.get();
            if (!(server.hasEntitlement(EntitlementManager.SALT) ||
                    server.hasEntitlement(EntitlementManager.FOREIGN))) {
                throw new SystemsExistException(List.of(server.getId()));
            }
            // Add the FQDNs as some may not be already known
            server.getFqdns().addAll(fqdns.stream()
                    .filter(fqdn -> !fqdn.contains("*"))
                    .map(fqdn -> new ServerFQDN(server, fqdn)).toList());

            server.updateServerInfo();
            SystemManager.updateSystemOverview(server.getId());
            return server;
        }
        Server server = ServerFactory.createServer();
        server.setName(serverName);
        server.setHostname(serverName);
        server.setOrg(Optional.ofNullable(creator).map(User::getOrg).orElse(OrgFactory.getSatelliteOrg()));
        server.setCreator(creator);

        String uniqueId = SystemManagerUtils.createUniqueId(List.of(serverName));
        server.setDigitalServerId(uniqueId);
        server.setMachineId(uniqueId);
        server.setOs("(unknown)");
        server.setRelease("(unknown)");
        server.setSecret(RandomStringUtils.random(64, 0, 0, true, true,
                null, new SecureRandom()));
        server.setAutoUpdate("N");
        server.setContactMethod(ServerFactory.findContactMethodByLabel("default"));
        server.setLastBoot(System.currentTimeMillis() / 1000);
        server.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        ServerFactory.save(server);

        server.getFqdns().addAll(fqdns.stream()
                .filter(fqdn -> !fqdn.contains("*"))
                .map(fqdn -> new ServerFQDN(server, fqdn)).toList());

        server.updateServerInfo();

        MgrServerInfo serverInfo = new MgrServerInfo();
        serverInfo.setServer(server);
        server.setMgrServerInfo(serverInfo);

        ServerFactory.save(server);

        // No need to call `updateSystemOverview`
        // It will be called inside the method setBaseEntitlement. If we remove this line we need to manually call it
        systemEntitlementManagerIn.setBaseEntitlement(server, EntitlementManager.FOREIGN);
        return server;
    }

    /**
     * Collect data about all organizations
     *
     * @param accessToken the accesstoken
     * @return return list of {@link Org}
     */
    public List<Org> collectAllOrgs(IssAccessToken accessToken) {
        ensureValidToken(accessToken);
        return OrgFactory.lookupAllOrgs();
    }

    /**
     * Collect data about all channels
     *
     * @param accessToken the accesstoken
     * @return return list of {@link Channel}
     */
    public List<Channel> collectAllChannels(IssAccessToken accessToken) {
        ensureValidToken(accessToken);
        return ChannelFactory.listAllChannels();
    }

    /**
     * Count the registered peripherals on "this" hub
     * @param user the SatAdmin
     * @param pc the page control object
     * @return the count of registered peripherals entities
     */
    public Long countRegisteredPeripherals(User user, PageControl pc) {
        ensureSatAdmin(user);
        return hubFactory.countPeripherals(pc);
    }

    /**
     * List the peripherals with pagination and filtering, based on the give page control.
     * @param user the SatAdmin
     * @param pc the page control object
     * @return a List of Peripherals entities
     */
    public List<IssPeripheral> listRegisteredPeripherals(User user, PageControl pc) {
        ensureSatAdmin(user);
        return hubFactory.listPaginatedPeripherals(pc);
    }

    /**
     * Get the Peripheral Organizations
     * @param user the SatAdmin
     * @param peripheralId the Peripheral ID
     * @return a List of Organization from the Peripheral
     * @throws CertificateException Wrong CA
     * @throws IOException Internal API Call has gone wrong
     */
    public List<OrgInfoJson> getPeripheralOrgs(User user, Long peripheralId)
            throws CertificateException, IOException {
        ensureSatAdmin(user);
        IssPeripheral issPeripheral = hubFactory.findPeripheralById(peripheralId);
        IssAccessToken accessToken = hubFactory.lookupAccessTokenFor(issPeripheral.getFqdn());
        var internalApi = clientFactory.newInternalClient(
                issPeripheral.getFqdn(),
                accessToken.getToken(),
                issPeripheral.getRootCa()
        );
        return internalApi.getAllPeripheralOrgs();
    }

    /**
     * Get the Peripheral Channels
     * @param user the SatAdmin
     * @param peripheralId the Peripheral ID
     * @return a Set of Channels from the Peripheral
     * @throws CertificateException Wrong CA
     */
    public Set<IssV3ChannelResponse> getPeripheralChannels(User user, Long peripheralId)
            throws CertificateException {
        ensureSatAdmin(user);
        IssPeripheral issPeripheral = hubFactory.findPeripheralById(peripheralId);
        return issPeripheral.getPeripheralChannels().stream()
                .map(entity -> buildIssV3ChannelResponse(entity.getChannel()))
                .collect(Collectors.toSet());
    }

    /**
     * Get the custom channels of the hub
     * @param user The SatAdmin
     * @return a Set of Channels
     */
    public Set<IssV3ChannelResponse> getHubCustomChannels(User user) {
        ensureSatAdmin(user);
        return ChannelFactory.listCustomBaseChannels(user).stream()
                .map(this::buildIssV3ChannelResponse)
                .collect(Collectors.toSet());
    }

    /**
     * Get the vendor channels of the hub
     * @param user The SatAdmin
     * @return a Set of Channels
     */
    public Set<IssV3ChannelResponse> getHubVendorChannels(User user) {
        ensureSatAdmin(user);
        return ChannelFactory.listRedHatBaseChannels(user).stream()
                .map(this::buildIssV3ChannelResponse)
                .collect(Collectors.toSet());
    }

    private IssV3ChannelResponse buildIssV3ChannelResponse(Channel channel) {
        List<IssV3ChannelResponse> children = ChannelFactory.listAllChildrenForChannel(channel).stream()
                .map(this::buildIssV3ChannelResponse)
                .toList();
        List<IssV3ChannelResponse> clones = channel.getClonedChannels() == null ?
                List.of() :
                channel.getClonedChannels().stream()
                        .map(this::buildIssV3ChannelResponse)
                        .toList();
        Channel originalChannel = ChannelFactory.lookupOriginalChannel(channel);
        String originalLabel = originalChannel != null ? originalChannel.getLabel() : null;
        String parentLabel = channel.getParentChannel() != null ? channel.getParentChannel().getLabel() : null;
        return new IssV3ChannelResponse(
                channel.getId(),
                channel.getName(),
                channel.getLabel(),
                channel.getChannelArch().getName(),
                channel.getOrg() != null ?
                        new IssV3ChannelResponse.ChannelOrgResponse(
                                channel.getOrg().getId(), channel.getOrg().getName()) :
                        null,
                parentLabel,
                originalLabel,
                children,
                clones
        );
    }

    /**
     * Returns the available and synced channels and a list of organizations from the peripheral
     * @param user the SatAdmin
     * @param peripheralId the Peripheral ID
     * @return the Sync Channel operations model
     */
    public ChannelSyncModel getChannelSyncModelForPeripheral(User user, Long peripheralId)
            throws CertificateException, IOException {
        // Fetch all required data
        List<OrgInfoJson> peripheralOrgs = getPeripheralOrgs(user, peripheralId);
        Set<IssV3ChannelResponse> syncedChannels = getPeripheralChannels(user, peripheralId);
        Set<IssV3ChannelResponse> hubVendorChannels = getHubVendorChannels(user);
        Set<IssV3ChannelResponse> hubCustomChannels = getHubCustomChannels(user);
        // Separate custom and vendor channels
        Map<Boolean, Set<IssV3ChannelResponse>> partitionedChannels = partitionChannelsByType(syncedChannels);
        Set<IssV3ChannelResponse> syncedCustomChannels = partitionedChannels.get(true);
        Set<IssV3ChannelResponse> syncedVendorChannels = partitionedChannels.get(false);
        // Filter to find available channels
        List<IssV3ChannelResponse> availableCustomChannels = filterAvailableChannels(hubCustomChannels, syncedChannels);
        List<IssV3ChannelResponse> availableVendorChannels = filterAvailableChannels(hubVendorChannels, syncedChannels);
        return new ChannelSyncModel(
                peripheralOrgs,
                syncedCustomChannels,
                syncedVendorChannels,
                availableCustomChannels,
                availableVendorChannels
        );
    }

    /**
     * Partitions channels into custom and vendor sets
     * @param channels Channels to partition
     * @return Map with true->custom channels, false->vendor channels
     */
    private Map<Boolean, Set<IssV3ChannelResponse>> partitionChannelsByType(Set<IssV3ChannelResponse> channels) {
        Map<Boolean, Set<IssV3ChannelResponse>> result = new HashMap<>();
        // Create partitioned lists based on whether channel has an org (custom) or not (vendor)
        Map<Boolean, List<IssV3ChannelResponse>> partitioned = channels.stream()
                .collect(Collectors.partitioningBy(ch -> ch.getChannelOrg() != null));
        // Convert lists to sets
        result.put(true, new HashSet<>(partitioned.get(true)));
        result.put(false, new HashSet<>(partitioned.get(false)));
        return result;
    }

    /**
     * Filters hub channels to find those available for syncing
     * @param hubChannels Hub channels to filter
     * @param syncedChannels Already synced channels
     * @return List of available channels
     */
    private List<IssV3ChannelResponse> filterAvailableChannels(
            Set<IssV3ChannelResponse> hubChannels,
            Set<IssV3ChannelResponse> syncedChannels) {
        // Create sets for quick lookups
        Set<String> syncedLabels = syncedChannels.stream()
                .map(IssV3ChannelResponse::getChannelLabel)
                .collect(Collectors.toSet());
        Map<String, String> childToParentMap = buildChildToParentMap(hubChannels);
        // Identify channels that have parents in the hub set
        Set<String> childLabels = hubChannels.stream()
                .filter(ch -> ch.getParentChannelLabel() != null)
                .map(IssV3ChannelResponse::getChannelLabel)
                .collect(Collectors.toSet());
        List<IssV3ChannelResponse> result = new ArrayList<>();
        // Only process top-level channels and filter their hierarchies
        for (IssV3ChannelResponse channel : hubChannels) {
            // Skip if this is a child channel (will be handled by parent)
            if (childLabels.contains(channel.getChannelLabel())) {
                continue;
            }
            // Skip if already synced
            if (syncedLabels.contains(channel.getChannelLabel())) {
                continue;
            }
            // Skip if any ancestor is synced
            if (hasAncestorSynced(channel.getChannelLabel(), syncedLabels, childToParentMap)) {
                continue;
            }
            // Create a filtered version with only unsynced children
            IssV3ChannelResponse filteredChannel = cloneChannelWithUnsyncedChildren(channel, syncedLabels);
            result.add(filteredChannel);
        }
        // Also add standalone channels that have no parent in the hub
        for (IssV3ChannelResponse channel : hubChannels) {
            String parentLabel = channel.getParentChannelLabel();
            // If it has a parent but that parent isn't in our hub set
            // This is an edge case that shouldn't happen, we still check for it because if it happens you are stuck.
            if (parentLabel != null && !childToParentMap.containsValue(parentLabel)) {
                // Skip if already synced
                if (syncedLabels.contains(channel.getChannelLabel())) {
                    continue;
                }
                // Skip if already processed as a top-level channel
                if (result.stream().anyMatch(c -> c.getChannelLabel().equals(channel.getChannelLabel()))) {
                    continue;
                }
                // Skip if any ancestor is synced
                if (hasAncestorSynced(channel.getChannelLabel(), syncedLabels, childToParentMap)) {
                    continue;
                }
                // Create a filtered version with only unsynced children
                IssV3ChannelResponse filteredChannel = cloneChannelWithUnsyncedChildren(channel, syncedLabels);
                result.add(filteredChannel);
            }
        }
        return result;
    }

    /**
     * Creates a clone of a channel that only includes unsynced children
     * @param channel The channel to clone
     * @param syncedLabels Set of labels for synced channels
     * @return A new channel with only unsynced children
     */
    private IssV3ChannelResponse cloneChannelWithUnsyncedChildren(
            IssV3ChannelResponse channel,
            Set<String> syncedLabels) {
        // Filter children if they exist
        List<IssV3ChannelResponse> filteredChildren = Optional.ofNullable(channel.getChildren())
                .map(children -> children.stream()
                        .filter(child -> !syncedLabels.contains(child.getChannelLabel()))
                        .map(child -> cloneChannelWithUnsyncedChildren(child, syncedLabels))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        return new IssV3ChannelResponse(
                channel.getChannelId(),
                channel.getChannelName(),
                channel.getChannelLabel(),
                channel.getChannelArch(),
                channel.getChannelOrg(),
                channel.getParentChannelLabel(),
                channel.getOriginalChannelLabel(),
                filteredChildren,
                channel.getClones()
        );
    }

    /**
     * Builds a map from child channel label to parent channel label
     * @param channels Set of channels to process
     * @return Map of child labels to parent labels
     */
    private Map<String, String> buildChildToParentMap(Set<IssV3ChannelResponse> channels) {
        Map<String, String> childToParentMap = new HashMap<>();
        channels.forEach(channel -> addParentChildRelationships(channel, null, childToParentMap));
        return childToParentMap;
    }

    /**
     * Recursively adds parent-child relationships to the map
     * @param channel Current channel to process
     * @param parentLabel Parent label or null if top-level
     * @param childToParentMap Map to populate
     */
    private void addParentChildRelationships(
            IssV3ChannelResponse channel,
            String parentLabel,
            Map<String, String> childToParentMap) {
        String currentLabel = channel.getChannelLabel();
        if (parentLabel != null) {
            childToParentMap.put(currentLabel, parentLabel);
        }
        if (channel.getChildren() != null) {
            channel.getChildren().forEach(
                    child -> addParentChildRelationships(child, currentLabel, childToParentMap)
            );
        }
    }

    /**
     * Determines if a channel is available for syncing
     * @param channel Channel to check
     * @param syncedLabels Set of all synced channel labels
     * @param childToParentMap Map of child to parent relationships
     * @return true if channel is available for sync
     */
    private boolean isChannelAvailableForSync(
            IssV3ChannelResponse channel,
            Set<String> syncedLabels,
            Map<String, String> childToParentMap) {
        String channelLabel = channel.getChannelLabel();
        // 1. Check if this channel is already synced
        if (syncedLabels.contains(channelLabel)) {
            return false;
        }
        // 2. Check if any ancestor is synced
        return !hasAncestorSynced(channelLabel, syncedLabels, childToParentMap);
    }

    /**
     * Checks if any ancestor of the channel is synced
     * @param channelLabel Label of channel to check
     * @param syncedLabels Set of synced labels
     * @param childToParentMap Child to parent relationship map
     * @return true if any ancestor is synced
     */
    private boolean hasAncestorSynced(
            String channelLabel,
            Set<String> syncedLabels,
            Map<String, String> childToParentMap) {
        String parentLabel = childToParentMap.get(channelLabel);
        // If no parent, then no ancestor can be synced
        if (parentLabel == null) {
            return false;
        }
        // Check if parent is synced
        if (syncedLabels.contains(parentLabel)) {
            return true;
        }
        // Check parent's ancestors recursively
        return hasAncestorSynced(parentLabel, syncedLabels, childToParentMap);
    }

    /**
     * Checks if any descendant of the channel is synced
     * @param channel Channel to check
     * @param syncedLabels Set of synced labels
     * @return true if any descendant is synced
     */
    private boolean hasDescendantSynced(
            IssV3ChannelResponse channel,
            Set<String> syncedLabels) {
        if (channel.getChildren() == null || channel.getChildren().isEmpty()) {
            return false;
        }
        for (IssV3ChannelResponse child : channel.getChildren()) {
            // Check if child is synced
            if (syncedLabels.contains(child.getChannelLabel())) {
                return true;
            }
            // Check child's descendants recursively
            if (hasDescendantSynced(child, syncedLabels)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sync the channels from "this" hub to the selected peripheral
     * @param user the SatAdmin
     * @param peripheralId the peripheral id
     * @param orgId the org id to sync the channels to
     * @param channelsId the list of channels id from the hub
     */
    public void syncChannelsByIdForPeripheral(User user, Long peripheralId, Long orgId, List<Long> channelsId)
            throws CertificateException, IOException {
        ensureSatAdmin(user);
        // Get peripheral and prepare client
        IssPeripheral peripheral = hubFactory.findPeripheralById(peripheralId);
        HubInternalClient client = createClientForPeripheral(peripheral);
        // Prepare channels for synchronization
        Set<Long> syncedChannelIds = getSyncedChannelIds(peripheral);
        List<Channel> channelsToSync = prepareChannelsToSync(channelsId, syncedChannelIds);
        // Execute the synchronization in a transaction
        executeInTransaction(() -> synchronizeChannels(peripheral, channelsToSync, syncedChannelIds, orgId, client));
    }

    /**
     * Create a client for communicating with the peripheral
     */
    private HubInternalClient createClientForPeripheral(IssPeripheral peripheral) throws CertificateException {
        IssAccessToken accessToken = hubFactory.lookupAccessTokenFor(peripheral.getFqdn());
        return clientFactory.newInternalClient(
                peripheral.getFqdn(),
                accessToken.getToken(),
                peripheral.getRootCa()
        );
    }

    /**
     * Get the set of channel IDs that are already synced to the peripheral
     */
    private Set<Long> getSyncedChannelIds(IssPeripheral peripheral) {
        return peripheral.getPeripheralChannels().stream()
                .map(pc -> pc.getChannel().getId())
                .collect(Collectors.toSet());
    }

    /**
     * Prepare the list of channels to sync, including originals and parent channels
     */
    private List<Channel> prepareChannelsToSync(List<Long> channelsId, Set<Long> syncedChannelIds) {
        // Load requested channels
        List<Channel> requestedChannels = loadChannelsById(channelsId);
        // Include original channels for clones
        Set<Channel> channelsWithOriginals = includeOriginalChannels(requestedChannels);
        // Ensure parent-child hierarchy and sort
        Set<Channel> completeChannelSet = ensureParentChildHierarchy(channelsWithOriginals, syncedChannelIds);
        return sortChannelsByHierarchy(completeChannelSet);
    }

    /**
     * Load channels by their IDs
     */
    private List<Channel> loadChannelsById(List<Long> channelsId) {
        return ChannelFactory.getSession()
                .byMultipleIds(Channel.class)
                .multiLoad(channelsId);
    }

    /**
     * Include original channels for any cloned channels
     */
    private Set<Channel> includeOriginalChannels(List<Channel> channels) {
        Set<Channel> result = new HashSet<>();
        for (Channel channel : channels) {
            result.add(channel);
            Channel originalChannel = ChannelFactory.lookupOriginalChannel(channel);
            if (originalChannel != null) {
                result.add(originalChannel);
            }
        }

        return result;
    }

    /**
     * Synchronize the channels to the peripheral
     */
    private void synchronizeChannels(IssPeripheral peripheral, List<Channel> channelsToSync,
                                     Set<Long> syncedChannelIds, Long orgId, HubInternalClient client)
            throws IOException {
        // Create channel associations for new channels
        Set<IssPeripheralChannels> newAssociations = createChannelAssociations(
                peripheral, channelsToSync, syncedChannelIds, orgId);
        if (newAssociations.isEmpty()) {
            return; // Nothing new to sync
        }
        // Prepare channel info objects
        List<ChannelInfoDetailsJson> channelInfoList = prepareChannelInfoObjects(
                channelsToSync, syncedChannelIds, orgId);
        // Send to peripheral
        client.syncChannels(channelInfoList);
        // Update peripheral with the new associations
        updatePeripheralChannels(peripheral, newAssociations);
    }

    /**
     * Create channel associations for channels that need to be synced
     */
    private Set<IssPeripheralChannels> createChannelAssociations(
            IssPeripheral peripheral, List<Channel> channels, Set<Long> syncedChannelIds, Long orgId) {
        Set<IssPeripheralChannels> newAssociations = new HashSet<>();
        for (Channel channel : channels) {
            if (!syncedChannelIds.contains(channel.getId())) {
                IssPeripheralChannels association = new IssPeripheralChannels(peripheral, channel, orgId);
                hubFactory.save(association);
                newAssociations.add(association);
            }
        }
        return newAssociations;
    }

    /**
     * Prepare channel info objects for channels that need to be synced
     */
    private List<ChannelInfoDetailsJson> prepareChannelInfoObjects(
            List<Channel> channels, Set<Long> syncedChannelIds, Long orgId) {
        List<ChannelInfoDetailsJson> result = new ArrayList<>();
        for (Channel channel : channels) {
            if (!syncedChannelIds.contains(channel.getId())) {
                // For cloned channels, pass the original channel label
                Optional<String> originalLabel = getOriginalChannelLabel(channel);
                ChannelInfoDetailsJson channelInfo = ChannelFactory.toChannelInfo(channel, orgId, originalLabel);
                result.add(channelInfo);
            }
        }
        return result;
    }

    /**
     * Get the label of the original channel if this is a clone
     */
    private Optional<String> getOriginalChannelLabel(Channel channel) {
        Channel originalChannel = ChannelFactory.lookupOriginalChannel(channel);
        if (originalChannel != null) {
            return Optional.of(originalChannel.getLabel());
        }
        return Optional.empty();
    }

    /**
     * Update the peripheral with new channel associations
     */
    private void updatePeripheralChannels(IssPeripheral peripheral, Set<IssPeripheralChannels> newAssociations) {
        Set<IssPeripheralChannels> allChannels = new HashSet<>(peripheral.getPeripheralChannels());
        allChannels.addAll(newAssociations);
        peripheral.setPeripheralChannels(allChannels);
        hubFactory.save(peripheral);
    }
    /**
     * Execute operations within a transaction, handling commit/rollback
     * @param operation The operation to execute in the transaction
     * @throws IOException if communication errors occur
     * @throws CertificateException if certificate errors occur
     */
    private void executeInTransaction(TransactedOperation operation) throws IOException, CertificateException {
        Transaction transaction = HubFactory.getSession().getTransaction();
        try {
            transaction.begin();
            operation.execute();
            transaction.commit();
        }
        catch (IOException | CertificateException e) {
            transaction.rollback();
            throw e;
        }
        catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException("Unexpected error during transaction", e);
        }
    }

    /**
     * Functional interface for transacted operation
     */
    @FunctionalInterface
    private interface TransactedOperation {
        void execute() throws Exception;
    }

    /**
     * Ensures that for each channel, its parent channel is included in the channels to sync
     * @param requestedChannels the channels requested for sync
     * @param alreadySyncedIds IDs of channels already synced
     * @return A complete set of channels to sync including all necessary parent channels
     */
    private Set<Channel> ensureParentChildHierarchy(Set<Channel> requestedChannels, Set<Long> alreadySyncedIds) {
        Set<Channel> result = new HashSet<>();
        requestedChannels.forEach(channel -> addChannelWithParents(channel, result, alreadySyncedIds));
        return result;
    }

    /**
     * Recursively adds a channel and all its parent channels to the result set
     * @param channel the channel to add
     * @param result the set of channels to sync
     * @param alreadySyncedIds IDs of channels already synced (to avoid reloading)
     */
    private void addChannelWithParents(Channel channel, Set<Channel> result, Set<Long> alreadySyncedIds) {
        // If this channel is already being synced or is already synced, nothing to do
        if (result.contains(channel) || alreadySyncedIds.contains(channel.getId())) {
            return;
        }
        // First handle the parent if this is a child channel
        if (!channel.isBaseChannel()) {
            Channel parentChannel = channel.getParentChannel();
            if (parentChannel != null) {
                // Recursively ensure the parent is added first
                addChannelWithParents(parentChannel, result, alreadySyncedIds);
            }
        }
        // Then add this channel
        result.add(channel);
    }

    /**
     * Sorts channels ensuring parent channels come before their children
     * @param channels the set of channels to sort
     * @return a sorted list with parent channels before their children
     */
    private List<Channel> sortChannelsByHierarchy(Set<Channel> channels) {
        List<Channel> baseChannels = new ArrayList<>();
        Map<Long, List<Channel>> childrenByParentId = new HashMap<>();
        // Separate base channels and organize child channels by parent ID
        for (Channel channel : channels) {
            if (channel.isBaseChannel()) {
                baseChannels.add(channel);
            }
            else {
                Channel parent = channel.getParentChannel();
                if (parent != null) {
                    Long parentId = parent.getId();
                    childrenByParentId.computeIfAbsent(parentId, k -> new ArrayList<>()).add(channel);
                }
            }
        }
        // Sort base channels (if needed, e.g., by name or ID)
        baseChannels.sort(Comparator.comparing(Channel::getLabel));
        // Build the final sorted list
        List<Channel> result = new ArrayList<>(baseChannels);
        // Add children in order, following the parent hierarchy
        for (Channel baseChannel : baseChannels) {
            addChildrenRecursively(baseChannel, childrenByParentId, result);
        }
        return result;
    }

    /**
     * Recursively adds child channels to the result list in the correct hierarchy order
     */
    private void addChildrenRecursively(
            Channel parent, Map<Long, List<Channel>> childrenByParentId, List<Channel> result
    ) {
        List<Channel> children = childrenByParentId.get(parent.getId());
        if (children == null || children.isEmpty()) {
            return;
        }
        // Sort children (if needed)
        children.sort(Comparator.comparing(Channel::getLabel));
        for (Channel child : children) {
            if (!result.contains(child)) {
                result.add(child);
                // Process this child's children
                addChildrenRecursively(child, childrenByParentId, result);
            }
        }
    }

    /**
     * Desync the channels from "this" hub to the selected peripheral
     * @param user the SatAdmin
     * @param peripheralId the peripheral id
     * @param channelsId the list of channels id from the hub
     */
    public void desyncChannelsByIdForPeripheral(User user, Long peripheralId, List<Long> channelsId) {
        ensureSatAdmin(user);
        //TODO: check for children, desync children is parent is desynced
        IssPeripheral issPeripheral = hubFactory.findPeripheralById(peripheralId);
        List<Channel> channels = ChannelFactory.getSession().byMultipleIds(Channel.class).multiLoad(channelsId);
        Set<IssPeripheralChannels> peripheralChannels = new HashSet<>();
        channels.forEach(ch -> peripheralChannels.add(new IssPeripheralChannels(issPeripheral, ch)));
        issPeripheral.setPeripheralChannels(peripheralChannels);
        hubFactory.save(issPeripheral);
    }

    /**
     * Synchornize channels from the hub on this peripheral server
     * @param accessToken the access token
     * @param channelInfo a set of channel information to create or update the channels
     */
    public void syncChannels(IssAccessToken accessToken, List<ChannelInfoDetailsJson> channelInfo) {
        ensureValidToken(accessToken);
        ChannelFactory.ensureValidChannelInfo(channelInfo);
        Set<String> syncFinished = new HashSet<>();
        Map<String, ChannelInfoDetailsJson> channelInfoByLabel = channelInfo.stream()
                .collect(Collectors.toMap(ChannelInfoDetailsJson::getLabel, v -> v));
        for (ChannelInfoDetailsJson info : channelInfo) {
            ChannelFactory.syncChannel(info, channelInfoByLabel, syncFinished);
        }
    }

    /**
     * Trigger a synchronization of Channel Families on the peripheral
     *
     * @param accessToken the access token
     * @return a boolean flag of the success/failed result
     */
    public boolean synchronizeChannelFamilies(IssAccessToken accessToken) {
        ensureValidToken(accessToken);
        return ProductsController.doSynchronizeChannelFamilies();
    }

    /**
     * Trigger a synchronization of Products on the peripheral
     *
     * @param accessToken the access token
     * @return a boolean flag of the success/failed result
     */
    public boolean synchronizeProducts(IssAccessToken accessToken) {
        ensureValidToken(accessToken);
        return ProductsController.doSynchronizeProducts();
    }

    /**
     * Trigger a synchronization of Repositories on the peripheral
     *
     * @param accessToken the access token
     * @return a boolean flag of the success/failed result
     */
    public boolean synchronizeRepositories(IssAccessToken accessToken) {
        ensureValidToken(accessToken);
        return ProductsController.doSynchronizeRepositories();
    }

    /**
     * Trigger a synchronization of Subscriptions on the peripheral
     *
     * @param accessToken the access token
     * @return a boolean flag of the success/failed result
     */
    public boolean synchronizeSubscriptions(IssAccessToken accessToken) {
        ensureValidToken(accessToken);
        return ProductsController.doSynchronizeSubscriptions();
    }

}
