/**
 * CHECKSTYLE:OFF
 */
package com.suse.manager.webui.utils.salt.events;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a event fired from zypper
 */
public abstract class ZypperEvent {

    public static class CommonEventData {
        private String id;

        public String getId() {
            return id;
        }
    }

    public static class Blocked extends ZypperEvent {
        private final CommonEventData data;

        public Blocked(Event event) {
            this.data = event.getData(CommonEventData.class);
        }

        @Override
        public CommonEventData getEventData() {
            return data;
        }

        public Optional<Blocked> asBlocked() {
            return Optional.of(this);
        }

    }

    public static class Released extends ZypperEvent {
        private final CommonEventData data;

        public Released(Event event) {
            this.data = event.getData(CommonEventData.class);
        }

        @Override
        public CommonEventData getEventData() {
            return data;
        }

        public Optional<Released> asReleased() {
            return Optional.of(this);
        }
    }

    public static class PackageSetChanged extends ZypperEvent {
        private final CommonEventData data;

        public PackageSetChanged(Event event) {
            this.data = event.getData(CommonEventData.class);
        }

        @Override
        public CommonEventData getEventData() {
            return data;
        }

        public Optional<PackageSetChanged> asPackageSetChanged() {
            return Optional.of(this);
        }
    }

    private static final Pattern PATTERN =
            Pattern.compile("^zypper/([a-z]+)$");

    public Optional<PackageSetChanged> asPackageSetChanged() {
        return Optional.empty();
    }

    public Optional<Blocked> asBlocked() {
        return Optional.empty();
    }

    public Optional<Released> asReleased() {
        return Optional.empty();
    }

    /**
     * The id of the minion that emitted this event
     *
     * @return the minion id
     */
    public String getMinionId() {
        return getEventData().getId();
    }

    public abstract CommonEventData getEventData();

    /**
     * Utility method to parse e generic type to a more specific one
     * @param event the generic type to parse
     * @return an option containing the parsed value or non if it could not be parsed
     */
    public static Optional<ZypperEvent> parse(Event event) {
        Matcher matcher = PATTERN.matcher(event.getTag());
        if (matcher.matches()) {
            switch (matcher.group(1)) {
                case "packagesetchanged": {
                   return Optional.of(new PackageSetChanged(event));
                }
                case "blocked": {
                    return Optional.of(new Blocked(event));
                }
                case "released": {
                    return Optional.of(new Released(event));
                }
                default: return Optional.empty();
            }
        }
        else {
            return Optional.empty();
        }
    }
}
