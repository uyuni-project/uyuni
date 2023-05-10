/*
 * Copyright (c) 2009--2011 Red Hat, Inc.
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

package org.cobbler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcInvocationHandler;

/**
 * XMLRPCHelper - class that contains wraps calls to Redstone's XML-RPC client.
 * Intentionally implements the XMLRPCInvoker interface so we can also provide
 * a mock implementation to our unit tests, so they don't require an actual
 * Cobbler server.
 *
 * @author paji
 */
public class CobblerConnection {
    // FIXME: Provide interface to abstract real and test implementation

    /**
     * Encapsulated Redstone XML-RPC client
     */
    private XmlRpcInvocationHandler client;
    /**
     * Computed URL that contains the Cobbler API endpoint
     */
    private String actualUrl;
    /**
     * Logger that is responsible to write the called methods to the Cobbler API
     */
    private static final Logger LOG = LogManager.getLogger(CobblerConnection.class);
    /**
     * Contains the currently valid token
     */
    private String token;

    /**
     * Default empty constructor that does not contain any initialization code
     */
    protected CobblerConnection() {
    }

    /**
     * Constructor to just connect the client to the server NO token is
     * setup. Client has to call {@link #setToken}.
     *
     * @param url cobbler base url, example {@code http://localhost}
     * @throws XmlRpcException if there is some communication issue..
     */

    public CobblerConnection(String url) {
        try {
            actualUrl = url + "/cobbler_api";
            client = new XmlRpcClient(actualUrl, false);
        }
        catch (MalformedURLException e) {
            throw new XmlRpcException(e);
        }
    }

    /**
     * Constructor to setup the client based on username and password.
     * Connects to cobbler and logs in the user right here to obtain the token.
     *
     * @param url  cobbler base url, example {@code http://localhost}
     * @param user the username
     * @param pass the password
     * @throws XmlRpcException if there is some communication issue..
     */
    public CobblerConnection(String url, String user, String pass) {
        this(url);
        login(user, pass);
    }

    /**
     * Constructor to setup the client based on the token itself. Connects to
     * cobbler. Idea here is that if you have the XML-RPC token by logging in
     * previously you could use that here.
     *
     * @param url     cobbler base url, example {@code http://localhost}
     * @param tokenIn the token
     * @throws XmlRpcException if there is some communication issue..
     */
    public CobblerConnection(String url, String tokenIn) {
        this(url);
        token = tokenIn;
    }

    /**
     * This constructor creates the connection but does not authentication
     * against the Cobbler server.
     *
     * @param urlIn    cobbler base url, example {@code http://localhost}
     * @param clientIn The XML-RPC Client that will be used to connect to Cobbler.
     */
    public CobblerConnection(String urlIn, XmlRpcInvocationHandler clientIn) {
        client = clientIn;
        actualUrl = urlIn + "/cobbler_api";
    }

    /**
     * Simple method to login in to cobbler with the given username and
     * password. The returned token is stored in the connection itself so that
     * it could be used for future operations. It is also returned if so
     * needed.
     *
     * @param login    username
     * @param password password
     * @return the login token
     */
    public String login(String login, String password) {
        token = (String) invokeMethod("login", login, password);
        return token;
    }

    /**
     * Check to see if the passed in token is still valid from Cobbler's perspective. If it is, return true,
     * else return false.
     *
     * @param tokenToCheck to check
     * @return boolean indicating validity
     */
    public boolean tokenCheck(String tokenToCheck) {
        return (boolean) invokeMethod("token_check", tokenToCheck);
    }


    /**
     * Invoke an XML-RPC method.
     *
     * @param procedureName to invoke
     * @param args          to pass to method
     * @return Object returned.
     */
    private Object invokeMethod(String procedureName, List<Object> args) throws XmlRpcException {
        LOG.debug("procedure: {} args: {}", procedureName, args);
        Object retval;
        try {
            retval = client.invoke(procedureName, args);
        }
        catch (Throwable e) {
            throw new XmlRpcException("XmlRpcException calling cobbler.", e);
        }
        return retval;
    }

    /**
     * Invoke an XML-RPC method.
     *
     * @param procedureName to invoke
     * @param args          to pass to method
     * @return Object returned.
     */
    public Object invokeMethod(String procedureName, Object... args) throws XmlRpcException {
        return invokeMethod(procedureName, Arrays.asList(args));
    }

    /**
     * Invoke an XML-RPC method, but this one appends the cobbler XML-RPC
     * token at the end of the args. Basically this serves the majority of
     * calls to cobbler where token is expected as the last param.
     *
     * @param procedureName to invoke
     * @param args          to pass to method
     * @return Object returned.
     * @throws XmlRpcException if any unexpected error occurs
     */
    public Object invokeTokenMethod(String procedureName,
                                    Object... args) throws XmlRpcException {
        List<Object> params = new LinkedList<>(Arrays.asList(args));
        params.add(token);
        return invokeMethod(procedureName, params);
    }

    /**
     * Updates the token with the provided one. Validity of it is not checked.
     *
     * @param tokenIn the cobbler auth token
     */
    public void setToken(String tokenIn) {
        token = tokenIn;
    }

    /**
     * Returns the actual cobbler server url including the suffix
     *
     * @return the server URL
     */
    public String getUrl() {
        return actualUrl;
    }

    /**
     * Returns the cobbler server version
     *
     * @return the server version
     */
    public Double getVersion() {
        return (Double) invokeMethod("version", new LinkedList<>());
    }

    /**
     * Executes a synchronous "cobbler sync".
     */
    public void sync() {
        invokeTokenMethod("sync");
    }

    /**
     * Executes a "cobbler sync" but returns an event id
     *
     * @return The Cobbler event ID.
     */
    public String backgroundSync() {
        Map<String, Object> args = new HashMap<>();
        args.put("verbose", Boolean.TRUE);
        return (String) invokeTokenMethod("background_sync", args);
    }

    /**
     * Generate a random MAC address that doesn't conflict with an existing object in Cobbler.
     *
     * @return A random MAC address that is not yet used inside Cobbler.
     */
    public String getRandomMac() {
        // argument is corresponding to virt_type
        // Valid value prefixes are: vmware, xen, qemu, kvm
        return (String) invokeTokenMethod("get_random_mac", "xenpv");
    }

    /**
     * Returns the last time in Cobbler any object received an update.
     *
     * @return The time that the last object was modified. In case the functionality is disabled or not working 0.0
     * is returned by Cobbler.
     */
    public double lastModifiedTime() {
        double result = 0.0;
        try {
            result = (Double) invokeTokenMethod("last_modified_time");
        }
        catch (XmlRpcException e) {
            LOG.error("Error calling cobbler.", e);
        }
        catch (NumberFormatException e) {
            LOG.error("Error converting cobbler response", e);
        }
        return result;
    }

    /**
     * Uptime check if the API is responsive.
     *
     * @return True if API is available, error in any other case.
     */
    public boolean ping() {
        return (boolean) invokeMethod("ping");
    }

}
