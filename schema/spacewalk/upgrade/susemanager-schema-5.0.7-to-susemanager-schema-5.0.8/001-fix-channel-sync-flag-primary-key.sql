
ALTER TABLE rhnChannelSyncFlag DROP CONSTRAINT IF EXISTS rhn_chsf_cid_pk;
ALTER TABLE rhnChannelSyncFlag DROP CONSTRAINT IF EXISTS rhn_chsf_cid_fk;

DELETE FROM rhnChannelSyncFlag T1
      USING rhnChannelSyncFlag T2
WHERE  T1.ctid       < T2.ctid          -- delete the "older" ones
  AND  T1.channel_id = T2.channel_id;   -- list columns that define duplicates

ALTER TABLE rhnChannelSyncFlag ADD CONSTRAINT rhn_chsf_cid_pk PRIMARY KEY (channel_id);
ALTER TABLE rhnChannelSyncFlag ADD CONSTRAINT rhn_chsf_cid_fk FOREIGN KEY (channel_id) REFERENCES rhnChannel(id) ON DELETE CASCADE;

