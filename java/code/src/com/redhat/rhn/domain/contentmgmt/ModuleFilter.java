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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Optional;

/**
 * Package Filter
 */
@Entity
@DiscriminatorValue("module")
public class ModuleFilter extends ContentFilter<Module> {

    @Override
    public boolean test(Module module) {
        FilterCriteria.Matcher matcher = getCriteria().getMatcher();
        String field = getCriteria().getField();
        String value = getCriteria().getValue();

        if (!matcher.equals(FilterCriteria.Matcher.EQUALS)) {
            throw new UnsupportedOperationException("Matcher " + matcher + " not supported");
        }

        switch (field) {
            case "module":
                return module.getName().equals(value);
            case "stream":
                return module.getStream().equals(value);
            default:
                throw new UnsupportedOperationException("Field " + field + " not supported");
        }
    }

    @Override
    @Transient
    public EntityType getEntityType() {
        return EntityType.MODULE;
    }

    @Override
    public Optional<PackageFilter> asPackageFilter() {
        return Optional.empty();
    }

    @Override
    public Optional<ErrataFilter> asErrataFilter() {
        return Optional.empty();
    }

    @Override
    public Optional<ModuleFilter> asModuleFilter() {
        return Optional.of(this);
    }
}
