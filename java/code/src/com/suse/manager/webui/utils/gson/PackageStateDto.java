/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.PackageStates;
import com.redhat.rhn.domain.state.VersionConstraints;
import com.redhat.rhn.manager.rhnpackage.PackageManager;

import org.apache.log4j.Logger;

import java.util.Optional;

/**
 * Transfer object for serializing package states as JSON.
 */
public class PackageStateDto {

    private static final Logger LOG = Logger.getLogger(PackageStateDto.class);

    private final String name;
    private final String evr;
    private final String arch;
    private final Optional<Integer> packageStateId;
    private final Optional<Integer> versionConstraintId;

    /**
     * @param nameIn the package name
     * @param evrIn the package evr
     * @param archIn the package arch
     * @param packageStateIdIn the state type id
     * @param versionConstraintIdIn the version constraint id
     */
    public PackageStateDto(String nameIn, String evrIn, String archIn,
            Optional<Integer> packageStateIdIn, Optional<Integer> versionConstraintIdIn) {
        this.name = nameIn;
        this.evr = evrIn;
        this.arch = archIn;
        this.packageStateId = packageStateIdIn;
        this.versionConstraintId = versionConstraintIdIn;
    }

    /**
     * @param nameIn the package name
     * @param evrIn the package evr
     * @param archIn the package arch
     */
    public PackageStateDto(String nameIn, String evrIn, String archIn) {
        this.name = nameIn;
        this.evr = evrIn;
        this.arch = archIn;
        this.packageStateId = Optional.empty();
        this.versionConstraintId = Optional.empty();
    }

    /**
     * @return the package name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the package evr
     */
    public String getEvr() {
        return evr;
    }

    /**
     * @return the package arch
     */
    public String getArch() {
        return arch;
    }

    /**
     * @return the state type id
     */
    public Optional<Integer> getPackageStateId() {
        return packageStateId;
    }

    /**
     * @return the version constraint id
     */
    public Optional<Integer> getVersionConstraintId() {
        return versionConstraintId;
    }

    /**
     * Convert this object to a {@link PackageState} object to be persisted.
     *
     * @return this object as a PackageState
     */
    public Optional<PackageState> convertToPackageState() {
        Optional<PackageStates> state = getPackageStateId().flatMap(PackageStates::byId);

        // Create the return object only if we have a valid state
        return state.flatMap(ps -> {
            PackageState packageState = new PackageState();
            packageState.setPackageState(ps);
            packageState.setName(PackageManager.lookupPackageName(getName()));
            // TODO: state.setEvr(PackageEvrFactory.lookupOrCreatePackageEvr(e, v, r));
            packageState.setArch(PackageFactory.lookupPackageArchByLabel(getArch()));

            // Some package states *require* a version constraint
            Optional<VersionConstraints> versionConstraint = getVersionConstraintId()
                    .flatMap(VersionConstraints::byId);
            if (PackageStates.requiresVersionConstraint(ps) &&
                    !versionConstraint.isPresent()) {
                LOG.error("Version constraint required for " + ps + ": " +
                        packageState.getName());
                return Optional.empty();
            }
            else {
                versionConstraint.ifPresent(packageState::setVersionConstraint);
                return Optional.of(packageState);
            }
        });
    }
}
