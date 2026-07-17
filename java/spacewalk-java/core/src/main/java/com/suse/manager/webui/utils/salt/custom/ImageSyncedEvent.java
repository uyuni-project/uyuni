/*
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.webui.utils.salt.custom;

import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.parser.JsonParser;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Represents an event fired when a minion connects to the salt master
 */
public class ImageSyncedEvent {

    private static final Pattern PATTERN = Pattern.compile("^suse/manager/image_synced");

    private static final Gson GSON = JsonParser.GSON;

    private final String minionId;
    private final JsonElement data;

    /**
     * Creates a new ImageSyncedEvent
     * @param minionId the id of the minion sending the event
     * @param data data containing more information about this event
     */
    private ImageSyncedEvent(String minionIdIn, JsonElement dataIn) {
        this.minionId = minionIdIn;
        this.data = dataIn;
    }

    /**
     * The id of the minion that started
     *
     * @return the minion id
     */
    public String getMinionId() {
        return minionId;
    }

    public String getAction() {
        return data.getAsJsonObject().get("data").getAsJsonObject().get("action").getAsString();
    }

    public String getBranch() {
        return data.getAsJsonObject().get("data").getAsJsonObject().get("branch").getAsString();
    }

    public String getImageName() {
        return data.getAsJsonObject().get("data").getAsJsonObject().get("image_name").getAsString();
    }

    public String getImageVersion() {
        return data.getAsJsonObject().get("data").getAsJsonObject().get("image_version").getAsString();
    }

    /**
     * Return the event data parsed into the given type.
     * @param type type token to parse data
     * @param <R> type to parse the data into
     * @return the event data
     */
    public <R> R getData(TypeToken<R> type) {
        return GSON.fromJson(data, type.getType());
    }

    /**
     * Return event data as Map
     * @return event data as map
     */
    public Map<String, Object> getData() {
        TypeToken<Map<String, Object>> typeToken = new TypeToken<>() {
        };
        return getData(typeToken);
    }

    /**
     * Utility method to parse e generic event to a more specific one
     * @param event the generic event to parse
     * @return an option containing the parsed value or non if it could not be parsed
     */
    public static Optional<ImageSyncedEvent> parse(Event event) {
        Matcher matcher = PATTERN.matcher(event.getTag());
        if (matcher.matches()) {
            ImageSyncedEvent result = new ImageSyncedEvent(
                (String) event.getData().get("id"),
                event.getData(JsonElement.class)
            );
            return Optional.of(result);
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("minionId", minionId)
                .append("data", data)
                .toString();
    }

}
