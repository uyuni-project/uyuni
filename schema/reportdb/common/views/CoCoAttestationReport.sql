--
-- Copyright (c) 2024 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE OR REPLACE VIEW CoCoAttestationReport AS
  SELECT CoCoAttestation.mgm_id
            , CoCoAttestation.report_id
            , System.system_id
            , CoCoAttestation.action_id
            , System.hostname
            , System.organization
            , CoCoAttestation.environment_type
            , CoCoAttestation.status AS report_status
            , CoCoAttestation.pass
            , CoCoAttestation.fail
            , CoCoAttestation.create_time
            , CoCoAttestation.synced_date
    FROM CoCoAttestation 
            LEFT JOIN System ON ( CoCoAttestation.mgm_id = System.mgm_id AND CoCoAttestation.system_id = System.system_id )
ORDER BY CoCoAttestation.mgm_id
            , CoCoAttestation.report_id
            , System.system_id
            , CoCoAttestation.create_time
;
