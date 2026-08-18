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

-- Idempotent: safe if re-applied (ADD COLUMN IF NOT EXISTS + guarded constraint).
ALTER TABLE rhnUserInfo
    ADD COLUMN IF NOT EXISTS auth_type VARCHAR(16) DEFAULT ('LOCAL') NOT NULL;

-- Migrate existing users: those flagged for PAM become PAM, everyone else stays LOCAL.
-- Guarded with auth_type = 'LOCAL' so a second run does not overwrite LDAP/PAM already set.
UPDATE rhnUserInfo
    SET auth_type = 'PAM'
    WHERE use_pam_authentication = 'Y' AND auth_type = 'LOCAL';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
            WHERE conname = 'rhn_user_info_auth_type_ck'
              AND conrelid = 'rhnuserinfo'::regclass
    ) THEN
        ALTER TABLE rhnUserInfo
            ADD CONSTRAINT rhn_user_info_auth_type_ck
            CHECK (auth_type in ('LOCAL','PAM','LDAP'));
    END IF;
END $$;
