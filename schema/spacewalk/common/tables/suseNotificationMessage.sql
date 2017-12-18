CREATE TABLE suseNotificationMessage
(
  id                NUMBER NOT NULL
                        CONSTRAINT suse_notifmess_pk PRIMARY KEY,
  type              VARCHAR2(32) NOT NULL,
  data              VARCHAR2(1024) NOT NULL,
  created           timestamp with local time zone default(current_timestamp) NOT NULL
);

CREATE SEQUENCE suse_notif_message_id_seq;
