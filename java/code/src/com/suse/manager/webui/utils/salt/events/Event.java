/**
 * CHECKSTYLE:OFF
 */
package com.suse.manager.webui.utils.salt.events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.reactor.utils.ZonedDateTimeISOAdapter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Parse events into objects.
 */
public class Event {

    private String tag;
    private JsonElement data;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .create();

    /**
     * Return this event's tag.
     * @return the tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * Return this event's data.
     * @return the data
     */
    public <R> R getData(TypeToken<R> dataType) {
        return GSON.fromJson(data, dataType.getType());
    }

    /**
     * Return this event's data parsed into the given type.
     * @return the data
     */
    public <R> R getData(Class<R> dataType) {
        return GSON.fromJson(data, dataType);
    }

    /**
     * Return this event's data as a Map<String, Object>.
     * @return the data
     */
    public Map<String, Object> getData() {
        TypeToken<Map<String, Object>> typeToken = new TypeToken<Map<String, Object>>() {};
        return GSON.fromJson(data, typeToken.getType());
    }
}
