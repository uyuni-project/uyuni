/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.utils;

import static com.suse.utils.Fn.applyIf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.reactor.utils.ZonedDateTimeISOAdapter;
import com.suse.manager.webui.utils.salt.custom.ChecksumAdapter;
import com.suse.manager.webui.utils.salt.custom.ImageChecksum.Checksum;
import com.suse.salt.netapi.parser.XorTypeAdapterFactory;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Function;

/**
 * Helper for composing json extractors to extract a single element from a json tree
 * without converting the whole structure to a class.
 */
public class Json {

   private Json() {
   }

   public static final Gson GSON = new GsonBuilder()
           .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
           .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeISOAdapter())
           .registerTypeAdapter(Checksum.class, new ChecksumAdapter())
           .registerTypeAdapterFactory(new XorTypeAdapterFactory())
           .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
           .create();

   /**
    * @param element json element
    * @return optional containing the JsonObject if element was one.
     */
   public static Optional<JsonObject> asObj(JsonElement element) {
      return applyIf(element, JsonElement::isJsonObject, JsonElement::getAsJsonObject);
   }

    /**
     * @param element json element
     * @return optional containing the JsonPrimitive if element was one.
     */
   public static Optional<JsonPrimitive> asPrim(JsonElement element) {
      return applyIf(element, JsonElement::isJsonPrimitive,
              JsonElement::getAsJsonPrimitive);
   }

    /**
     * @param element json primitive
     * @return optional containing the Long if element was one.
     */
   public static Optional<Long> asLong(JsonPrimitive element) {
      return applyIf(element, JsonPrimitive::isNumber, JsonPrimitive::getAsLong);
   }

    /**
     * @param field filed to extract
     * @return a function that given a JsonObject will try to extract
     * the field into an Optional
     */
   public static Function<JsonObject, Optional<JsonElement>> getField(String field) {
      return element -> Optional.ofNullable(element.get(field));
   }


}
