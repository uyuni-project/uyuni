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

import com.redhat.rhn.domain.contentmgmt.modulemd.Module;

import java.util.Optional;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * This filter must be applied on {@link Module} objects. In order to apply the filtering on
 * {@link com.redhat.rhn.domain.rhnpackage.Package} objects instead, these filters must be replaced with
 * {@link PackageFilter} counterparts.
 *
 * @see com.redhat.rhn.manager.contentmgmt.DependencyResolver#resolveModularDependencies
 */
@Entity
@DiscriminatorValue("module")
public class ModuleFilter extends ContentFilter<Module> {

    @Override
    public boolean test(Module module) {
        FilterCriteria.Matcher matcher = getCriteria().getMatcher();
        String field = getCriteria().getField();

        if (!matcher.equals(FilterCriteria.Matcher.EQUALS)) {
            throw new UnsupportedOperationException("Matcher " + matcher + " not supported");
        }

        Module moduleValue = getModule();
        if ("module_stream".equals(field)) {
            // Match every stream if no stream value is provided
            return moduleValue.getName().equals(module.getName()) && (moduleValue.getStream() == null || moduleValue
                    .getStream().equals(module.getStream()));
        }
        throw new UnsupportedOperationException("Field " + field + " not supported");
    }

    /**
     * Get the value of this filter as a {@link Module} instance
     * @return the module instance
     */
    @Transient
    public Module getModule() {
        String[] nameStreamPair = getCriteria().getValue().split(":", 2);
        return new Module(nameStreamPair[0], nameStreamPair.length > 1 ? nameStreamPair[1] : null);
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
