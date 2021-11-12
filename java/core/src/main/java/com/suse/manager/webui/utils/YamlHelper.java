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
package com.suse.manager.webui.utils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Helper class for serializing objects into YAML.
 */
public enum YamlHelper {
    /**
     * Singleton instance
     */
    INSTANCE;

    private final DumperOptions options;

    YamlHelper() {
        // Configure the YAML output here
        options = new DumperOptions();
        options.setIndent(4);
        options.setWidth(150);
        options.setAllowUnicode(true);
        options.setPrettyFlow(true);
        options.setLineBreak(DumperOptions.LineBreak.UNIX);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setCanonical(false);
    }

    /**
     * Dump a given object into Yaml.
     *
     * @param object the object to dump
     * @return the YAML representation of the given object
     */
    public String dump(Object object) {
        Yaml yaml = new Yaml(options);
        return yaml.dump(object);
    }
}
