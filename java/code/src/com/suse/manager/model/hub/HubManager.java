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

package com.suse.manager.model.hub;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.iss.IssRole;

import com.suse.manager.iss.IssClientFactory;
import com.suse.manager.iss.IssInternalClient;
import com.suse.manager.webui.utils.token.IssTokenBuilder;
import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenParser;
import com.suse.manager.webui.utils.token.TokenParsingException;
import com.suse.utils.CertificateUtils;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Optional;

/**
 * Business logic to manage ISSv3 Sync
 */
public class HubManager {

    private final HubFactory hubFactory;

    private final IssClientFactory clientFactory;

    /**
     * Default constructor
     */
    public HubManager() {
        this(new HubFactory(), new IssClientFactory());
    }

    /**
     * Builds an instance with the given dependencies
     * @param hubFactoryIn the hub factory
     * @param clientFactoryIn the ISS client factory
     */
    public HubManager(HubFactory hubFactoryIn, IssClientFactory clientFactoryIn) {
        this.hubFactory = hubFactoryIn;
        this.clientFactory = clientFactoryIn;
    }

    /**
     * Create a new access token for the given FQDN and store it in the database
     * @param fqdn the FQDN of the peripheral/hub
     * @return the serialized form of the token
     * @throws TokenBuildingException when an error occurs during generation
     * @throws TokenParsingException when the generated token cannot be parsed
     */
    public String issueAccessToken(String fqdn) throws TokenBuildingException, TokenParsingException {
        Token token = new IssTokenBuilder(fqdn)
            .usingServerSecret()
            .build();

        hubFactory.saveToken(fqdn, token.getSerializedForm(), TokenType.ISSUED, token.getExpirationTime());
        return token.getSerializedForm();
    }

    /**
     * Save the given remote server as hub or peripheral depending on the specified role
     * @param role the role of the server
     * @param serverFqdn the fqdn of the server
     * @param rootCA the root certificate, if needed
     * @return the persisted remote server
     */
    public IssServer saveNewServer(IssRole role, String serverFqdn, String rootCA) {
        return switch (role) {
            case HUB -> {
                IssHub hub = new IssHub(serverFqdn, rootCA);
                hubFactory.save(hub);
                yield hub;
            }
            case PERIPHERAL -> {
                IssPeripheral peripheral = new IssPeripheral(serverFqdn, rootCA);
                hubFactory.save(peripheral);
                yield peripheral;
            }
        };
    }

    /**
     * Stores in the database the access token of the given FQDN
     * @param fqdn the FQDN of the peripheral/hub that generated this token
     * @param token the token
     * @throws TokenParsingException when it's not possible to process the token
     */
    public void storeAccessToken(String fqdn, String token) throws TokenParsingException {
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

    /**
     * Register a remote server with the specified role for ISS
     *
     * @param remoteServer the peripheral server FQDN
     * @param role the ISS role, either hub or peripheral
     * @param username the username of a {@link com.redhat.rhn.domain.role.RoleFactory#SAT_ADMIN} of the remote server
     * @param password the password of the specified user
     * @param rootCA the optional root CA of the remote server. can be null
     *
     * @throws CertificateException if the specified certificate is not parseable
     * @throws TokenParsingException if the specified token is not parseable
     * @throws TokenBuildingException if an error occurs while generating the token for the server
     * @throws IOException when connecting to the server fails
     */
    public void register(String remoteServer, IssRole role, String username, String password, String rootCA)
        throws CertificateException, TokenBuildingException, IOException, TokenParsingException {
        // Verify this server is not already registered as hub or peripheral
        ensureServerNotRegistered(remoteServer);

        // Generate a token for this server on the remote
        String remoteToken;
        try (var externalClient = clientFactory.newExternalClient(remoteServer, username, password, rootCA)) {
            remoteToken = externalClient.generateAccessToken(ConfigDefaults.get().getHostname());
        }

        registerWithToken(remoteServer, role, rootCA, remoteToken);
    }

    /**
     * Register a remote server with the specified role for ISS
     *
     * @param remoteServer the peripheral server FQDN
     * @param role the ISS role, either hub or peripheral
     * @param remoteToken the token used to connect to the peripheral server
     * @param rootCA the optional root CA of the peripheral server
     *
     * @throws CertificateException if the specified certificate is not parseable
     * @throws TokenParsingException if the specified token is not parseable
     * @throws TokenBuildingException if an error occurs while generating the token for the peripheral server
     * @throws IOException when connecting to the peripheral server fails
     */
    public void register(String remoteServer, IssRole role, String remoteToken, String rootCA)
        throws CertificateException, TokenBuildingException, IOException, TokenParsingException {
        // Verify this server is not already registered as hub or peripheral
        ensureServerNotRegistered(remoteServer);

        registerWithToken(remoteServer, role, rootCA, remoteToken);
    }

    private void registerWithToken(String remoteServer, IssRole role, String rootCA, String remoteToken)
        throws TokenParsingException, CertificateException, TokenBuildingException, IOException {
        storeAccessToken(remoteServer, remoteToken);

        IssServer registeredServer = saveNewServer(role, remoteServer, rootCA);
        registerToRemote(registeredServer, remoteToken, rootCA);
    }

    private void registerToRemote(IssServer remoteServer, String remoteToken, String rootCA)
        throws CertificateException, TokenParsingException, TokenBuildingException, IOException {

        // Create a client to connect to the internal API of the remote server
        IssInternalClient internalApi = clientFactory.newInternalClient(remoteServer.getFqdn(), remoteToken, rootCA);

        // Register this server on the remote with the opposite role
        IssRole localRoleForRemote = remoteServer.getRole() == IssRole.HUB ? IssRole.PERIPHERAL : IssRole.HUB;
        // Issue a token for granting access to the remote server
        String localAccessToken = issueAccessToken(remoteServer.getFqdn());
        // Send the local trusted root, if we needed a different certificate to connect
        String localRootCA = rootCA != null ? CertificateUtils.loadLocalTrustedRoot() : null;

        internalApi.register(localRoleForRemote, localAccessToken, localRootCA);
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
}
