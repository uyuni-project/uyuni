/*
 * Copyright (c) 2021 SUSE LLC
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

package com.redhat.rhn.domain.server.ansible;

import com.redhat.rhn.domain.server.MinionServer;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Ansible Playbook path
 */
@Entity
@DiscriminatorValue("playbook")
public class PlaybookPath extends AnsiblePath {

    /**
     * Standard constructor
     */
    public PlaybookPath() { }

    /**
     * Standard constructor
     * @param minionServer the minion server
     */
    public PlaybookPath(MinionServer minionServer) {
        super(minionServer);
    }

    @Override
    @Transient
    public Type getEntityType() {
        return Type.PLAYBOOK;
    }
}
