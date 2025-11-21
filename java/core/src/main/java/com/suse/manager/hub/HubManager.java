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
import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.iss.IssMaster;
import com.redhat.rhn.domain.iss.IssSlave;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
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
import com.suse.manager.model.hub.ChannelInfoJson;
import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.model.hub.IssHub;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.IssPeripheralChannels;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.model.hub.IssServer;
import com.suse.manager.model.hub.ManagerInfoJson;
import com.suse.manager.model.hub.OrgInfoJson;
import com.suse.manager.model.hub.ServerInfoJson;
import com.suse.manager.model.hub.TokenType;
import com.suse.manager.model.hub.UpdatableServerData;
import com.suse.manager.webui.controllers.ProductsController;
import com.suse.manager.webui.controllers.admin.beans.ChannelOrg;
import com.suse.manager.webui.controllers.admin.beans.ChannelOrgGroup;
import com.suse.manager.webui.controllers.admin.beans.ChannelSyncDetail;
import com.suse.manager.webui.controllers.admin.beans.ChannelSyncModel;
import com.suse.manager.webui.utils.token.IssTokenBuilder;
import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenParser;
import com.suse.manager.webui.utils.token.TokenParsingException;
import com.suse.scc.SCCTaskManager;
import com.suse.scc.proxy.SCCProxyFactory;
import com.suse.utils.CertificateUtils;
import com.suse.utils.Maps;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * Checks if the server can be registered as peripheral
     * @param accessToken the access token granting access and identifying the caller
     * @return return {@link ServerInfoJson}
     */
    public ServerInfoJson getServerInfo(IssAccessToken accessToken) {
        ensureValidToken(accessToken);

        return new ServerInfoJson(hubFactory.countPeripherals() > 0,
                hubFactory.lookupIssHub().isPresent());
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

        boolean thisIsISSHub = hubFactory.isISSHub();
        boolean thisIsISSPeripheral = hubFactory.isISSPeripheral();
        IssRole serverRole = null;

        if (!thisIsISSHub && thisIsISSPeripheral) {
            serverRole = IssRole.HUB;
        }
        if (thisIsISSHub && !thisIsISSPeripheral) {
            serverRole = IssRole.PERIPHERAL;
        }
        if (thisIsISSHub && thisIsISSPeripheral) {
            //this is both hub and peripheral: whose server do I have to deregister from?

            //try deregister a peripheral
            serverRole = IssRole.PERIPHERAL;
            IssServer server = findServer(user, fqdn, serverRole);
            if (null == server) {
                //try deregister a hub
                serverRole = IssRole.HUB;
                server = findServer(user, fqdn, serverRole);
                if (null == server) {
                    LOG.error("Cannot deregister: server {} not found (neither hub nor peripheral)", fqdn);
                    throw new IllegalStateException(
                            "Cannot deregister: server %s not found (neither hub nor peripheral)".formatted(fqdn));
                }
            }
        }
        if (!thisIsISSHub && !thisIsISSPeripheral) {
            return; //nothing to do: probably a race condition
        }

        deregister(user, fqdn, serverRole, onlyLocal);
    }

    /**
     * Deregister the server with the given FQDN. The de-registration can be optionally performed also on the
     * remote server.
     * @param user the user
     * @param fqdn the FQDN
     * @param serverRole the server role (HUB or PERIPHERAL)
     * @param onlyLocal specify if the de-registration has to be performed also on the remote server
     * @throws CertificateException when it's not possible to use remote server certificate
     * @throws IOException when the connection with the remote server fails
     */
    public void deregister(User user, String fqdn, IssRole serverRole, boolean onlyLocal)
            throws CertificateException, IOException {
        ensureSatAdmin(user);
        IssServer server = findServer(user, fqdn, serverRole);

        if (null == server) {
            return;
        }

        if (!onlyLocal) {
            IssAccessToken accessToken = hubFactory.lookupAccessTokenFor(server.getFqdn());
            var internalClient = clientFactory.newInternalClient(fqdn, accessToken.getToken(), server.getRootCa());
            internalClient.deregister();
        }

        switch (serverRole) {
            case HUB -> deleteHub(fqdn);
            case PERIPHERAL -> deletePeripheral(fqdn);
            default -> throw new IllegalStateException("Role should either be HUB or PERIPHERAL");
        }
    }

    /**
     * Delete locally all ISS artifacts for the hub or peripheral server identified by the FQDN
     * @param accessToken the token
     * @param fqdn the FQDN
     * @return the role of the removed server
     */
    public IssRole deleteIssServerLocal(IssAccessToken accessToken, String fqdn) {
        ensureValidToken(accessToken);

        Optional<IssPeripheral> issPeripheral = hubFactory.lookupIssPeripheralByFqdn(fqdn);
        if (issPeripheral.isPresent()) {
            deletePeripheral(issPeripheral.get());
            return IssRole.PERIPHERAL;
        }

        Optional<IssHub> issHub = hubFactory.lookupIssHubByFqdn(fqdn);
        if (issHub.isPresent()) {
            deleteHub(issHub.get());
            return IssRole.HUB;
        }

        LOG.info("Peripheral Server with name {} not found", fqdn);
        return IssRole.PERIPHERAL; //default value?
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

    private void cleanupSccWhenDeregisteringPeripheral(IssPeripheral peripheral) {
        //update proxy entries
        SCCTaskManager sccTaskManager = new SCCTaskManager();
        sccTaskManager.cleanupSccProxyWhenDeregisteringPeripheral(peripheral.getFqdn());

        if (null != peripheral.getMirrorCredentials()) {
            CredentialsFactory.removeCredentials(peripheral.getMirrorCredentials());
        }
    }

    private void deletePeripheral(IssPeripheral peripheral) {
        cleanupSccWhenDeregisteringPeripheral(peripheral);

        hubFactory.remove(peripheral);
        hubFactory.removeAccessTokensFor(peripheral.getFqdn());
        try {
            taskomaticApi.scheduleSingleRootCaCertDelete(IssRole.PERIPHERAL, peripheral.getFqdn());
        }
        catch (TaskomaticApiException ex) {
            //if unable to delete ca certificate, just log a warning
            LOG.warn("Cannot remove ca certificate for peripheral {}", peripheral.getFqdn());
        }
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

    private void cleanupSccDeletingHub(IssHub hub) {
        SCCCredentials mirrorCredentials = hub.getMirrorCredentials();
        if (null != mirrorCredentials) {
            // Clear Repository Authentications
            SCCCachingFactory.listRegItemsByCredentials(mirrorCredentials)
                    .forEach(SCCCachingFactory::deleteRegCacheItem);
            SCCCachingFactory.lookupRepositoryAuthByCredential(mirrorCredentials)
                    .forEach(SCCCachingFactory::deleteRepositoryAuth);
            CredentialsFactory.removeCredentials(mirrorCredentials);
        }

        //set scc proxy entries as if they were just created, remove pending to be removed
        SCCProxyFactory sccProxyFactory = new SCCProxyFactory();
        sccProxyFactory.setReregisterProxyEntries();
        sccProxyFactory.removeRemovalPendingProxyEntries();
    }

    private void deleteHub(IssHub hub) {
        cleanupSccDeletingHub(hub);
        hubFactory.remove(hub);
        hubFactory.removeAccessTokensFor(hub.getFqdn());
        try {
            taskomaticApi.scheduleSingleRootCaCertDelete(IssRole.HUB, hub.getFqdn());
        }
        catch (TaskomaticApiException ex) {
            //if unable to delete ca certificate, just log a warning
            LOG.warn("Cannot remove ca certificate for peripheral {}", hub.getFqdn());
        }
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
                new IllegalStateException(remoteServer + " is not registered as peripheral"));

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
     * @return the registered peripheral
     * @throws CertificateException if the specified certificate is not parseable
     * @throws TokenParsingException if the specified token is not parseable
     * @throws TokenBuildingException if an error occurs while generating the token for the server
     * @throws IOException when connecting to the server fails
     */
    public IssPeripheral register(User user, String remoteServer, String username, String password, String rootCA)
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

        return registerWithToken(user, remoteServer, rootCA, remoteToken);
    }

    /**
     * Register a remote PERIPHERAL server
     *
     * @param user the user performing the operation
     * @param remoteServer the peripheral server FQDN
     * @param remoteToken the token used to connect to the peripheral server
     * @param rootCA the optional root CA of the peripheral server
     *
     * @return the registered peripheral
     * @throws CertificateException if the specified certificate is not parseable
     * @throws TokenParsingException if the specified token is not parseable
     * @throws TokenBuildingException if an error occurs while generating the token for the peripheral server
     * @throws IOException when connecting to the peripheral server fails
     */
    public IssPeripheral register(User user, String remoteServer, String remoteToken, String rootCA)
        throws CertificateException, TokenBuildingException, IOException, TokenParsingException,
            TaskomaticApiException {
        ensureSatAdmin(user);

        // Verify this server is not already registered as hub or peripheral
        ensureServerNotRegistered(remoteServer);

        return registerWithToken(user, remoteServer, rootCA, remoteToken);
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
     * Regenerate the username and the password for an existing peripheral
     * @param user the user performing the operation
     * @param peripheralId the id of the peripheral linked to the credentials to refresh
     * @return the updated credentials
     * @throws IllegalArgumentException when the peripheral does not exist
     * @throws CertificateException if the peripheral certificate is not parseable
     * @throws IOException when the calling the peripheral APIs fails
     */
    public HubSCCCredentials regenerateCredentials(User user, long peripheralId)
        throws CertificateException, IOException {
        ensureSatAdmin(user);

        IssPeripheral peripheral = hubFactory.findPeripheralById(peripheralId);
        if (peripheral == null) {
            throw new IllegalArgumentException("No peripheral found with id " + peripheralId);
        }

        return regenerateCredentials(peripheral);
    }

    /**
     * Regenerate the username and the password for an existing peripheral
     * @param user the user performing the operation
     * @param fqdn the fqdn of the peripheral linked to the credentials to refresh
     * @return the updated credentials
     * @throws IllegalArgumentException when the peripheral does not exist
     * @throws CertificateException if the peripheral certificate is not parseable
     * @throws IOException when the calling the peripheral APIs fails
     */
    public HubSCCCredentials regenerateCredentials(User user, String fqdn)
        throws CertificateException, IOException {
        ensureSatAdmin(user);

        IssPeripheral peripheral = hubFactory.lookupIssPeripheralByFqdn(fqdn)
            .orElseThrow(() -> new IllegalArgumentException("No peripheral found with fqdn " + fqdn));

        return regenerateCredentials(peripheral);
    }

    private HubSCCCredentials regenerateCredentials(IssPeripheral peripheral) throws IOException, CertificateException {
        // Generate a new credentials for this peripheral
        HubSCCCredentials newCredentials = generateCredentials(peripheral);

        // If credentials are already connected to the peripheral, update the existing ones rather than creating new
        if (peripheral.getMirrorCredentials() != null) {
            HubSCCCredentials existingCredentials = peripheral.getMirrorCredentials();
            existingCredentials.setUsername(newCredentials.getUsername());
            existingCredentials.setPassword(newCredentials.getPassword());
            existingCredentials.setPeripheralUrl(newCredentials.getPeripheralUrl());

            newCredentials = existingCredentials;
        }

        // Send the new credentials to peripheral
        IssAccessToken accessToken = hubFactory.lookupAccessTokenFor(peripheral.getFqdn());
        clientFactory.newInternalClient(peripheral.getFqdn(), accessToken.getToken(), peripheral.getRootCa())
            .storeCredentials(newCredentials.getUsername(), newCredentials.getPassword());

        // If everything went ok, update the local credentials
        savePeripheralCredentials(peripheral, newCredentials);

        return newCredentials;
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
     * Collect data about a Manager Server
     *
     * @param user The current user
     * @return return {@link ManagerInfoJson}
     */
    public ManagerInfoJson collectManagerInfo(User user) {
        ensureSatAdmin(user);
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
            taskomaticApi.scheduleSingleRootCaCertUpdate(role, fqdn, data.getRootCA());
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

    private void checkIfRegistrableAsPeripheral(ServerInfoJson serverInfo) throws PeripheralRegistrationException {
        // if server is already a peripheral, then it has already a hub.
        // multiple hubs are not allowed: replacing the hub could lead to circular dependencies
        if (serverInfo.isPeripheral()) {
            throw new PeripheralRegistrationException(
                    "Candidate peripheral server already registered to a hub. Deregister it first");
        }

        // To avoid circular dependencies:
        // if the server is not a hub (has no peripherals), it can be safely registered
        // if current node is not subscribed to a hub, it can safely register any server (regardless of it already
        // having peripherals)
        if (serverInfo.isHub() && hubFactory.lookupIssHub().isPresent()) {
            // how can we be sure the server is not our ancestor? Are forests allowed?
            // with the following throw:
            // PRO: we avoid mistakenly registering our tree root
            // CON: we do not allow to register the root of another tree of a forest
            throw new PeripheralRegistrationException(
                    "Candidate peripheral server already owns peripherals. Cannot register another server tree");
        }
    }

    private IssPeripheral registerWithToken(User user, String remoteServer, String rootCA, String remoteToken)
        throws TokenParsingException, CertificateException, TokenBuildingException, IOException,
            TaskomaticApiException {

        var internalApi = clientFactory.newInternalClient(remoteServer, remoteToken, rootCA);
        ServerInfoJson serverInfo = internalApi.getServerInfo();
        checkIfRegistrableAsPeripheral(serverInfo);

        parseAndSaveToken(remoteServer, remoteToken);

        IssServer registeredServer = createServer(IssRole.PERIPHERAL, remoteServer, rootCA, null, user);

        // Ensure the remote server is a peripheral
        if (!(registeredServer instanceof IssPeripheral peripheral)) {
            throw new IllegalStateException(remoteServer + "is not a peripheral server");
        }

        registerToRemote(user, peripheral, remoteToken, rootCA);

        return peripheral;
    }

    private void registerToRemote(User user, IssPeripheral peripheral, String remoteToken, String rootCA)
            throws CertificateException, TokenParsingException, TokenBuildingException, IOException {

        // Create a client to connect to the internal API of the remote server
        var internalApi = clientFactory.newInternalClient(peripheral.getFqdn(), remoteToken, rootCA);
        try {
            // Issue a token for granting access to the remote server
            Token localAccessToken = createAndSaveToken(peripheral.getFqdn());
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
            savePeripheralCredentials(peripheral, credentials);

            // Query Report DB connection values and set create a User
            ManagerInfoJson managerInfo = internalApi.getManagerInfo();
            Server peripheralServer = getOrCreateManagerSystem(systemEntitlementManager, user,
                    peripheral.getFqdn(), Set.of(peripheral.getFqdn()));
            boolean changed = SystemManager.updateMgrServerInfo(peripheralServer, managerInfo);
            if (changed) {
                setReportDbUser(user, peripheralServer, false);
            }
            internalApi.scheduleProductRefresh();
        }
        catch (Exception ex) {
            cleanup(internalApi);
            throw ex;
        }
    }

    private void cleanup(HubInternalClient internalApi) {
        try {
            // try to clean up the remote side
            internalApi.deregister();
        }
        catch (Exception ex) {
            LOG.warn("Exception thrown while cleaning up and deregistering from iternalApi: ignoring it.");
        }
    }

    private void ensureServerNotRegistered(String peripheralServer) {
        Optional<IssHub> issHub = hubFactory.lookupIssHubByFqdn(peripheralServer);
        if (issHub.isPresent()) {
            throw new IllegalStateException(peripheralServer + " is already registered as hub");
        }

        Optional<IssPeripheral> issPeripheral = hubFactory.lookupIssPeripheralByFqdn(peripheralServer);
        if (issPeripheral.isPresent()) {
            throw new IllegalStateException(peripheralServer + " is already registered as peripheral");
        }
    }

    private HubSCCCredentials generateCredentials(IssPeripheral peripheral) {
        String username = "peripheral-%06d".formatted(peripheral.getId());
        String password = RandomStringUtils.random(24, 0, 0, true, true, null, new SecureRandom());

        return CredentialsFactory.createHubSCCCredentials(username, password, peripheral.getFqdn());
    }

    private void savePeripheralCredentials(IssPeripheral peripheral, HubSCCCredentials hubSCCCredentials) {
        CredentialsFactory.storeCredentials(hubSCCCredentials);

        peripheral.setMirrorCredentials(hubSCCCredentials);
        saveServer(peripheral);
    }

    private SCCCredentials saveCredentials(IssHub hub, String username, String password) {
        SCCCredentials currentCredentials = hub.getMirrorCredentials();
        // If credentials already exist linked to this hub, just updated them with the new values
        if (currentCredentials != null) {

            currentCredentials.setUsername(username);
            currentCredentials.setPassword(password);
            currentCredentials.setUrl("https://" + hub.getFqdn());

            CredentialsFactory.storeCredentials(currentCredentials);
            return currentCredentials;
        }

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
        if (StringUtils.isNotEmpty(rootCA)) {
            taskomaticApi.scheduleSingleRootCaCertUpdate(role, serverFqdn, rootCA);
        }
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
     * Remotely collect data about peripheral organizations
     *
     * @param user The current user
     * @param peripheralFqdn the remote peripheral server FQDN
     * @return return list of {@link OrgInfoJson}
     */
    public List<OrgInfoJson> getAllPeripheralOrgs(User user, String peripheralFqdn)
            throws IOException, CertificateException {
        ensureSatAdmin(user);
        IssPeripheral issPeripheral = hubFactory.lookupIssPeripheralByFqdn(peripheralFqdn).orElseThrow(() ->
                new IllegalStateException(peripheralFqdn + " is not registered as peripheral"));
        return getPeripheralOrgs(issPeripheral);
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
        return getPeripheralOrgs(issPeripheral);
    }

    private List<OrgInfoJson> getPeripheralOrgs(IssPeripheral issPeripheral)
            throws CertificateException, IOException {
        IssAccessToken accessToken = hubFactory.lookupAccessTokenFor(issPeripheral.getFqdn());
        var internalApi = clientFactory.newInternalClient(
                issPeripheral.getFqdn(),
                accessToken.getToken(),
                issPeripheral.getRootCa()
        );
        return internalApi.getAllPeripheralOrgs();
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
     * Remotely collect data about peripheral channels
     *
     * @param user The current user
     * @param peripheralFqdn the remote peripheral server FQDN
     * @return return list of {@link ChannelInfoJson}
     */
    public List<ChannelInfoJson> getAllPeripheralChannels(User user, String peripheralFqdn)
            throws IOException, CertificateException {
        ensureSatAdmin(user);

        IssPeripheral issPeripheral = hubFactory.lookupIssPeripheralByFqdn(peripheralFqdn).orElseThrow(() ->
                new IllegalStateException(peripheralFqdn + " is not registered as peripheral"));

        IssAccessToken accessToken = hubFactory.lookupAccessTokenFor(issPeripheral.getFqdn());
        var internalClient = clientFactory.newInternalClient(issPeripheral.getFqdn(), accessToken.getToken(),
                issPeripheral.getRootCa());
        return internalClient.getAllPeripheralChannels();
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

        IssPeripheral issPeripheral = hubFactory.findPeripheralById(peripheralId);
        Map<Long, IssPeripheralChannels> syncedChannelToIssChannelMap = issPeripheral.getPeripheralChannels().stream()
            .collect(Collectors.toMap(pc -> pc.getChannel().getId(), pc -> pc));

        List<ChannelSyncDetail> channelDetails = ChannelFactory.listAllBaseChannels().stream()
            .map(channel -> buildChannelSyncDetail(channel, user, syncedChannelToIssChannelMap, peripheralOrgs))
            .toList();

        // Mandatory map, in Javascript compatible format
        Map<Long, List<Long>>  mandatoryMap = channelDetails.stream()
            .flatMap(channel -> Stream.concat(Stream.of(channel), channel.children().stream()))
            .collect(Collectors.toMap(
                ChannelSyncDetail::id, HubManager::getMandatoryChannelsFor
            ));

        return new ChannelSyncModel(
            peripheralOrgs,
            channelDetails,
            Maps.mapToEntryList(mandatoryMap),
            Maps.mapToEntryList(Maps.invertMultimap(mandatoryMap))
        );
    }

    private ChannelSyncDetail buildChannelSyncDetail(Channel channel, User user,
                                                     Map<Long, IssPeripheralChannels> syncedChannelToIssChannelMap,
                                                     List<OrgInfoJson> peripheralOrgs) {
        List<ChannelSyncDetail> children = ChannelFactory.listAllChildrenForChannel(channel).stream()
            .map(child -> buildChannelSyncDetail(child, user, syncedChannelToIssChannelMap, peripheralOrgs))
            .toList();

        List<ChannelSyncDetail> clones = Optional.ofNullable(channel.getClonedChannels()).stream()
                .flatMap(Collection::stream)
                .map(clone -> buildChannelSyncDetail(clone, user, syncedChannelToIssChannelMap, peripheralOrgs))
                .toList();

        Channel originalChannel = ChannelFactory.lookupOriginalChannel(channel);

        ChannelOrg selectedChannelOrg = null;
        if (channel.getOrg() != null) {
            // Custom Channel
            IssPeripheralChannels peripheralChannel = syncedChannelToIssChannelMap.get(channel.getId());
            selectedChannelOrg = peripheralOrgs.stream().filter(po ->
                        //channel is synced
                        (peripheralChannel != null &&
                                Objects.equals(peripheralChannel.getPeripheralOrgId(), po.getOrgId())) ||
                        // or channel exists on the peripheral side
                                ((null != po.getOrgChannelLabels()) &&
                                        (po.getOrgChannelLabels().contains(channel.getLabel()))))
                    .map(po -> new ChannelOrg(po.getOrgId(), po.getOrgName()))
                    .findFirst()
                    .orElse(null);
        }

        return new ChannelSyncDetail(
            channel.getId(),
            channel.getName(),
            channel.getLabel(),
            channel.getChannelArch().getName(),
            Optional.ofNullable(channel.getOrg()).map(ChannelOrg::new).orElse(null),
            selectedChannelOrg,
            Optional.ofNullable(channel.getParentChannel()).map(Channel::getId).orElse(null),
            Optional.ofNullable(originalChannel).map(Channel::getId).orElse(null),
            children,
            clones,
            selectedChannelOrg != null,
            syncedChannelToIssChannelMap.containsKey(channel.getId())
        );
    }

    /**
     * Sync the channels from "this" hub to the selected peripheral
     * @param user the SatAdmin
     * @param peripheralId the peripheral id
     * @param channelsToAdd the list of channels labels from the hub with the selected org for syncing
     * @param channelsToRemove the list of channels labels from the hub to desync
     */
    public void syncChannelsByLabelForPeripheral(User user,
                                                 Long peripheralId,
                                                 List<ChannelOrgGroup> channelsToAdd,
                                                 List<String> channelsToRemove)
            throws CertificateException, IOException {
        ensureSatAdmin(user);
        IssPeripheral peripheral = hubFactory.findPeripheralById(peripheralId);
        HubInternalClient client = createClientForPeripheral(peripheral);
        // TODO: put this into a transaction for save and removal, commit after the api call, rollback otherwise
        for (ChannelOrgGroup group : channelsToAdd) {
            Set<String> syncedChannelLabels = getSyncedChannelLabels(peripheral);
            List<Channel> channelsToSync = prepareChannelsToSync(group.getChannelLabels(), syncedChannelLabels);
            saveChannels(peripheral, channelsToSync, syncedChannelLabels, group.getOrgId());
        }
        removeChannelsByLabelForPeripheral(peripheral, channelsToRemove);
        List<ChannelInfoDetailsJson> fullSyncList = hubFactory.listChannelInfoForPeripheral(peripheral);
        client.syncChannels(fullSyncList);
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
    private Set<String> getSyncedChannelLabels(IssPeripheral peripheral) {
        return peripheral.getPeripheralChannels().stream()
                .map(pc -> pc.getChannel().getLabel())
                .collect(Collectors.toSet());
    }

    /**
     * Prepare the list of channels to sync, including originals and parent channels
     */
    private List<Channel> prepareChannelsToSync(List<String> channelsLabels, Set<String> syncedChannelLabels) {
        Set<Channel> requestedChannels = loadChannelsByLabel(channelsLabels);
        Set<Channel> completeChannelSet = ensureParentChildHierarchy(requestedChannels, syncedChannelLabels);
        return sortChannelsByHierarchy(completeChannelSet);
    }

    /**
     * Load channels by their IDs
     */
    private Set<Channel> loadChannelsByLabel(List<String> channelsLabels) {
        Set<Channel> channels = new HashSet<>();
        for (String label : channelsLabels) {
            channels.add(ChannelFactory.lookupByLabel(label));
        }
        return channels;
    }

    /**
     * Synchronize the channels to the peripheral
     */
    private void saveChannels(
            IssPeripheral peripheral, List<Channel> channelsToSync, Set<String> syncedChannelLabels, Long orgId
    ) {
        // Create channel associations for new channels
        Set<IssPeripheralChannels> newAssociations = createChannelAssociations(
                peripheral, channelsToSync, syncedChannelLabels, orgId);
        if (!newAssociations.isEmpty()) {
            updatePeripheralChannels(peripheral, newAssociations);
        }
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
     * Create channel associations for channels that need to be synced
     */
    private Set<IssPeripheralChannels> createChannelAssociations(
            IssPeripheral peripheral, List<Channel> channels, Set<String> syncedChannelLabels, Long orgId
    ) {
        Set<IssPeripheralChannels> newAssociations = new HashSet<>();
        for (Channel channel : channels) {
            if (!syncedChannelLabels.contains(channel.getLabel())) {
                IssPeripheralChannels association = new IssPeripheralChannels(peripheral, channel, orgId);
                hubFactory.save(association);
                newAssociations.add(association);
            }
        }
        return newAssociations;
    }

    /**
     * Update the peripheral with new channel associations
     */
    private void updatePeripheralChannels(IssPeripheral peripheral, Set<IssPeripheralChannels> newAssociations) {
        Set<IssPeripheralChannels> allChannels = new HashSet<>(peripheral.getPeripheralChannels());
        allChannels.addAll(newAssociations);
        peripheral.setPeripheralChannels(allChannels);
        hubFactory.save(peripheral);
        allChannels.forEach(hubFactory::save);
    }

    /**
     * Ensures that for each channel, its parent channel is included in the channels to sync
     * @param requestedChannels the channels requested for sync
     * @param alreadySyncedLabels Labels of channels already synced
     * @return A complete set of channels to sync including all necessary parent channels
     */
    private Set<Channel> ensureParentChildHierarchy(Set<Channel> requestedChannels, Set<String> alreadySyncedLabels) {
        Set<Channel> result = new HashSet<>();
        requestedChannels.forEach(channel -> addChannelWithParents(channel, result, alreadySyncedLabels));
        return result;
    }

    /**
     * Recursively adds a channel and all its parent channels to the result set
     * @param channel the channel to add
     * @param result the set of channels to sync
     * @param alreadySyncedLabels Labels of channels already synced
     */
    private void addChannelWithParents(Channel channel, Set<Channel> result, Set<String> alreadySyncedLabels) {
        // If this channel is already being synced or is already synced, nothing to do
        if (result.contains(channel)) {
            return;
        }
        // First handle the parent if this is a child channel
        if (!channel.isBaseChannel()) {
            Channel parentChannel = channel.getParentChannel();
            if (parentChannel != null) {
                // Recursively ensure the parent is added first
                addChannelWithParents(parentChannel, result, alreadySyncedLabels);
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

    private void removeChannelsByLabelForPeripheral(IssPeripheral peripheral, List<String> channelsLabels) {
        Set<IssPeripheralChannels> currentPeripheralChannels = peripheral.getPeripheralChannels();
        if (currentPeripheralChannels == null || currentPeripheralChannels.isEmpty()) {
            return;
        }
        Set<String> channelLabelsToDesync = new HashSet<>(channelsLabels);
        // Find parent channel labels that are being desynced
        Set<String> parentChannelLabelsToDesync = currentPeripheralChannels.stream()
                .filter(pc -> channelLabelsToDesync.contains(pc.getChannel().getLabel()))
                .map(pc -> pc.getChannel().getLabel())
                .collect(Collectors.toSet());
        // Collect channels that should be desynced
        Set<IssPeripheralChannels> channelsToDelete = currentPeripheralChannels.stream()
                .filter(pc -> {
                    Channel channel = pc.getChannel();
                    String channelLabel = channel.getLabel();
                    // Include if the channel is in the list to desync
                    if (channelLabelsToDesync.contains(channelLabel)) {
                        return true;
                    }
                    // Include child channels if their parent is being desynced
                    Channel parentChannel = channel.getParentChannel();
                    if (parentChannel != null &&
                            parentChannelLabelsToDesync.contains(parentChannel.getLabel())) {
                        LOG.debug("Desyncing child channel {} because its parent channel is being desynced",
                                channelLabel);
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toSet());
        hubFactory.deleteChannels(channelsToDelete);
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

    /**
     * Add peripheral channels to synchronize on a peripheral server
     *
     * @param user            The current user
     * @param fqdn            the FQDN identifying the peripheral Server
     * @param channelLabels   a list of labels of the channels to be added
     * @param peripheralOrgIdWhenCustomChannel the peripheral org to be set in custom channels
     */
    public void addPeripheralChannelsToSync(User user, String fqdn, List<String> channelLabels,
                                            Long peripheralOrgIdWhenCustomChannel) {
        ensureSatAdmin(user);
        IssPeripheral issPeripheral = hubFactory.lookupIssPeripheralByFqdn(fqdn).orElseThrow(() ->
                new IllegalArgumentException(fqdn + " is not registered as peripheral"));

        Set<Channel> allChannels = new HashSet<>();
        for (String channelLabel : channelLabels) {
            Channel channel = ChannelFactory.lookupByLabel(channelLabel);
            if (null == channel) {
                throw new IllegalArgumentException(
                        String.format("Channel with label [%s] does not exist", channelLabel));
            }
            Stream.iterate(channel, Objects::nonNull, Channel::getParentChannel).forEach(allChannels::add);

            for (Channel ch : allChannels) {
                if (ch.isCustom() && (peripheralOrgIdWhenCustomChannel == null)) {
                    throw new IllegalArgumentException(
                            String.format("A peripheral OrgID must be specified for custom channel with label [%s]",
                                    channelLabel));
                }
                Long orgId = ch.isVendorChannel() ? null : peripheralOrgIdWhenCustomChannel;

                hubFactory.lookupIssPeripheralChannelsByFqdnAndChannel(issPeripheral, ch)
                        .ifPresentOrElse(e -> {
                            e.setPeripheralOrgId(orgId);
                            hubFactory.save(e);
                        }, () -> {
                            IssPeripheralChannels issPeripheralChannel = new IssPeripheralChannels(issPeripheral, ch);
                            issPeripheralChannel.setPeripheralOrgId(orgId);
                            hubFactory.save(issPeripheralChannel);
                        });
            }
        }
    }

    /**
     * Remove peripheral channels to synchronize on a peripheral server
     *
     * @param user          The current user
     * @param fqdn          the FQDN identifying the peripheral Server
     * @param channelLabels a list of labels of the channels to be added
     */
    public void removePeripheralChannelsToSync(User user, String fqdn, List<String> channelLabels) {
        ensureSatAdmin(user);
        IssPeripheral issPeripheral = hubFactory.lookupIssPeripheralByFqdn(fqdn).orElseThrow(() ->
                new IllegalStateException(fqdn + " is not registered as peripheral"));

        for (String channelLabel : channelLabels) {
            Channel channel = ChannelFactory.lookupByLabel(channelLabel);
            if (null == channel) {
                throw new IllegalArgumentException(
                        String.format("Channel with label [%s] does not exist", channelLabel));
            }

            hubFactory.lookupIssPeripheralChannelsByFqdnAndChannel(issPeripheral, channel)
                    .ifPresent(hubFactory::remove);
        }
    }

    /**
     * Lists current peripheral channels to synchronize on a peripheral server
     *
     * @param user The current user
     * @param fqdn the FQDN identifying the peripheral Server
     * @return a list of channel labels on success, exception otherwise
     */
    public List<IssPeripheralChannels> listPeripheralChannelsToSync(User user, String fqdn) {
        ensureSatAdmin(user);
        IssPeripheral issPeripheral = hubFactory.lookupIssPeripheralByFqdn(fqdn).orElseThrow(() ->
                new IllegalStateException(fqdn + " is not registered as peripheral"));

        return hubFactory.listIssPeripheralChannels(issPeripheral);
    }

    /**
     * Synchronize peripheral channels on a peripheral server
     *
     * @param user The current user
     * @param fqdn the FQDN identifying the peripheral Server
     */
    public void syncPeripheralChannels(User user, String fqdn) throws CertificateException, IOException {
        ensureSatAdmin(user);

        IssPeripheral issPeripheral = hubFactory.lookupIssPeripheralByFqdn(fqdn).orElseThrow(() ->
                new IllegalStateException(fqdn + " is not registered as peripheral"));

        IssAccessToken accessToken = hubFactory.lookupAccessTokenFor(issPeripheral.getFqdn());
        List<ChannelInfoDetailsJson> channelInfo = hubFactory.listChannelInfoForPeripheral(issPeripheral);
        var internalClient = clientFactory.newInternalClient(
                issPeripheral.getFqdn(),
                accessToken.getToken(),
                issPeripheral.getRootCa());
        internalClient.syncChannels(channelInfo);
    }

    /**
     * Delete an ISS v1 slave if it's also registered as a ISS v3 peripheral
     * @param user the user performing the operation
     * @param fqdn the fully qualified domain name of the slave and corresponding peripheral
     * @param onlyLocal true to perform the removal only on the local server, false to also delete the master on the
     * remote slave
     * @throws CertificateException when it's not possible to use remote server certificate
     * @throws IOException when the connection with the remote server fails
     */
    public void deleteIssV1Slave(User user, String fqdn, boolean onlyLocal) throws CertificateException, IOException {
        ensureSatAdmin(user);

        IssSlave slave = Optional.ofNullable(IssFactory.lookupSlaveByName(fqdn)).orElseThrow(() ->
            new IllegalStateException(fqdn + " is not registered as an ISS v1 slave"));
        IssPeripheral peripheral = hubFactory.lookupIssPeripheralByFqdn(fqdn).orElseThrow(() ->
            new IllegalStateException(fqdn + " is not registered as an ISS v3 peripheral"));

        if (!onlyLocal) {
            // Delete the master remotely
            IssAccessToken accessToken = hubFactory.lookupAccessTokenFor(fqdn);
            var internalClient = clientFactory.newInternalClient(fqdn, accessToken.getToken(), peripheral.getRootCa());
            internalClient.deleteIssV1Master();
        }

        // Delete the slave locally
        IssFactory.delete(slave);
    }

    /**
     * Delete an ISS v1 master if it's also registered as an ISS v3 hub
     * @param accessToken the access token granting access and identifying the caller
     * remote slave
     * @throws IllegalStateException if a master or a hub does not exist with the fqdn identified by the token
     */
    public void deleteIssV1Master(IssAccessToken accessToken) {
        ensureValidToken(accessToken);

        String fqdn = accessToken.getServerFqdn();

        IssMaster master = Optional.ofNullable(IssFactory.lookupMasterByLabel(fqdn)).orElseThrow(() ->
            new IllegalStateException(fqdn + " is not registered as an ISS v1 master"));

        // Ensure that the server is registered as hub
        hubFactory.lookupIssHubByFqdn(fqdn).orElseThrow(() ->
            new IllegalStateException(fqdn + " is not registered as an ISS v3 hub"));

        // Remove the master
        IssFactory.delete(master);
    }

    private static List<Long> getMandatoryChannelsFor(ChannelSyncDetail channel) {
        Stream<Long> mandatoryChannelIds = SUSEProductFactory.findSyncedMandatoryChannels(channel.label())
            .map(Channel::getId);

        Stream<Long> parentIdStream = Optional.ofNullable(channel.parentId()).stream();

        // Ensure the parent is always marked as mandatory even for custom channels
        return Stream.concat(parentIdStream, mandatoryChannelIds)
            .distinct()
            .toList();
    }
}
