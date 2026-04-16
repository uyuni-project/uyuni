/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.webui.utils.salt.custom.coco;

import com.suse.salt.netapi.results.CmdResult;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.utils.Json;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CoCoAttestationResponseDataParser {

    protected final List<CoCoAbstractAttestationResponseData> chunks = new ArrayList<>();

    /**
     * Constructor
     */
    public CoCoAttestationResponseDataParser() {
        //empty constructor
    }

    /**
     * @param jsonResult dummy
     */
    public void parse(JsonElement jsonResult) {
        chunks.clear();
        chunks.add(Json.GSON.fromJson(jsonResult, CoCoAmdEpycAttestationResponseData.class));
        chunks.add(Json.GSON.fromJson(jsonResult, CoCoSecureBootAttestationResponseData.class));
        //add here further children of CoCoAbstractAttestationResponseData
    }

    /**
     * @return a map of tags to data items
     */
    public Map<String, Object> asMap() {
        Map<String, Object> out = new HashMap<>();
        chunks.forEach(c -> out.putAll(c.asMap()));
        return out;
    }

    /**
     * @param tagKey a tag representing a piece of response data
     * @return a response item associated to that tagKey
     */
    public Optional<StateApplyResult<CmdResult>> getResult(String tagKey) {

        return chunks.stream()
                .map(CoCoAbstractAttestationResponseData::getResults)
                .filter(r -> r.containsKey(tagKey))
                .findFirst()
                .flatMap(item -> item.get(tagKey));
    }

    /**
     * @param clazz a clazz
     * @return an CoCoAbstractAttestationResponseData object if any
     */
    public Optional<CoCoAbstractAttestationResponseData> getChunk(final Class<?> clazz) {
        return chunks.stream().filter(c -> c.getClass().equals(clazz)).findAny();
    }
}
