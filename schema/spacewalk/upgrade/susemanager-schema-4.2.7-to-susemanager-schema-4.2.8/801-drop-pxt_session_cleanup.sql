drop function if exists pxt_session_cleanup
   (bound_in in numeric, commit_interval in numeric,
    batch_size in numeric, sessions_deleted in numeric);
