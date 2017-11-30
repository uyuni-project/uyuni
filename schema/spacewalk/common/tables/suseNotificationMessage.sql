CREATE TABLE susenotificationmessage
(
  id NUMBER NOT NULL
    CONSTRAINT suse_notifmess_pk PRIMARY KEY,
  severity VARCHAR2(64) NOT NULL,
  description VARCHAR2(256) NOT NULL,
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
)

CREATE SEQUENCE suse_user_notification_id_seq;
