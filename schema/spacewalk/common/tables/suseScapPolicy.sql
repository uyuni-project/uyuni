--
-- Copyright (c) 2021 SUSE
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
                         REFERENCES suse_scap_tailoring_file (id)
                         ON DELETE CASCADE,

    tailoring_profile_id TEXT,

    org_id               INTEGER NOT NULL
                         CONSTRAINT fk_org_id
                         REFERENCES web_customer (id)
                         ON DELETE CASCADE,
    created              TIMESTAMPTZ DEFAULT current_timestamp NOT NULL,
    modified             TIMESTAMPTZ DEFAULT current_timestamp NOT NULL
);

CREATE UNIQUE INDEX suseScapPolicy_org_id_name_uq
ON suseScapPolicy (org_id, policy_name);
