-- oracle equivalent source sha1 38390271a5a283be047ea8057d64606891dbfccc

select logging.clear_log_id();

UPDATE rhnServerGroup
   SET max_members = 0
 WHERE max_members IS NULL
   AND group_type IS NOT NULL;
