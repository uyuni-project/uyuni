CREATE TABLE susenotificationmessage
(
  id NUMBER NOT NULL
    CONSTRAINT suse_notifmess_pk PRIMARY KEY,
  type VARCHAR2(32) NOT NULL,
  data VARCHAR2(1024) NOT NULL,
  created timestamp with time zone NOT NULL DEFAULT now()
);

CREATE SEQUENCE suse_notification_message_id_seq;

CREATE TABLE suseusernotification
(
    id              NUMBER NOT NULL
                        CONSTRAINT suse_user_notif_pk PRIMARY KEY, 
    user_id         NUMBER NOT NULL
                        CONSTRAINT suse_user_notif_uid_fk
                        REFERENCES web_contact (id)
                        ON DELETE CASCADE,
    message_id      NUMBER NOT NULL
                        CONSTRAINT suse_user_notif_mid_fk
                        REFERENCES susenotificationmessage (id)
                        ON DELETE CASCADE,
    read CHAR(1) DEFAULT ('N') NOT NULL
);

CREATE SEQUENCE suse_user_notification_id_seq;
