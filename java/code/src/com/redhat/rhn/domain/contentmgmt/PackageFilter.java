/*
 * Copyright (c) 2019--2021 SUSE LLC
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
import com.redhat.rhn.domain.rhnpackage.PackageEvr;

import java.util.Optional;
import java.util.regex.Pattern;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Package Filter
 */
@Entity
@DiscriminatorValue("package")
public class PackageFilter extends ContentFilter<Package> {

    private Pattern pattern;

    @Override
    public boolean test(Package pack) {
        FilterCriteria.Matcher matcher = getCriteria().getMatcher();
        String field = getCriteria().getField();
        String value = getCriteria().getValue();

        switch (matcher) {
            case CONTAINS:
                return getField(pack, field, String.class).contains(value);
            case LOWER:
                return checkNameAndArch(field, value, pack) && pack.getPackageEvr().compareTo(
                        PackageEvr.parsePackageEvr(pack.getPackageType(), getEvr(field, value))) < 0;
            case LOWEREQ:
                return checkNameAndArch(field, value, pack) && pack.getPackageEvr().compareTo(
                        PackageEvr.parsePackageEvr(pack.getPackageType(), getEvr(field, value))) <= 0;
            case EQUALS:
                return getField(pack, field, String.class).equals(value);
            case GREATEREQ:
                return checkNameAndArch(field, value, pack) && pack.getPackageEvr().compareTo(
                        PackageEvr.parsePackageEvr(pack.getPackageType(), getEvr(field, value))) >= 0;
            case GREATER:
                return checkNameAndArch(field, value, pack) && pack.getPackageEvr().compareTo(
                        PackageEvr.parsePackageEvr(pack.getPackageType(), getEvr(field, value))) > 0;
            case MATCHES:
                if (pattern == null) {
                    pattern = Pattern.compile(value);
                }
                return pattern.matcher(getField(pack, field, String.class)).matches();
            case PROVIDES_NAME:
                return pack.getProvides().stream()
                        .map(p -> p.getCapability().getName())
                        .anyMatch(n -> n.equals(value));
            default:
                throw new UnsupportedOperationException("Matcher " + matcher + " not supported");
        }
    }

    private static <T> T getField(Package pack, String field, Class<T> type) {
        switch (field) {
            case "name":
                return type.cast(pack.getPackageName().getName());
            case "nevr":
                return type.cast(pack.getNameEvr());
            case "nevra":
                //Case for null epoch: Module metadata reports epoch as '0' even if there's none. We need to match it.
                // pack.getNameEvra() omits the epoch if null so instead, pack.getNevraWithEpoch() is used here.
                return type.cast(pack.getNevraWithEpoch());
            default:
                throw new UnsupportedOperationException("Field " + field + " not supported");
        }
    }

    private static boolean checkNameAndArch(String field, String value, Package pack) {
        if (field.equals("nevr")) {
            return value.replaceAll("(.*)-(.*:)?(.*)-(.*)", "$1").equals(pack.getPackageName().getName());
        }
        else if (field.equals("nevra")) {
            return value.replaceAll("(.*)-(.*:)?(.*)-(.*)\\.(.*)", "$1$5")
                    .equals(pack.getPackageName().getName() + pack.getPackageArch().getLabel());
        }
        else {
            throw new UnsupportedOperationException("Field " + field + " not supported for filter Package (NEVRA)");
        }
    }

    private static String getEvr(String field, String value) {
        if (field.equals("nevr")) {
            return value.replaceAll("(.*)-(.*:)?(.*)-(.*)", "$2$3-$4");
        }
        else if (field.equals("nevra")) {
            return value.replaceAll("(.*)-(.*:)?(.*)-(.*)\\.(.*)", "$2$3-$4");
        }
        else {
            throw new UnsupportedOperationException("Field " + field + " not supported for filter Package (NEVRA)");
        }
    }

    @Override
    @Transient
    public EntityType getEntityType() {
        return EntityType.PACKAGE;
    }

    @Override
    public Optional<PackageFilter> asPackageFilter() {
        return Optional.of(this);
    }

    @Override
    public Optional<ErrataFilter> asErrataFilter() {
        return Optional.empty();
    }

    @Override
    public Optional<ModuleFilter> asModuleFilter() {
        return Optional.empty();
    }
}
