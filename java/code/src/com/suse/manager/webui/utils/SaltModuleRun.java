/**
 * Copyright (c) 2018 SUSE LLC
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonMap;

/**
 * Encapsulates the execution of a state.
 */
public class SaltModuleRun extends AbstractSaltRequisites implements IdentifiableSaltState {
    private String id;
    private String name;
    private Map<String, ?> args;
    private Map<String, ?> kwargs;

    /**
     * Standard constructor
     * @param idIn state id
     * @param nameIn module name
     * @param argsIn positional arguments
     */
    public SaltModuleRun(String idIn, String nameIn, Map<String, ?> argsIn) {
        this.id = idIn;
        this.name = nameIn;
        this.args = argsIn;
    }

    /**
     * Complete constructor
     * @param idIn state id
     * @param nameIn module name
     * @param argsIn positional arguments
     * @param kwargsIn keyword arguments
     */
    public SaltModuleRun(String idIn, String nameIn, Map<String, ?> argsIn, Map<String, ?> kwargsIn) {
        this.id = idIn;
        this.name = nameIn;
        this.args = argsIn;
        this.kwargs = kwargsIn;
    }

    @Override
    public Map<String, Object> getData() {
        List<Map<String, ?>> arguments = new ArrayList<>();
        arguments.add(singletonMap("name", name));

        if (this.args != null) {
            arguments.addAll(this.args.entrySet()
                    .stream()
                    .map(e -> singletonMap(e.getKey(), e.getValue()))
                    .collect(Collectors.toList())
            );
        }
        if (this.kwargs != null) {
            arguments.add(singletonMap("kwargs", this.kwargs));
        }

        addRequisites(arguments);

        return singletonMap(id,
                singletonMap("module.run", arguments)
        );
    }

    /**
     * @return id to get
     */
    public String getId() {
        return id;
    }

    /**
     * @param idIn to set
     */
    public void setId(String idIn) {
        this.id = idIn;
    }

    /**
     * @return name to get
     */
    public String getName() {
        return name;
    }

    /**
     * @return kwargs to get
     */
    public Map<String, ?> getKwargs() {
        return kwargs;
    }

    /**
     * @return params to get
     */
    public Map<String, ?> getArgs() {
        return args;
    }

}
