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
package com.redhat.rhn.domain.action.virtualization;

import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.virtualization.PoolSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Represents the virtual storage source parameters for the storage pool creation action.
 * Note that to save database fields this is stored as a json string in the database.
 */
public class VirtualizationPoolCreateActionSource extends PoolSource {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .create();

    /**
     * Create from a serialized string.
     *
     * @param json the serialized string as output by toString()
     * @return the parsed object
     */
    public static VirtualizationPoolCreateActionSource parse(String json) {
        return GSON.fromJson(json, new TypeToken<VirtualizationPoolCreateActionSource>() { }.getType());
    }

    /**
     * Serializes into a json string
     * {@inheritDoc}
     */
    public String toString() {
        return GSON.toJson(this);
    }
}
