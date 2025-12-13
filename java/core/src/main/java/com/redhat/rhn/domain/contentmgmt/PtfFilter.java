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

package com.redhat.rhn.domain.contentmgmt;

import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageProperty;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.function.Predicate;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("ptf")
public class PtfFilter extends ContentFilter {

    public static final String FIELD_PTF_ALL = "ptf_all";
    public static final String FIELD_PTF_NUMBER = "ptf_number";
    public static final String FIELD_PTF_PACKAGE = "ptf_package";

    private static final Logger LOGGER = LogManager.getLogger(PtfFilter.class);

    @Override
    public boolean test(Object o) {
        if (!(o instanceof Package pack)) {
            return false;
        }
        // If the package is neither a ptf nor part of a ptf we cannot have a match
        if (!pack.isMasterPtfPackage() && !pack.isPartOfPtf()) {
            return false;
        }

        FilterCriteria.Matcher matcher = getCriteria().getMatcher();
        String field = getCriteria().getField();
        String value = getCriteria().getValue();

        if (FIELD_PTF_ALL.equals(field)) {
            // If we filter all we have already a match
            return true;
        }

        if (FIELD_PTF_NUMBER.equals(field)) {
            if (!NumberUtils.isParsable(value)) {
                LOGGER.warn("Filter value {} is invalid for field ptf_number", value);
                return false;
            }

            String numberPart = findPtfNumber(pack);
            if (numberPart == null) {
                // No ptf number found, something wrong as the package should be already a ptf here
                LOGGER.warn("Unable to retrieve ptf number from package #{}", pack.getId());
                return false;
            }

            long ptfNumber = Long.parseLong(numberPart);
            long threshold = Long.parseLong(value);
            switch (matcher) {
                case LOWER:
                    return ptfNumber < threshold;
                case LOWEREQ:
                    return ptfNumber <= threshold;
                case EQUALS:
                    return ptfNumber == threshold;
                case GREATEREQ:
                    return ptfNumber >= threshold;
                case GREATER:
                    return ptfNumber > threshold;
                default:
                    throw new UnsupportedOperationException("Matcher " + matcher + " not supported for ptf_number");
            }
        }

        if (FIELD_PTF_PACKAGE.equals(field)) {
            switch (matcher) {
                case EQUALS:
                    return findFixedPackage(pack, name -> name.equals(value));
                case MATCHES:
                    return findFixedPackage(pack, name -> name.matches(value));
                case CONTAINS:
                    return findFixedPackage(pack, name -> name.contains(value));
                default:
                    throw new UnsupportedOperationException("Matcher " + matcher + " not supported for ptf_package");
            }
        }

        throw new UnsupportedOperationException("Field " + field + " not supported");
    }

    @Override
    @Transient
    public EntityType getEntityType() {
        return EntityType.PTF;
    }

    private static String findPtfNumber(Package pack) {
        // Decide where to look based on the package type
        Set<? extends PackageProperty> properties;
        if (pack.isMasterPtfPackage()) {
            properties = pack.getProvides();
        }
        else if (pack.isPartOfPtf()) {
            properties = pack.getRequires();
        }
        else {
            return null;
        }

        // Search for the property in the form ptf-<number>
        return properties.stream()
                         .map(property -> property.getCapability().getName())
                         .filter(name -> name.matches("ptf-[1-9]\\d*"))
                         .findFirst()
                         .map(name -> name.split("-")[1])
                         .orElse(null);
    }

    private static boolean findFixedPackage(Package pack, Predicate<String> namePredicate) {
        // Decide where to look based on the package type
        Set<? extends PackageProperty> properties;
        if (pack.isMasterPtfPackage()) {
            properties = pack.getRequires();
        }
        else if (pack.isPartOfPtf()) {
            properties = pack.getProvides();
        }
        else {
            return false;
        }

        // Search for the property matching the package filter
        return properties.stream()
                         .map(property -> property.getCapability().getName())
                         .anyMatch(namePredicate);
    }

}
