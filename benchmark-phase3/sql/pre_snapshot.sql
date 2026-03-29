\pset pager off
\pset format aligned
\pset null '(null)'

\echo '=== snapshot_type: pre ==='
SELECT now() AT TIME ZONE 'UTC' AS snapshot_utc;
SELECT current_database() AS database_name;
SHOW server_version_num;
SELECT pg_current_wal_insert_lsn() AS wal_insert_lsn;
SELECT pg_current_wal_flush_lsn() AS wal_flush_lsn;

\echo '=== selected_settings ==='
SELECT name, setting, unit, source
FROM pg_settings
WHERE name IN (
  'shared_buffers',
  'effective_cache_size',
  'effective_io_concurrency',
  'random_page_cost',
  'synchronous_commit',
  'wal_level',
  'wal_buffers',
  'max_wal_size',
  'min_wal_size',
  'checkpoint_timeout',
  'checkpoint_completion_target',
  'track_io_timing',
  'track_wal_io_timing',
  'shared_preload_libraries'
)
ORDER BY name;

\echo '=== pg_stat_database ==='
SELECT datname,
       numbackends,
       xact_commit,
       xact_rollback,
       blks_read,
       blks_hit,
       tup_returned,
       tup_fetched,
       tup_inserted,
       tup_updated,
       tup_deleted,
       temp_files,
       temp_bytes,
       deadlocks,
       blk_read_time,
       blk_write_time,
       stats_reset
FROM pg_stat_database
ORDER BY datname;

SELECT (to_regclass('pg_catalog.pg_stat_wal') IS NOT NULL)::int AS has_pg_stat_wal \gset
\if :has_pg_stat_wal
\echo '=== pg_stat_wal ==='
SELECT * FROM pg_catalog.pg_stat_wal;
\else
SELECT 'pg_stat_wal unavailable' AS info;
\endif

SELECT (to_regclass('pg_catalog.pg_stat_io') IS NOT NULL)::int AS has_pg_stat_io \gset
\if :has_pg_stat_io
\echo '=== pg_stat_io ==='
SELECT * FROM pg_catalog.pg_stat_io;
\else
SELECT 'pg_stat_io unavailable' AS info;
\endif

SELECT (to_regclass('pg_catalog.pg_stat_checkpointer') IS NOT NULL)::int AS has_pg_stat_checkpointer \gset
\if :has_pg_stat_checkpointer
\echo '=== pg_stat_checkpointer ==='
SELECT * FROM pg_catalog.pg_stat_checkpointer;
\else
SELECT 'pg_stat_checkpointer unavailable' AS info;
\endif

SELECT EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'pg_stat_statements')::int AS has_pg_stat_statements \gset
\if :has_pg_stat_statements
\echo '=== pg_stat_statements_top20 ==='
SELECT queryid,
       calls,
       total_exec_time,
       mean_exec_time,
       rows,
       shared_blks_hit,
       shared_blks_read,
       temp_blks_written,
       wal_records,
       wal_fpi,
       wal_bytes,
       left(regexp_replace(query, E'[\n\r\t ]+', ' ', 'g'), 200) AS query_sample
FROM pg_stat_statements
ORDER BY total_exec_time DESC
LIMIT 20;
\else
SELECT 'pg_stat_statements unavailable' AS info;
\endif
