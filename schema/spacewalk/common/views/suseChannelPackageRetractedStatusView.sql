--
-- Similar as rhnChannelPackage, but enhanced with the 'is_retracted' status if
-- given package is part of a retracted patch in the channel.
--

CREATE VIEW suseChannelPackageRetractedStatusView AS
SELECT
   cp.channel_id,
   cp.package_id,
       CASE
           WHEN e.advisory_status::text = 'retracted'::text THEN 1
           ELSE 0
       END AS is_retracted
  FROM rhnchannelpackage cp
    LEFT JOIN (rhnchannelerrata ce
      JOIN rhnerrata e ON e.id = ce.errata_id
      JOIN rhnerratapackage ep ON e.id = ep.errata_id) ON
        cp.channel_id = ce.channel_id
            AND ep.package_id = cp.package_id
            AND e.advisory_status::text = 'retracted'::text
GROUP BY cp.channel_id, cp.package_id, e.advisory_status;

