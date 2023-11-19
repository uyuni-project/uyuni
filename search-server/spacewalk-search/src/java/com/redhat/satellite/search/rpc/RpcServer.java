/*
 * Copyright (c) 2008--2015 Red Hat, Inc.
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

package com.redhat.satellite.search.rpc;

import com.redhat.satellite.search.config.Configuration;
import com.redhat.satellite.search.db.DatabaseManager;
import com.redhat.satellite.search.index.IndexManager;
import com.redhat.satellite.search.scheduler.ScheduleManager;

import org.picocontainer.Startable;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Map;

import redstone.xmlrpc.XmlRpcServer;
import simple.http.connect.Connection;
import simple.http.connect.ConnectionFactory;

/**
 * Manages the embedded webserver and configures the XML-RPC runtime
 */
public class RpcServer implements Startable {

    private final int listenPort;
    private InetAddress listenAddress;
    private final Map<String, String> handlers;
    private ServerSocket socket;
    private final IndexManager indexManager;
    private final DatabaseManager databaseManager;
    private final ScheduleManager scheduleManager;

    /**
     * Constructor
     *
     * @param config configuration
     * @param idxManager index manager
     * @param dbManager database manager
     * @param schedMgr schedule manager
     * @throws UnknownHostException if config contains bad rpc_address
     */
    public RpcServer(Configuration config, IndexManager idxManager, DatabaseManager dbManager, ScheduleManager schedMgr)
            throws UnknownHostException {
        listenPort = config.getInt("search.rpc_port", 2828);
        String addr = config.getString("search.rpc_address", "127.0.0.1");
        if (addr != null) {
            listenAddress = InetAddress.getByName(addr);
        }
        handlers = config.getMap("search.rpc_handlers");
        if (handlers == null) {
            throw new IllegalArgumentException(
                    "search.rpc_handlers config entry " + "is missing");
        }
        indexManager = idxManager;
        databaseManager = dbManager;
        scheduleManager = schedMgr;
    }

    /**
     * Starts the server
     * @throws RuntimeException something bad happened
     */
    public void start() {
        XmlRpcServer xmlrpcServer = new XmlRpcServer();
        for (Map.Entry<String, String> entry: handlers.entrySet()) {
            String className = entry.getValue();
            xmlrpcServer.addInvocationHandler(entry.getKey(), loadHandler(className));
        }
        XmlRpcInvoker invoker = new XmlRpcInvoker(xmlrpcServer);
        Connection connection = ConnectionFactory.getConnection(invoker);
        try {
            socket = new ServerSocket();
            SocketAddress sockAddr = null;
            if (listenAddress != null) {
                sockAddr = new InetSocketAddress(listenAddress, listenPort);
            }
            else {
                sockAddr = new InetSocketAddress(listenPort);
            }
            socket.bind(sockAddr);
            connection.connect(socket);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Stops the server
     */
    public void stop() {
        try {
            socket.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Object loadHandler(String className) {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<?> klass = cl.loadClass(className);
            Class<?>[] paramTypes = { IndexManager.class, DatabaseManager.class, ScheduleManager.class };
            Object[] params = { indexManager, databaseManager, scheduleManager};
            Constructor<?> c = klass.getConstructor(paramTypes);
            return c.newInstance(params);
        }
        catch (NoSuchMethodException | ClassNotFoundException | IllegalArgumentException | InstantiationException |
               IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
