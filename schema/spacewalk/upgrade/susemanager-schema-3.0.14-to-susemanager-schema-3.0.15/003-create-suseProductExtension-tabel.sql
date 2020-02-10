-- oracle equivalent source sha1 105cb719c888239297c868aab8650a27214fae92
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


create table if not exists
suseProductExtension
(
    base_pdid   NUMERIC not null
                  CONSTRAINT suse_prdext_bpid_fk
                  REFERENCES suseProducts (id)
                  ON DELETE CASCADE,
    ext_pdid   NUMERIC not null
                  CONSTRAINT suse_prdext_epid_fk
                  REFERENCES suseProducts (id)
                  ON DELETE CASCADE,
    created   TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL,
    modified  TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL
);

DO $$
  BEGIN
    BEGIN
      CREATE INDEX prdext_bpid_idx
      ON suseProductExtension (base_pdid);

      CREATE INDEX prdext_epid_idx
      ON suseProductExtension (ext_pdid);
    EXCEPTION
      WHEN duplicate_table
      THEN RAISE NOTICE 'indexes already exists, skipping';
    END;
  END;
$$;
