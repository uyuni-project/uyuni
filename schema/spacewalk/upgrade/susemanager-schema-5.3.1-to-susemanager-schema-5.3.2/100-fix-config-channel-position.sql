-- ensure that the position is numbered starting from 1
-- (re-run of the 5.1.7-to-5.1.8 remediation to clean up any config-channel position
--  gaps created after that migration by deleting a config channel: the ON DELETE CASCADE
--  on the join tables removes a row without renumbering the survivors - bsc#1272988)
UPDATE suseStateRevisionConfigChannel
SET position = (
    SELECT rank
    FROM (
        SELECT state_revision_id, config_channel_id, position, (row_number() OVER (PARTITION BY state_revision_id ORDER BY position ASC)) rank
        FROM suseStateRevisionConfigChannel scc WHERE position IS NOT NULL
    ) ranking
    WHERE config_channel_id = suseStateRevisionConfigChannel.config_channel_id
    AND state_revision_id = suseStateRevisionConfigChannel.state_revision_id) WHERE position IS NOT NULL;


UPDATE rhnServerConfigChannel
SET position = (
    SELECT rank
    from (
        SELECT server_id, config_channel_id, position, (row_number() OVER (PARTITION BY server_id ORDER BY position ASC)) rank
        FROM rhnServerConfigChannel sscc WHERE position IS NOT NULL
    ) ranking
    WHERE config_channel_id = rhnServerConfigChannel.config_channel_id
    AND server_id = rhnServerConfigChannel.server_id) WHERE position IS NOT NULL;

UPDATE rhnRegTokenConfigChannels
SET position = (
    SELECT rank
    from (
        SELECT token_id, config_channel_id, position, (row_number() OVER (PARTITION BY token_id ORDER BY position ASC)) rank
        FROM rhnRegTokenConfigChannels rrtcc
    ) ranking
    WHERE config_channel_id = rhnRegTokenConfigChannels.config_channel_id
    AND token_id = rhnRegTokenConfigChannels.token_id);
