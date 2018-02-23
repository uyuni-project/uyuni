/**
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.common.validator;

import com.redhat.rhn.common.localization.LocalizationService;

import org.apache.log4j.Logger;

/**
 * <p>
 *  The <code>Constraint</code> class represents a single data constraint,
 *    including the data type, allowed values, and required ranges.
 * </p>
 * @version $Rev$
 */
public class DoubleConstraint extends RequiredIfConstraint {

    /** Logger instance */
    private static Logger log = Logger.getLogger(DoubleConstraint.class);

    /** Minimum inclusive value allowed */
    private Double minInclusive;

    /** Maximum inclusive value allowed */
    private Double maxInclusive;

    /**
     * <p>
     *  This will create a new <code>Constraints</code> with the specified
     *    identifier as the "name".
     * </p>
     *
     * @param identifierIn <code>String</code> identifier for <code>Constraint</code>.
     */
    public DoubleConstraint(String identifierIn) {
        super(identifierIn);
        this.minInclusive = Double.MIN_VALUE;
        this.maxInclusive = Double.MAX_VALUE;
    }


    /** {@inheritDoc} */
    public ValidatorError checkConstraint(Object value) {

        ValidatorError requiredCheck = super.checkConstraint(value);
        if (requiredCheck != null) {
            return requiredCheck;
        }

        String localizedIdentifier =
            LocalizationService.getInstance().getMessage(getIdentifier());

        // Validate against range specifications
        try {
            double doubleValue = new Double(value.toString()).doubleValue();
            // Now we know its a valid number, lets check for a decimal value
            if (value.toString().indexOf(".") > -1) {
                Object[] args = new Object[2];
                args[0] = localizedIdentifier;
                args[1] = getMinInclusive();
                return new ValidatorError("errors.decimalvalue", args);
            }

            if (doubleValue < getMinInclusive().doubleValue()) {
                log.debug("Decimal too small ...");
                Object[] args = new Object[2];
                args[0] = localizedIdentifier;
                args[1] = getMinInclusive();
                return new ValidatorError("errors.minsize", args);
            }
            if (doubleValue > getMaxInclusive().doubleValue()) {
                log.debug("Decimal too big ...");
                Object[] args = new Object[2];
                args[0] = localizedIdentifier;
                args[1] = getMaxInclusive();
                return new ValidatorError("errors.maxsize", args);
            }
        }
        catch (NumberFormatException e) {
            log.debug("NumberFormatException .. ");
            Object[] args = new Object[1];
            args[0] = localizedIdentifier;
            return new ValidatorError("errors.notanumber", args);

        }


        return null;
    }


    /**
     * <p>
     *  This will set the minimum allowed value for this data type (inclusive).
     * </p>
     *
     * @param minInclusiveIn minimum allowed value (inclusive)
     */
    public void setMinInclusive(Double minInclusiveIn) {
        this.minInclusive = minInclusiveIn;
    }

    /**
     * <p>
     *  This will return the minimum allowed value for this data type (inclusive).
     * </p>
     *
     * @return <code>Double</code> - minimum value allowed (inclusive)
     */
    public Double getMinInclusive() {
        return minInclusive;
    }


    /**
     * <p>
     *  This will set the maximum allowed value for this data type (inclusive).
     * </p>
     *
     * @param maxInclusiveIn maximum allowed value (inclusive)
     */
    public void setMaxInclusive(Double maxInclusiveIn) {
        this.maxInclusive = maxInclusiveIn;
    }

    /**
     * <p>
     *  This will return the maximum allowed value for this data type (inclusive).
     * </p>
     *
     * @return <code>Double</code> - maximum value allowed (inclusive)
     */
    public Double getMaxInclusive() {
        return maxInclusive;
    }

}
