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
VALUES ('HTTP_PROXY', 'HTTP proxy used by the server to connect to the internet in the form host:port', null, null)
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('HTTP_PROXY_USERNAME', 'Username for the HTTP proxy', null, null)
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('HTTP_PROXY_PASSWORD', 'Password for the HTTP proxy', null, null)
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('NO_PROXY', 'Comma separated list of hosts and domains which are connected without the HTTP proxy', null, null)
ON CONFLICT (key) DO NOTHING;
