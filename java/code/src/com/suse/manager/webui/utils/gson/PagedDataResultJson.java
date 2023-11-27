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

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Immutable JSON wrapper class to hold paginated data.
 *
 * @param <T> the type of the data items
 * @param <K> the type of the identifiers
 */
public class PagedDataResultJson<T, K> {

    private final List<T> items;
    private final long total;

    private final Set<K> selectedIds;

    /**
     * Create an instance from a {@link DataResult}
     *
     * @param data the current page of data
     * @param selectedIdsIn list of SSM selected items
     */
    public PagedDataResultJson(DataResult<T> data, Set<K> selectedIdsIn) {
        this(data, data.getTotalSize(), selectedIdsIn);
    }

    /**
     * Create an instance from a list and the total number of items
     *
     * @param data the current page of data
     * @param totalIn total number of items
     * @param selectedIdsIn list of SSM selected items
     */
    public PagedDataResultJson(List<T> data, long totalIn, Set<K> selectedIdsIn) {
        items = data;
        total = totalIn;
        selectedIds = selectedIdsIn;
    }

    /**
     * Return the list of items of this result set. They list may be partial as the full result set is paged.
     * @return the list of items
     */
    public List<T> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * The total items of this result set.
     * @return the total number of items in the result set.
     */
    public long getTotal() {
        return total;
    }

    /**
     * The set of ids of the items current selected.
     * @return the set of selected item ids
     */
    public Set<K> getSelectedIds() {
        return Collections.unmodifiableSet(selectedIds);
    }
}
