package com.suse.manager.webui.utils;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by matei on 3/4/16.
 */
public class SaltPillar implements SaltState {

    private Map<String, Object> data = new TreeMap<>();

    @Override
    public Map<String, Object> getData() {
        return data;
    }

    public void add(String name, Object value) {
        data.put(name, value);
    }
}
