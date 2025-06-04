--
-- Copyright (c) 2023 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--


CREATE TABLE suseVEXHash (
    id NUMERIC NOT NULL
        CONSTRAINT suse_vex_hash_id_pk PRIMARY KEY,
    cve VARCHAR(20) NOT NULL
        CONSTRAINT suse_cve_hash_cve_fk REFERENCES rhnCVE(name),
    hash_type VARCHAR(20) NOT NULL
        CONSTRAINT suse_cve_hash_type_chk CHECK (hash_type IN ('MD5', 'SHA1', 'SHA256', 'SHA512', 'OTHER')),
    hash TEXT NOT NULL,
    created TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    cve_status NUMERIC DEFAULT 0,
    CONSTRAINT suse_cve_hash_unique UNIQUE (cve, hash),
    CONSTRAINT suse_vex_hash_cve_unique UNIQUE (cve)
);

CREATE SEQUENCE suse_cve_hash_id_seq;