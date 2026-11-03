/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.redhat.rhn.domain.errata;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class BugId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1890957416010947843L;

    private Long id;
    private Errata errata;

    /**
     * Constructor
     */
    public BugId() {
    }

    /**
     * Constructor
     * @param idIn the bug id
     * @param errataIn the errata
     */
    public BugId(Long idIn, Errata errataIn) {
        id = idIn;
        errata = errataIn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }

    public Errata getErrata() {
        return errata;
    }

    public void setErrata(Errata errataIn) {
        errata = errataIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof BugId bugId)) {
            return false;
        }
        return Objects.equals(id, bugId.id) &&
                Objects.equals(errata, bugId.errata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, errata);
    }

    @Override
    public String toString() {
        return "BugId{" +
                "id=" + id +
                ", errata=" + errata +
                '}';
    }
}
