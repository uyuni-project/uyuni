CREATE TABLE susenotificationmessage
(
  id NUMBER NOT NULL
    CONSTRAINT suse_notifmess_pk PRIMARY KEY,
  notificationSeverity notificationSeverity NOT NULL,
  description VARCHAR2(256) NOT NULL,
  is_read CHAR(1) DEFAULT ('N') NOT NULL
)

CREATE SEQUENCE suse_notification_message_id_seq;
