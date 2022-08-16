-- Replace the index with a non-unique one
DROP INDEX IF EXISTS rhn_channelcomps_cid_ctype_uq;
DROP INDEX IF EXISTS rhn_channelcomps_cid_ctype_idx;

CREATE INDEX rhn_channelcomps_cid_ctype_idx ON rhnChannelComps(channel_id, comps_type_id);

-- Add a unique constraint as (cid, comps_type, filename)
ALTER TABLE rhnChannelComps DROP CONSTRAINT IF EXISTS rhn_channelcomps_cid_ctype_filename_uq;

ALTER TABLE rhnChannelComps ADD CONSTRAINT rhn_channelcomps_cid_ctype_filename_uq
    UNIQUE (channel_id, comps_type_id, relative_filename);
