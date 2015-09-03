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

import com.redhat.rhn.common.messaging.MessageQueue;

import com.suse.manager.webui.models.MinionsModel;
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
public class SaltReactor implements EventListener {

    // Logger for this class
    private static Logger logger = Logger.getLogger(SaltReactor.class);

    // Salt properties
    private static final URI SALT_MASTER_URI = URI.create("http://localhost:9080");
    private static final String SALT_USER = "admin";
    private static final String SALT_PASSWORD = "";

    // The event stream object
    private static EventStream eventStream;

    /**
     * Start the salt reactor.
     */
    public static void start() {
        SaltStackClient client = new SaltStackClient(SALT_MASTER_URI);
        try {
            Token token = client.login(SALT_USER, SALT_PASSWORD, AuthModule.AUTO);
            if (logger.isDebugEnabled()) {
                logger.debug("Token: " + token.getToken());
            }

            // Sync minions to systems in the database
            logger.debug("Syncing minions to the database");
            MinionsModel.getKeys(null).getMinions().forEach(
                    (minionId) -> triggerMinionRegistration(minionId));

            // This is needed to prevent from early event stream close
            client.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 0);
            eventStream = client.events();
            eventStream.addEventListener(new SaltReactor());
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(Event event) {
        if (logger.isDebugEnabled()) {
            logger.debug("Event: " + event.getTag() + " -> " + event.getData());
        }

        // Trigger minion registration on minion start events (if not already registered)
        if (event.getTag().matches("salt/minion/(.*)/start")) {
            triggerMinionRegistration((String) event.getData().get("id"));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eventStreamClosed() {
        logger.warn("Event stream has closed!");
    }

    /**
     * Trigger event to register a given minion.
     *
     * @param minionId the minion id
     */
    private static void triggerMinionRegistration(String minionId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Trigger registration for minion: " + minionId);
        }
        MessageQueue.publish(new RegisterMinionEvent(minionId));
    }
}
