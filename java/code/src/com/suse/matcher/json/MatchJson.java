/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.matcher.json;

/**
 * JSON representation of a match.
 */
public class MatchJson {

    /** The system id. */
    private Long systemId;

    /** The subscription id. */
    private Long subscriptionId;

    /** The product id. */
    private Long productId;

    /** The number of subscription cents used in this match. */
    private Integer cents;

    /** True if this match has been confirmed, false if it is possible but not confirmed. */
    private Boolean confirmed;

    /**
     * Standard constructor.
     *
     * @param systemIdIn the system id
     * @param subscriptionIdIn the subscription id
     * @param productIdIn the product id
     * @param centsIn the number of subscription cents used in this match
     * @param confirmedIn whether this match has been confirmed or not
     */
    public MatchJson(Long systemIdIn, Long subscriptionIdIn, Long productIdIn,
                     Integer centsIn, Boolean confirmedIn) {
        systemId = systemIdIn;
        subscriptionId = subscriptionIdIn;
        productId = productIdIn;
        cents = centsIn;
        confirmed = confirmedIn;
    }

    /**
     * Gets the system id.
     *
     * @return the system id
     */
    public Long getSystemId() {
        return systemId;
    }

    /**
     * Sets the system id.
     *
     * @param systemIdIn the new system id
     */
    public void setSystemId(Long systemIdIn) {
        systemId = systemIdIn;
    }

    /**
     * Gets the subscription id.
     *
     * @return the subscription id
     */
    public Long getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Sets the subscription id.
     *
     * @param subscriptionIdIn the new subscription id
     */
    public void setSubscriptionId(Long subscriptionIdIn) {
        subscriptionId = subscriptionIdIn;
    }

    /**
     * Gets the product id.
     *
     * @return the product id
     */
    public Long getProductId() {
        return productId;
    }

    /**
     * Sets the product id.
     *
     * @param productIdIn the new product id
     */
    public void setProductId(Long productIdIn) {
        productId = productIdIn;
    }

    /**
     * Gets the number of subscription cents used in this match.
     *
     * @return the number of subscription cents used in this match
     */
    public Integer getCents() {
        return cents;
    }

    /**
     * Sets the number of subscription cents used in this match.
     *
     * @param centsIn the new number of subscription cents used in this match
     */
    public void setCents(Integer centsIn) {
        cents = centsIn;
    }

    /**
     * Checks if is confirmed.
     *
     * @return the boolean
     */
    public Boolean getConfirmed() {
        return confirmed;
    }

    /**
     * Sets this match confirmed.
     *
     * @param confirmedIn the new confirmed value
     */
    public void setConfirmed(Boolean confirmedIn) {
        confirmed = confirmedIn;
    }
}
