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

-- boolean flags use 'true' and 'false' like the values written by Java
UPDATE rhnConfiguration
   SET default_value = CASE default_value WHEN '1' THEN 'true' ELSE 'false' END
 WHERE key LIKE 'PSW_CHECK_%_FLAG'
   AND default_value IN ('0', '1');

UPDATE rhnConfiguration
   SET value = CASE value WHEN '1' THEN 'true' ELSE 'false' END
 WHERE key LIKE 'PSW_CHECK_%_FLAG'
   AND value IN ('0', '1');

-- a NULL value means the default value is used
UPDATE rhnConfiguration
   SET value = NULL
 WHERE value = default_value;
