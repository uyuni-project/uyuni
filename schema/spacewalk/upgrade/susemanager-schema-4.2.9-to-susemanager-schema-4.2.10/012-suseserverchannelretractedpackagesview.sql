CREATE OR REPLACE VIEW suseServerChannelsRetractedPackagesView AS
 SELECT DISTINCT p.id AS pid,
    s.id AS sid
   FROM rhnserver s
     JOIN rhnserverchannel sc ON s.id = sc.server_id
     JOIN rhnchannel c ON c.id = sc.channel_id
     JOIN rhnchannelerrata ce ON ce.channel_id = c.id
     JOIN rhnerrata e ON e.id = ce.errata_id
     JOIN rhnerratapackage ep ON ep.errata_id = e.id
     JOIN rhnpackage p ON p.id = ep.package_id
  WHERE e.advisory_status::text = 'retracted'::text;
