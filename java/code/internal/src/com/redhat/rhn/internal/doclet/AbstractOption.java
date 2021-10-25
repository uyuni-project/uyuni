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
package com.redhat.rhn.internal.doclet;

import java.util.List;

import jdk.javadoc.doclet.Doclet;

/**
 * Doclet option implementation
 */
public abstract class AbstractOption implements Doclet.Option {
    private final String name;
    private final boolean hasArg;
    private final String description;
    private final String parameters;

    /**
     * Constructor
     *
     * @param nameIn option name
     * @param hasArgIn true if the option has an argument, false otherwise
     * @param descriptionIn help text for the option
     * @param parametersIn parameters names
     */
    public AbstractOption(String nameIn,
            boolean hasArgIn,
            String descriptionIn,
            String parametersIn) {
        this.name = nameIn;
        this.hasArg = hasArgIn;
        this.description = descriptionIn;
        this.parameters = parametersIn;
    }

    @Override
    public int getArgumentCount() {
        return hasArg ? 1 : 0;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Kind getKind() {
        return Kind.STANDARD;
    }

    @Override
    public List<String> getNames() {
        return List.of(name);
    }

    @Override
    public String getParameters() {
        return hasArg ? parameters : null;
    }
}
