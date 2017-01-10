INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
             VALUES (sequence_nextval('rhn_tasko_bunch_id_seq'), 'token-cleanup-bunch', 'Cleanup expired channel tokens', null);

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES (sequence_nextval('rhn_tasko_schedule_id_seq'), 'token-cleanup-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='token-cleanup-bunch'),
        current_timestamp, '0 0 0 ? * *');

INSERT INTO rhnTaskoTask (id, name, class)
         VALUES (sequence_nextval('rhn_tasko_task_id_seq'), 'token-cleanup', 'com.redhat.rhn.taskomatic.task.TokenCleanup');

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
             VALUES (sequence_nextval('rhn_tasko_template_id_seq'),
                        (SELECT id FROM rhnTaskoBunch WHERE name='token-cleanup-bunch'),
                        (SELECT id FROM rhnTaskoTask WHERE name='token-cleanup'),
                        0,
                        null);


CREATE TABLE suseChannelAccessToken
(
    id               NUMBER NOT NULL
                         CONSTRAINT suse_chan_access_token_id_pk PRIMARY KEY,
    minion_id        NUMBER
                         CONSTRAINT suse_chan_access_token_mid_fk
                             REFERENCES suseMinionInfo (server_id)
                             ON DELETE SET NULL,
    token            varchar2(4000) NOT NULL,
    start            timestamp with local time zone NOT NULL,
    expiration       timestamp with local time zone NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_chan_access_token_id_seq;

CREATE TABLE suseChannelAccessTokenChannel
(
    token_id    NUMBER NOT NULL
                    CONSTRAINT suse_catc_tid_fk
                        REFERENCES suseChannelAccessToken (id),
    channel_id  NUMBER NOT NULL
                    CONSTRAINT suse_catc_cid_fk
                        REFERENCES rhnChannel (id),
    created     timestamp with local time zone
                    DEFAULT (current_timestamp) NOT NULL,
    modified    timestamp with local time zone
                    DEFAULT (current_timestamp) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX suse_catc_tid_cid_uq
    ON suseChannelAccessTokenChannel (token_id, channel_id)
    TABLESPACE [[8m_tbs]];
