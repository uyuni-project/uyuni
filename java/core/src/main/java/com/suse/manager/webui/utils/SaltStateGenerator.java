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

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Helper class for generating salt SLS files.
 */
public class SaltStateGenerator {
    private final Writer destination;

    /**
     * Constructor.
     *
     * @param destinationIn writer to write the state to
     */
    public SaltStateGenerator(Writer destinationIn) {
        this.destination = destinationIn;
    }

    /**
     * Constructor.
     *
     * @param destinationIn writer to write the state to
     * @throws IOException when file io fails
     */
    public SaltStateGenerator(File destinationIn)
            throws IOException {
        this.destination = new FileWriter(destinationIn);
    }

    /**
     * Generate the YAML.
     *
     * @param states the states to output
     *
     */
    public void generate(SaltState... states) {
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
