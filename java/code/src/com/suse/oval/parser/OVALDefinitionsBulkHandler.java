/*
 * Copyright (c) 2024 SUSE LLC
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

package com.suse.oval.parser;

import com.suse.oval.ovaltypes.DefinitionType;

import java.util.List;

/**
 * A handler class to apply an operation (.e.g. write them to database), on every bulk/list of parsed OVAL definitions.
 * */
@FunctionalInterface
public interface OVALDefinitionsBulkHandler {
    /**
     * Handles the given bulk/list of OVAL definitions.
     *
     * @param bulk list of OVAL definitions.
     * */
    void handle(List<DefinitionType> bulk);
}
