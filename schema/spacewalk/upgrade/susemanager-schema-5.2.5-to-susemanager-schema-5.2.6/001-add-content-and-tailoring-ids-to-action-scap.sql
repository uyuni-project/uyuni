--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

-- Migration: Add scap_content_id and tailoring_file_id to rhnActionScap
-- This enables per-ID directory file resolution for both policy-based and one-off scans

-- Fix constraint name from previous migration in case someone already migrated it in test instance
ALTER TABLE rhnActionScap DROP CONSTRAINT IF EXISTS fk_scap_policy;
ALTER TABLE rhnActionScap DROP CONSTRAINT IF EXISTS rhn_act_scap_policy_fk;

ALTER TABLE rhnActionScap ADD CONSTRAINT rhn_act_scap_policy_fk
    FOREIGN KEY (scap_policy_id) REFERENCES suseScapPolicy(id) ON DELETE SET NULL;

-- Add new columns
ALTER TABLE rhnActionScap
ADD COLUMN IF NOT EXISTS scap_content_id BIGINT
CONSTRAINT rhn_act_scap_content_fk
REFERENCES suseScapContent(id)
ON DELETE SET NULL;

ALTER TABLE rhnActionScap
ADD COLUMN IF NOT EXISTS tailoring_file_id BIGINT
CONSTRAINT rhn_act_scap_tailoring_fk
REFERENCES suseScapTailoringFile(id)
ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS rhn_act_scap_content_idx
ON rhnActionScap(scap_content_id);

CREATE INDEX IF NOT EXISTS rhn_act_scap_tailoring_idx
ON rhnActionScap(tailoring_file_id);
