package com.suse.oval.manager;

import com.suse.oval.ovaltypes.ObjectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OvalObjectManager {
    private final Map<String, ObjectType> objectsMap = new HashMap<>();

    public OvalObjectManager(List<ObjectType> objects) {
        for (ObjectType objectType : objects) {
            objectsMap.put(objectType.getId(), objectType);
        }
    }

    public ObjectType get(String objectId) {
        ObjectType object = objectsMap.get(objectId);
        if (object == null) {
            throw new IllegalArgumentException("The object id is invalid: " + objectId);
        }
        return object;
    }

    public boolean exists(String objectId) {
        return objectsMap.containsKey(objectId);
    }

    public void add(ObjectType objectType) {
        objectsMap.put(objectType.getId(), objectType);
    }
}
