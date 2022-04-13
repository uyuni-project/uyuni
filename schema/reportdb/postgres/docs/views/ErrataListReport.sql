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

COMMENT ON VIEW ErrataListReport
  IS 'Patches out of compliance information with their details.';

COMMENT ON COLUMN ErrataListReport.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN ErrataListReport.errata_id
  IS 'The id of the patch';
COMMENT ON COLUMN ErrataListReport.advisory_name
  IS 'The unique name of the advisory';
COMMENT ON COLUMN ErrataListReport.advisory_type
  IS 'The type of patch. Possible values: Product Enhancement Advisory, Security Advisory, Bug Fix Advisory';
COMMENT ON COLUMN ErrataListReport.cve
  IS 'A list of CVE ids that this patch addresses, separated by ;';
COMMENT ON COLUMN ErrataListReport.synopsis
  IS 'The brief description of this patch';
COMMENT ON COLUMN ErrataListReport.issue_date
  IS 'When this advisory was first issued';
COMMENT ON COLUMN ErrataListReport.update_date
  IS 'When this advisory was last updated';
COMMENT ON COLUMN ErrataListReport.affected_systems
  IS 'The number of system affected by this advisory';
COMMENT ON COLUMN ErrataListReport.synced_date
  IS 'The timestamp of when this data was last refreshed.';
