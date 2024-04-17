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

CREATE OR REPLACE VIEW CoCoAttestationResultReport AS
  SELECT CoCoAttestationResult.mgm_id
            , CoCoAttestationResult.report_id
            , CoCoAttestationResult.result_type_id
            , System.system_id
            , System.hostname
            , System.organization
            , CoCoAttestation.environment_type
            , CoCoAttestationResult.result_type
            , CoCoAttestationResult.result_status
            , CoCoAttestationResult.description
            , CoCoAttestationResult.attestation_time
            , CoCoAttestationResult.synced_date
    FROM CoCoAttestationResult
            LEFT JOIN CoCoAttestation ON (CoCoAttestationResult.mgm_id = CoCoAttestation.mgm_id AND CoCoAttestationResult.report_id = CoCoAttestation.report_id)
            LEFT JOIN System ON ( CoCoAttestationResult.mgm_id = System.mgm_id AND CoCoAttestation.system_id = System.system_id )
ORDER BY mgm_id, report_id, result_type_id
;
