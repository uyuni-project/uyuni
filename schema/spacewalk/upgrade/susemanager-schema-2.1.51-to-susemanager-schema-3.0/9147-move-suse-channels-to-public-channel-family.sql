INSERT INTO rhnPublicChannelFamily(channel_family_id)
  SELECT channel_family_id
    FROM rhnPrivateChannelFamily
    WHERE org_id = 1
      AND channel_family_id IN (
        SELECT id
          FROM rhnChannelFamily
          WHERE label NOT LIKE 'private-channel-family-%'
      );

DELETE FROM rhnPrivateChannelFamily
  WHERE org_id = 1
  AND channel_family_id IN (
    SELECT id
      FROM rhnChannelFamily
      WHERE label NOT LIKE 'private-channel-family-%'
  );
