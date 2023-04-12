DELETE FROM rhnPublicChannelFamily
 WHERE channel_family_id IN (
        SELECT id
          FROM rhnChannelFamily
         WHERE label LIKE '%-alpha'
            OR label LIKE '%-beta'
);

DELETE FROM rhnChannelFamily
 WHERE label LIKE '%-alpha'
    OR label LIKE '%-beta';
