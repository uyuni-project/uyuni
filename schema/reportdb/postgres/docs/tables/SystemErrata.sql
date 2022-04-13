--
-- Copyright (c) 2022 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

COMMENT ON TABLE SystemErrata
  IS 'The list of patches applicable to a system';

COMMENT ON COLUMN SystemErrata.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN SystemErrata.system_id
  IS 'The id of the system';
COMMENT ON COLUMN SystemErrata.errata_id
  IS 'The id of the patch';
COMMENT ON COLUMN SystemErrata.hostname
  IS 'The hostname that identifies the system';
COMMENT ON COLUMN SystemErrata.advisory_name
  IS 'The unique name of the advisory';
COMMENT ON COLUMN SystemErrata.advisory_type
  IS 'The type of patch. Possible values: Product Enhancement Advisory, Security Advisory, Bug Fix Advisory';
COMMENT ON COLUMN SystemErrata.synced_date
  IS 'The timestamp of when this data was last refreshed.';

ALTER TABLE SystemErrata
    ADD CONSTRAINT SystemErrata_system_fkey FOREIGN KEY (mgm_id, system_id) REFERENCES System(mgm_id, system_id),
    ADD CONSTRAINT SystemErrata_errata_fkey FOREIGN KEY (mgm_id, errata_id) REFERENCES Errata(mgm_id, errata_id);
