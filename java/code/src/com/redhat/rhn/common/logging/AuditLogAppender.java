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

    // URL of the log daemon
    private String url;
    // The method to be called
    private final String method = "audit.log";

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        // nop
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requiresLayout() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void append(LoggingEvent event) {
        if (url == null) {
            // Lazy initialize the URL
            url = Config.get().getString(ConfigDefaults.AUDIT_SERVER);
        }
        AuditLogMessage m = (AuditLogMessage) event.getMessage();
        try {
            XmlRpcClient client = new XmlRpcClient(url, true);
            List<Object> args = new ArrayList<Object>();
            args.add(m.getUid());
            args.add(m.getMessage());
            args.add(m.getHost());
            args.add(m.getExtmap() != null ? m.getExtmap() : new HashMap());
            client.invoke(method, args);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error sending log event", e);
        } catch (XmlRpcException e) {
            throw new RuntimeException("Error sending log event", e);
        } catch (XmlRpcFault e) {
            throw new RuntimeException("Error sending log event", e);
        }
    }
}
