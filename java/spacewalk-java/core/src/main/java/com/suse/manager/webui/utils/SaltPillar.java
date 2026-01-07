/*
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.webui.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * YAML generator for a Salt Pillar.
 */
public class SaltPillar implements SaltState {

    private Map<String, Object> data = new TreeMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Add an entry to the pillar
     * @param name the name of the entry
     * @param value the value
     */
    public void add(String name, Object value) {
        data.put(name, value);
    }

    /**
     * Adds all entries from the map passed as argument to the pillar
     * @param map mappings to be stored in the pillar
     */
    public void addAll(Map<String, Object> map) {
        data.putAll(map);
    }

    /**
     * Loads the pillar entries from a file in the disk
     * @param file the file containing the pillar entries
     * @throws FileNotFoundException if the file does not exist
     */
    @SuppressWarnings("unchecked")
    public void load(File file) throws FileNotFoundException {
        InputStream reader =  new FileInputStream(file);
        Yaml yaml = new Yaml();
        Map<String, Object> pillarData = yaml.loadAs(reader,  Map.class);

        if (pillarData != null && !pillarData.isEmpty()) {
            this.addAll(pillarData);
        }
    }

    public boolean isEmpty() {
        return this.data.isEmpty();
    }
}
