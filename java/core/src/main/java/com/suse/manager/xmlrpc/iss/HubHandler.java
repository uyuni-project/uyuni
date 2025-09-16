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

package com.suse.manager.xmlrpc.iss;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.IOFaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.InvalidTokenException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.TokenAlreadyExistsException;
import com.redhat.rhn.frontend.xmlrpc.TokenCreationException;
import com.redhat.rhn.frontend.xmlrpc.TokenExchangeFailedException;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.api.ReadOnly;
import com.suse.manager.hub.HubManager;
import com.suse.manager.hub.migration.IssMigratorFactory;
import com.suse.manager.model.hub.ChannelInfoJson;
import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssPeripheralChannels;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.model.hub.ManagerInfoJson;
import com.suse.manager.model.hub.OrgInfoJson;
import com.suse.manager.model.hub.UpdatableServerData;
import com.suse.manager.model.hub.migration.MigrationResult;
import com.suse.manager.model.hub.migration.SlaveMigrationData;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenException;
import com.suse.manager.webui.utils.token.TokenParsingException;
import com.suse.manager.xmlrpc.InvalidCertificateException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * HubHandler
 *
 * @apidoc.namespace sync.hub
 * @apidoc.doc Contains methods to set up and manage Hub Inter-Server synchronization
 */
public class HubHandler extends BaseHandler {

    private static final Logger LOGGER = LogManager.getLogger(HubHandler.class);

    private final HubManager hubManager;

    private final IssMigratorFactory migratorFactory;

    /**
     * Default constructor
     */
    public HubHandler() {
        this(new HubManager(), new IssMigratorFactory());
    }

    /**
     * Builds a handler with the specified dependencies
     * @param hubManagerIn the hub manager
     */
    public HubHandler(HubManager hubManagerIn) {
        this(hubManagerIn, new IssMigratorFactory());
    }

    /**
     * Builds a handler with the specified dependencies
     * @param hubManagerIn the hub manager
     * @param migratorFactoryIn the migrator factory
     */
    public HubHandler(HubManager hubManagerIn, IssMigratorFactory migratorFactoryIn) {
        this.hubManager = hubManagerIn;
        this.migratorFactory = migratorFactoryIn;
    }

    protected String logAndGetErrorMessage(Throwable ex, String message, Object... args) {
        String fullMessage = LOGGER.getMessageFactory().newMessage(message, args).getFormattedMessage();
        if (!StringUtils.isEmpty(ex.getMessage()) && !StringUtils.isEmpty(fullMessage)) {
            fullMessage += ": ";
        }
        fullMessage += ex.getMessage();
        LOGGER.error(fullMessage);
        return fullMessage;
    }

    protected String logAndGetErrorMessage(String message, Object... args) {
        String fullMessage = LOGGER.getMessageFactory().newMessage(message, args).getFormattedMessage();
        LOGGER.error(fullMessage);
        return fullMessage;
    }

    /**
     * Generate a new access token for ISS for accessing this system
     * @param loggedInUser the user logged in. It must have the sat admin role.
     * @param fqdn the FQDN of the peripheral/hub that will be using this access token
     * @return the serialized form of the token
     *
     * @apidoc.doc Generate a new access token for ISS for accessing this system
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "fqdn", "FQDN of the peripheral/hub that will be using this access token")
     * @apidoc.returntype #param("string", "The serialized form of the token")
     */
    public String generateAccessToken(User loggedInUser, String fqdn) {
        ensureSatAdmin(loggedInUser);

        if (StringUtils.isEmpty(fqdn)) {
            throw new InvalidParameterException(logAndGetErrorMessage("No FQDN specified"));
        }

        try {
            return hubManager.issueAccessToken(loggedInUser, fqdn);
        }
        catch (TokenException ex) {
            throw new TokenCreationException(logAndGetErrorMessage(ex, "Unable to issue a token for {}", fqdn));
        }
        catch (ConstraintViolationException ex) {
            throw new TokenAlreadyExistsException(logAndGetErrorMessage(ex,
                    "Unable to issue a token, it already exists for {}", fqdn));
        }
    }

    /**
     * Store a third party access token for ISS
     * @param loggedInUser the user logged in. It must have the sat admin role.
     * @param fqdn the FQDN of the peripheral/hub that generated this access token
     * @param token the access token
     * @return 1 on success, exception otherwise
     *
     * @apidoc.doc Generate a new access token for ISS for accessing this system
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "fqdn", "the FQDN of the peripheral/hub that generated this access token")
     * @apidoc.param #param_desc("string", "token", "the access token")
     * @apidoc.returntype #return_int_success()
     */
    public int storeAccessToken(User loggedInUser, String fqdn, String token) {
        ensureSatAdmin(loggedInUser);

        if (StringUtils.isEmpty(fqdn)) {
            throw new InvalidParameterException(logAndGetErrorMessage("No FQDN specified"));
        }

        if (StringUtils.isEmpty(token)) {
            throw new InvalidParameterException(logAndGetErrorMessage("No token specified"));
        }

        try {
            hubManager.storeAccessToken(loggedInUser, fqdn, token);
        }
        catch (TokenParsingException ex) {
            throw new InvalidTokenException(logAndGetErrorMessage(ex,
                    "Unable to process the token from {}", fqdn));
        }
        catch (ConstraintViolationException ex) {
            throw new TokenAlreadyExistsException(logAndGetErrorMessage(ex,
                    "Unable to store token, it already exists for {}", fqdn));
        }
        return 1;
    }

    /**
     * Replace the auth tokens for connections between this hub and the given peripheral server
     * @param loggedInUser the user logged in. It must have the sat admin role.
     * @param fqdn the FQDN of the remote peripheral server which tokens should be changed
     * @return 1 on success, otherwise exception
     *
     * @apidoc.doc Replace the auth tokens for connections between this hub and the given peripheral server
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "fqdn", "the FQDN of the remote peripheral server to replace the tokens")
     * @apidoc.returntype #return_int_success()
     */
    public int replaceTokens(User loggedInUser, String fqdn) {
        ensureSatAdmin(loggedInUser);

        if (StringUtils.isEmpty(fqdn)) {
            throw new InvalidParameterException(logAndGetErrorMessage("No FQDN specified"));
        }

        try {
            hubManager.replaceTokensHub(loggedInUser, fqdn);
        }
        catch (CertificateException ex) {
            throw new InvalidCertificateException(logAndGetErrorMessage(ex, "Unable to load the provided certificate"));
        }
        catch (TokenBuildingException ex) {
            throw new TokenExchangeFailedException(logAndGetErrorMessage(ex, "Unable to create a token for {}", fqdn));
        }
        catch (IOException ex) {
            throw new TokenExchangeFailedException(logAndGetErrorMessage(ex,
                    "Unable to connect to remote server {}", fqdn));
        }
        catch (TokenParsingException ex) {
            throw new TokenExchangeFailedException(logAndGetErrorMessage(ex, "Unable to parse the specified token"));
        }
        catch (IllegalStateException ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex, "Illegal state"));
        }
        return 1;
    }

    /**
     * Registers automatically a remote PERIPHERAL server.
     * @param loggedInUser the user logged in. It must have the sat admin role.
     * @param fqdn the FQDN of the remote server to register
     * @param username the name of the user, needed to access the remote server. It must have the sat admin role.
     * @param password the password of the user, needed to access the remote server.
     * @return 1 on success, exception otherwise
     *
     * @apidoc.doc Registers automatically a remote server with the specified ISS role.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "fqdn", "the FQDN of the remote server to register")
     * @apidoc.param #param_desc("string", "username", "the name of the user, needed to access the remote server
     * It must have the sat admin role")
     * @apidoc.param #param_desc("string", "password", "the password of the user, needed to access the remote
     * server")
     * @apidoc.returntype #return_int_success()
     */
    public int registerPeripheral(User loggedInUser, String fqdn, String username, String password) {
        return registerPeripheral(loggedInUser, fqdn, username, password, null);
    }

    /**
     * Registers automatically a remote PERIPHERAL server.
     * @param loggedInUser the user logged in. It must have the sat admin role.
     * @param fqdn the FQDN of the remote server to register
     * @param username the name of the user, needed to access the remote server. It must have the sat admin role.
     * @param password the password of the user, needed to access the remote server.
     * @param rootCA the root CA certificate, in case it's needed to establish a secure connection
     * @return 1 on success, exception otherwise
     *
     * @apidoc.doc Registers automatically a remote server with the specified ISS role.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "fqdn", "the FQDN of the remote server to register")
     * @apidoc.param #param_desc("string", "username", "the name of the user, needed to access the remote server
     * It must have the sat admin role")
     * @apidoc.param #param_desc("string", "password", "the password of the user, needed to access the remote
     * server")
     * @apidoc.param #param_desc("string", "rootCA", "the root CA certificate, in case it's needed to establish a secure
     * connection")
     * @apidoc.returntype #return_int_success()
     */
    public int registerPeripheral(User loggedInUser, String fqdn, String username, String password, String rootCA) {
        ensureSatAdmin(loggedInUser);

        if (StringUtils.isEmpty(fqdn)) {
            throw new InvalidParameterException(logAndGetErrorMessage("No FQDN specified"));
        }

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new InvalidParameterException(logAndGetErrorMessage("No credentials specified"));
        }

        try {
            hubManager.register(loggedInUser, fqdn, username, password, rootCA);
        }
        catch (CertificateException ex) {
            throw new InvalidCertificateException(logAndGetErrorMessage(ex, "Unable to load the provided certificate"));
        }
        catch (TokenParsingException ex) {
            throw new TokenExchangeFailedException(logAndGetErrorMessage(ex, "Unable to parse the specified token"));
        }
        catch (TokenBuildingException ex) {
            throw new TokenExchangeFailedException(logAndGetErrorMessage(ex, "Unable to create a token for {}", fqdn));
        }
        catch (IOException ex) {
            throw new TokenExchangeFailedException(logAndGetErrorMessage(ex,
                    "Unable to connect to remote server {}", fqdn));
        }
        catch (TaskomaticApiException ex) {
            throw new TokenExchangeFailedException(logAndGetErrorMessage(ex,
                    "Unable to schedule root CA certificate update for server {}", fqdn));
        }
        catch (IllegalStateException ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex, "Illegal state"));
        }

        return 1;
    }

    /**
     * Registers a remote PERIPHERAL server using an existing specified access token.
     * @param loggedInUser the user logged in. It must have the sat admin role.
     * @param fqdn the FQDN of the remote server to register
     * @param token the token used to authenticate on the remote server.
     * @return 1 on success, exception otherwise
     *
     * @apidoc.doc Registers a remote server with the specified ISS role using an existing specified access token.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "fqdn", "the FQDN of the remote server to register")
     * @apidoc.param #param_desc("string", "token", "the token used to authenticate on the remote server.")
     * @apidoc.returntype #return_int_success()
     */
    public int registerPeripheralWithToken(User loggedInUser, String fqdn, String token) {
        return registerPeripheralWithToken(loggedInUser, fqdn, token, null);
    }

    /**
     * Registers a remote PERIPHERAL server using an existing specified access token.
     * @param loggedInUser the user logged in. It must have the sat admin role.
     * @param fqdn the FQDN of the remote server to register
     * @param token the token used to authenticate on the remote server.
     * @param rootCA the root CA certificate, in case it's needed to establish a secure connection
     * @return 1 on success, exception otherwise
     *
     * @apidoc.doc Registers a remote server with the specified ISS role using an existing specified access token.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "fqdn", "the FQDN of the remote server to register")
     * @apidoc.param #param_desc("string", "token", "the token used to authenticate on the remote server.")
     * @apidoc.param #param_desc("string", "rootCA", "the root CA certificate, in case it's needed to establish a secure
     * connection")
     * @apidoc.returntype #return_int_success()
     */
    public int registerPeripheralWithToken(User loggedInUser, String fqdn, String token, String rootCA) {
        ensureSatAdmin(loggedInUser);

        if (StringUtils.isEmpty(fqdn)) {
            throw new InvalidParameterException(logAndGetErrorMessage("No FQDN specified"));
        }

        if (StringUtils.isEmpty(token)) {
            throw new InvalidParameterException(logAndGetErrorMessage("No token"));
        }

        try {
            hubManager.register(loggedInUser, fqdn, token, rootCA);
        }
        catch (CertificateException ex) {
            throw new InvalidCertificateException(logAndGetErrorMessage(ex, "Unable to load the provided certificate"));
        }
        catch (TokenParsingException ex) {
            throw new TokenExchangeFailedException(logAndGetErrorMessage(ex, "Unable to parse the specified token"));
        }
        catch (TokenBuildingException ex) {
            throw new TokenExchangeFailedException(logAndGetErrorMessage(ex, "Unable to create a token for {}", fqdn));
        }
        catch (IOException ex) {
            throw new TokenExchangeFailedException(logAndGetErrorMessage(ex,
                    "Unable to connect to remote server {}", fqdn));
        }
        catch (TaskomaticApiException ex) {
            throw new com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException(logAndGetErrorMessage(ex,
                    "Unable to refresh root CA certificate {}", fqdn));
        }
        catch (IllegalStateException ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex, "Illegal state"));
        }
        return 1;
    }

    /**
     * De-register the server locally identified by the fqdn.
     * @param loggedInUser the user
     * @param fqdn the FQDN of the server to de-register
     * @return 1 on success, exception otherwise
     *
     * @apidoc.doc De-register the server locally identified by the fqdn.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "fqdn", "the FQDN of the remote server to de-register")
     * @apidoc.returntype #return_int_success()
     */
    public int deregister(User loggedInUser, String fqdn) {
        return deregister(loggedInUser, fqdn, true);
    }

    /**
     * De-register the server identified by the fqdn.
     * @param loggedInUser the user
     * @param fqdn the FQDN of the server to de-register
     * @param onlyLocal true if the de-registration has to be performed only this server, false to instead fully
     * deregister on both sides
     * @return 1 on success, exception otherwise
     *
     * @apidoc.doc De-register the server identified by the fqdn.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "fqdn", "the FQDN of the remote server to de-register")
     * @apidoc.param #param_desc("boolean", "onlyLocal", " true if the de - registration has to be performed only this
     * server, false to instead fully deregister on both sides")
     * @apidoc.returntype #return_int_success()
     */
    public int deregister(User loggedInUser, String fqdn, boolean onlyLocal) {
        ensureSatAdmin(loggedInUser);

        if (StringUtils.isEmpty(fqdn)) {
            throw new InvalidParameterException(logAndGetErrorMessage("No FQDN specified"));
        }

        try {
            hubManager.deregister(loggedInUser, fqdn, onlyLocal);
        }
        catch (CertificateException ex) {
            throw new InvalidCertificateException(logAndGetErrorMessage(ex, "De-registration failed for {}", fqdn));
        }
        catch (IOException ex) {
            throw new TokenExchangeFailedException(logAndGetErrorMessage(ex,
                    "Unable to connect to remote server {}", fqdn));
        }
        catch (IllegalStateException ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex, "Illegal state"));
        }
        return 1;
    }

    /**
     * Set server details
     *
     * @param loggedInUser The current user
     * @param fqdn the FQDN identifying the Hub or Peripheral Server
     * @param role the role which should be changed
     * @param data the new data
     * @return 1 on success, exception otherwise
     *
     * @apidoc.doc Set server details. All arguments are optional and will only be modified
     * if included in the struct.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "fqdn", "The FQDN of Hub or Peripheral server to lookup details for.")
     * @apidoc.param #param_desc("string", "role", "The role which should be updated. Either 'HUB' or 'PERIPHERAL'.")
     * @apidoc.param
     *      #struct_begin("data")
     *          #prop_desc("string", "root_ca", "The root ca")
     *          #prop_desc("string", "gpg_key", "The root gpg key - only for role HUB")
     *      #struct_end()
     *  @apidoc.returntype #return_int_success()
     */
    public int setDetails(User loggedInUser, String fqdn, String role, Map<String, String> data) {
        ensureSatAdmin(loggedInUser);
        ensureValidRole(role);
        try {
            hubManager.updateServerData(loggedInUser, fqdn, IssRole.valueOf(role), new UpdatableServerData(data));
        }
        catch (TaskomaticApiException e) {
            throw new com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException("Unable to refresh root CA certificate");
        }
        catch (IllegalArgumentException ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex, "Illegal state"));
        }
        return 1;
    }

    private void ensureValidRole(String role) throws PermissionCheckFailureException {
        try {
            IssRole.valueOf(role);
        }
        catch (IllegalArgumentException ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(
                    role, "Invalid role, must either be HUB or PERIPHERAL"));
        }
    }

    /**
     * Collect data about a Manager Server
     *
     * @param loggedInUser the user
     * @return a {@link ManagerInfoJson} on success, exception otherwise
     * @apidoc.doc Get manager info.
     * @apidoc.param #session_key()
     * @apidoc.returntype $ManagerInfoJsonSerializer
     */
    @ReadOnly
    public ManagerInfoJson getManagerInfo(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        try {
            return hubManager.collectManagerInfo(loggedInUser);
        }
        catch (Exception ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex, "Error while collecting manager info"));
        }
    }

    /**
     * Remotely collect data about peripheral organizations
     *
     * @param loggedInUser the user
     * @param fqdn         the FQDN identifying the peripheral Server
     * @return a list of {@link Org} on success, exception otherwise
     * @apidoc.doc Remotely collect data about peripheral organizations
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_array_begin()
     * $OrgInfoJsonSerializer
     * #array_end()
     */
    @ReadOnly
    public List<OrgInfoJson> getAllPeripheralOrgs(User loggedInUser, String fqdn) {
        ensureSatAdmin(loggedInUser);
        try {
            return hubManager.getAllPeripheralOrgs(loggedInUser, fqdn);
        }
        catch (Exception ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex,
                    "Error while collecting peripheral organizations for {}", fqdn));
        }
    }

    /**
     * Remotely collect data about peripheral channels
     *
     * @param loggedInUser the user
     * @param fqdn         the FQDN identifying the peripheral Server
     * @return a list of {@link Channel} on success, exception otherwise
     * @apidoc.doc Remotely collect data about peripheral channels
     * @apidoc.param #session_key()
     * @apidoc.returntype #return_array_begin()
     * $ChannelInfoJsonSerializer
     * #array_end()
     */
    @ReadOnly
    public List<ChannelInfoJson> getAllPeripheralChannels(User loggedInUser, String fqdn) {
        ensureSatAdmin(loggedInUser);
        try {
            return hubManager.getAllPeripheralChannels(loggedInUser, fqdn);
        }
        catch (Exception ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex,
                    "Error while collecting peripheral channels for {}", fqdn));
        }
    }

    /**
     * Add peripheral channels to synchronize on a peripheral server
     *
     * @param loggedInUser  The current user
     * @param fqdn          the FQDN identifying the peripheral Server
     * @param channelLabels a list of labels of the channels to be added
     * @return 1 on success, exception otherwise
     * @apidoc.doc Add peripheral channels to synchronize on a peripheral server
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc(" string ", " fqdn ", " The FQDN identifying the peripheral server ")
     * @apidoc.param #prop_array_begin(" channelLabels ")
     * #prop_desc("string", "label", "The channel label")
     * #array_end()
     * @apidoc.returntype #return_int_success()
     */
    public int addPeripheralChannelsToSync(User loggedInUser, String fqdn, List<String> channelLabels) {
        ensureSatAdmin(loggedInUser);
        try {
            hubManager.addPeripheralChannelsToSync(loggedInUser, fqdn, channelLabels, null);
        }
        catch (IllegalArgumentException ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex,
                    "Invalid parameter while adding peripheral channels to sync for {}", fqdn));
        }
        catch (Exception ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex,
                    "Error while adding peripheral channels to sync for {}", fqdn));
        }

        return 1;
    }

    /**
     * Add peripheral channels to synchronize on a peripheral server, forcing the peripheral org
     *
     * @param loggedInUser  The current user
     * @param fqdn          the FQDN identifying the peripheral Server
     * @param channelLabels a list of labels of the channels to be added
     * @param peripheralOrgIdWhenCustomChannel the peripheral org to be set in custom channels
     * @return 1 on success, exception otherwise
     * @apidoc.doc
     * Add peripheral channels to synchronize on a peripheral server, forcing the peripheral org in custom channels
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "fqdn", "The FQDN identifying the peripheral server")
     * @apidoc.param #prop_array_begin("channelLabels")
     * #prop_desc("string", "label", "The channel label")
     * #array_end()
     * @apidoc.param #prop_desc("int", "peripheralOrgIdWhenCustomChannel",
     * "ID of the peripheral Org to be set in custom channels")
     * @apidoc.returntype #return_int_success()
     */
    public int addPeripheralChannelsToSync(User loggedInUser, String fqdn, List<String> channelLabels,
                                           Integer peripheralOrgIdWhenCustomChannel) {
        ensureSatAdmin(loggedInUser);
        try {
            hubManager.addPeripheralChannelsToSync(loggedInUser, fqdn, channelLabels,
                    (long)peripheralOrgIdWhenCustomChannel);
        }
        catch (IllegalArgumentException ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex,
                    "Invalid parameter while adding peripheral channels to sync for {}", fqdn));
        }
        catch (Exception ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex,
                    "Error while adding peripheral channels to sync for {}", fqdn));
        }
        return 1;
    }

    /**
     * Remove peripheral channels to synchronize on a peripheral server
     *
     * @param loggedInUser  The current user
     * @param fqdn          the FQDN identifying the peripheral Server
     * @param channelLabels a list of labels of the channels to be removed
     * @return 1 on success, exception otherwise
     * @apidoc.doc Remove peripheral channels to synchronize on a peripheral server
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc(" string ", " fqdn ", " The FQDN identifying the peripheral server ")
     * @apidoc.param #prop_array_begin(" channelLabels ")
     * #prop_desc("string", "label", "The channel label")
     * #array_end()
     * @apidoc.returntype #return_int_success()
     */
    public int removePeripheralChannelsToSync(User loggedInUser, String fqdn, List<String> channelLabels) {
        ensureSatAdmin(loggedInUser);
        try {
            hubManager.removePeripheralChannelsToSync(loggedInUser, fqdn, channelLabels);
        }
        catch (IllegalArgumentException ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex,
                    "Invalid parameter while removing peripheral channels to sync for {}", fqdn));
        }
        catch (Exception ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex,
                    "Error while removing peripheral channels to sync for {}", fqdn));
        }
        return 1;
    }

    /**
     * Lists current peripheral channels to synchronize on a peripheral server
     *
     * @param loggedInUser The current user
     * @param fqdn         the FQDN identifying the peripheral Server
     * @return a list of channel labels on success, exception otherwise
     * @apidoc.doc Lists current peripheral channel to synchronize on a peripheral server
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc(" string ", " fqdn ", " The FQDN identifying the peripheral server ")
     * @apidoc.returntype #return_array_begin()
     * #prop_desc("string", "label", "Label of a peripheral channel to sync")
     * #array_end()
     */
    @ReadOnly
    public List<String> listPeripheralChannelsToSync(User loggedInUser, String fqdn) {
        ensureSatAdmin(loggedInUser);
        try {
            List<IssPeripheralChannels> issPeripheralChannels = hubManager.listPeripheralChannelsToSync(
                    loggedInUser, fqdn);
            return issPeripheralChannels.stream().map(e -> e.getChannel().getLabel()).toList();
        }
        catch (IllegalArgumentException ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex,
                    "Invalid parameter while listing peripheral channels to sync for {}", fqdn));
        }
        catch (Exception ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex,
                    "Error while listing peripheral channels to sync for {}", fqdn));
        }
    }

    /**
     * Synchronize peripheral channels on a peripheral server
     *
     * @param loggedInUser The current user
     * @param fqdn         the FQDN identifying the peripheral Server
     * @return 1 on success, exception otherwise
     * @apidoc.doc Synchronize peripheral channels on a peripheral server
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc(" string ", " fqdn ", " The FQDN identifying the peripheral server ")
     * @apidoc.param #prop_array_begin(" channelLabels ")
     * #prop_desc("string", "label", "The channel label")
     * #array_end()
     * @apidoc.returntype #return_int_success()
     */
    public int syncPeripheralChannels(User loggedInUser, String fqdn) {
        ensureSatAdmin(loggedInUser);
        try {
            hubManager.syncPeripheralChannels(loggedInUser, fqdn);
        }
        catch (IllegalArgumentException ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex,
                    "Invalid parameter while synchronizing peripheral channels for {}", fqdn));
        }
        catch (Exception ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex,
                    "Error while synchronizing channels for {}", fqdn));
        }
        return 1;
    }

    /**
     * Regenerate the username and the password for an existing peripheral.
     * @param loggedInUser The current user
     * @param fqdn         the FQDN identifying the peripheral Server
     * @return 1 on success, exception otherwise
     *
     * @apidoc.doc Regenerate the username and the password for an existing peripheral.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc(" string ", " fqdn ", " The FQDN identifying the peripheral server ")
     * @apidoc.returntype #return_int_success()
     */
    public int regenerateSCCCredentials(User loggedInUser, String fqdn) {
        ensureSatAdmin(loggedInUser);

        try {
            hubManager.regenerateCredentials(loggedInUser, fqdn);
        }
        catch (IllegalArgumentException ex) {
            throw new InvalidParameterException(logAndGetErrorMessage(ex,
                "Invalid parameter while regenerating the scc credentials for {}", fqdn));
        }
        catch (CertificateException ex) {
            throw new InvalidCertificateException(logAndGetErrorMessage(ex, "Unable to load the provided certificate"));
        }
        catch (IOException ex) {
            throw new IOFaultException(logAndGetErrorMessage(ex, "Error while calling the peripheral server {}", fqdn));
        }

        return 1;
    }

    /**
     * Migrate the existing ISSv1 slaves to Hub Online Synchronization peripherals.
     *
     * @param loggedInUser The current user
     * @param migrationData A list of peripheral migration data
     * @return the migration result
     *
     * @apidoc.doc Migrate the existing ISSv1 slaves to Hub Online Synchronization peripherals.
     * @apidoc.param #session_key()
     * @apidoc.param
     *   #array_begin("migration_data")
     *     #struct_begin("slave_migration_data")
     *       #prop_desc("string", "fqdn", "The fully qualified domain name of the remote slave server.")
     *       #prop_desc("string", "token", "The token used to authenticate on the remote server.")
     *       #prop_desc("string", "root_ca", "The root ca needed to establish a secure connection to the
     *       remote server.")
     *     #struct_end()
     *   #array_end()
     * @apidoc.returntype $MigrationResultSerializer
     */
    public MigrationResult migrateFromISSv1(User loggedInUser, List<Map<String, String>> migrationData) {
        ensureSatAdmin(loggedInUser);

        if (CollectionUtils.isEmpty(migrationData)) {
            throw new InvalidParameterException("migration data must not be empty");
        }

        Map<String, SlaveMigrationData> migrationDataMap;

        try {
            migrationDataMap = migrationData.stream()
                .map(SlaveMigrationData::new)
                .collect(Collectors.toMap(SlaveMigrationData::fqdn, Function.identity()));
        }
        catch (NullPointerException ex) {
            throw new InvalidParameterException("Migration data is not valid: %s".formatted(ex.getMessage()));
        }

        return migratorFactory.createFor(loggedInUser)
            .migrateFromV1(migrationDataMap);
    }

    /**
     * Migrate the existing ISSv2 peripherals to Hub Online Synchronization peripherals.
     *
     * @param loggedInUser The current user
     * @param migrationData A list of peripheral migration data
     * @return the migration result
     *
     * @apidoc.doc Migrate the existing ISSv2 peripherals to Hub Online Synchronization peripherals.
     * @apidoc.param #session_key()
     * @apidoc.param
     *   #array_begin("migration_data")
     *     #struct_begin("peripheral_migration_data")
     *       #prop_desc("string", "fqdn", "The fully qualified domain name of the remote peripheral server.")
     *       #prop_desc("string", "token", "The token used to authenticate on the remote server.")
     *       #prop_desc("string", "root_ca", "The root ca needed to establish a secure connection to the
     *       remote server.")
     *     #struct_end()
     *   #array_end()
     * @apidoc.returntype $MigrationResultSerializer
     */
    public MigrationResult migrateFromISSv2(User loggedInUser, List<Map<String, String>> migrationData) {
        ensureSatAdmin(loggedInUser);

        if (CollectionUtils.isEmpty(migrationData)) {
            throw new InvalidParameterException("migration data must not be empty");
        }

        List<SlaveMigrationData> migrationDataList;

        try {
            migrationDataList = migrationData.stream()
                .map(SlaveMigrationData::new)
                .toList();
        }
        catch (NullPointerException ex) {
            throw new InvalidParameterException("Migration data is not valid: %s".formatted(ex.getMessage()));
        }

        return migratorFactory.createFor(loggedInUser)
            .migrateFromV2(migrationDataList);
    }

    /**
     * Return if this server is configured as peripheral server
     * @param loggedInUser the logged in user
     * @return return true if this is a peripheral server, otherwise false
     *
     * @apidoc.doc Check if this server is configured as peripheral server and read data from a Hub
     * @apidoc.param #session_key()
     * @apidoc.returntype #param_desc("boolean", "peripheral", "True if this is an ISS peripheral, false otherwise")
     */
    @ReadOnly
    public boolean isISSPeripheral(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        HubFactory hubFactory = new HubFactory();
        return hubFactory.isISSPeripheral();
    }
}
