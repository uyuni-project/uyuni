/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.reactor;

import com.suse.saltstack.netapi.AuthModule;
import com.suse.saltstack.netapi.client.SaltStackClient;
import com.suse.saltstack.netapi.config.ClientConfig;
import com.suse.saltstack.netapi.datatypes.Event;
import com.suse.saltstack.netapi.datatypes.Token;
import com.suse.saltstack.netapi.event.EventListener;
import com.suse.saltstack.netapi.event.EventStream;

import org.apache.log4j.Logger;

import java.net.URI;

/**
 * Salt event reactor.
 */
public class SaltReactor {

    // Logger for this class
    private static Logger logger = Logger.getLogger(SaltReactor.class);

    // The salt URI
    private static final URI saltMasterURI = URI.create("http://localhost:9080");

    // The event stream object
    private static EventStream eventStream;

    /**
     * Start the salt reactor.
     */
    public static void start() {
        SaltStackClient client = new SaltStackClient(saltMasterURI);
        try {
            Token token = client.login("admin", "", AuthModule.AUTO);
            logger.debug("Token: " + token.getToken());

            client.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 0);
            eventStream = client.events();
            eventStream.addEventListener(new EventListener() {
                @Override
                public void notify(Event event) {
                    logger.debug("Event tag: " + event.getTag());
                    logger.debug("Event data: " + event.getData());
                }

                @Override
                public void eventStreamClosed() {
                    logger.debug("Event stream has closed");
                }
            });
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Stop the salt reactor.
     */
    public static void stop() {
        if (eventStream != null) {
            eventStream.close();
        }
    }
}
