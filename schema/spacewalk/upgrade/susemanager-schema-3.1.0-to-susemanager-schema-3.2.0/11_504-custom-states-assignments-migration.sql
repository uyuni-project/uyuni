-------------------------------------------------------------------------------
-- CHANNEL ASSIGNMENT ---------------------------------------------------------
-------------------------------------------------------------------------------
-- OLD STYLE SYSTEM-CHANNEL ASSIGNMENT (via rhnServerConfigChannel)
-- we need to add new assignments based on 'server - custom states' assignments
INSERT INTO rhnServerConfigChannel(
    server_id,
    config_channel_id,
    position)
SELECT
    serverRev.server_id,
    configChannel.id,
    -- generate the position for the new assignments
    rank() OVER (PARTITION BY server_id ORDER BY configChannel.id) + COALESCE((SELECT max(position) FROM rhnServerConfigChannel WHERE server_id = serverRev.server_id), 0)
FROM
    suseCustomState customState
    INNER JOIN suseStateRevisionCustomState revCustomState ON customState.id = revCustomState.state_id
    INNER JOIN suseStateRevision rev ON revCustomState.state_revision_id = rev.id
    INNER JOIN suseServerStateRevision serverRev ON rev.id = serverRev.state_revision_id
    INNER JOIN rhnConfigChannel configChannel ON configChannel.label = customState.state_name
WHERE
    customState.state_deleted = 'N' AND
    -- we only want the latest revision
    rev.id = (SELECT max(state_revision_id) FROM suseServerStateRevision WHERE server_id = serverRev.server_id) AND
    NOT EXISTS (SELECT server_id
                FROM rhnServerConfigChannel
                WHERE server_id = serverRev.server_id
                      AND config_channel_id = configChannel.id);

-- NEW STYLE SYSTEM-CHANNEL ASSIGNMENT (via suseStateRevisionConfigChannel)
-- (suseStateRevisionConfigChannel duplicates the information from rhnServerConfigChannel)
-- Here we insert new 'revision - config channel' association based on
-- 'server - config channel' assignment.
-- We only update the latest revision as the revisions from the past as we
-- can't reconstruct the history of the assignments from the information in
-- rhnServerConfigChannel.
INSERT INTO suseStateRevisionConfigChannel(
    state_revision_id,
    config_channel_id,
    position
)
SELECT
    state_revision_id,
    serverConfigChannel.config_channel_id,
    serverConfigChannel.position
FROM
    suseServerStateRevision serverStateRevision
    INNER JOIN rhnServerConfigChannel serverConfigChannel ON serverStateRevision.server_id = serverConfigChannel.server_id
WHERE
    serverStateRevision.state_revision_id = (SELECT max(state_revision_id)
                                             FROM suseServerStateRevision
                                             WHERE server_id = serverConfigChannel.server_id)
    AND NOT EXISTS (SELECT *
                    FROM suseStateRevisionConfigChannel
                    WHERE state_revision_id = serverStateRevision.state_revision_id
                        AND config_channel_id = serverConfigChannel.config_channel_id);

-- SUSE REVISION ASSIGNMENT
-- Converting the revision-custom state assignment to revision-config channel
-- assignment for servers, server groups and organizations.
INSERT INTO suseStateRevisionConfigChannel(
    state_revision_id,
    config_channel_id)
SELECT DISTINCT
    rev.id, channel.id
FROM suseCustomState state
    INNER JOIN suseStateRevisionCustomState revState on state.id = revState.state_id
    INNER JOIN suseStateRevision rev ON rev.id = revState.state_revision_id
    INNER JOIN rhnConfigChannel channel ON channel.label = state.state_name
WHERE
    state.state_deleted = 'N'
    AND NOT EXISTS (SELECT state_revision_id
                    FROM suseStateRevisionConfigChannel
                    WHERE state_revision_id = rev.id
                          AND config_channel_id = channel.id);

-- GENERATE POSITION
-- STATE-SERVER assignments
-- position of the latest revision for servers is determined by rhnServerConfigChannel
UPDATE suseStateRevisionConfigChannel rev
SET position = (SELECT position
                FROM rhnServerConfigChannel serverChannel
                    INNER JOIN suseServerStateRevision serverStateRev ON serverChannel.server_id = serverStateRev.server_id
                WHERE
                    serverStateRev.state_revision_id = rev.state_revision_id
                    AND serverChannel.config_channel_id = rev.config_channel_id)
WHERE
    position IS NULL
    AND rev.state_revision_id IN (SELECT max(state_revision_id) FROM suseServerStateRevision GROUP BY server_id);
    -- and is a server revision, not org/grp


-- STATE-ORGANIZATION assignments
-- We generate initial ranking.
UPDATE suseStateRevisionConfigChannel
SET position = (SELECT rank
                      -- the inner SELECT gives us ranking of channels in form of: (channel ID, revision ID, position in ranking)
                      -- this is an initial ranking and we determine it based on channel id
                FROM (SELECT config_channel_id, revConfigChannel.state_revision_id, (rank() OVER (PARTITION BY orgRev.state_revision_id ORDER BY config_channel_id)) rank
                      FROM suseStateRevisionConfigChannel revConfigChannel
                          INNER JOIN suseOrgStateRevision orgRev
                          ON revConfigChannel.state_revision_id = orgRev.state_revision_id) RANKING
                WHERE config_channel_id = suseStateRevisionConfigChannel.config_channel_id
                    AND state_revision_id = suseStateRevisionConfigChannel.state_revision_id)
WHERE position IS NULL
    AND state_revision_id in (SELECT state_revision_id from suseOrgStateRevision);

-- STATE-SERVER GROUP assignments
-- We generate initial ranking.
UPDATE suseStateRevisionConfigChannel
SET position = (SELECT rank
                      -- the inner SELECT gives us ranking of channels in form of: (channel ID, revision ID, position in ranking)
                      -- this is an initial ranking and we determine it based on channel id
                FROM (SELECT config_channel_id, revConfigChannel.state_revision_id, (rank() OVER (PARTITION BY grpRev.state_revision_id ORDER BY config_channel_id)) rank
                      FROM suseStateRevisionConfigChannel revConfigChannel
                          INNER JOIN suseServerGroupStateRevision grpRev
                          ON revConfigChannel.state_revision_id = grpRev.state_revision_id) RANKING
                WHERE config_channel_id = suseStateRevisionConfigChannel.config_channel_id
                    AND state_revision_id = suseStateRevisionConfigChannel.state_revision_id)
WHERE position IS NULL
    AND state_revision_id in (SELECT state_revision_id from suseServerGroupStateRevision);

-- DROP OLD STATES
DROP TABLE suseStateRevisionCustomState;
DROP TABLE suseCustomState;
