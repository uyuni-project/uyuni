/*
 * Copyright (c) 2010 Red Hat, Inc.
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
package com.redhat.rhn.taskomatic;

import com.redhat.rhn.common.conf.Config;

import com.sun.net.httpserver.HttpServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import redstone.xmlrpc.XmlRpcCustomSerializer;
import redstone.xmlrpc.XmlRpcServer;


/**
 * TaskoXmlRpcServer
 */
public class TaskoXmlRpcServer {

    private static final Logger LOG = LogManager.getLogger(TaskoXmlRpcServer.class);

    private final XmlRpcServer xmlrpcServer;

    private final HttpServer httpServer;

    /**
     * Create a server instance from the given configuration
     *
     * @param config the configuration, which may contain the entries {@code tasko_server.host} and
     * {@code tasko_server.port}
     * @throws IOException when the server cannot be created
     */
    public TaskoXmlRpcServer(Config config) throws IOException {
        this(
            InetAddress.getByName(config.getString("tasko_server.host", "localhost")),
            config.getInt("tasko_server.port", 2829)
        );
    }

    /**
     * Create a server instance on the given address and port
     *
     * @param address the address where the server accepts connections
     * @param port the port where the server accepts connections
     * @throws IOException when the server cannot be created
     */
    public TaskoXmlRpcServer(InetAddress address, int port) throws IOException {
        xmlrpcServer = new XmlRpcServer();
        xmlrpcServer.addInvocationHandler("tasko", new TaskoXmlRpcHandler());
        xmlrpcServer.addInvocationInterceptor(new TaskoXmlRpcInvocationInterceptor());
        addTaskoSerializers();

        httpServer = HttpServer.create(new InetSocketAddress(address, port), 0);
        httpServer.createContext("/RPC2", new TaskoXmlRpcHttpHandler(xmlrpcServer));
    }

    /**
     * Starts accepting XML-RPC requests on the configured endpoint.
     */
    public void start() {
        httpServer.start();
    }

    /**
     * Stops the server immediately and closes all active connections.
     */
    public void stop() {
        httpServer.stop(0);
    }

    private void addTaskoSerializers() {
        for (Class<? extends XmlRpcCustomSerializer> clazz : TaskoSerializerRegistry.getSerializationClasses()) {
            try {
                xmlrpcServer.getSerializer().addCustomSerializer(clazz.getDeclaredConstructor().newInstance());
            }
            catch (ReflectiveOperationException ex) {
                LOG.error("Unable to create serializer for class {}", clazz, ex);
            }
        }
    }
}
