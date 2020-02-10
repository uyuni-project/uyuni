-- oracle equivalent source sha1 3b6136cb195be227322b77e4097148ebbf95400a

DO $$
BEGIN
IF EXISTS (SELECT * FROM pg_tables WHERE tablename='rhnservernetwork')
THEN
  BEGIN
    BEGIN
      ALTER TABLE rhnServer ADD COLUMN hostname VARCHAR(128);
      UPDATE rhnServer
        SET hostname = (
          SELECT rhnServerNetwork.hostname
            FROM rhnServerNetwork
            WHERE rhnServerNetwork.id = (
              SELECT rhnServerNetwork.id
                FROM rhnServerNetwork
                WHERE server_id = rhnServer.id
                  AND id <= ALL (
                    SELECT rhnServerNetwork.id
                      FROM rhnServerNetwork
                      WHERE server_id = rhnServer.id
                  )
            )
        );
    EXCEPTION
      WHEN duplicate_column THEN RAISE NOTICE 'column hostname already exists in rhnServer. Safe to ignore';
    END;

    ALTER TABLE rhnServerNetwork DROP COLUMN IF EXISTS hostname;
    IF NOT EXISTS (
        SELECT 1
        FROM   pg_class c
        JOIN   pg_namespace n ON n.oid = c.relnamespace
        WHERE  c.relname = 'rhn_server_hostname_idx'
        ) THEN

        CREATE INDEX rhn_server_hostname_idx
        ON rhnServer (hostname);
    END IF;
  END;
  END IF;
END $$;

