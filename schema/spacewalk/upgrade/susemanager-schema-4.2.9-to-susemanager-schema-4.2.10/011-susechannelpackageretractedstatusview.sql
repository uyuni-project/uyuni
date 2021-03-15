CREATE OR REPLACE VIEW suseChannelPackageRetractedStatusView AS
 SELECT
    cp.channel_id,
    cp.package_id,
    CASE
        WHEN (EXISTS ( SELECT 1
           FROM rhnchannelerrata ce
             JOIN rhnerrata e ON ce.errata_id = e.id
             JOIN rhnerratapackage ep ON ep.errata_id = e.id
          WHERE e.advisory_status::text = 'retracted'::text AND ce.channel_id = cp.channel_id AND ep.package_id = cp.package_id)) THEN true
        ELSE false
    END AS is_retracted
   FROM rhnchannelpackage cp;

