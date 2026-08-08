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

-- Bind password of an LDAP authentication server. Idempotent: safe if re-applied.
ALTER TABLE suseCredentials
    DROP CONSTRAINT IF EXISTS rhn_type_ck;

ALTER TABLE suseCredentials
    ADD CONSTRAINT rhn_type_ck
    CHECK (type IN ('scc', 'vhm', 'registrycreds', 'cloudrmt', 'reportcreds', 'rhui', 'hub_scc', 'ldap'));

ALTER TABLE susecredentials
    DROP CONSTRAINT IF EXISTS cred_type_check;

ALTER TABLE susecredentials
    ADD CONSTRAINT cred_type_check CHECK (
        CASE type
            WHEN 'scc' THEN
                username is not null and username <> ''
                    and password is not null and password <> ''
            WHEN 'cloudrmt' THEN
                username is not null and username <> ''
                    and password is not null and password <> ''
                    and url is not null and url <> ''
            WHEN 'vhm' THEN
                username is not null and username <> ''
                    and password is not null and password <> ''
            WHEN 'registrycreds' THEN
                username is not null and username <> ''
                    and password is not null and password <> ''
            WHEN 'reportcreds' THEN
                username is not null and username <> ''
                    and password is not null and password <> ''
            WHEN 'hub_scc' THEN
                username is not null and username <> ''
                    and password is not null and password <> ''
                    and url is not null and url <> ''
            WHEN 'ldap' THEN
                -- The bind DN is stored on suseLdapAuthServer, so only the password lives here.
                password is not null and password <> ''
        END
    );

DO $$
  BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'susecredentialstype') THEN
      INSERT INTO suseCredentialsType (id, label, name)
        SELECT sequence_nextval('suse_credtype_id_seq'), 'ldap', 'LDAP Authentication Server'
        WHERE NOT EXISTS (SELECT 1 FROM suseCredentialsType WHERE label = 'ldap');
    END IF;
  END;
$$;
