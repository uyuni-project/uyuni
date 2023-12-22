DO $$
  BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'susecredentials' AND column_name = 'type') THEN
        ALTER TABLE susecredentials
            ADD COLUMN type VARCHAR(128) DEFAULT ('scc') NOT NULL
                                            CONSTRAINT rhn_type_ck
                                            CHECK (type IN ('scc', 'vhm', 'registrycreds', 'cloudrmt', 'reportcreds', 'rhui'));

        UPDATE susecredentials cc
           SET type = ct.label
          FROM susecredentials c
                    INNER JOIN susecredentialstype ct ON c.type_id = ct.id
         WHERE cc.id = c.id;

        ALTER TABLE susecredentials
            DROP COLUMN type_id;

        ALTER TABLE susecredentials
            ALTER COLUMN username DROP NOT NULL;

        ALTER TABLE susecredentials
            ALTER COLUMN password DROP NOT NULL;

        ALTER TABLE susecredentials
            ADD CONSTRAINT cred_type_check CHECK (
                CASE type
                    WHEN 'scc' THEN
                        username is not null and username <> ''
                            and password is not null and password <> ''
                    WHEN 'cloudrmt' THEN
                        username is not null and username <> ''
                            and password is not null and password <> ''
                            and url is not null and url <> ''
                    WHEN 'vhm' THEN
                        username is not null and username <> ''
                            and password is not null and password <> ''
                    WHEN 'registrycreds' THEN
                        username is not null and username <> ''
                            and password is not null and password <> ''
                    WHEN 'reportcreds' THEN
                        username is not null and username <> ''
                            and password is not null and password <> ''
                END
            );
    ELSE
      RAISE NOTICE 'type column already exists for susecredentials. Table is already up to date.';
    END IF;
  END;
$$;

DROP TABLE IF EXISTS susecredentialstype;

DROP SEQUENCE IF EXISTS suse_credtype_id_seq;
