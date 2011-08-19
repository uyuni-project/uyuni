package com.redhat.rhn.common.logging;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;

import com.redhat.rhn.common.conf.ConfigDefaults;

/**
 * {@link Appender} for log4j that calls the locally running XMLRPC audit log
 * daemon.
 *
 * @author jrenner
 */
public class AuditLogAppender extends AppenderSkeleton {

    // URL of the log daemon
    private String url;

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
            int port = ConfigDefaults.get().getAuditPort();
            url = "http://localhost:" + port;
        }
        final AuditLogMessage m = (AuditLogMessage) event.getMessage();
        new Runnable() {
            @Override
            public void run() {
                try {
                    XmlRpcClient client = new XmlRpcClient(url, true);
                    List<Object> args = new ArrayList<Object>();
                    args.add(m.getUid());
                    args.add(m.getMessage());
                    args.add(m.getHost());
                    args.add(m.getExtmap() != null ? m.getExtmap() : new HashMap());
                    client.invoke("log", args);
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Error sending log event", e);
                } catch (XmlRpcException e) {
                    throw new RuntimeException("Error sending log event", e);
                } catch (XmlRpcFault e) {
                    throw new RuntimeException("Error sending log event", e);
                }
            }
        }.run();
    }
}
