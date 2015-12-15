/**
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author bo
 */
public class SaltStateGenerator {
    private final Writer destination;

    /**
     * Constructor.
     *
     * @param destination
     */
    public SaltStateGenerator(Writer destination) {
        this.destination = destination;
    }

    /**
     * Constructor.
     *
     * @param destination
     * @throws IOException
     */
    public SaltStateGenerator(File destination)
            throws IOException {
        this.destination = new FileWriter(destination);
    }

    /**
     * Generate the YAML.
     *
     * @param state
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void generate(SaltState... states)
            throws IOException {
        DumperOptions setup = new DumperOptions();
        setup.setIndent(4);
        setup.setAllowUnicode(true);
        setup.setPrettyFlow(true);
        setup.setLineBreak(DumperOptions.LineBreak.UNIX);
        setup.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        setup.setCanonical(false);

        Yaml yaml = new Yaml(setup);
        for (SaltState state : states) {
            yaml.dump(state.getData(), destination);
        }
    }
}
