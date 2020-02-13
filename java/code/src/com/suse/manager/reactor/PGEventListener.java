/**
 * Copyright (c) 2018 SUSE LLC
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

import com.suse.manager.tasks.ActorManager;
import com.suse.manager.tasks.Command;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.event.EventListener;
import org.apache.log4j.Logger;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * An EventListener that executes notifications immediately in the current thread. This is made to work
 * with {@link PGEventStream}
 */
public class PGEventListener implements EventListener {
    /**
     * Logger for this class
     */
    private static final Logger LOG = Logger.getLogger(PGEventListener.class);

    /**
     * Function to call when the event stream gets closed
     */
    private final Runnable eventStreamClosed;

    /**
     * Function that maps (JSON-based Salt) {@link Event}s to {@link com.suse.manager.tasks.ActorManager}'s {@link com.suse.manager.tasks.Command}s
     */
    private final Function<Event, Stream<Command>> eventToCommands;

    /**
     * Standard constructor.
     *  @param eventStreamClosedIn function to call when the event stream gets closed
     * @param eventToCommandsIn
     */
    public PGEventListener(Runnable eventStreamClosedIn, Function<Event, Stream<Command>> eventToCommandsIn) {
        this.eventStreamClosed = eventStreamClosedIn;
        eventToCommands = eventToCommandsIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(Event event) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Event: " + event.getTag() + " -> " + event.getData());
        }

        Stream<Command> commands = eventToCommands.apply(event);
        commands.forEach(c -> {
            LOG.debug("HANDLED VIA ACTORS: Event: " + event.getTag() + " -> " + event.getData());
            ActorManager.tell(c);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eventStreamClosed(int code, String phrase) {
        LOG.warn("Event stream closed: " + phrase + " [" + code + "]");
        eventStreamClosed.run();
    }
}
