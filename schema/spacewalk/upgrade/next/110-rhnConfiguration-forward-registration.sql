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
VALUES ('SCC_FORWARD_REGISTRATION', 'Forward the registrations of clients to the SUSE Customer Center (SCC)', null, '1')
ON CONFLICT (key) DO NOTHING;
