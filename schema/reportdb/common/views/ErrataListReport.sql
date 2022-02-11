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

CREATE OR REPLACE VIEW ErrataListReport AS
  SELECT Errata.mgm_id
            , Errata.errata_id
            , Errata.advisory_name
            , Errata.advisory_type
            , Errata.cve
            , Errata.synopsis
            , count(*) AS affected_systems
            , Errata.synced_date

    FROM SystemErrata
            INNER JOIN Errata ON SystemErrata.mgm_id = Errata.mgm_id AND SystemErrata.errata_id = Errata.errata_id

GROUP BY Errata.mgm_id
            , Errata.errata_id
            , Errata.advisory_name
            , Errata.advisory_type
            , Errata.cve
            , Errata.synopsis
            , Errata.synced_date
;
