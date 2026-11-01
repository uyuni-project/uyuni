-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

DROP TRIGGER IF EXISTS rhn_satellite_info_mod_trig ON rhnSatelliteInfo;

DROP FUNCTION IF EXISTS rhn_satellite_info_mod_trig_fun();

DROP TABLE IF EXISTS rhnSatelliteInfo;


