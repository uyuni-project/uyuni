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

-- Records which directory authenticated or provisioned the user. Idempotent: safe if re-applied.
-- Runs after 120-create-suseldapauthserver.sql, which creates the referenced table.
ALTER TABLE rhnUserInfo
    ADD COLUMN IF NOT EXISTS ldap_server_id NUMERIC
        CONSTRAINT rhn_user_info_ldap_srv_fk
            REFERENCES suseLdapAuthServer (id)
            ON DELETE SET NULL;
