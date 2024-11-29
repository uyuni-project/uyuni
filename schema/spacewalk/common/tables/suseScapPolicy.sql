--
-- Copyright (c) 2025 SUSE
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE suseScapPolicy
(
    id                   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    policy_name          VARCHAR(255) NOT NULL ,
    data_stream_name     TEXT NOT NULL ,
    xccdf_profile_id     TEXT NOT NULL,
    tailoring_file       INTEGER
                         CONSTRAINT fk_tailoring_file
                         REFERENCES suseScapTailoringFile (id)
                         ON DELETE CASCADE,

    tailoring_profile_id TEXT,

    org_id               INTEGER NOT NULL
                         CONSTRAINT fk_org_id
                         REFERENCES web_customer (id)
                         ON DELETE CASCADE,
    created              TIMESTAMPTZ DEFAULT current_timestamp NOT NULL,
    modified             TIMESTAMPTZ DEFAULT current_timestamp NOT NULL
);

CREATE UNIQUE INDEX idx_org_id_policy_name
    ON suseScapPolicy (org_id, policy_name); -- Ensures unique policy names within an organization

-- Trigger to automatically update 'modified' column
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.modified = current_timestamp; -- Set the `modified` column to the current timestamp
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger that runs before any update
CREATE TRIGGER trg_update_modified
BEFORE UPDATE ON suse_scap_policy
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();
