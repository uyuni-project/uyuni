--
-- Copyright (c) 2012 Red Hat, Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

-- Migration: Add scap_policy_id to rhnActionScap
-- This links SCAP scans back to policies for compliance reporting

ALTER TABLE rhnActionScap 
ADD COLUMN IF NOT EXISTS scap_policy_id  BIGINT 
CONSTRAINT fk_scap_policy 
REFERENCES suseScapPolicy(id) 
ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS rhn_act_scap_policy_idx 
ON rhnActionScap(scap_policy_id);
