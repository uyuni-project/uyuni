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
VALUES ('REPOSYNC_DOWNLOAD_THREADS', 'Number of threads used by reposync to download packages', null, '5')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('REPOSYNC_TIMEOUT', 'Timeout of reposync downloads in seconds', null, '300')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('REPOSYNC_MINRATE', 'Minimal transfer rate of reposync downloads in bytes per second', null, '1000')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('REPOSYNC_NEVRA_FILTER', 'Filter the packages of a repository by their full NEVRA instead of the name only', null, '0')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('SYNC_SOURCE_PACKAGES', 'Synchronize source packages with reposync', null, '0')
ON CONFLICT (key) DO NOTHING;

INSERT INTO rhnConfiguration (key, description, value, default_value)
VALUES ('URLGRABBER_LOGSPEC', 'urlgrabber debug logging of reposync in the form level,filename (for example 1,-)', null, null)
ON CONFLICT (key) DO NOTHING;
