CREATE TABLE susenotificationmessage
(
  id NUMBER NOT NULL
    CONSTRAINT suse_notifmess_pk PRIMARY KEY,
  severity VARCHAR2(64) NOT NULL,
  description VARCHAR2(256) NOT NULL,
  org_id  NUMBER CONSTRAINT suse_notifmess_oid_fk
                       REFERENCES web_customer (id)
                       ON DELETE CASCADE,
  is_read CHAR(1) DEFAULT ('N') NOT NULL
);

CREATE SEQUENCE suse_notification_message_id_seq;


CREATE TABLE susenotificationmessagerole
(
    message_id      NUMBER NOT NULL
                        CONSTRAINT suse_notifmessage_mid_fk
                        REFERENCES susenotificationmessage (id)
                        ON DELETE CASCADE,
    role_id   NUMBER NOT NULL
                        CONSTRAINT suse_notifmessage_rid_fk
                        REFERENCES rhnUserGroup (id)
                        ON DELETE CASCADE
);
