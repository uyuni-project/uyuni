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

package com.suse.manager.webui.utils;

import static java.util.Collections.singletonMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * YAML generator for the Salt cmd script state.
 */
public class SaltCmdScript extends AbstractSaltRequisites implements IdentifiableSaltState {

    private String id;
    private String source;

    /**
     * Constructor
     * @param idIn state id
     * @param sourceIn script source path
     */
    public SaltCmdScript(String idIn, String sourceIn) {
        this.id = idIn;
        this.source = sourceIn;
    }

    @Override
    public Map<String, Object> getData() {
        List<Map<String, ?>> arguments = new ArrayList<>();
        arguments.add(singletonMap("source", source));
        addRequisites(arguments);
        return singletonMap(id,
                singletonMap("cmd.script", arguments)
        );
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String stateId) {
        this.id = stateId;
    }
}
