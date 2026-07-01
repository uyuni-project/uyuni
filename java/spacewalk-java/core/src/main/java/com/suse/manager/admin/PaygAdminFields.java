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
package com.suse.manager.admin;

public enum PaygAdminFields {
    description(true),
    host(false),
    port(true),
    username(true),
    password(true),
    key(true),
    key_password(true),
    bastion_host(true),
    bastion_port(true),
    bastion_username(true),
    bastion_password(true),
    bastion_key(true),
    bastion_key_password(true);

    private boolean editable;
    PaygAdminFields(boolean editableIn) {
        this.editable = editableIn;
    }

    public boolean isEditable() {
        return editable;
    }
}
