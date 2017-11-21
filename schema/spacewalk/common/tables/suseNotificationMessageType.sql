-- Table: public.susenotificationmessagetype

-- DROP TABLE public.susenotificationmessagetype;

CREATE TABLE susenotificationmessagetype
(
  id NUMBER NOT NULL
        CONSTRAINT suse_notifmesstype_id_pk PRIMARY KEY,
  label VARCHAR2(64) NOT NULL,
  name VARCHAR2(64) NOT NULL,
  priority NUMBER NOT NULL
    CONSTRAINT suse_notifmesstype_label_uq UNIQUE,
)

CREATE SEQUENCE suse_notifmesstype_id_seq;