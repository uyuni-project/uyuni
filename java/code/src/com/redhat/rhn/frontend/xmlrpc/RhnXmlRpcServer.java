/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc;

import java.io.InputStream;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcDispatcher;
import redstone.xmlrpc.XmlRpcServer;

/**
 * RhnXmlRpcServer
 */
public class RhnXmlRpcServer extends XmlRpcServer {

    private static ThreadLocal<String> server = new ThreadLocal<>();
    private static ThreadLocal<String> proto = new ThreadLocal<>();
    private static ThreadLocal<String> caller = new ThreadLocal<>();

    /**
     * Adding a method to get the callerIp into the XmlRpc for logging.
     * For some dumb reason XmlRpcServer doesn't know about callerIp
     * address unless it runs as a standalone service.
     * @param xmlInput  The XML-RPC message.
     * @param output Writer
     * @param callerIp This is supplied for informational purposes and is  made
     * @param serverHost the hostname/ipaddress that the client used in
     *      reference to the server
     * @param protoc the protocol the client used in connection to the server
     * available  to  custom processors.
     */
    public void execute(InputStream xmlInput, Writer output, String callerIp,
            String serverHost, String protoc) {
        server.set(serverHost);
        proto.set(protoc);
        caller.set(callerIp);
        XmlRpcDispatcher dispatcher = new XmlRpcDispatcher(this, callerIp);
        dispatcher.dispatch(xmlInput, output);
    }

    /**
     * Retrieve the server name used for the current xmlrpc call
     * @return the server name
     */
    public static String getServerName() {
        return server.get();
    }

    /**
     * Retrieve the server protocol (http or https) used for the current xmlrpc call
     * @return the protocol
     */
    public static String getProtocol() {
        return proto.get();
    }

    /**
     * Retrieve the IP of the current xmlrpc call's caller.
     * @return the IP of the caller
     */
    public static String getCallerIp() {
        return caller.get();
    }
}
