/**
 * Copyright (c) 2018 SUSE LLC
 * <p>
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * <p>
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.manager.webui.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonMap;

public class SaltModuleRun extends AbstractSaltRequisites implements SaltState {
    private String id;
    private String name;
    private Map<String, ?> args;
    private Map<String, ?> kwargs;

    public SaltModuleRun(String id, String name, Map<String, ?> args) {
        this.id = id;
        this.name = name;
        this.args = args;
    }

    public SaltModuleRun(String id, String name, Map<String, ?> args, Map<String, ?> kwargs) {
        this.id = id;
        this.name = name;
        this.args = args;
        this.kwargs = kwargs;
    }

    @Override
    public Map<String, Object> getData() {
        List<Map<String, ?>> args = new ArrayList<>();
        args.add(singletonMap("name", name));

        if (this.args != null) {
            args.addAll(this.args.entrySet()
                    .stream()
                    .map(e -> singletonMap(e.getKey(), e.getValue()))
                    .collect(Collectors.toList())
            );
        }
        if (this.kwargs != null) {
            args.add(singletonMap("kwargs", this.kwargs));
        }

        addRequisites(args);

        return singletonMap(id,
                singletonMap("module.run", args)
        );
    }

    /**
     * @return id to get
     */
    public String getId() {
        return id;
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
