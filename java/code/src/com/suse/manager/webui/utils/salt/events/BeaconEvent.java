/**
 * CHECKSTYLE:OFF
 */
package com.suse.manager.webui.utils.salt.events;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an event fired by beacons
 */
public class BeaconEvent {
    private static final Pattern PATTERN =
            Pattern.compile("^salt/beacon/([^/]+)/([^/]+)/(.*)$");

    private final String beacon;
    private final String minionId;
    private final String additional;
    private final Map<String, Object> data;

    /**
     * Creates a new BeaconEvent
     * @param minionId the id of the minion sending the event
     * @param beacon the beacon name
     * @param additional additional information depending on the beacon
     * @param data data containing more information about this event
     */
    public BeaconEvent(String minionId, String beacon, String additional,
                       Map<String, Object> data) {
        this.minionId = minionId;
        this.beacon = beacon;
        this.additional = additional;
        this.data = data;
    }

    /**
     * Returns the beacon name.
     *
     * @return the beacon name
     */
    public String getBeacon() {
        return beacon;
    }

    /**
     * Returns the id of the minion that triggered the beacon
     *
     * @return the minion id
     */
    public String getMinionId() {
        return minionId;
    }

    /**
     * Provides additional information from the tag depending on the type of beacon
     *
     * @return additional information
     */
    public String getAdditional() {
        return additional;
    }

    /**
     * The event data containing more information about this event
     *
     * @return the event data
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Utility method to parse e generic event to a more specific one
     * @param event the generic event to parse
     * @return an option containing the parsed value or non if it could not be parsed
     */
    public static Optional<BeaconEvent> parse(Event event) {
        Matcher matcher = PATTERN.matcher(event.getTag());
        if (matcher.matches()) {
            BeaconEvent result = new BeaconEvent(matcher.group(1), matcher.group(2),
                    matcher.group(3), event.getData());
            return Optional.of(result);
        }
        else {
            return Optional.empty();
        }
    }
}
