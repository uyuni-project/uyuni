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

ALTER TABLE rhnUserInfo ADD COLUMN IF NOT EXISTS
    beta_features_enabled CHAR(1)
        DEFAULT ('N') NOT NULL
            CONSTRAINT rhn_user_info_beta_ck
                CHECK (beta_features_enabled in ('Y', 'N'));
