CREATE TABLE suseNotificationMessage
(
  id                NUMBER NOT NULL
                        CONSTRAINT suse_notifmess_pk PRIMARY KEY,
  type              VARCHAR2(32) NOT NULL,
  data              VARCHAR2(1024) NOT NULL,
  created           timestamp with local time zone default(current_timestamp) NOT NULL
);

CREATE SEQUENCE suse_notif_message_id_seq;

CREATE TABLE suseUserNotification
(
    id              NUMBER NOT NULL
                        CONSTRAINT suse_user_notif_pk PRIMARY KEY,
    user_id         NUMBER NOT NULL
                        CONSTRAINT suse_user_notif_uid_fk
                        REFERENCES web_contact (id)
                        ON DELETE CASCADE,
    message_id      NUMBER NOT NULL
                        CONSTRAINT suse_user_notif_mid_fk
                        REFERENCES suseNotificationMessage (id)
                        ON DELETE CASCADE,
    read            CHAR(1) DEFAULT ('N') NOT NULL,
    CONSTRAINT suse_notif_uid_mid_uq UNIQUE (user_id, message_id)
);

CREATE SEQUENCE suse_user_notif_id_seq;
