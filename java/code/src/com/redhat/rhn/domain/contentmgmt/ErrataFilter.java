/*
 * Copyright (c) 2019 SUSE LLC
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

import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Errata Filter
 */
@Entity
@DiscriminatorValue("errata")
public class ErrataFilter extends ContentFilter<Errata> {

    private Pattern pattern;

    @Override
    public boolean test(Errata erratum) {
        FilterCriteria.Matcher matcher = getCriteria().getMatcher();
        String field = getCriteria().getField();
        String value = getCriteria().getValue();

        switch (field) {
            case "issue_date":
                ZonedDateTime valDate = ZonedDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                ZonedDateTime issueDate = getField(erratum, field, ZonedDateTime.class);
                switch (matcher) {
                    case GREATEREQ:
                        return !issueDate.isBefore(valDate);
                    case GREATER:
                        return issueDate.isAfter(valDate);
                    default:
                        throw new UnsupportedOperationException("Matcher " + matcher + " not supported");
                }
            case "advisory_name":
                switch (matcher) {
                    case EQUALS:
                        return getField(erratum, field, String.class).equals(value);
                    case MATCHES:
                        if (pattern == null) {
                            pattern = Pattern.compile(value);
                        }
                        return pattern.matcher(getField(erratum, field, String.class)).matches();
                    default:
                        throw new UnsupportedOperationException("Matcher " + matcher + " not supported");
                }
            case "package_name":
                switch (matcher) {
                    case CONTAINS_PKG_NAME:
                        return erratum.getPackages().stream()
                                .anyMatch(p -> p.getPackageName().getName().equals(value));
                    case MATCHES_PKG_NAME:
                        if (pattern == null) {
                            pattern = Pattern.compile(value);
                        }
                        return erratum.getPackages().stream()
                                .anyMatch(p -> pattern.matcher(p.getPackageName().getName()).matches());
                    default:
                        throw new UnsupportedOperationException("Matcher " + matcher + " not supported");
                }
            case "package_nevr":
                List<String> split = Arrays.asList(value.split(" "));
                if (split.size() != 2) {
                    throw new IllegalArgumentException("Missing EVR in value");
                }
                String name = split.get(0);
                String evr = split.get(1);
                Stream<Package> pstream = erratum.getPackages().stream()
                        .filter(p -> p.getPackageName().getName().equals(name));
                switch (matcher) {
                    case CONTAINS_PKG_LT_EVR:
                        return pstream.anyMatch(p -> p.getPackageEvr().compareTo(
                                PackageEvr.parsePackageEvr(p.getPackageEvr().getPackageType(), evr)) < 0);
                    case CONTAINS_PKG_LE_EVR:
                        return pstream.anyMatch(p -> p.getPackageEvr().compareTo(
                                PackageEvr.parsePackageEvr(p.getPackageEvr().getPackageType(), evr)) <= 0);
                    case CONTAINS_PKG_EQ_EVR:
                        return pstream.anyMatch(p -> p.getPackageEvr().compareTo(
                                PackageEvr.parsePackageEvr(p.getPackageEvr().getPackageType(), evr)) == 0);
                    case CONTAINS_PKG_GE_EVR:
                        return pstream.anyMatch(p -> p.getPackageEvr().compareTo(
                                PackageEvr.parsePackageEvr(p.getPackageEvr().getPackageType(), evr)) >= 0);
                    case CONTAINS_PKG_GT_EVR:
                        return pstream.anyMatch(p -> p.getPackageEvr().compareTo(
                                PackageEvr.parsePackageEvr(p.getPackageEvr().getPackageType(), evr)) > 0);
                    default:
                        throw new UnsupportedOperationException("Matcher " + matcher + " not supported");
                }
            case "advisory_type":
                switch (matcher) {
                    case EQUALS:
                        return getField(erratum, field, String.class).equals(value);
                    default:
                        throw new UnsupportedOperationException("Matcher " + matcher + " not supported");
                }
            case "synopsis":
                switch (matcher) {
                    case EQUALS:
                        return getField(erratum, field, String.class).equals(value);
                    case CONTAINS:
                        return getField(erratum, field, String.class).contains(value);
                    case MATCHES:
                        if (pattern == null) {
                            pattern = Pattern.compile(value);
                        }
                        return pattern.matcher(getField(erratum, field, String.class)).matches();
                    default:
                        throw new UnsupportedOperationException("Matcher " + matcher + " not supported");
                }
            case "keyword":
                switch (matcher) {
                    case CONTAINS:
                        return erratum.hasKeyword(value);
                    default:
                        throw new UnsupportedOperationException("Matcher " + matcher + " not supported");
                }
            case "package_provides_name":
                switch (matcher) {
                case CONTAINS_PROVIDES_NAME:
                    return erratum.getPackages().stream()
                            .flatMap(pkg -> pkg.getProvides().stream())
                            .map(p -> p.getCapability().getName())
                            .anyMatch(n -> n.equals(value));
                default:
                    throw new UnsupportedOperationException("Matcher " + matcher + " not supported");
                }
            default:
                throw new UnsupportedOperationException("Field " + field + " not supported");
        }
    }

    private static <T> T getField(Errata erratum, String field, Class<T> type) {
        switch (field) {
            case "advisory_name":
                return type.cast(erratum.getAdvisoryName());
            case "advisory_type":
                return type.cast(erratum.getAdvisoryType());
            case "issue_date":
                return type.cast(erratum.getIssueDate().toInstant().atZone(ZoneId.systemDefault()));
            case "synopsis":
                return type.cast(erratum.getSynopsis());
            default:
                throw new UnsupportedOperationException("Field " + field + " not supported");
        }
    }

    @Override
    @Transient
    public EntityType getEntityType() {
        return EntityType.ERRATUM;
    }

    @Override
    public Optional<PackageFilter> asPackageFilter() {
        return Optional.empty();
    }

    @Override
    public Optional<ErrataFilter> asErrataFilter() {
        return Optional.of(this);
    }

    @Override
    public Optional<ModuleFilter> asModuleFilter() {
        return Optional.empty();
    }
}
