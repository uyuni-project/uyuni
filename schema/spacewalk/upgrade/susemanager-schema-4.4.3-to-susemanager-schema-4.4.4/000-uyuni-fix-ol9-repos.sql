INSERT INTO rhnContentSource (id, org_id, type_id, source_url, label, metadata_signed)
  SELECT sequence_nextval('rhn_chan_content_src_id_seq'), CS.org_id, CS.type_id,
         'https://yum.oracle.com/repo/OracleLinux/OL9/baseos/latest/x86_64/', 'Extern - Oracle Linux 9 (x86_64)', 'N'
    FROM rhnContentSource CS
    JOIN rhnChannelContentSource CCS ON CS.id = CCS.source_id
    JOIN rhnChannel C ON CCS.channel_id = C.id
   WHERE CS.source_url = 'file:///etc/pki/rpm-gpg/RPM-GPG-KEY-oracle'
     AND C.label = 'oraclelinux9-x86_64';

INSERT INTO rhnContentSource (id, org_id, type_id, source_url, label, metadata_signed)
  SELECT sequence_nextval('rhn_chan_content_src_id_seq'), CS.org_id, CS.type_id,
         'https://yum.oracle.com/repo/OracleLinux/OL9/baseos/latest/aarch64/', 'Extern - Oracle Linux 9 (aarch64)', 'N'
    FROM rhnContentSource CS
    JOIN rhnChannelContentSource CCS ON CS.id = CCS.source_id
    JOIN rhnChannel C ON CCS.channel_id = C.id
   WHERE CS.source_url = 'file:///etc/pki/rpm-gpg/RPM-GPG-KEY-oracle'
     AND C.label = 'oraclelinux9-aarch64';

DELETE FROM rhnChannelContentSource
WHERE channel_id in (SELECT id FROM rhnChannel WHERE label IN ('oraclelinux9-x86_64', 'oraclelinux9-aarch64'));

DELETE FROM rhnContentSource
WHERE source_url = 'file:///etc/pki/rpm-gpg/RPM-GPG-KEY-oracle';

UPDATE rhnContentSource
   SET label = 'External - Oracle Linux 9 (x86_64)'
 WHERE label = 'Extern - Oracle Linux 9 (x86_64)';

UPDATE rhnContentSource
   SET label = 'External - Oracle Linux 9 (aarch64)'
 WHERE label = 'Extern - Oracle Linux 9 (aarch64)';

INSERT INTO rhnChannelContentSource (source_id, channel_id)
  SELECT src.id AS source_id, C.id AS channel_id
  FROM rhnContentSource src
  JOIN rhnChannel C ON C.label = 'oraclelinux9-x86_64'
  WHERE src.label = 'Extern - Oracle Linux 9 (x86_64)';

INSERT INTO rhnChannelContentSource (source_id, channel_id)
  SELECT src.id AS source_id, C.id AS channel_id
  FROM rhnContentSource src
  JOIN rhnChannel C ON C.label = 'oraclelinux9-aarch64'
  WHERE src.label = 'Extern - Oracle Linux 9 (aarch64)';
