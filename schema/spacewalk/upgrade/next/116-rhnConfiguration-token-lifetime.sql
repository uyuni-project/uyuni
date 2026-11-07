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

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('TOKEN_LIFETIME', 'Default lifetime in minutes of the channel access tokens of clients and the tokens of hub peripherals', null, '525600')
ON CONFLICT (key) DO NOTHING;
