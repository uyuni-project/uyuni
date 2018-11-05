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

package com.suse.manager.webui.utils.salt;

import com.suse.manager.reactor.utils.ValueMap;
import com.suse.salt.netapi.datatypes.Event;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Event signalling an OS image has been deployed on the minion.
 */
public class ImageDeployedEvent {

    private static final Pattern PATTERN = Pattern.compile("^suse/manager/image_deployed$");

    private ValueMap grains;

    private static final Logger LOG = Logger.getLogger(ImageDeployedEvent.class);

    /**
     * Standard constructor.
     *
     * @param grainsIn - the minion grains
     */
    public ImageDeployedEvent(ValueMap grainsIn) {
        this.grains = grainsIn;
    }

    /**
     * Gets the grains.
     *
     * @return grains
     */
    public ValueMap getGrains() {
        return grains;
    }

    /**
     * Convenience method for getting machine_id flag
     * @return machine_id flag
     */
    public Optional<String> getMachineId() {
        return grains.getOptionalAsString("machine_id");
    }

    /**
     * Parse the generic event
     *
     * @param event the generic event to parse
     * @return Optional of {@link com.suse.manager.webui.utils.salt.ImageDeployedEvent} or
     * an empty Optional if the event data did not match the ImageDeployedEvent shape
     */
    public static Optional<ImageDeployedEvent> parse(Event event) {
        Matcher matcher = PATTERN.matcher(event.getTag());

        if (!matcher.matches()) {
            return Optional.empty();
        }

        Object innerData = event.getData().get("data");
        if (!(innerData instanceof Map)) {
            LOG.error("Error parsing ImageDeployedEvent: event parameter 'data' in invalid format or missing.");
            return Optional.empty();
        }

        Object grains = ((Map) innerData).get("grains");
        if (!(grains instanceof Map)) {
            LOG.error("Error parsing ImageDeployedEvent: event parameter 'grains' in invalid format or missing.");
            return Optional.empty();
        }

        return Optional.of(new ImageDeployedEvent(new ValueMap((Map<String, ?>) grains)));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("grains", grains)
                .toString();
    }
}
