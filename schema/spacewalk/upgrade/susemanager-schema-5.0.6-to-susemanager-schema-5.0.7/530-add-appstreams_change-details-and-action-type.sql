CREATE TABLE IF NOT EXISTS suseActionAppstream(
    id          NUMERIC NOT NULL
                CONSTRAINT suse_act_appstream_id_pk PRIMARY KEY,
    action_id   NUMERIC NOT NULL
                CONSTRAINT suse_act_appstream_act_fk
                REFERENCES rhnAction (id) ON DELETE CASCADE,
    module_name VARCHAR(128) NOT NULL,
    stream      VARCHAR(128) NULL,
    type        VARCHAR(10)  NOT NULL,
    created             TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL,
    modified            TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL
);

CREATE INDEX IF NOT EXISTS suse_act_appstream_aid_idx ON suseActionAppstream (action_id);

CREATE SEQUENCE IF NOT EXISTS suse_act_appstream_id_seq;

insert into rhnActionType
  select 524, 'appstreams.change', 'Change AppStreams in a system', 'N', 'N', 'N'
  where not exists(select 1  from rhnActionType where id = 524);
