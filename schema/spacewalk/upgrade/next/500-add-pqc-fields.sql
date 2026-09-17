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
-- SPDX-License-Identifier: GPL-2.0-only
--

-- add column to enable PQC signing
ALTER TABLE rhnContentSource ADD COLUMN IF NOT EXISTS
    pqc_metadata_signed CHAR(1) DEFAULT ('N') NOT NULL
    CONSTRAINT rhn_cs_pqcms_ck
    CHECK (pqc_metadata_signed in ( 'Y' , 'N' ));

-- add column to enable PQC signing
ALTER TABLE suseSCCRepository ADD COLUMN IF NOT EXISTS
    pqc_signed CHAR(1) DEFAULT ('N') NOT NULL
    CONSTRAINT suse_sccrepo_pqcsig_ck
    CHECK (pqc_signed in ('Y', 'N'));

-- add column to enable PQC signing
ALTER TABLE rhnChannel ADD COLUMN IF NOT EXISTS
    pqc_check CHAR(1) DEFAULT ('N') NOT NULL
    CONSTRAINT rhn_channel_pqc_ck
    CHECK (pqc_check in ('Y', 'N'));

-- add column to enable PQC signing
ALTER TABLE suseISSHub ADD COLUMN IF NOT EXISTS
    pqc_cert TEXT;