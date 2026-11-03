--
-- Copyright (c) 2013 Novell
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
--

ALTER TABLE rhnActionPackage DROP CONSTRAINT rhn_act_p_param_ck;

-- add the contraints
ALTER TABLE rhnActionPackage
    ADD CONSTRAINT rhn_act_p_param_ck
    CHECK (parameter IN ('upgrade', 'install', 'remove', 'downgrade', 'lock'));

