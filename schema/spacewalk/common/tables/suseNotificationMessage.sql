CREATE TABLE suseNotificationMessage
(
  id                NUMERIC NOT NULL
                        CONSTRAINT suse_notifmess_pk PRIMARY KEY,
  type              VARCHAR(32) NOT NULL,
  data              TEXT NOT NULL,
  created           TIMESTAMPTZ default(current_timestamp) NOT NULL
);

CREATE SEQUENCE suse_notif_message_id_seq;
