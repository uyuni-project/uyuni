/**
 * Copyright (c) 2011 Novell
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

package com.redhat.rhn.common.logging;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;

/**
 * Implementation of log4j {@link AppenderSkeleton} that calls the locally
 * running XMLRPC audit log daemon.
 */
public class AuditLogAppender extends AppenderSkeleton {

    // The client object
    private XmlRpcClient client = null;
    // The method to be called
    private final String method = "audit.log";

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        client = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requiresLayout() {
        return false;
    }

    /**
     * Setup the {@link XmlRpcClient} for sending the events.
     *
     * @return client
     */
    private XmlRpcClient createClient() {
        String url = Config.get().getString(ConfigDefaults.AUDIT_SERVER);
        XmlRpcClient ret = null;
        try {
            ret = new XmlRpcClient(url, true);
        }
        catch (MalformedURLException e) {
            throw new AuditLogException("Error initializing XML-RPC client", e);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void append(LoggingEvent event) {
        // Lazy initialize the client
        if (client == null) {
            client = createClient();
        }

        // Setup the arguments
        AuditLogMessage m = (AuditLogMessage) event.getMessage();
        List<Object> args = new ArrayList<Object>();
        args.add(m.getUid());
        args.add(m.getMessage());
        args.add(m.getHost());
        args.add(m.getExtmap() != null ? m.getExtmap() : new HashMap());

        // Try to call the log method
        try {
            client.invoke(method, args);
        }
        catch (XmlRpcException e) {
            throw new AuditLogException("Error sending log event", e);
        }
        catch (XmlRpcFault e) {
            throw new AuditLogException("Error sending log event", e);
        }
    }
}
