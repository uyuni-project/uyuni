CREATE TABLE suseRecurringAction
(
  id                number NOT NULL
                    CONSTRAINT suse_recurring_action_id_pk PRIMARY KEY,
  target_type        VARCHAR2(32) NOT NULL,
  minion_id          number
                    CONSTRAINT suse_rec_action_minion_fk
                      REFERENCES suseMinionInfo(server_id)
                      ON DELETE CASCADE,
  group_id          number
                    CONSTRAINT suse_rec_action_group_fk
                      REFERENCES rhnServerGroup(id)
                      ON DELETE CASCADE,
  org_id            number
                    CONSTRAINT suse_rec_action_org_fk
                      REFERENCES web_customer(id)
                      ON DELETE CASCADE,
  active            CHAR(1) DEFAULT ('Y') NOT NULL,
  test_mode         CHAR(1) DEFAULT ('Y') NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_recurring_action_id_seq;
