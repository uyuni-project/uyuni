/**
 * Copyright (c) 2015--2018 SUSE LLC
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

package com.suse.scc.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


/**
 * This is an Order parsed from JSON coming from SCC
 */
public class SCCOrderJson {

    @SerializedName("order_number")
    private long orderNumber;
    @SerializedName("order_items")
    private List<SCCOrderItemJson> orderItems = new ArrayList<>();

    /**
     * @return the orderNumber
     */
    public long getOrderNumber() {
        return orderNumber;
    }

    /**
     * @return the orderItems
     */
    public List<SCCOrderItemJson> getOrderItems() {
        return orderItems;
    }
}
