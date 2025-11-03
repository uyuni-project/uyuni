/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.rhnpackage;


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;

public class PackageExtraTagId implements Serializable {

    @Serial
    private static final long serialVersionUID = -361145593789019802L;

    private Package pack;
    private PackageExtraTagsKeys key;

    /**
     * Constructor
     */
    public PackageExtraTagId() {
    }

    /**
     * Constructor
     * @param packIn the package
     * @param keyIn the key
     */
    public PackageExtraTagId(Package packIn, PackageExtraTagsKeys keyIn) {
        pack = packIn;
        key = keyIn;
    }

    public Package getPack() {
        return pack;
    }

    public void setPack(Package packIn) {
        pack = packIn;
    }

    public PackageExtraTagsKeys getKey() {
        return key;
    }

    public void setKey(PackageExtraTagsKeys keyIn) {
        key = keyIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }

        if (!(oIn instanceof PackageExtraTagId that)) {
            return false;
        }

        return new EqualsBuilder().append(pack, that.pack).append(key, that.key).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(pack).append(key).toHashCode();
    }
}
