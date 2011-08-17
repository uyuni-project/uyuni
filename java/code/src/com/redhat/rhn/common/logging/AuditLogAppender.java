package com.redhat.rhn.common.logging;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;

/**
 * {@link Appender} for log4j that calls the locally running XMLRPC audit log
 * daemon.
 *
 * @author jrenner
 */
public class AuditLogAppender extends AppenderSkeleton {

    // Logger for this class
    private static Logger log = Logger.getLogger(AuditLogAppender.class);
    // URL of the audit log daemon
    private String url = "http://127.0.0.1:6888";

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
                    log.error(e.getMessage(), e);
                } catch (XmlRpcException e) {
                    log.error(e.getMessage(), e);
                } catch (XmlRpcFault e) {
                    log.error(e.getMessage(), e);
                }
            }
        }.run();
    }
}
