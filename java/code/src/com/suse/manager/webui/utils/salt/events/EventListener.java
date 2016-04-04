package com.suse.manager.webui.utils.salt.events;


import javax.websocket.CloseReason;

/**
 * Defines a client notification interface for events stream.
 */
public interface EventListener {

    /**
     * Notify the listener of a new event. Returned data is a {@link Event} object.
     * @param event object representation of the latest stream event
     */
    void notify(Event event);

    /**
     * Notify the listener that the backing event stream was closed.  Listener may
     * need to recreate the event stream or take other actions.
     * @param closeReason the close reason
     */
    void eventStreamClosed(CloseReason closeReason);
}
