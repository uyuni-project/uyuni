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

import java.util.Date;

import org.simpleframework.xml.Element;

/**
 * Class representation of a NCC subscription.
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

    @Element(name = "start-date")
    private long startDate;

    @Element(name = "end-date")
    private long endDate;

    @Element
    private long duration;

    @Element(name = "server-class")
    private String serverClass;

    @Element(name = "product-class")
    private String productClass;

    @Element
    private String productlist;

    @Element
    private int nodecount;

    @Element
    private int consumed;

    @Element(name = "consumed-virtual")
    private int consumedVirtual;

    /**
     * @return the subid
     */
    public String getSubid() {
        return subid;
    }

    /**
     * @return the regcode
     */
    public String getRegcode() {
        return regcode;
    }

    /**
     * @return the subname
     */
    public String getSubname() {
        return subname;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the substatus
     */
    public String getSubstatus() {
        return substatus;
    }

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return new Date(startDate * 1000);
    }

    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return new Date(endDate * 1000);
    }

    /**
     * @return the duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     * @return the serverClass
     */
    public String getServerClass() {
        return serverClass;
    }

    /**
     * @return the productClass
     */
    public String getProductClass() {
        return productClass;
    }

    /**
     * @return the productlist
     */
    public String getProductlist() {
        return productlist;
    }

    /**
     * @return the nodecount
     */
    public int getNodecount() {
        return nodecount;
    }

    /**
     * @return the consumed
     */
    public int getConsumed() {
        return consumed;
    }

    /**
     * @return the consumedVirtual
     */
    public int getConsumedVirtual() {
        return consumedVirtual;
    }
}
