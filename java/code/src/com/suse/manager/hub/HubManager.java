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
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.SystemManagerUtils;
import com.redhat.rhn.manager.system.SystemsExistException;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.model.hub.IssHub;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.model.hub.IssServer;
import com.suse.manager.model.hub.ManagerInfoJson;
import com.suse.manager.model.hub.TokenType;
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

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Business logic to manage ISSv3 Sync
 */
public class HubManager {

    private final MirrorCredentialsManager mirrorCredentialsManager;

    private final HubFactory hubFactory;

    private final HubClientFactory clientFactory;

    private final SystemEntitlementManager systemEntitlementManager;

    private TaskomaticApi taskomaticApi;

    private static final Logger LOG = LogManager.getLogger(HubManager.class);

    private static final String ROOT_CA_FILENAME_TEMPLATE = "%s_%s_root_ca.pem";

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
     * @param accessToken the access token granting access and identifying the the caller
     * @param role the role of the server
     * @return an {@link IssHub} or {@link IssPeripheral} depending on the specified role, null if the FQDN is unknown
     */
    public IssServer findServer(IssAccessToken accessToken, IssRole role) {
        ensureValidToken(accessToken);

        return switch (role) {
            case HUB -> hubFactory.lookupIssHubByFqdn(accessToken.getServerFqdn()).orElse(null);
            case PERIPHERAL -> hubFactory.lookupIssPeripheralByFqdn(accessToken.getServerFqdn()).orElse(null);
        };
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

        // Issue a token for granting access to the remote server
        Token localAccessToken = createAndSaveToken(remoteServer.getFqdn());
        // Send the local trusted root, if we needed a different certificate to connect
        String localRootCA = rootCA != null ? CertificateUtils.loadLocalTrustedRoot() : null;
        // Send the local GPG key used to sign metadata, if configured.
        // This force metadata checking on the peripheral server when mirroring from the Hub
        String localGpgKey = (ConfigDefaults.get().isMetadataSigningEnabled()) ? CertificateUtils.loadGpgKey() : null;

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

        // TODO Ensure the path is correct once the SCC Endoint is implemented
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
}
