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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.servlets.ajax.dto;

import java.util.List;

/**
 * DTO class used by {@link com.redhat.rhn.frontend.servlets.ajax.AjaxHandlerServlet}
 * when processing requests addressed to
 * {@link com.redhat.rhn.frontend.action.schedule.ActionChainSaveAction#save}
 */
public class ActionChainSaveDto {

    private Long actionChainId;
    private String label;
    private List<Long> deletedEntries;
    private List<Integer> deletedSortOrders;
    private List<Integer> reorderedSortOrders;

    public Long getActionChainId() {
        return actionChainId;
    }

    public String getLabel() {
        return label;
    }

    public List<Long> getDeletedEntries() {
        return deletedEntries;
    }

    public List<Integer> getDeletedSortOrders() {
        return deletedSortOrders;
    }

    public List<Integer> getReorderedSortOrders() {
        return reorderedSortOrders;
    }
}
