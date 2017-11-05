-- Table: public.susenotificationmessage

-- DROP TABLE public.susenotificationmessage;

CREATE TABLE public.susenotificationmessage
(
  id numeric NOT NULL,
  notifmess_type_id numeric NOT NULL
    CONSTRAINT suse_notifmess_ctid_fk
    REFERENCES suseNotificationMessageType (id),
  description character varying(256) NOT NULL,
  isRead boolean NOT NULL DEFAULT false,
  created timestamp with time zone NOT NULL DEFAULT now(),
  modified timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT suse_notifmess_pk PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.susenotificationmessage
  OWNER TO spacewalk;

-- Index: public.suse_notification_message_id_idx

-- DROP INDEX public.suse_notification_message_id_idx;

CREATE INDEX suse_notification_message_id_idx
  ON public.susenotificationmessage
  USING btree
  (id);

CREATE SEQUENCE suse_notification_message_id_seq;