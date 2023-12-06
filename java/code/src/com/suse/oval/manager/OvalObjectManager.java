/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.oval.manager;

import com.suse.oval.ovaltypes.ObjectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A cache for {@link ObjectType} to access OVAL objects quickly
 */
public class OvalObjectManager {
    private final Map<String, ObjectType> objectsMap = new HashMap<>();

    /**
     * Standard constructor
     *
     * @param objects the objects to store and lookup later
     * */
    public OvalObjectManager(List<ObjectType> objects) {
        for (ObjectType objectType : objects) {
            objectsMap.put(objectType.getId(), objectType);
        }
    }

    /**
     * Looks up an OVAL object with an id of {@code objectId}
     *
     * @param objectId the object id to look up
     * @return the cached {@link ObjectType} object that correspond to the object id
     * */
    public ObjectType get(String objectId) {
        ObjectType object = objectsMap.get(objectId);
        if (object == null) {
            throw new IllegalArgumentException("The object id is invalid: " + objectId);
        }
        return object;
    }
    /**
     * Check if an OVAL object with an id of {@code objectId} exists
     * */
    protected boolean exists(String objectId) {
        return objectsMap.containsKey(objectId);
    }
}
