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

package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.common.db.datasource.DataResult;

import java.util.List;

/**
 * JSON wrapper class to hold paginated data
 *
 * @param <T> the type of the data items
 */
public class PagedDataResultJson<T> {

    private List<T> items;
    private long total;

    /**
     * Create an instance from a {@link DataResult}
     *
     * @param data the current page of data
     */
    public PagedDataResultJson(DataResult<T> data) {
        this(data, data.getTotalSize());
    }

    /**
     * Create an instance from a list and the total number of items
     *
     * @param data the current page of data
     * @param totalIn total number of items
     */
    public PagedDataResultJson(List<T> data, long totalIn) {
        items = data;
        total = totalIn;
    }
}
