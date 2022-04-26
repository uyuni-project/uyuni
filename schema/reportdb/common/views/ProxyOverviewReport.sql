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

CREATE OR REPLACE VIEW ProxyOverviewReport AS
    SELECT prx.mgm_id
              , prx.system_id AS proxy_id
              , prx.hostname AS proxy_name
              , sys.hostname AS system_name
              , sys.system_id
              , prx.synced_date
      FROM system prx
              INNER JOIN system sys ON sys.proxy_system_id = prx.system_id AND sys.mgm_id = prx.mgm_id
    WHERE prx.is_proxy
 ORDER BY prx.mgm_id, prx.system_id, sys.system_id
;
