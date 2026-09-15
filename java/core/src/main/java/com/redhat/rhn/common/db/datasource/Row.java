/*
 * Copyright (c) 2022 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.common.db.datasource;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic query result row
 */
public class Row extends HashMap<String, Object> {

    /**
     * Build an empty row
     */
    public Row() {
        super();
    }

    /**
     * Build a row with initial data
     *
     * @param data data to create the row with
     */
    public Row(Map<String, Object> data) {
        super(data);
    }
}
