--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

-- Optionally scope an external-group mapping to one LDAP directory. NULL keeps the
-- server-agnostic behaviour used by REMOTE_USER. Idempotent: safe if re-applied.
ALTER TABLE rhnUserExtGroup
    ADD COLUMN IF NOT EXISTS ldap_server_id NUMERIC
        CONSTRAINT rhn_userextgroup_ldap_srv_fk
            REFERENCES suseLdapAuthServer (id)
            ON DELETE SET NULL;

DROP INDEX IF EXISTS rhn_userextgroup_label_oid_uq;

-- NULLS NOT DISTINCT keeps a single server-agnostic row per (label, org_id).
CREATE UNIQUE INDEX IF NOT EXISTS rhn_userextgroup_label_oid_srv_uq
    ON rhnUserExtGroup (label, org_id, ldap_server_id) NULLS NOT DISTINCT;
