--
-- Copyright (c) 2012 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

-- Backup table 'suseCredentials'
CREATE TABLE  suseCredentialsBackup
    AS SELECT * FROM suseCredentials;

-- Truncate the original table
TRUNCATE TABLE suseCredentials;

-- Add new (NOT NULL) column 'type_id' to 'suseCredentials'
ALTER TABLE suseCredentials ADD type_id NUMBER NOT NULL
    CONSTRAINT suse_credentials_type_fk
    REFERENCES suseCredentialsType (id);

-- Re-insert everything including the 'type_id'
INSERT INTO suseCredentials
SELECT c.id, c.user_id, c.type, c.url, c.username, c.password, c.created, c.modified, d.id as type_id
    FROM suseCredentialsBackup c, suseCredentialsType d
    WHERE d.label = 'susestudio';

-- Delete column 'type' and the backup table
ALTER TABLE suseCredentials DROP COLUMN type;
DROP TABLE suseCredentialsBackup;

-- Rename foreign key constraint on the user
ALTER TABLE suseCredentials
    RENAME CONSTRAINT suse_credentials_oid_fk
    TO suse_credentials_user_fk;

-- Allow more than one pair of creds per user
ALTER TABLE suseCredentials DROP UNIQUE (user_id);

