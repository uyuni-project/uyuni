CREATE OR REPLACE FUNCTION minion_pillars(
  mid VARCHAR
) RETURNS TABLE (pillar JSONB)
AS $$
DECLARE
  sid NUMERIC;
BEGIN
  SELECT server_id INTO sid FROM suseMinionInfo WHERE minion_id = mid;
  RETURN QUERY
  SELECT p.pillar from suseSaltPillar AS p WHERE (p.server_id is NULL AND p.group_id is NULL AND p.org_id is NULL)
       OR (p.org_id = (SELECT s.org_id FROM rhnServer AS s WHERE s.id = sid))
       OR (p.group_id IN (SELECT g.server_group_id FROM rhnServerGroupMembers AS g WHERE g.server_id = sid))
       OR (p.server_id = sid)
       ORDER BY CASE WHEN p.org_id IS NULL AND p.group_id IS NULL and p.server_id is NULL THEN 0 ELSE 1 END, p.org_id NULLS LAST, p.group_id ASC NULLS LAST, p.server_id ASC NULLS FIRST;
END
$$ language plpgsql;

