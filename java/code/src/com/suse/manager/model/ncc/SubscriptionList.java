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

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * List of subscriptions as parsed from NCC.
 */
@Root(name = "subscriptionlist")
public class SubscriptionList {

    @Attribute
    private String lang;

    @Element
    private String authuser;

    @Element(required = false)
    private String smtguid;

    @ElementList(name = "subscription", inline = true, required = false)
    private List<Subscription> subscriptions;

    /**
     * Return the list of {@link Subscription} oblects.
     * @return subscriptions
     */
    public List<Subscription> getSubscriptions() {
        if (subscriptions == null) {
            subscriptions = new ArrayList<Subscription>();
        }
        return subscriptions;
    }
}
