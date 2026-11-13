/*
 * Copyright (c) 2015 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.domain.rhnpackage.PackageEvr;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Optional;

/**
 * JSON representation of a package state.
 */
public class PackageStateJson {

    /** Name of the package */
    private final String name;

    /** Package epoch */
    private final String epoch;

    /** Package version */
    private final String version;

    /** Package release */
    private final String release;

    /** Package architecture */
    private final String arch;

    /** Id to represent the state of the package */
    private final Optional<Integer> packageStateId;

    /** Id to represent a version constraint as part of the state */
    private final Optional<Integer> versionConstraintId;

    /** Default constructor used for Gson parsing */
    public PackageStateJson() {
        this.name = null;
        this.epoch = null;
        this.version = null;
        this.release = null;
        this.arch = null;
        this.packageStateId = Optional.empty();
        this.versionConstraintId = Optional.empty();
    }

    /**
     * @param nameIn the package name
     * @param evrIn the package evr
     * @param archIn the package arch
     * @param packageStateIdIn the state type id
     * @param versionConstraintIdIn the version constraint id
     */
    public PackageStateJson(String nameIn, PackageEvr evrIn, String archIn,
                            Optional<Integer> packageStateIdIn, Optional<Integer> versionConstraintIdIn) {
        this.name = nameIn;
        this.epoch = evrIn.getEpoch();
        this.version = evrIn.getVersion();
        this.release = evrIn.getRelease();
        this.arch = archIn;
        this.packageStateId = packageStateIdIn;
        this.versionConstraintId = versionConstraintIdIn;
    }

    /**
     * @param nameIn the package name
     * @param evrIn the package evr
     * @param archIn the package arch
     */
    public PackageStateJson(String nameIn, PackageEvr evrIn, String archIn) {
        this.name = nameIn;
        this.epoch = evrIn.getEpoch();
        this.version = evrIn.getVersion();
        this.release = evrIn.getRelease();
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
     * @return the package epoch
     */
    public String getEpoch() {
        return epoch;
    }

    /**
     * @return the package version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return the package release
     */
    public String getRelease() {
        return release;
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
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PackageStateJson otherState)) {
            return false;
        }
        return new EqualsBuilder()
                .append(getName(), otherState.getName())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getName())
                .toHashCode();
    }
}
