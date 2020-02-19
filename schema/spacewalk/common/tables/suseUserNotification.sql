CREATE TABLE suseUserNotification
(
    id              NUMERIC NOT NULL
                        CONSTRAINT suse_user_notif_pk PRIMARY KEY, 
    user_id         NUMERIC NOT NULL
                        CONSTRAINT suse_user_notif_uid_fk
                        REFERENCES web_contact (id)
                        ON DELETE CASCADE,
    message_id      NUMERIC NOT NULL
                        CONSTRAINT suse_user_notif_mid_fk
                        REFERENCES suseNotificationMessage (id)
                        ON DELETE CASCADE,
    read            CHAR(1) DEFAULT ('N') NOT NULL,
    CONSTRAINT suse_notif_uid_mid_uq UNIQUE (user_id, message_id)
);

CREATE SEQUENCE suse_user_notif_id_seq;
