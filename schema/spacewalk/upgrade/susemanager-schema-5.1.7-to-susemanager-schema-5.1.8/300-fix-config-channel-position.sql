UPDATE suseStateRevisionConfigChannel
SET position = (
    SELECT rank - 1
    FROM (
        SELECT state_revision_id, config_channel_id, position, (row_number() OVER (PARTITION BY state_revision_id ORDER BY position ASC)) rank
        FROM suseStateRevisionConfigChannel scc
    ) ranking
    WHERE config_channel_id = suseStateRevisionConfigChannel.config_channel_id
    AND state_revision_id = suseStateRevisionConfigChannel.state_revision_id);


UPDATE rhnServerConfigChannel
SET position = (
    SELECT rank - 1
    from (
        SELECT server_id, config_channel_id, position, (row_number() OVER (PARTITION BY server_id ORDER BY position ASC)) rank
        FROM rhnServerConfigChannel sscc
    ) ranking
    WHERE config_channel_id = rhnServerConfigChannel.config_channel_id
    AND server_id = rhnServerConfigChannel.server_id);
