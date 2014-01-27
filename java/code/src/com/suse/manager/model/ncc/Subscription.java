/**
 * Copyright (c) 2014 SUSE
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

package com.suse.manager.model.ncc;

import org.simpleframework.xml.Element;

/**
 * Class representation of a NCC subscription.
 * TODO: Add getters and setters.
 */
public class Subscription {

    @Element
    private String subid;

    @Element
    private String regcode;

    @Element
    private String subname;

    @Element
    private String type;

    @Element
    private String substatus;

    @Element(name="start-date")
    private int startDate;

    @Element(name="end-date")
    private int endDate;

    @Element
    private int duration;

    @Element(name="server-class")
    private String serverClass;

    @Element(name="product-class")
    private String productClass;

    @Element
    private String productlist;

    @Element
    private int nodecount;

    @Element
    private int consumed;

    @Element(name="consumed-virtual")
    private int consumedVirtual;
}
