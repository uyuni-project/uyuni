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

COMMENT ON VIEW ScapScanResultReport
  IS 'List the identifiers and the results of SCAP rules for each scan performed.';

COMMENT ON COLUMN ScapScanResultReport.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN ScapScanResultReport.scan_id
  IS 'The id of the security scan';
COMMENT ON COLUMN ScapScanResultReport.rule_id
  IS 'The id of the rule';
COMMENT ON COLUMN ScapScanResultReport.idref
  IS 'The reference of the rule';
COMMENT ON COLUMN ScapScanResultReport.rulesystem
  IS 'The name of the rule system';
COMMENT ON COLUMN ScapScanResultReport.system_id
  IS 'The id of the system';
COMMENT ON COLUMN ScapScanResultReport.hostname
  IS 'The hostname that identifies this system';
COMMENT ON COLUMN ScapScanResultReport.organization
  IS 'The organization that owns this data';
COMMENT ON COLUMN ScapScanResultReport.ident
  IS 'The CCE v5 id of this rule';
COMMENT ON COLUMN ScapScanResultReport.result
  IS 'The result of the scan for this rule';
COMMENT ON COLUMN ScapScanResultReport.synced_date
  IS 'The timestamp of when this data was last refreshed.';
