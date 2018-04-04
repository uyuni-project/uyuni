/**
 * Copyright (c) 2017 SUSE LLC
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
package com.redhat.rhn.domain.server;

/**
 * Wraps an errata name and its update stack bit.
 */
public class ErrataInfo {

    /** The errata name. */
    private String name;

    /** The update stack bit. */
    private boolean updateStack;

    /** The include Salt bit. */
    private boolean includeSalt;

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if is update stack.
     *
     * @return true, if is update stack
     */
    public boolean isUpdateStack() {
        return updateStack;
    }

    /**
     * Checks if errata includes Salt upgrade.
     *
     * @return true, if errata includes Salt upgrade.
     */
    public boolean includeSalt() {
        return includeSalt;
    }

    /**
     * Instantiates a new errata info.
     *
     * @param nameIn the name in
     * @param updateStackIn the update stack in
     */
    public ErrataInfo(String nameIn, boolean updateStackIn, boolean includeSaltIn) {
        name = nameIn;
        updateStack = updateStackIn;
        includeSalt = includeSaltIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object objIn) {
        if (!(objIn instanceof ErrataInfo)) {
            return false;
        }
        ErrataInfo errataInfo = (ErrataInfo) objIn;
        return this.getName().equals(errataInfo.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
