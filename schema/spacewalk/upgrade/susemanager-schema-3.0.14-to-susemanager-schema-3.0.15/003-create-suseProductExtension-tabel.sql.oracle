--
-- Copyright (c) 2016 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--


create table
suseProductExtension
(
    base_pdid   number not null
                  CONSTRAINT suse_prdext_bpid_fk
                  REFERENCES suseProducts (id)
                  ON DELETE CASCADE,
    ext_pdid   number not null
                  CONSTRAINT suse_prdext_epid_fk
                  REFERENCES suseProducts (id)
                  ON DELETE CASCADE,
    created   timestamp with local time zone
                  DEFAULT (current_timestamp) NOT NULL,
    modified  timestamp with local time zone
                  DEFAULT (current_timestamp) NOT NULL
);

CREATE INDEX prdext_bpid_idx
ON suseProductExtension (base_pdid)
TABLESPACE [[64k_tbs]];

CREATE INDEX prdext_epid_idx
ON suseProductExtension (ext_pdid)
TABLESPACE [[64k_tbs]];
