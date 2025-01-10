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

import com.redhat.rhn.domain.iss.IssRole;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.InvalidTokenException;
import com.redhat.rhn.frontend.xmlrpc.TokenAlreadyExistsException;
import com.redhat.rhn.frontend.xmlrpc.TokenCreationException;
import com.redhat.rhn.frontend.xmlrpc.TokenExchangeFailedException;

import com.suse.manager.model.hub.HubManager;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenException;
import com.suse.manager.webui.utils.token.TokenParsingException;
import com.suse.manager.xmlrpc.InvalidCertificateException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Arrays;

/**
 * IssHandler
 *
 * @apidoc.namespace sync.iss
 * @apidoc.doc Contains methods to set up and manage ISS v3 sync
 */
public class IssHandler extends BaseHandler {

    private static final Logger LOGGER = LogManager.getLogger(IssHandler.class);

    private final HubManager hubManager;

    /**
     * Default constructor
     */
    public IssHandler() {
        this(new HubManager());
    }

    /**
     * Builds a handler with the specified dependencies
     * @param hubManagerIn the hub manager
     */
    public IssHandler(HubManager hubManagerIn) {
        this.hubManager = hubManagerIn;
    }

    /**
     * Generate a new access token for ISS for accessing this system
     * @param loggedInUser the user logged in. It must have the sat admin role.
     * @param fqdn the FQDN of the peripheral/hub that will be using this access token
     *
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
            throw new InvalidParameterException("No FQDN specified");
        }

        try {
            return hubManager.issueAccessToken(fqdn);
        }
        catch (TokenException ex) {
            LOGGER.error("Unable to issue a token for {}", fqdn, ex);
            throw new TokenCreationException();
        }
        catch (ConstraintViolationException ex) {
            LOGGER.error("Unable to issue a token, it already exists for {}", fqdn, ex);
            throw new TokenAlreadyExistsException();
        }
    }

    /**
     * Store a third party access token for ISS
     * @param loggedInUser the user logged in. It must have the sat admin role.
     * @param fqdn the FQDN of the peripheral/hub that generated this access token
     * @param token the access token
     * @return 1 on success, exception otherwise

     * @apidoc.doc Generate a new access token for ISS for accessing this system
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "fqdn", "the FQDN of the peripheral/hub that generated this access token")
     * @apidoc.param #param_desc("string", "token", "the access token")
     * @apidoc.returntype #return_int_success()
     */
    public int storeAccessToken(User loggedInUser, String fqdn, String token) {
        ensureSatAdmin(loggedInUser);

        if (StringUtils.isEmpty(fqdn)) {
            throw new InvalidParameterException("No FQDN specified");
        }

        if (StringUtils.isEmpty(token)) {
            throw new InvalidParameterException("No token specified");
        }

        try {
            hubManager.storeAccessToken(fqdn, token);
        }
        catch (TokenParsingException ex) {
            LOGGER.error("Unable to process the token from {}", fqdn, ex);
            throw new InvalidTokenException();
        }
        catch (ConstraintViolationException ex) {
            LOGGER.error("Unable to store token, it already exists for {}", fqdn, ex);
            throw new TokenAlreadyExistsException();
        }

        return 1;
    }

    /**
     * Registers automatically a remote server with the specified ISS role.
     * @param loggedInUser the user logged in. It must have the sat admin role.
     * @param fqdn the FQDN of the remote server to register
     * @param role the ISS role of the remote server. Can be either HUB or PERIPHERAL
     * @param username the name of the user, needed to access the remote server. It must have the sat admin role.
     * @param password the password of the user, needed to access the remote server.
     * @return 1 on success, exception otherwise
     * @apidoc.doc Registers automatically a remote server with the specified ISS role.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "fqdn", "the FQDN of the remote server to register ")
     * @apidoc.param #param_desc("string", "role", "the ISS role of the remote server. Either HUB or PERIPHERAL")
     * @apidoc.param #param_desc("string", "username", "the name of the user, needed to access the remote server
     * It must have the sat admin role")
     * @apidoc.param #param_desc("string", "password", "the password of the user, needed to access the remote
     * server")
     * @apidoc.returntype #return_int_success()
     */
    public int register(User loggedInUser, String fqdn, String role, String username, String password) {
        return register(loggedInUser, fqdn, role, username, password, null);
    }

    /**
     * Registers automatically a remote server with the specified ISS role.
     * @param loggedInUser the user logged in. It must have the sat admin role.
     * @param fqdn the FQDN of the remote server to register
     * @param role the ISS role of the remote server. Can be either HUB or PERIPHERAL
     * @param username the name of the user, needed to access the remote server. It must have the sat admin role.
     * @param password the password of the user, needed to access the remote server.
     * @param rootCA the root CA certificate, in case it's needed to establish a secure connection
     * @return 1 on success, exception otherwise
     * @apidoc.doc Registers automatically a remote server with the specified ISS role.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "fqdn", "the FQDN of the remote server to register ")
     * @apidoc.param #param_desc("string", "role", "the ISS role of the remote server. Either HUB or PERIPHERAL")
     * @apidoc.param #param_desc("string", "username", "the name of the user, needed to access the remote server
     * It must have the sat admin role")
     * @apidoc.param #param_desc("string", "password", "the password of the user, needed to access the remote
     * server")
     * @apidoc.param #param_desc("string", "rootCA", the root CA certificate, in case it's needed to establish a secure
     * connection")
     * @apidoc.returntype #return_int_success()
     */
    public int register(User loggedInUser, String fqdn, String role, String username, String password, String rootCA) {
        ensureSatAdmin(loggedInUser);

        IssRole remoteRole = Arrays.stream(IssRole.values())
            .filter(r -> r.name().equalsIgnoreCase(role))
            .findFirst()
            .orElseThrow(() -> new InvalidParameterException("Invalid role specified"));

        if (StringUtils.isEmpty(fqdn)) {
            throw new InvalidParameterException("No FQDN specified");
        }

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new InvalidParameterException("No credentials specified");
        }

        try {
            hubManager.register(fqdn, remoteRole, username, password, rootCA);
        }
        catch (CertificateException ex) {
            LOGGER.error("Unable to load the provided certificate", ex);
            throw new InvalidCertificateException(ex);
        }
        catch (TokenParsingException ex) {
            LOGGER.error("Unable to parse the specified token", ex);
            throw new TokenExchangeFailedException(ex);
        }
        catch (TokenBuildingException ex) {
            LOGGER.error("Unable to create a token for {}", fqdn, ex);
            throw new TokenExchangeFailedException(ex);
        }
        catch (IOException ex) {
            LOGGER.error("Unable to connect to remote server {}", fqdn, ex);
            throw new TokenExchangeFailedException(ex);
        }

        return 1;
    }

    /**
     * Registers a remote server with the specified ISS role using an existing specified access token.
     * @param loggedInUser the user logged in. It must have the sat admin role.
     * @param fqdn the FQDN of the remote server to register
     * @param role the ISS role of the remote server. Can be either HUB or PERIPHERAL
     * @param token the token used to authenticate on the remote server.
     * @return 1 on success, exception otherwise
     * @apidoc.doc Registers a remote server with the specified ISS role using an existing specified access token.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "fqdn", "the FQDN of the remote server to register ")
     * @apidoc.param #param_desc("string", "role", "the ISS role of the remote server. Either HUB or PERIPHERAL")
     * @apidoc.param #param_desc("string", "token", "the token used to authenticate on the remote server.")
     * @apidoc.returntype #return_int_success()
     */
    public int registerWithToken(User loggedInUser, String fqdn, String role, String token) {
        return registerWithToken(loggedInUser, fqdn, role, token, null);
    }

    /**
     * Registers a remote server with the specified ISS role using an existing specified access token.
     * @param loggedInUser the user logged in. It must have the sat admin role.
     * @param fqdn the FQDN of the remote server to register
     * @param role the ISS role of the remote server. Can be either HUB or PERIPHERAL
     * @param token the token used to authenticate on the remote server.
     * @param rootCA the root CA certificate, in case it's needed to establish a secure connection
     * @return 1 on success, exception otherwise
     * @apidoc.doc Registers a remote server with the specified ISS role using an existing specified access token.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "fqdn", "the FQDN of the remote server to register ")
     * @apidoc.param #param_desc("string", "role", "the ISS role of the remote server. Either HUB or PERIPHERAL")
     * @apidoc.param #param_desc("string", "token", "the token used to authenticate on the remote server.")
     * @apidoc.param #param_desc("string", "rootCA", the root CA certificate, in case it's needed to establish a secure
     * connection")
     * @apidoc.returntype #return_int_success()
     */
    public int registerWithToken(User loggedInUser, String fqdn, String role, String token, String rootCA) {
        ensureSatAdmin(loggedInUser);

        IssRole remoteRole = Arrays.stream(IssRole.values())
            .filter(r -> r.name().equalsIgnoreCase(role))
            .findFirst()
            .orElseThrow(() -> new InvalidParameterException("Invalid role specified"));

        if (StringUtils.isEmpty(fqdn)) {
            throw new InvalidParameterException("No FQDN specified");
        }

        if (StringUtils.isEmpty(token)) {
            throw new InvalidParameterException("No token");
        }

        try {
            hubManager.register(fqdn, remoteRole, token, rootCA);
        }
        catch (CertificateException ex) {
            LOGGER.error("Unable to load the provided certificate", ex);
            throw new InvalidCertificateException(ex);
        }
        catch (TokenParsingException ex) {
            LOGGER.error("Unable to parse the specified token", ex);
            throw new TokenExchangeFailedException(ex);
        }
        catch (TokenBuildingException ex) {
            LOGGER.error("Unable to create a token for {}", fqdn, ex);
            throw new TokenExchangeFailedException(ex);
        }
        catch (IOException ex) {
            LOGGER.error("Unable to connect to remote server {}", fqdn, ex);
            throw new TokenExchangeFailedException(ex);
        }

        return 1;
    }
}
