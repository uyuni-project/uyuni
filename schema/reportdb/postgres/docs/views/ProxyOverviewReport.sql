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

COMMENT ON VIEW ProxyOverviewReport
  IS 'List of proxies and the systems registered through them';

COMMENT ON COLUMN ProxyOverviewReport.mgm_id
  IS 'The id of the SUSE Manager instance that contains this data';
COMMENT ON COLUMN ProxyOverviewReport.proxy_id
  IS 'The id of the proxy system';
COMMENT ON COLUMN ProxyOverviewReport.proxy_name
  IS 'The unique descriptive name of the proxy';
COMMENT ON COLUMN ProxyOverviewReport.system_name
  IS 'The unique descriptive name of the system behind the proxy';
COMMENT ON COLUMN ProxyOverviewReport.system_id
  IS 'The id of the system behind the proxy';
COMMENT ON COLUMN ProxyOverviewReport.synced_date
  IS 'The timestamp of when this data was last refreshed.';
