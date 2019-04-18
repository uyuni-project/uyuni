/**
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

import java.util.Optional;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Errata Filter
 */
@Entity
@DiscriminatorValue("errata")
public class ErrataFilter extends ContentFilter<Errata> {

    @Override
    public boolean testInternal(Errata errata) {
        return false;
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
}
