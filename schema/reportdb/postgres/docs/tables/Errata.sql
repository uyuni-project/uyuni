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

COMMENT ON TABLE Errata
  IS 'The list of patches managed by a SUSE Manager instance';

COMMENT ON COLUMN Errata.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN Errata.errata_id
  IS 'The id of the patch';
COMMENT ON COLUMN Errata.advisory_name
  IS 'The unique name of this advisory';
COMMENT ON COLUMN Errata.advisory_type
  IS 'The type of patch. Possible values: Product Enhancement Advisory, Security Advisory, Bug Fix Advisory';
COMMENT ON COLUMN Errata.advisory_status
  IS 'The status of the patch. Possible values: final, stable, retracted';
COMMENT ON COLUMN Errata.issue_date
  IS 'When this advisory was first issued';
COMMENT ON COLUMN Errata.update_date
  IS 'When this advisory was last updated';
COMMENT ON COLUMN Errata.severity
  IS 'The serverity of this advisory. Possible values: Critical, Important, Moderate, Low';
COMMENT ON COLUMN Errata.reboot_required
  IS 'True if a reboot of the system is required after applying this patch';
COMMENT ON COLUMN Errata.affects_package_manager
  IS 'True if this patch make changes to the package management system';
COMMENT ON COLUMN Errata.cve
  IS 'A comma separated list of CVE ids that this patch addresses';
COMMENT ON COLUMN Errata.synopsis
  IS 'The brief description of this patch';
COMMENT ON COLUMN Errata.organization
  IS 'The organization that owns this data';
COMMENT ON COLUMN Errata.synced_date
  IS 'The timestamp of when this data was last refreshed.';
