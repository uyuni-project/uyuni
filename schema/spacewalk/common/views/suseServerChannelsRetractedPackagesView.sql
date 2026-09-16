-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

--
-- Returns the retracted packages (no matter if installed or not installed)
-- from channels of given server.
--

CREATE OR REPLACE VIEW suseServerChannelsRetractedPackagesView AS
 SELECT DISTINCT ep.package_id AS pid,
    sc.server_id AS sid
   FROM rhnserverchannel sc
     JOIN rhnchannel c ON c.id = sc.channel_id
     JOIN rhnchannelerrata ce ON ce.channel_id = c.id
     JOIN rhnerrata e ON e.id = ce.errata_id
     JOIN rhnerratapackage ep ON ep.errata_id = e.id
  WHERE e.advisory_status::text = 'retracted'::text;

