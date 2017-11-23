ALTER TABLE rhnServer ADD hostname VARCHAR2(128);

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

ALTER TABLE rhnServerNetwork DROP COLUMN hostname;

CREATE INDEX rhn_server_hostname_idx
    ON rhnServer (hostname)
    TABLESPACE [[4m_tbs]]
    NOLOGGING;
