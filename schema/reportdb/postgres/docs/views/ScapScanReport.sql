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

COMMENT ON VIEW ScapScanReport
  IS 'List the SCAP scans performed for each system.';

COMMENT ON COLUMN ScapScanReport.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN ScapScanReport.scan_id
  IS 'The id of the security scan';
COMMENT ON COLUMN ScapScanReport.system_id
  IS 'The id of the system';
COMMENT ON COLUMN ScapScanReport.action_id
  IS 'The id of the action that triggered the scan';
COMMENT ON COLUMN ScapScanReport.hostname
  IS 'The hostname that identifies this system';
COMMENT ON COLUMN ScapScanReport.organization
  IS 'The organization that owns this data';
COMMENT ON COLUMN ScapScanReport.ip_address
  IS 'The IPv4 address of the system';
COMMENT ON COLUMN ScapScanReport.name
  IS 'The name of the security scan';
COMMENT ON COLUMN ScapScanReport.benchmark
  IS 'The name of the performed benchmark';
COMMENT ON COLUMN ScapScanReport.benchmark_version
  IS 'The version of the benchmark';
COMMENT ON COLUMN ScapScanReport.profile
  IS 'The name of the profile used for the scan';
COMMENT ON COLUMN ScapScanReport.profile_title
  IS 'The descriptive title of the profile';
COMMENT ON COLUMN ScapScanReport.end_time
  IS 'When the scan has ended';
COMMENT ON COLUMN ScapScanReport.pass
  IS 'The number of passed rules';
COMMENT ON COLUMN ScapScanReport.fail
  IS 'The number of failed rules';
COMMENT ON COLUMN ScapScanReport.error
  IS 'The number of erroneous rules';
COMMENT ON COLUMN ScapScanReport.not_selected
  IS 'The number of rules not selected for this scan';
COMMENT ON COLUMN ScapScanReport.informational
  IS 'The number of informational rules';
COMMENT ON COLUMN ScapScanReport.other
  IS 'The number of rules with other outcomes';
COMMENT ON COLUMN ScapScanReport.synced_date
  IS 'The timestamp of when this data was last refreshed.';
