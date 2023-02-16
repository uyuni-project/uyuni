-- Drop the indexes
DROP INDEX IF EXISTS rhn_channelcomps_cid_ctype_uq;
DROP INDEX IF EXISTS rhn_channelcomps_cid_ctype_idx;
ALTER TABLE rhnChannelComps DROP CONSTRAINT IF EXISTS rhn_channelcomps_cid_ctype_filename_uq;

-- Delete outdated (archived) comps entries,
-- keeping only the latest for each type, per channel
DELETE FROM rhnChannelComps c
WHERE EXISTS (
    SELECT 1
    FROM rhnChannelComps
    WHERE id != c.id
        AND channel_id = c.channel_id
        AND last_modified >= c.last_modified
        AND comps_type_id = c.comps_type_id
);

CREATE UNIQUE INDEX rhn_channelcomps_cid_ctype_uq ON rhnChannelComps(channel_id, comps_type_id);
