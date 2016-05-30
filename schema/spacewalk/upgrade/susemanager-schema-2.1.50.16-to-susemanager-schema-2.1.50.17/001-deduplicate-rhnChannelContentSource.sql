UPDATE rhnChannelContentSource
  SET source_id = (

    -- first, construct a temporary view on duplicates
    -- that is, couples of ids of rhnCountentSource rows that only differ by the URL token
    WITH duplicates AS (
      SELECT c1.id AS new_id, c2.id AS old_id
        -- take a couple of rhnCountentSource rows
        FROM rhnContentSource c1, rhnContentSource c2
        -- making sure that c1 is the newest, or in case they are equally old, c1 has higher id
        WHERE (c1.modified > c2.modified OR (c1.modified = c2.modified AND c1.id > c2.id))
        -- make sure we are talking about SCC URLs
          AND c1.source_url LIKE 'https://updates.suse.com/%'
          AND c1.source_url LIKE 'https://updates.suse.com/%'
        -- make sure both have a URL with a token part
          AND instr(c1.source_url, '?') > 0
          AND instr(c2.source_url, '?') > 0
        -- finally, they have equal initial (non-token) part
          AND c1.source_url LIKE substr(c2.source_url, 0, instr(c2.source_url, '?') - 1) || '%'
    )

    -- second, the new source id is simply the new_id from the duplicates table
    SELECT duplicates.new_id
      FROM duplicates
      WHERE duplicates.old_id = rhnChannelContentSource.source_id
  )
  WHERE EXISTS (
    -- the update must only be executed on those rows that actually have a duplicate
    -- duplicate definition below is identical to the one above, it's copypasted because
    -- of cross-RDBMS syntax restrictions
    WITH duplicates AS (
      SELECT c1.id AS new_id, c2.id AS old_id
        FROM rhnContentSource c1, rhnContentSource c2
        WHERE (c1.modified > c2.modified OR (c1.modified = c2.modified AND c1.id > c2.id))
          AND c1.source_url LIKE 'https://updates.suse.com/%'
          AND c1.source_url LIKE 'https://updates.suse.com/%'
          AND instr(c1.source_url, '?') > 0
          AND instr(c2.source_url, '?') > 0
          AND c1.source_url LIKE substr(c2.source_url, 0, instr(c2.source_url, '?') - 1) || '%'
    )
    SELECT 1
      FROM duplicates
      WHERE duplicates.old_id = rhnChannelContentSource.source_id
  )
;

DELETE FROM rhnContentSource
  WHERE rhnContentSource.id NOT IN (
    SELECT source_id
      FROM rhnChannelContentSource
  );

