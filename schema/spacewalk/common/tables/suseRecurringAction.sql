CREATE TABLE suseRecurringAction
(
  id                NUMERIC NOT NULL
                    CONSTRAINT suse_recurring_action_id_pk PRIMARY KEY,
  target_type       VARCHAR(32) NOT NULL,
  name              VARCHAR(256),
  cron_expr         VARCHAR(32) NOT NULL,
  minion_id         NUMERIC
                    CONSTRAINT suse_rec_action_minion_fk
                      REFERENCES suseMinionInfo(server_id)
                      ON DELETE CASCADE,
  group_id          NUMERIC
                    CONSTRAINT suse_rec_action_group_fk
                      REFERENCES rhnServerGroup(id)
                      ON DELETE CASCADE,
  org_id            NUMERIC
                    CONSTRAINT suse_rec_action_org_fk
                      REFERENCES web_customer(id)
                      ON DELETE CASCADE,
  creator_id        NUMERIC
                    CONSTRAINT suse_rec_action_creator_fk
                      REFERENCES web_contact(id)
                      ON DELETE CASCADE,
  active            CHAR(1) DEFAULT ('Y') NOT NULL,
  test_mode         CHAR(1) DEFAULT ('Y') NOT NULL
);

CREATE SEQUENCE suse_recurring_action_id_seq;

CREATE UNIQUE INDEX suse_rec_action_name_minion_uq
    ON suseRecurringAction(name, minion_id);

CREATE UNIQUE INDEX suse_rec_action_name_grp_uq
    ON suseRecurringAction(name, group_id);

CREATE UNIQUE INDEX suse_rec_action_name_org_uq
    ON suseRecurringAction(name, org_id);

